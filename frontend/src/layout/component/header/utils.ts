import { reqMergeFileChunks, reqUplaodFile, reqUploadFileChunk } from "@/api/file";
import { getCategoryByFilename, getMediaTypeByFilename } from "@/constants/fileIcon";
import { useBaseFileStore, useFileItemStore, useFileNodeStore, useUploadFileStore } from '@/stores/modules/file'
import { useUserStore } from '@/stores/modules/user';
import type { IUploadChunks } from "@/stores/modules/file";
import type { IItemCategory, IItemMediaType } from "@/types/file";
import { Message } from "@arco-design/web-vue";
import SparkMD5 from "spark-md5";
import ChunkWorker from './calcMD5Worker?worker'
import { debounce } from "@/utils";

const { isTree, breadcrumbs } = storeToRefs(useBaseFileStore())
const { updateFileItem } = useFileItemStore()
const { updateTreeData } = useFileNodeStore()
const { uploadFileList } = storeToRefs(useUploadFileStore())
const { updateUserInfo } = useUserStore()



/**
 * 上传成功的回调
 */
const uploadSuccessCallback = debounce(() => {
    Message.success('上传成功')
    isTree.value ? updateTreeData() : updateFileItem()
    updateUserInfo()
})


/**
 * 上传整个文件
 * @param file 
 */
export async function uploadFile(item: IUploadChunks) {
    const { md5 } = item
    const file = item.fileItem.file!
    const data = new FormData()
    data.append('file', file)
    data.append('filename', file.name + '')
    data.append('md5', md5)
    data.append('totalSize', file.size + '')
    data.append('pid', item.pid)
    data.append('category', getCategoryByFilename(file.name))
    data.append('mediaType', getMediaTypeByFilename(file.name))
    try {
        const r = await reqUplaodFile(data);
        if (r.code === 200) {
            item.status = 'finish'
            uploadSuccessCallback()
            item.uploadedChunkIndexs.push(0) // 相当于只有一个分块
        }
        else {
            item.status = 'error'
            Message.error(r.message);
        }
    } catch {
        item.status = 'error'
        Message.error('上传失败');
    }
}



/* ---------------------------------- 分块 --------------------------------- */
export function createChunks(file: File): Blob[] {
    const result: Blob[] = []
    let startPos = 0
    while (startPos < file.size) {
        const chunk = file.slice(startPos, startPos + ChunksUploader.CHUNK_SIZE)
        result.push(new Blob([chunk], { type: file.type }))
        startPos += ChunksUploader.CHUNK_SIZE
    }
    return result
}



type Job<T> = () => Promise<T>
class ReqQueue<T = any> {
    private jobs: Job<T>[]
    private currTask: ((job: Job<T>) => Promise<any>) | null
    stopFlag: boolean
    /**
     * 总共上传的数量
     */
    private executeCount: number

    constructor(item: IUploadChunks, jobs: Job<T>[] = []) {
        this.jobs = jobs
        this.currTask = null
        this.stopFlag = false
        this.executeCount = 0
    }
    addTask(task: Job<T>) {
        this.jobs.push(task)
    }
    get queue() {
        return this.jobs
    }
    async start() {
        const queue = []
        this.currTask = async (_job: Job<T>) => {
            if (this.stopFlag) return
            this.stopFlag = false
            await _job()
            this.executeCount += 1
            const next = this.jobs.shift()
            if (next) {
                queue.push(this.currTask?.(next))
            }
        }
        // 5个请求
        for (let i = 0; i < 5; i++) {
            const next = this.jobs.shift()
            if (next) {
                queue.push(this.currTask?.(next))
            }
        }
        await Promise.all(queue)
    }
    stop() {
        this.stopFlag = true
    }
}

export class ChunksUploader {
    /**
     * 是否已显示过报错信息，只显示一次
     */
    private showedTip: boolean
    private chunks: Blob[]
    controller: AbortController
    readonly reqQueue: ReqQueue
    private item: IUploadChunks
    // 分块大小 10M，minio最小分块限制5M左右
    public static readonly CHUNK_SIZE = 10 * 1024 * 1024
    /**
     * 已上传的分块索引，从后端获取
     */
    private uploadedChunkIndexes: number[]
    options: IUploadChunks
    private category: IItemCategory
    private mediaType: IItemMediaType
    constructor(options: IUploadChunks) {
        this.showedTip = false
        this.controller = new AbortController()
        this.item = uploadFileList.value[options.index] as IUploadChunks
        this.reqQueue = new ReqQueue(this.item)
        this.uploadedChunkIndexes = this.item.uploadedChunkIndexs
        this.chunks = createChunks(this.item.fileItem.file!)
        this.options = options
        this.category = getCategoryByFilename(this.item.fileItem.name ?? '')
        this.mediaType = getMediaTypeByFilename(this.item.fileItem.name ?? '')
    }
    /**
     * 分块之后请求已上传的分块索引、拼装请求
     */
    private async prepareReqs() {
        const { item, chunks, controller } = this
        const { md5, fileItem } = item
        const { file } = fileItem
        this.chunks.forEach((chunk, i) => {
            // 如果已上传
            if (this.uploadedChunkIndexes.includes(i)) {
                return
            }
            const formData = new FormData()
            formData.append('file', chunk)
            formData.append('filename', file!.name + '')
            formData.append('chunkIndex', i.toString())
            formData.append('totalChunkNum', chunks.length + '')
            formData.append('totalSize', file!.size + '')
            formData.append('pid', item.pid)
            formData.append('md5', md5)
            formData.append('category', this.category)
            this.reqQueue.addTask((index: number = i) => reqUploadFileChunk(formData, controller.signal).then(r => {
                if (r.code === 200) {
                    // 更新分块上传进度
                    item.uploadedChunkIndexs.push(index)
                }
                else {
                    this.reqQueue.stop()
                    // 如果是暂停的，不报错
                    if (item.status === 'pause') return
                    !this.showedTip && Message.error(r.message)
                    this.showedTip = true
                    throw new Error(r.message)
                }
                return r
            }).catch(() => {
                this.reqQueue.stop()
                if (item.status === 'pause') return
                item.status = 'error'
                !this.showedTip && Message.error('上传失败')
            }))
        })
    }

    /**
     * 上传分块
     */
    async upload() {
        await this.prepareReqs()
        await this.reqQueue.start()
        // 如果没停过，表示都上传完了，合并
        if (!this.reqQueue.stopFlag) await this.merge()
    }


    /**
     * 合并分块
     */
    private async merge() {
        const { item, category, mediaType } = this
        const file = item.fileItem.file!
        const filename = file!.name
        await reqMergeFileChunks({
            filename,
            pid: breadcrumbs.value[breadcrumbs.value.length - 1].id,
            md5: item.md5,
            totalSize: file.size,
            totalChunkNum: this.chunks.length,
            category,
            mediaType
        })
        item.status = 'finish'
        uploadSuccessCallback()
    }
}

/* ---------------------------------- 分块md5 --------------------------------- */
const MAX_WORKER_NUM = 10
interface WorkerHashMessage {
    type: 'hash'
    md5: string,
}
/**
 * 计算分块hash
 * @param chunks 
 * @returns 
 */
export async function calcChunksHash(chunks: Blob[]): Promise<string> {
    const workers = Array.from({ length: MAX_WORKER_NUM }).map(() => new ChunkWorker())
    const queue: Promise<string>[] = []
    const job = (worker: Worker) => new Promise<string>(async res => {
        const chunk = chunks.shift()
        if (!chunk) res('')
        else {
            worker.postMessage({
                type: 'start',
                file: await chunk.arrayBuffer()
            })
            worker.onmessage = (e: MessageEvent<WorkerHashMessage>) => {
                res(e.data.md5)
                queue.push(job(worker))
            }
        }
    })
    for (const worker of workers) {
        queue.push(job(worker))
    }
    const chunksHashList: string[] = (await Promise.all(queue)).filter(p => p.length)
    if (chunksHashList.length === 1) return chunksHashList[0]
    return new Promise<string>(async res => {
        const worker = new ChunkWorker()
        worker.postMessage({
            type: 'start',
            file: await new Blob(chunksHashList, { type: 'text/plain' }).arrayBuffer()
        })
        worker.onmessage = (e: MessageEvent<WorkerHashMessage>) => {
            res(e.data.md5)
        }
    })
}


/* ------------------------------- Merkle tree ------------------------------ */

interface IMerkleNode {
    h: string
    l: IMerkleNode | null
    r: IMerkleNode | null
}
interface IMerkleTree {
    root: IMerkleNode
    leafs: IMerkleNode[]
}
class MerkleNode implements IMerkleNode {
    h: string
    l: IMerkleNode | null
    r: IMerkleNode | null
    constructor(hash: string, left: IMerkleNode | null = null, right: IMerkleNode | null = null) {
        this.h = hash
        this.l = left
        this.r = right
    }
}
export class MerkleTree implements IMerkleTree {
    root: IMerkleNode
    leafs: IMerkleNode[]
    constructor(hashList: string[])
    constructor(leafNodes: IMerkleNode[])
    constructor(nodes: string[] | IMerkleNode[]) {
        if (nodes.length === 0) {
            throw new Error('Empty Nodes')
        }
        if (typeof nodes[0] === 'string') {
            this.leafs = nodes.map((node) => new MerkleNode(node as string))
        } else {
            this.leafs = nodes as IMerkleNode[]
        }
        this.root = this.buildTree()
    }
    getRootHash() {
        return this.root.h
    }

    buildTree(): IMerkleNode {
        // 实现构建 Merkle 树的逻辑。根据叶子节点创建父节点，一直到根节点。
        let currentLevelNodes = this.leafs
        while (currentLevelNodes.length > 1) {
            const parentNodes: IMerkleNode[] = []
            for (let i = 0; i < currentLevelNodes.length; i += 2) {
                const left = currentLevelNodes[i]
                const right = i + 1 < currentLevelNodes.length ? currentLevelNodes[i + 1] : null
                // 具体的哈希计算方法
                const parentHash = this.calculateHash(left, right)
                parentNodes.push(new MerkleNode(parentHash, left, right))
            }
            currentLevelNodes = parentNodes
        }

        return currentLevelNodes[0] // 返回根节点
    }

    // 序列化 Merkle 树
    serialize(): string {
        const serializeNode = (node: IMerkleNode | null): any => {
            if (node === null) {
                return null
            }
            return {
                h: node.h,
                l: serializeNode(node.l),
                r: serializeNode(node.r),
            }
        }

        const serializedRoot = serializeNode(this.root)
        return JSON.stringify(serializedRoot)
    }

    // 反序列化 Merkle 树
    static deserialize(serializedTree: string): MerkleTree {
        const parsedData = JSON.parse(serializedTree)

        const deserializeNode = (data: any): IMerkleNode | null => {
            if (data === null) {
                return null
            }
            return new MerkleNode(data.h, deserializeNode(data.l), deserializeNode(data.r))
        }

        const root = deserializeNode(parsedData)
        if (!root) {
            throw new Error('Invalid serialized tree data')
        }

        // 创建一个包含所有叶子节点的数组，这是为了与 MerkleTree 的构造函数兼容
        // 没有保存这些叶子节点的序列化版本，所以这里需要一些额外的逻辑来处理
        // 如果你需要将整个树的所有节点存储为序列化版本，那么可能需要修改这部分逻辑
        const extractLeafNodes = (node: IMerkleNode): IMerkleNode[] => {
            if (node.l === null && node.r === null) {
                return [node]
            }
            return [
                ...(node.l ? extractLeafNodes(node.l) : []),
                ...(node.r ? extractLeafNodes(node.r) : []),
            ]
        }
        const leafNodes = extractLeafNodes(root)

        return new MerkleTree(leafNodes)
    }

    private calculateHash(left: IMerkleNode, right: IMerkleNode | null): string {
        return right ? SparkMD5.hash(left.h + right.h) : left.h
    }
}
import SparkMD5 from 'spark-md5'


interface Message {
    type: 'start',
    file: ArrayBuffer,
}

class Md5Calculator {
    // 文件
    private file: ArrayBuffer
    // worker的self
    private self: Window
    constructor(msg: Message, self: Window) {
        this.file = msg.file
        this.self = self
    }

    async clacHash() {
        const { file } = this
        const md5 = SparkMD5.ArrayBuffer.hash(file)
        this.self.postMessage({
            type: 'chunk',
            md5
        })
    }

}


self.onmessage = (e: MessageEvent<Message>) => {
    if (e.data.type === 'start') {
        new Md5Calculator(e.data, self).clacHash()
    }
}



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
class MerkleTree implements IMerkleTree {
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
<template>
    <a-trigger trigger="click" :unmount-on-close="false">
        <a-button shape="circle" size="large" class="mr-1" :style="iconStyle">
            <template #icon>
                <IconSwap :rotate="90" class="scale-125" :size="20" />
            </template>
        </a-button>
        <template #content>
            <a-card title="文件上传管理"
                style="width: 400px;max-width: 100vw;max-height: 600px;overflow: auto;border: 2px solid #ddd;border-radius: 6px;box-shadow: 0 0 2px #ddd;">
                <a-list :bordered="false" :data="reversedTotlFileList">
                    <template #item="{ item }: { item: IUploadChunks }">
                        <a-list-item style="width: 360px;padding: 3px;">
                            <a-list-item-meta>
                                <template #avatar>
                                    <component :is="getIconByFilename(item.fileItem.name!)" class="scale-150 max-w-20">
                                    </component>
                                </template>
                                <template #title>
                                    <div class="w-56 truncate">
                                        {{ item.fileItem.name }}
                                    </div>
                                </template>
                                <template #description>
                                    位置：{{ item.level }} /
                                    <div class="w-64 truncate flex items-center">
                                        {{ calcFileSize(item.fileItem.file?.size ?? '') }}&emsp;
                                        <span
                                            :class="`${item.status === 'error' ? 'text-[red]' : item.status === 'finish' ? 'text-[green]' : ''}`">
                                            {{ uploadStatus[item.status] }}</span>
                                    </div>
                                    <a-progress :percision="2" style="width: 260px;"
                                        :percent="+(item.uploadedChunkIndexs.length / item.totalChunks).toFixed(3)" />
                                </template>
                            </a-list-item-meta>
                            <template #actions>
                                <div class="flex">
                                    <a-button shape="round" type="text"
                                        v-show="['upload', 'pause'].includes(item.status)">
                                        <template #icon>
                                            <icon-pause-circle v-if="item.status === 'upload'"
                                                @click="handlePause(item)" />
                                            <icon-play-arrow v-if="item.status === 'pause'"
                                                @click="handleRecover(item)" />
                                        </template>
                                    </a-button>
                                    <a-button shape="round" type="text" @click="handleRecover(item)"
                                        v-show="item.status === 'error'">
                                        <template #icon>
                                            <icon-refresh />
                                        </template>
                                    </a-button>
                                    <a-popconfirm type="warning" position="lb" content="确定要删除吗？"
                                        v-if="['pause', 'finish', 'error'].includes(item.status)"
                                        @ok="handleDelete(item)">
                                        <a-button shape="round" type="text">
                                            <template #icon>
                                                <icon-delete />
                                            </template>
                                        </a-button>
                                    </a-popconfirm>
                                </div>
                            </template>
                        </a-list-item>
                    </template>
                </a-list>
                <template #extra>
                    <a-tooltip content="清空已上传文件" v-if="uploadFileList.length">
                        <component :is="Icons.deleteCompletely" class="cursor-pointer" @click="clearUploadedFiles">
                        </component>
                    </a-tooltip>
                </template>
            </a-card>
        </template>
    </a-trigger>
</template>

<script setup lang="ts">
import { useAppStore } from '@/stores/modules/app';
import { useBaseFileStore, useFileItemStore, useFileNodeStore, useUploadFileStore } from '@/stores/modules/file'
import { useUserStore } from '@/stores/modules/user';
import { getCategoryByFilename, getIconByFilename } from '@/constants/fileIcon'
import type { IUploadChunks } from '@/stores/modules/file';
import { calcFileSize } from '@/utils/calc';
import { Message, type FileItem } from '@arco-design/web-vue';
import { calcChunksHash, ChunksUploader, createChunks, uploadFile } from './utils'
import { reqDeleteChunks, reqInstantUpload, reqUploadedChunkIndexes } from '@/api/file';
import type { GetUploadedChunkIndexes } from '@/types/file';

const { isTree } = storeToRefs(useBaseFileStore())
const { updateTreeData } = useFileNodeStore()
const { uploadFileList } = storeToRefs(useUploadFileStore())
const { updateFileItem } = useFileItemStore()
const { updateUserInfo } = useUserStore()
const { iconColor, Icons } = storeToRefs(useAppStore());
const { userInfo } = storeToRefs(useUserStore())
const iconStyle = computed(() => ({ color: iconColor.value, backgroundColor: 'transparent' }))
// 反转的数组，考虑到后上传的要显示在上面
const reversedTotlFileList = computed<IUploadChunks[]>(() => {
    const res: IUploadChunks[] = []
    uploadFileList.value.map(f => res.unshift(f as IUploadChunks))
    return res
})

// 计算md5
const postMessageToCalcMD5 = async (data: FileItem, index: number) => {
    const item = uploadFileList.value[index]
    const chunks = createChunks(data.file!)
    item.totalChunks = chunks.length
    const md5 = await calcChunksHash(chunks)
    item.md5 = md5
    await handleRecover(item as IUploadChunks)
}

/**
 * 判断是否需要分块
 * @param item 
 */
const needSplit = (item: IUploadChunks): boolean => {
    return item.fileItem.file!.size > ChunksUploader.CHUNK_SIZE
}

// 已上传的文件uid
const uploadedFileUidList = ref<string[]>([])
watch(uploadFileList, async (newValue) => {
    // 如果是删除
    newValue.forEach(async f => {
        // 已上传的
        if (uploadedFileUidList.value.includes(f.fileItem.uid)) return
        uploadedFileUidList.value.push(f.fileItem.uid)
        f.status = 'parse'
        await postMessageToCalcMD5(f.fileItem, f.index)
    })
}, {
    deep: true
})

// 显示的状态
const uploadStatus: Record<IUploadChunks['status'], string> = {
    parse: '正在解析',
    upload: '正在上传',
    finish: '上传完成',
    pause: '暂停上传',
    error: '上传出错'
}
/**
 * 删除上传记录和已上传的分块
 * @param item 
 */
const handleDelete = async (item: IUploadChunks) => {
    // 修改本地记录
    const updateLocalRecord = () => {
        // 只删除上传记录
        uploadFileList.value.splice(item.index, 1);
        // 更新索引
        uploadFileList.value.forEach((f, i) => f.index = i)
    }
    // 删除已上传的分块
    if (item.uploadedChunkIndexs.length < item.totalChunks) {
        try {
            const resp = await reqDeleteChunks({
                md5: item.md5,
                chunkIndexes: [...item.uploadedChunkIndexs],
                category: getCategoryByFilename(item.fileItem.name ?? '')
            })
            if (resp.code === 200) updateLocalRecord()
            else Message.error(resp.message)
        } catch {
            Message.error('删除失败')
        }
    } else {
        updateLocalRecord()
    }
}

/**
 * 暂停上传
 * @param item 
 */
const handlePause = (item: IUploadChunks) => {
    item.status = 'pause'
    const uploader = item.uploader
    uploader?.controller.abort()
    uploader?.reqQueue.stop()
}

/**
 * 恢复上传
 * @param item 
 */
const handleRecover = async (item: IUploadChunks) => {
    item.status = 'upload'
    let data: GetUploadedChunkIndexes = {
        uploaded: false,
        indexes: []
    }
    if (userInfo.value.totalSpace - userInfo.value.usedSpace < (item.fileItem.file?.size ?? 0)) {
        Message.error('上传失败，空间不足')
        uploadFileList.value = uploadFileList.value.filter(f => f.fileItem.uid !== item.fileItem.uid)
        return
    }
    // 请求是否已上传和已上传的分块
    try {
        const resp = await reqUploadedChunkIndexes(item.md5)
        if (resp.code === 200) {
            data = resp.data
        }
    } catch { }
    // 如果已上传，无需上传，只需秒传
    if (data.uploaded) {
        reqInstantUpload(item).then(res => {
            if (res.code === 200) {
                item.uploadedChunkIndexs = [0]
                item.totalChunks = 1
                item.status = 'finish'
                // 更新
                if (isTree.value) updateTreeData()
                else updateFileItem()
                updateUserInfo()
                Message.success('上传成功，已启用秒传')
            } else Message.error(res.message)
        }).catch(e => Message.error(e))
    } else {
        item.uploadedChunkIndexs = [...new Set([...item.uploadedChunkIndexs, ...data.indexes])]
        if (needSplit(item)) {
            const uploader = new ChunksUploader(item)
            item.uploader = uploader
            await uploader.upload()
        } else {
            await uploadFile(item)
        }
    }
}

// 清空已上传文件
const clearUploadedFiles = () => {
    for (let i = uploadFileList.value.length - 1; i >= 0; i--) {
        if (uploadFileList.value[i].status === 'finish') {
            uploadFileList.value.splice(i, 1)
        }
    }
    Message.success('清空成功')
}
</script>

<style scoped></style>
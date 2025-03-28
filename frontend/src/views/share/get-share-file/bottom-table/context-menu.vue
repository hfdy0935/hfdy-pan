<template>
    <a-dropdown trigger="contextMenu" alignPoint>
        <slot></slot>
        <template #content>
            <a-doption @click="doOpen" v-if="!shouldHandleGroup">
                <component :is="appStore.Icons.open" />
                打开&emsp;&emsp;
            </a-doption>
            <a-doption @click="modalPos = 'bottom'; treeModalShow = true">
                <icon-save />
                转存
            </a-doption>
            <a-doption @click="doDownload">
                <icon-download />
                下载
            </a-doption>
        </template>
    </a-dropdown>
</template>

<script setup lang="ts">
import { reqDownloadShareFiles } from '@/api/share';
import { useViewFileStore } from '@/stores/modules/file'
import { useAppStore } from '@/stores/modules/app';
import { isFolder } from '@/utils/file';
import { Message } from '@arco-design/web-vue';
import { useGetShareStore } from '@/stores/modules/share';

const { shouldHandleGroup, contextItem, selectedIds, treeModalShow, modalPos } = storeToRefs(useGetShareStore());
const { updateFolder } = useGetShareStore();
const { openPreviewModal } = useViewFileStore();
const appStore = useAppStore()
const doOpen = async () => {
    if (isFolder(contextItem.value)) {
        selectedIds.value = [];
        await updateFolder({ pid: contextItem.value.id });
    } else openPreviewModal(contextItem.value);
};


/**
 * 下载
 */
const doDownload = async () => {
    try {
        Message.info('下载请求已提交');
        const ids = shouldHandleGroup.value ? selectedIds.value : [contextItem.value.id];
        const name = ids.length > 1 ? 'download.zip' : contextItem.value.name
        const resp = await reqDownloadShareFiles(ids);
        const a = document.createElement('a');
        a.href = URL.createObjectURL(resp);
        a.download = name;
        a.click();
        a.remove();
    } catch {
        Message.error('下载失败');
    }
};
</script>

<style scoped></style>

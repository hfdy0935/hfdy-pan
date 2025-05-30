<script setup lang="ts">
import { adaptedThemeIcons } from '@/constants/fileIcon';
import { useBaseFileStore, useFileItemStore, useFileDetailStore } from '@/stores/modules/file'
import { useUserStore } from '@/stores/modules/user';
import { useAppStore } from '@/stores/modules/app';
import { reqFileDetail, reqDownloadFiles, reqUploadLyric } from '@/api/file';
import { Message, Upload, type FileItem } from '@arco-design/web-vue';
import { IconDownload } from '@arco-design/web-vue/es/icon';
import { isAudio, calcFileLevel } from '@/utils/file';
import { useSetShareStore } from '@/stores/modules/share';
import { useViewFileStore } from '@/stores/modules/viewFile';


const { isDark } = storeToRefs(useAppStore());
const { isContextmenuShow, editNameOp, layoutType, editingId, isMaxFolderLevel, currParentFolder } = storeToRefs(useBaseFileStore())
const { contextmenuItem, contextmenuType, query, selectedFiles, shouldHandleGroup } = storeToRefs(useFileItemStore());
const { updateFileItem, doDelete: del, doPaste, prepareCreateFolder, prepareCut, prepareCopy, clearSelected } = useFileItemStore();
const { openPreviewModal } = useViewFileStore();
const { userInfo } = storeToRefs(useUserStore());
const { openDetailModal } = useFileDetailStore();
const { openShareFileModal } = useSetShareStore();
const Icons = computed(() => adaptedThemeIcons(isDark.value))

/**
 * 点击重命名
 */
const doRename = () => {
    editingId.value = contextmenuItem.value!.id;
    editNameOp.value = 'change';
};

/**
 * 点击打开
 */
const doOpen = async () => {
    // 文件夹
    if (contextmenuType.value === 'folder') {
        if (calcFileLevel(currParentFolder.value) === userInfo.value.maxFolderLevel) {
            Message.warning('已达到最大深度，不支持打开');
            return;
        }
        // 打开
        query.value = {
            ...query.value,
            page: 1,
            pid: contextmenuItem.value?.id
        }
        clearSelected()
    } else openPreviewModal(contextmenuItem.value!);
};
/**
 * 点击详情
 */
const clickDetail = async () => {
    try {
        const resp = await reqFileDetail(contextmenuItem.value!.id);
        if (resp.code === 200) {
            openDetailModal(resp.data);
        } else Message.error(resp.message);
    } catch {
        Message.error('获取文件信息失败');
    }
};

/* -------------------------- 根据选中的文件id列表判断当前操作的文件 -------------------------- */
const opFiles = computed(() => shouldHandleGroup.value ? selectedFiles.value : [contextmenuItem.value!])
// 删除对话框是否显示
const isDeleteModalShow = ref(false);
// 是否彻底删除
const isDeleteCompletely = ref(false);
const doDelete = (completely: boolean) => {
    isDeleteCompletely.value = completely;
    isDeleteModalShow.value = true;
};

/* ---------------------------------- 上传歌词 ---------------------------------- */
const shouldUploadLyricShow = computed(() => !shouldHandleGroup.value && isAudio(contextmenuItem.value!));
const uploadLyricRef = useTemplateRef<InstanceType<typeof Upload>>('uploadLyric');
const doUploadLyric = async (_: any, fileItem: FileItem) => {
    if (!fileItem.name?.endsWith('.lrc')) {
        Message.warning('只支持lyc格式的歌词文件');
    }
    try {
        const data = new FormData();
        data.append('lyric', fileItem.file!);
        data.append('fileId', contextmenuItem.value!.id);
        const resp = await reqUploadLyric(data);
        if (resp.code === 200) {
            Message.success('上传成功');
            updateFileItem();
        } else {
            Message.error(resp.message);
        }
    } catch {
        Message.error('上传失败');
    }
};

/* ---------------------------------- 文件分享 ---------------------------------- */
const doShare = () => {
    const files = shouldHandleGroup.value ? selectedFiles.value : [contextmenuItem.value!];
    openShareFileModal(files);
};

/**
 * 下载
 */
const doDownload = async () => {
    try {
        Message.info('下载请求已提交');
        const ids = shouldHandleGroup.value ? selectedFiles.value.map(f => f.id) : [contextmenuItem.value!.id];
        const name = contextmenuItem.value!.name
        const resp = await reqDownloadFiles(ids);
        const a = document.createElement('a');
        a.href = URL.createObjectURL(resp);
        a.download = shouldHandleGroup.value ? 'download.zip' : name;
        a.click();
        a.remove();
    } catch {
        Message.error('下载失败');
    }
};
</script>

<template>
    <a-dropdown trigger="contextMenu" alignPoint @popup-visible-change="s => (isContextmenuShow = s)">
        <slot></slot>
        <template #content>
            <a-dgroup title="查看">
                <a-doption @click="doOpen" v-if="!shouldHandleGroup">
                    <component :is="Icons.open" />
                    &emsp;打开
                </a-doption>
                <a-doption @click="clickDetail" v-if="!shouldHandleGroup">
                    <component :is="Icons.detail"></component>
                    &emsp;详情
                </a-doption>
                <a-dsubmenu trigger="hover">
                    <component :is="Icons.contextmenuLayout"></component>
                    &emsp;布局
                    <template #content>
                        <a-doption @click="layoutType = 'list'">
                            <component :is="Icons.listLayout" style="transform: scale(1.4);"></component>
                            &emsp;&nbsp;列表&emsp;
                        </a-doption>
                        <a-doption @click="layoutType = 'block'">
                            <component :is="Icons.blockLayout"></component>
                            &emsp;&nbsp;方块&emsp;
                        </a-doption>
                        <a-doption @click="layoutType = 'tree'">
                            <component :is="Icons.treeLayout"></component>
                            &emsp;&nbsp;树&emsp;
                        </a-doption>
                    </template>
                </a-dsubmenu>
                <a-doption @click="doDownload">
                    <icon-download />
                    &emsp;下载
                </a-doption>
                <a-doption @click="doShare">
                    <icon-share-internal />
                    &emsp;分享
                </a-doption>
            </a-dgroup>
            <a-dgroup title="移动">
                <a-doption @click="prepareCopy(opFiles)">
                    <icon-copy />
                    &emsp;复制
                </a-doption>
                <a-doption @click="prepareCut(opFiles)">
                    <component :is="Icons.cut"></component>
                    &emsp;剪切
                </a-doption>
                <a-doption @click="doPaste">
                    <icon-paste />
                    &emsp;粘贴
                </a-doption>
            </a-dgroup>
            <a-dgroup title="编辑">
                <a-doption @click="prepareCreateFolder" v-if="!isMaxFolderLevel">
                    <component :is="Icons.newFolder"></component>
                    &emsp;新建文件夹&emsp;
                </a-doption>
                <a-doption @click="uploadLyricRef?.$el.click()" v-if="shouldUploadLyricShow">
                    <icon-music />
                    &emsp;上传歌词
                </a-doption>
                <a-doption @click="doRename" v-if="!shouldHandleGroup">
                    <icon-edit />
                    &emsp;重命名
                </a-doption>
                <a-doption @click="doDelete(false)">
                    <icon-delete />
                    &emsp;删除
                </a-doption>
                <a-doption @click="doDelete(true)">
                    <component :is="Icons.deleteCompletely"></component>
                    &emsp;彻底删除
                </a-doption>
            </a-dgroup>
        </template>
    </a-dropdown>
    <a-modal v-model:visible="isDeleteModalShow" title="提示" @ok="del(isDeleteCompletely)" unmount-on-close
        :ok-button-props="{ status: 'danger' }" :closable="false" :width="300">
        <h2>{{ isDeleteCompletely ? '删除之后无法复原' : '删除之后可在回收站找回' }}</h2>
    </a-modal>
    <a-upload ref="uploadLyric" accept=".lrc" style="display: none" :auto-upload="false" @change="doUploadLyric"
        :show-file-list="false" />
</template>

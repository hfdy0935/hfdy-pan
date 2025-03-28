<template>
    <td>
        <context-menu>
            <div class="min-h-14 flex items-center">
                <template v-if="column.dataIndex === 'name'">
                    <div class="flex justify-start items-center">
                        <a-image v-if="imageUrl" :src="imageUrl" :width="30" :height="30"></a-image>
                        <component v-else :is="icon" class="scale-150" />
                        <span class="cursor-pointer hover:text-h-blue select-none ml-3" @dblclick="openFileOrFolder"> {{
                            record.name }}</span>
                    </div>
                </template>
                <div v-else-if="column.dataIndex === 'updateTime'" class="min-w-36 ml-3">
                    {{ record.updateTime }}
                </div>
                <div v-else-if="column.dataIndex === 'index'" class="w-full text-center">
                    {{ rowIndex + 1 }}
                </div>
                <span class="mx-4" v-else> {{ column.dataIndex === 'size' && record.mediaType !== 'folder' ?
                    calcFileSize(record.size) :
                    record.size }}</span>
            </div>
        </context-menu>
    </td>
</template>

<script setup lang="ts">
import { getIconByFilename } from '@/constants/fileIcon';
import type { IFileItem } from '@/types/file';
import { calcFileSize } from '@/utils/calc';
import type { TableColumnData, TableData } from '@arco-design/web-vue';
import ContextMenu from './context-menu.vue';
import { useViewFileStore } from '@/stores/modules/file';
import { reqPreviewFile } from '@/api/file';
import { useGetShareStore } from '@/stores/modules/share'
import { useAppStore } from '@/stores/modules/app';
import { isFolder } from '@/utils/file';

interface Props {
    column: TableColumnData;
    record: TableData;
    rowIndex: number;
}

const { column, record } = defineProps<Props>();
const { selectedIds, shareId } = storeToRefs(useGetShareStore());
const { updateFolder } = useGetShareStore();
const { openPreviewModal } = useViewFileStore();
const appStore = useAppStore()
const icon = computed(() => {
    if (isFolder(record as IFileItem)) return appStore.FileIcons.folder;
    return getIconByFilename(record.name);
});

/**
 * 双击文件夹/文件
 */
const openFileOrFolder = async () => {
    if (isFolder(record as IFileItem)) {
        selectedIds.value = [];
        await updateFolder({ pid: record.id });
    } else await openPreviewModal(record as IFileItem);
};

/* ----------------------------------- 图片 ----------------------------------- */
// TODO 懒加载a
const imageUrl = ref('');
watchEffect(async () => {
    if (record.category !== 'image') return;
    const resp = await reqPreviewFile(record.id, new AbortController(), shareId.value);
    imageUrl.value = URL.createObjectURL(resp);
});
</script>

<style scoped></style>

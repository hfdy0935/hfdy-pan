<script setup lang="ts">
import { Message, type TableColumnData, type TableData } from '@arco-design/web-vue';
import { reqDelRecycleFile } from '@/api/recycle';
import { getIconByFilename } from '@/constants/fileIcon';
import type { IRecycle } from '@/types/recycle';
import { useAppStore } from '@/stores/modules/app';

interface Props {
    column: TableColumnData;
    record: TableData;
    rowIndex: number;
}

const { record } = defineProps<Props>();
const emit = defineEmits<{
    update: [id: string];
    open: [file: IRecycle];
}>();
const appStore = useAppStore()
/**
 * 彻底删除
 */
const doCompleteDelete = async () => {
    try {
        const resp = await reqDelRecycleFile([record.id], true);
        if (resp.code === 200) {
            Message.success('已彻底删除');
            emit('update', record.id);
        } else Message.error(resp.message);
    } catch {
        Message.error('删除失败');
    }
};
</script>

<template>
    <td>
        <div class="min-h-14 flex items-center justify-center">
            <div v-if="column.dataIndex === 'index'">
                {{ rowIndex + 1 }}
            </div>
            <div v-else-if="column.dataIndex === 'operation'" class="w-64 flex justify-evenly">
                <a-button type="primary" status="success" @click="$emit('open', record as IRecycle)">
                    <template #icon>
                        <icon-refresh />
                    </template>
                </a-button>
                <a-popconfirm type="warning" position="lt" content="确定要彻底删除该文件/文件夹吗？" @ok="doCompleteDelete">
                    <a-button type="primary" status="danger">
                        <template #icon>
                            <icon-delete />
                        </template>
                    </a-button>
                </a-popconfirm>
            </div>
            <div v-else-if="column.dataIndex === 'name'" class="w-36 ml-12 flex justify-start items-center">
                <component
                    :is="record.mediaType === 'folder' ? appStore.FileIcons.folder : getIconByFilename(record.name)"
                    class="mr-2 scale-150"></component>
                {{ record.name }}
            </div>
            <div v-else>
                {{ record[column.dataIndex!] }}
            </div>
        </div>
    </td>
</template>

<style scoped></style>

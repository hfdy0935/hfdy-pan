<template>
    <td>
        <div class="min-h-14 flex items-center justify-center">
            <div v-if="column.dataIndex === 'index'">
                {{ rowIndex + 1 }}
            </div>
            <div v-else-if="column.dataIndex === 'operation'" class="w-64 flex justify-evenly">
                <a-button type="primary" status="success" @click="updateFile()">
                    <template #icon>
                        <icon-file />
                    </template>
                </a-button>
                <a-button @click="updateConfig()" type="primary">
                    <template #icon>
                        <icon-settings />
                    </template>
                </a-button>
                <a-popconfirm type="warning" position="lt" content="确定要删除该分享吗？" @ok="doDelete">
                    <a-button type="primary" status="danger">
                        <template #icon>
                            <icon-delete />
                        </template>
                    </a-button>
                </a-popconfirm>
            </div>
            <a-link v-else-if="column.dataIndex === 'link'" :href="link" target="'_blank'">
                {{ link }}
            </a-link>
            <div v-else>
                {{ record[column.dataIndex!] }}
            </div>
        </div>
    </td>
</template>

<script setup lang="ts">
import { reqDeleteMyShare } from '@/api/share';
import { useUpdateShareStore } from '@/stores/modules/share';
import { Message, type TableColumnData, type TableData } from '@arco-design/web-vue';

interface Props {
    column: TableColumnData;
    record: TableData;
    rowIndex: number;
}

const emit = defineEmits<{
    updateConfig: [];
    updateFile: [];
}>();

const { shareId, config, selectedIds } = storeToRefs(useUpdateShareStore())
const { updateData } = useUpdateShareStore()
const { column, record } = defineProps<Props>();

/**
 * 打开分享配置对话框时更新配置
 */
const updateConfig = () => {
    shareId.value = record.id
    config.value.pwd = record.pwd
    config.value.noDdl = record.expire.startsWith('9999')
    emit('updateConfig')
}
/**
 * 打开更新文件对话框，把该分享的文件id作为默认值
 */
const updateFile = () => {
    shareId.value = record.id
    selectedIds.value = record.fileIds
    emit('updateFile')
}
// 删除分享
const doDelete = async () => {
    try {
        const resp = await reqDeleteMyShare(record.id);
        if (resp.code === 200) {
            Message.success('删除成功');
            await updateData();
        } else Message.error(resp.message);
    } catch {
        Message.error('删除失败');
    }
};
// 链接
const link = computed(() => location.protocol + '//' + location.host + '/share/' + record.id + (record.pwd ? `?pwd=${record.pwd}` : ''));
</script>

<style scoped></style>

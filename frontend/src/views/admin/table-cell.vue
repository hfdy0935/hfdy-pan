<template>
    <td>
        <div v-if="column.dataIndex === 'index'" class="h-14 flex justify-center items-center">{{ rowIndex + 1 }}</div>
        <div v-else-if="column.dataIndex === 'downloadSpeed'" class="w-24 text-center">
            {{ calcFileSize(record.downloadSpeed) }}/s
        </div>
        <div v-else-if="column.dataIndex === 'status'" class="w-24 text-center font-bold"
            :class="`${record.status ? 'text-[#23C343]' : 'text-[red]'}`">
            {{ record.status ? '正常' : '封禁' }}
        </div>
        <div v-else-if="column.dataIndex === 'isVip'" class="w-24 text-center">
            {{ record.isVip ? 'vip用户' : '普通用户' }}
        </div>
        <div v-else-if="column.dataIndex === 'usedSpace'" class="w-28 text-center">
            {{ (record.usedSpace * 100 / record.totalSpace).toFixed(2) }}%
        </div>
        <div v-else-if="column.dataIndex === 'operation'" class="flex w-24 justify-evenly items-center">
            <a-button @click="$emit('openModal', rowIndex)" type="primary">
                <template #icon>
                    <icon-edit />
                </template>
            </a-button>
            <a-popconfirm type="warning" position="lt" content="确定要删除该用户吗？" @ok="doDeleteUser">
                <a-button type="primary" status="danger">
                    <template #icon>
                        <icon-delete />
                    </template>
                </a-button>
            </a-popconfirm>
        </div>
        <div v-else class="w-48 text-center truncate">{{ record[column.dataIndex!] }}</div>
    </td>
</template>

<script setup lang="ts">
import { reqDeleteUser } from '@/api/admin';
import { calcFileSize } from '@/utils/calc';
import { Message, type TableColumnData, type TableData } from '@arco-design/web-vue';

interface Props {
    column: TableColumnData
    record: TableData
    rowIndex: number
}

const { column, record, rowIndex } = defineProps<Props>();
const emit = defineEmits<{
    deleteUser: [idx: number]
    openModal: [idx: number]
}>()

const doDeleteUser = async () => {
    const resp = await reqDeleteUser(record.id)
    if (resp.code === 200) {
        Message.success('删除成功')
        emit('deleteUser', rowIndex)
    }
}

</script>

<style scoped></style>
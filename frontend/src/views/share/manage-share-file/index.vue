<template>
    <a-table v-if="shareData.length" :columns class="m-3 mt-10" :scroll="{ x: 700 }" :data="shareData" hoverable
        :pagination="false">
        <template #td="{ column, record, rowIndex }">
            <table-cell :column :record :rowIndex @updateConfig="configModalRef?.open()"
                @updateFile="fileModalRef?.open()" />
        </template>
    </a-table>
    <a-empty v-else class="mt-24"></a-empty>
    <config-modal ref="configModal" />
    <file-modal ref="fileModal" />
</template>

<script setup lang="ts">
import { type TableColumnData } from '@arco-design/web-vue';
import TableCell from './table-cell.vue';
import ConfigModal from './config-modal.vue';
import FileModal from './file-modal.vue';
import { useUpdateShareStore } from '@/stores/modules/share';


const { shareData } = storeToRefs(useUpdateShareStore())
const { updateData, updateTreeLayerData } = useUpdateShareStore()
watchEffect(() => {
    updateData()
    updateTreeLayerData()
})

// 分享文件表格的列
const columns: TableColumnData[] = [
    {
        title: '序号',
        dataIndex: 'index',
        width: 60,
    },
    {
        title: '创建时间',
        dataIndex: 'createTime',
        width: 180,
    },
    {
        title: '过期时间',
        dataIndex: 'expire',
        width: 180,
    },
    {
        title: '链接',
        dataIndex: 'link',
        width: 300,
    },
    {
        title: '提取码',
        dataIndex: 'pwd',
        width: 120,
    },
    {
        title: '文件/文件夹数量',
        dataIndex: 'itemNum',
        width: 180,
    },
    {
        title: '访问量',
        dataIndex: 'visitNum',
        width: 100,
    },
    {
        title: '操作',
        dataIndex: 'operation',
        width: 180,
        fixed: 'right' as const
    },
].map(o => ({ ...o, align: 'center' }));

const configModalRef = useTemplateRef<InstanceType<typeof ConfigModal>>('configModal')
const fileModalRef = useTemplateRef<InstanceType<typeof FileModal>>('fileModal')

</script>

<style scoped></style>

<template>
    <section class="h-full overflow-auto">
        <a-divider></a-divider>
        <breadcrumb />
        <a-table :columns class="mt-3 mr-3" :data="showedFiles" hoverable :pagination="false" :scroll="{ x: 700 }"
            @row-contextmenu="contextmenuChange" :row-selection="{ type: 'checkbox', showCheckedAll: true }"
            v-model:selectedKeys="selectedIds">
            <template #td="{ column, record, rowIndex }">
                <table-cell :column :record :rowIndex />
            </template>
            <template #empty>
                <a-empty description="还没有任何文件~"></a-empty>
            </template>
        </a-table>
        <file-view-modal></file-view-modal>
    </section>
</template>

<script setup lang="ts">
import type { TableColumnData, TableData } from '@arco-design/web-vue';
import TableCell from './table-cell.vue';
import Breadcrumb from './breadcrumb.vue';
import type { IFileItem } from '@/types/file';
import FileViewModal from '@/views/file/componnet/file-view-modal/index.vue'
import { useGetShareStore } from '@/stores/modules/share';

const { contextItem, sharedFiles, selectedIds } = storeToRefs(useGetShareStore())

// 显示的文件列表，加了个key字段
const showedFiles = computed(() => sharedFiles.value.records?.map(item => ({ ...item, key: item.id })) ?? [])
/**
 * 点击右键菜单
 */
const contextmenuChange = (record: TableData) => {
    const { key, ...rest } = record;
    contextItem.value = { ...(rest as IFileItem) };
};


const columns: TableColumnData[] = [
    {
        title: '序号',
        dataIndex: 'index',
        width: 60
    },
    {
        title: '文件名',
        dataIndex: 'name',
    },
    {
        title: '修改时间',
        dataIndex: 'updateTime',
    },
    {
        title: '大小',
        dataIndex: 'size',
    },
];



</script>

<style scoped></style>
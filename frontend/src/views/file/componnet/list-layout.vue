<template>
    <a-table :columns class="my-3" :scroll="{ x: 700 }" :data="showedFileList" hoverable
        @page-change="p => query.page = p" @page-size-change="ps => query.pageSize = ps" :pagination
        :row-selection="{ type: 'checkbox', showCheckedAll: true }" @row-contextmenu="contextmenuChange"
        @sorterChange="sorterChange" v-model:selected-keys="selectedIds">
        <template #td="{ column, record, rowIndex }">
            <table-td-item :column :record :rowIndex />
        </template>
        <template #empty>
            <blank-file-contextmenu />
        </template>
    </a-table>
</template>

<script setup lang="ts">
import { type IFileItem, type TableFileList } from '@/types/file';
import TableTdItem from './table-cell-item/index.vue';

import { OrderType } from '@/enums';
import type { PaginationProps, TableColumnData, TableData } from '@arco-design/web-vue';
import BlankFileContextmenu from './table-cell-item/contextmenu/blank.vue';
import {  useFileItemStore } from '@/stores/modules/file'

const { itemList, query, contextmenuItem, selectedIds } = storeToRefs(useFileItemStore());

// 切换文件分类时把页码重置为1
onMounted(() => {
    query.value.page = 1;
})
// 表格的列
const columns: TableColumnData[] = [
    {
        title: '序号',
        dataIndex: 'index',
        width: 60,
    },
    {
        title: '文件名',
        dataIndex: 'name',
    },
    {
        title: '修改时间',
        dataIndex: 'updateTime',
        ellipsis: true,
        sortable: {
            sortDirections: ['ascend' as const, 'descend' as const],
            sorter: true,
        },
    },
    {
        title: '大小',
        dataIndex: 'size',
        sortable: {
            sortDirections: ['ascend' as const, 'descend' as const],
            sorter: true,
        },
    },
];
/**
 * 表格要显示的文件列表，增加key字段
 */
const showedFileList = computed<TableFileList>(
    () =>
        itemList.value.records.map(f => {
            return {
                ...f,
                key: f.id,
            };
        }) ?? [],
);
/**
 * 更新时间/大小排序变化时
 * @param field
 * @param order
 */
const sorterChange = (field: string, order: string) => {
    switch (field) {
        case 'updateTime':
            query.value.orderByUpdateTime = order === 'ascend' ? OrderType.ASC : order === 'descend' ? OrderType.DESC : OrderType.DEFAULT;
            break;
        case 'size':
            query.value.orderBySize = order === 'ascend' ? OrderType.ASC : order === 'descend' ? OrderType.DESC : OrderType.DEFAULT;
            break;
    }
};


// 分页数据
const pagination = computed<PaginationProps>(() => ({
    total: itemList.value?.total,
    current: query.value.page,
    pageSize: query.value.pageSize,
    showPageSize: true,
}));

/**
 * 点击右键菜单
 */
const contextmenuChange = (record: TableData) => {
    // record少category多个key
    const { key, ...rest } = record;
    contextmenuItem.value = { ...(rest as IFileItem) };
};
</script>

<style scoped></style>
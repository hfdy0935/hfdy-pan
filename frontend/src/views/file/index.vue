<script setup lang="ts">
import type { IFileItem, TableFileList } from '@/types/file';
import TableTdItem from './componnet/table-td-item.vue';
import FileOperation from './componnet/file-operation.vue';
import FileFooter from './componnet/file-panigation.vue';
import { useFileStore } from '@/stores';
import { OrderType } from '@/enums';
import type { TableColumnData, TableData } from '@arco-design/web-vue';
import FileBreadcrumb from './componnet/file-breadcrumb.vue';
import BlankFileContextmenu from './componnet/contextmenu/blank.vue';

defineOptions({
  name: 'File'
});
const route = useRoute();
const {
  fileListData,
  contextItem,
  selectedIdList,
  orderBySize,
  orderByUpdateTime,
  page,
  keyword
} = storeToRefs(useFileStore());
const { getFileListData } = useFileStore();
watch([() => route.params.category, page, keyword, orderBySize, orderByUpdateTime], async () => getFileListData(), {
  immediate: true
});
// 切换文件分类时把页码重置为0
onBeforeRouteUpdate(to => {
  if (/\/file\//.test(to.fullPath)) page.value = 0;
});
// 表格的列
const columns = [
  {
    title: '文件名',
    dataIndex: 'name'
  },
  {
    title: '修改时间',
    dataIndex: 'updateTime',
    ellipsis: true,
    sortable: {
      sortDirections: ['ascend', 'descend']
    }
  },
  {
    title: '大小',
    dataIndex: 'size',
    sortable: {
      sortDirections: ['ascend', 'descend']
    }
  }
];
/**
 * 要显示的文件列表，在请求数据的基础上提取列表、增加key字段
 */
const fileList = computed<TableFileList>(() => fileListData.value.records?.map(f => {
    return {
      ...f,
      key: f.id
    };
  })
);
/**
 * 更新时间/大小排序变化时
 * @param field
 * @param order
 */
const sorterChange = (field: string, order: string) => {
  switch (field) {
    case 'updateTime':
      orderByUpdateTime.value = order === 'ascend' ? OrderType.ASC : order === 'descend' ? OrderType.DESC : OrderType.DEFAULT;
      break;
    case 'size':
      orderBySize.value = order === 'ascend' ? OrderType.ASC : order === 'descend' ? OrderType.DESC : OrderType.DEFAULT;
      break;
  }
};
/**
 * 点击右键菜单
 */
const contextmenuChange = (record: TableData) => {
  // record少category多个key
  const { key, ...rest } = record;
  contextItem.value = { ...rest as IFileItem };
};
/**
 * 双击查看
 */
const onDBClick = async (record: TableData, column: TableColumnData) => {
  if (column.dataIndex !== 'name') return;
  if (record.mediaType === 'folder') await getFileListData({ pid: record.id });
  else {
    // 打开文件
  }
};
</script>

<template>
  <section class="h-full px-2 overflow-auto">
    <file-operation />
    <file-breadcrumb />
    <a-table :columns="columns"
             class="my-6"
             :data="fileList"
             hoverable :pagination="false"
             :row-selection="{type: 'checkbox',showCheckedAll: true}"
             v-model:selectedKeys="selectedIdList"
             @row-contextmenu="contextmenuChange"
             @sorterChange="sorterChange"
             @cell-dblclick="onDBClick"
    >
      <template #td="{column,record,rowIndex}">
        <table-td-item :column :record :rowIndex />
      </template>
      <template #empty>
        <blank-file-contextmenu />
      </template>
    </a-table>
    <div class="flex justify-end">
      <file-footer />
    </div>
  </section>
</template>

<script setup lang="ts">
import { findNodeById, getAllNodeIds } from '@/composable/useFolderLevelTree';
import { useBaseFileStore, useFileItemStore, useFileNodeStore, useViewFileStore } from '@/stores/modules/file'
import type { IFileItem } from '@/types/file';

const { breadcrumbs, isTree } = storeToRefs(useBaseFileStore())
const { switchTo } = useBaseFileStore()
const { treeSelectedIds, treeData } = storeToRefs(useFileNodeStore())
const { itemList, query, selectedIds: selectedItemIds } = storeToRefs(useFileItemStore());
const { selectedIds: selectedNodeIds } = storeToRefs(useFileNodeStore())
const { file } = storeToRefs(useViewFileStore())
/**
 * 点击面包屑导航
 */
const clickBreadcrumb = async (pid: string) => {
  // tree不需要发送请求，只需要修改树选中的节点和导航
  if (isTree.value) {
    // 1. 修改树中选择的id
    treeSelectedIds.value = [pid]
    // 2. 修改breadcrumb
    const targetNode = findNodeById(treeData.value, pid)
    // 如果找到了，表示不是根目录
    if (targetNode) switchTo(targetNode)
    else breadcrumbs.value.splice(1)
    // 3. 点的肯定是文件夹，不显示文件
    file.value = null
  }
  else {
    // 不在该界面时才能去该界面
    if (itemList.value.parent.id === pid) return;
    query.value = {
      ...query.value,
      page: 1,
      pid
    }
    selectedItemIds.value = []
  }
};

const isSelectAll = computed({
  get() {
    if (isTree.value) return treeData.value.length !== 0 && selectedNodeIds.value.length === getAllNodeIds(treeData.value).length
    else return itemList.value.records.length !== 0 && itemList.value.records.every(r => selectedItemIds.value.includes(r.id))
  },
  set(v) {
    if (v) {
      if (isTree.value) selectedNodeIds.value = getAllNodeIds(treeData.value)
      else selectedItemIds.value = itemList.value.records.map(r => r.id)
    } else {
      selectedItemIds.value = selectedNodeIds.value = []
    }
  }
})
</script>

<template>
  <div class="h-6 flex justify-start items-center">
    <label>全选&nbsp;<a-checkbox v-model:model-value="isSelectAll"></a-checkbox></label>&nbsp;
    <a-breadcrumb separator="/" class="select-none flex flex-wrap translate-y-[2px]">
      <a-breadcrumb-item @click="clickBreadcrumb(breadcrumbs[0].id)">
        <icon-home :size="18" class="hover:!text-h-blue cursor-pointer" />
      </a-breadcrumb-item>
      <a-breadcrumb-item v-for="b in breadcrumbs.slice(1)" :key="b.id" @click="clickBreadcrumb(b.id)">
        <div class="hover:text-h-blue cursor-pointer">
          {{ (b as IFileItem).name }}
        </div>
      </a-breadcrumb-item>
    </a-breadcrumb>
  </div>
</template>

<style scoped></style>

<script setup lang="ts">
import { useFileStore } from '@/stores';
import type { IFileItem } from '@/types/file';

defineOptions({
  name: 'FileBreadcrumb'
});

const { fileBreadcrumbList, fileListData } = storeToRefs(useFileStore());
const { getFileListData } = useFileStore();
/**
 * 点击面包屑导航
 */
const clickBreadcrumb = async (pid?: string) => {
  // 不在该界面时才能去该界面
  if (fileListData.value.parent.id === pid) return;
  await getFileListData({ pid });
};
</script>

<template>
  <a-breadcrumb separator=">" class="select-none">
    <a-breadcrumb-item class="hover:text-h-blue cursor-pointer" @click="clickBreadcrumb()">
      <icon-home :size="18" />
    </a-breadcrumb-item>
    <a-breadcrumb-item v-for="b in fileBreadcrumbList.slice(1)" :key="b.id" @click="clickBreadcrumb(b.id)">
      <div class="hover:text-h-blue cursor-pointer">
        {{ (b as IFileItem).name }}
      </div>
    </a-breadcrumb-item>
  </a-breadcrumb>
</template>

<style scoped>

</style>

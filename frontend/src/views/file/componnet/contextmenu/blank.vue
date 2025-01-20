<script setup lang="ts">
defineOptions({
  name: 'BlankFileContextmenu'
});
import { createFolder } from './utils';
</script>

<template>
  <a-dropdown trigger="contextMenu" alignPoint>
    <a-empty />
    <template #content>
      <a-doption>
        <icon-drag-arrow />
        粘贴
      </a-doption>
      <a-doption @click="createFolder">
        <icon-plus-circle />
        新建文件夹
      </a-doption>
    </template>
  </a-dropdown>
</template>

<style scoped>

</style>

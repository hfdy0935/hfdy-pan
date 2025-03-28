<script setup lang="ts">
import { useBaseFileStore, useFileItemStore} from '@/stores/modules/file'
import { useAppStore } from '@/stores/modules/app';

const { isMaxFolderLevel, layoutType } = storeToRefs(useBaseFileStore())
const { doPaste, prepareCreateFolder } = useFileItemStore()
const { Icons } = storeToRefs(useAppStore())
</script>

<template>
  <a-dropdown trigger="contextMenu" alignPoint>
    <a-empty />
    <template #content>
      <a-doption @click="doPaste" v-if="!isMaxFolderLevel">
        <icon-drag-arrow />
        &emsp;粘贴
      </a-doption>
      <a-doption @click="prepareCreateFolder" v-if="!isMaxFolderLevel">
        <component :is="Icons.newFolder"></component>
        &emsp;新建文件夹&emsp;
      </a-doption>
      <a-dsubmenu trigger="hover">
        <component :is="Icons.contextmenuLayout"></component>&emsp;&nbsp;布局
        <template #content>
          <a-doption @click="layoutType = 'list'">
            <component :is="Icons.listLayout" style="transform: scale(1.4);"></component>&emsp;&nbsp;列表&emsp;
          </a-doption>
          <a-doption @click="layoutType = 'block'">
            <component :is="Icons.blockLayout"></component>&emsp;&nbsp;方块&emsp;
          </a-doption>
          <a-doption @click="layoutType = 'tree'">
            <component :is="Icons.treeLayout"></component>&emsp;&nbsp;树形&emsp;
          </a-doption>
        </template>
      </a-dsubmenu>
    </template>
  </a-dropdown>
</template>

<style scoped></style>

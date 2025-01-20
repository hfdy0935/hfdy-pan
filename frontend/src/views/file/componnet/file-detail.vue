<script setup lang="ts">
import type { FileDetailVO } from '@/types/file';
import { useFileStore } from '@/stores';
import { menus } from '@/constants/menuData';
import { calcFileSize } from '@/utils/calc';
import { FileIcon } from '@/constants/fileIcon';

defineOptions({
  name: 'FileDetail'
});
const { contextItem, isContextFolder, isEditing, editOp } = storeToRefs(useFileStore());
const {
  name,
  username,
  mediaType,
  createTime,
  updateTime,
  deleteTime,
  size,
  childNum,
  category
} = defineProps<FileDetailVO>();
const isDetailModalShow = defineModel();
const categoryName = computed<string>(() => menus[0].children!.filter(m => m.path.includes(category))[0].name);
const data = computed(() => [
  [`文件${isContextFolder.value ? '夹' : ''}名`, name, true],
  ['所属用户', username, true],
  ['分类', categoryName, true],
  ['文件类型', mediaType, mediaType !== 'folder'],
  ['创建时间', createTime, true],
  ['更新时间', updateTime, true],
  ['删除时间', deleteTime, true],
  ['大小', size ? calcFileSize(size) : '', mediaType !== 'folder'],
  ['子项数量', childNum, mediaType === 'folder']
]);
</script>

<template>
  <a-modal v-model:visible="isDetailModalShow" :closable="false" simple hide-cancel
  >
    <a-list v-if="id">
      <template v-for="([k,v,s],i) in data" :key="i">
        <a-list-item v-if="s">
          <div class="flex">
            <div class="w-32">{{ k }}：</div>
            <div>{{ v }}</div>
          </div>
        </a-list-item>
      </template>

    </a-list>
    <template #title>
      {{ `文件${isContextFolder ? '夹' : ''}详情` }}
      <component :is="FileIcon[mediaType]" class="scale-150 ml-3" />
    </template>
  </a-modal>

</template>

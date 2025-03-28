<script setup lang="ts">
import { useFileItemStore, useFileDetailStore } from '@/stores/modules/file'
import { useAppStore } from '@/stores/modules/app';
import { menus } from '@/constants/menuData';
import { calcFileSize } from '@/utils/calc';


const { contextmenuType } = storeToRefs(useFileItemStore())
const { detail, isDetailModalShow } = storeToRefs(useFileDetailStore())
const appStore = useAppStore()
const categoryName = computed<string>(() => menus[0].children!.find(m => m.path.includes(detail.value.category ?? ''))!.name);
const fileStatus = ['无需转码', '转码中', '转码成功', '转码失败']
const data = computed(() => [
  [`文件${contextmenuType.value === 'folder' ? '夹' : ''}名`, detail.value.name, true],
  ['所属用户', detail.value.username, true],
  ['分类', categoryName, true],
  ['文件类型', detail.value.mediaType, true],
  ['状态', fileStatus[detail.value.status ?? 0], detail.value.category === 'video'],
  ['歌词', detail.value.lyricPath?.length ? '已上传' : '未上传', detail.value.category === 'audio'],
  ['创建时间', detail.value.createTime, true],
  ['更新时间', detail.value.updateTime, true],
  ['删除时间', detail.value.deleteTime, true],
  ['大小', detail.value.size ? calcFileSize(detail.value.size) : '', detail.value.mediaType !== 'folder'],
  ['子项数量', detail.value.childNum, detail.value.mediaType === 'folder']
]);
</script>

<template>
  <a-modal v-model:visible="isDetailModalShow" unmount-on-close :closable="false" simple hide-cancel :width="480">
    <a-list>
      <template v-for="([k, v, s], i) in data" :key="i">
        <a-list-item v-if="s">
          <div class="flex">
            <div class="w-32">{{ k }}：</div>
            <div>{{ v }}</div>
          </div>
        </a-list-item>
      </template>
    </a-list>
    <template #title>
      {{ `文件${contextmenuType === 'folder' ? '夹' : ''}详情` }}
      <component :is="appStore.FileIcons[detail.mediaType ?? 'folder']" class="scale-150 ml-3" />
    </template>
  </a-modal>

</template>

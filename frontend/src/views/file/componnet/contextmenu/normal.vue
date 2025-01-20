<script setup lang="ts">
import { Icons } from '@/constants/fileIcon';
import { useFileStore } from '@/stores';
import type { FileDetailVO, IFileItem } from '@/types/file';
import { createFolder } from './utils';
import { reqFileDetail } from '@/api/file';
import { Message } from '@arco-design/web-vue';
import FileDetail from '@/views/file/componnet/file-detail.vue';


defineOptions({
  name: 'NormalFileContextmenu'
});
const { contextItem, isContextFolder, isEditing, editOp } = storeToRefs(useFileStore());
const { getFileListData, deleteItem } = useFileStore();

/**
 * 点击重命名
 */
const rename = () => {
  isEditing.value = true;
  editOp.value = 'change';
};
// 删除对话框是否显示
const isDeleteModalShow = ref(false);
/**
 * 点击打开
 */
const doOpen = async () => {
  if (isContextFolder.value) await getFileListData({ pid: contextItem.value.id });
  else {
    console.log('打开其他文件');
    // TODO
  }
};

const isDetailModalShow = ref(false);
const detail = ref<FileDetailVO>({} as FileDetailVO);
/**
 * 点击详情
 */
const clickDetail = async () => {
  try {
    const resp = await reqFileDetail(contextItem.value.id);
    if (resp.code === 200) {
      detail.value = resp.data;
      isDetailModalShow.value = true;
    } else Message.error(resp.message);
  } catch {
    Message.error('获取失败');
  }
};
</script>

<template>
  <a-dropdown trigger="contextMenu" alignPoint>
    <slot></slot>
    <template #content>
      <a-dgroup title="查看">
        <a-doption @click="doOpen">
          <component :is="Icons.open" />
          打开
        </a-doption>
        <a-doption @click="clickDetail">
          <component :is="Icons.detail" />
          详情
        </a-doption>
        <a-doption>
          <icon-share-internal />
          分享
        </a-doption>
      </a-dgroup>
      <a-dgroup title="移动">
        <a-doption>
          <icon-drag-arrow />
          移动
        </a-doption>
        <a-doption>
          <icon-drag-arrow />
          复制
        </a-doption>
        <a-doption>
          <icon-drag-arrow />
          剪贴
        </a-doption>
        <a-doption>
          <icon-drag-arrow />
          粘贴
        </a-doption>
      </a-dgroup>
      <a-dgroup title="编辑">
        <a-doption @click="createFolder">
          <icon-plus-circle />
          新建文件夹
        </a-doption>
        <a-doption @click="rename">
          <icon-edit />
          重命名
        </a-doption>
        <a-doption @click="isDeleteModalShow=true">
          <icon-delete />
          删除
        </a-doption>
      </a-dgroup>
    </template>
  </a-dropdown>
  <a-modal v-model:visible="isDeleteModalShow" @ok="deleteItem" :ok-button-props="{status:'danger'}" :closable="false">
    <h2>确定要删除 {{ (contextItem as IFileItem).name }}
      吗？{{ isContextFolder ? '(将会删除该文件夹下的所有文件)' : '' }}</h2>
  </a-modal>
  <template v-if="detail.mediaType">
    <file-detail v-bind="detail" v-model="isDetailModalShow" />
  </template>
</template>


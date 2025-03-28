<script setup lang="ts">
import { useBaseFileStore, useFileItemStore, useFileNodeStore, useUploadFileStore } from '@/stores/modules/file'
import { Input, Message, type FileItem } from '@arco-design/web-vue';


const { baseQuery, isTree, currParentFolder, isMaxFolderLevel } = storeToRefs(useBaseFileStore())
const { itemList } = storeToRefs(useFileItemStore())
const { prepareCreateFolder: prepareItemCreateFolder } = useFileItemStore()
const { prepareCreateFolder: prepareNodeCreateFolder } = useFileNodeStore()
const { uploadFileList } = storeToRefs(useUploadFileStore())
const tableFileNameList = computed(() => itemList.value.records.map(i => i.name))

const inputFileRef = useTemplateRef<InstanceType<typeof Input>>('inputFile')

const change = (_: FileItem[], fileItem: FileItem) => {
  if (tableFileNameList.value.includes(fileItem.name ?? '')) {
    Message.warning('同一个目录下的文件、文件夹名不能重复')
    return
  }
  // 上传列表显示的
  uploadFileList.value.push({
    md5: '', // 算出来了再赋值
    fileItem,
    totalChunks: 1,
    uploadedChunkIndexs: [],
    index: uploadFileList.value.length,
    status: 'parse' as const,
    pid: currParentFolder.value.id,
    level: currParentFolder.value.level
  })
}
</script>

<template>
  <div class="flex justify-between items-start">
    <a-space wrap size="medium">
      <a-button @click="inputFileRef?.$el.click()" type="primary" :disabled="isMaxFolderLevel">
        <template #icon>
          <icon-upload :stroke-width="6" :size="16" />
        </template>
        上传
        <a-upload @change="change" multiple ref="inputFile" style="display: none;" :auto-upload="false"
          :show-file-list="false" />
      </a-button>
      <a-button type="primary" @click="isTree ? prepareNodeCreateFolder() : prepareItemCreateFolder()" status="success"
        :disabled="isMaxFolderLevel">新建
        <template #icon>
          <icon-folder :stroke-width="6" :size="16" />
        </template>
      </a-button>
    </a-space>
    <a-space size="medium">
      <a-input-search v-model.trim="baseQuery.keyword" placeholder="输入文件 / 文件夹名" allow-clear />
    </a-space>
  </div>
</template>

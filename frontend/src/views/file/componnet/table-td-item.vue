<script setup lang="ts">
import { Message, type TableColumnData, type TableData } from '@arco-design/web-vue';
import { FileIcon } from '@/constants/fileIcon';
import type { IFileItem } from '@/types/file';
import { calcFileSize } from '@/utils/calc';
import { useFileStore } from '@/stores';
import { Input } from '@arco-design/web-vue';
import { useEditFileName } from '@/composable/useEditFileName';
import NormalFileContextmenu from './contextmenu/normal.vue';

defineOptions({
  name: 'TableTdItem'
});

interface Props {
  column: TableColumnData,
  record: TableData,
  rowIndex: number
}

const { column, record, rowIndex } = defineProps<Props>();
const { fileListData, selectedIdList, contextItem, isContextFolder, isEditing, editOp } = storeToRefs(useFileStore());
const { createFolder, renameItem } = useFileStore();
const { name, checkName, doInitName } = useEditFileName({
  file: fileListData.value.records.filter((f: IFileItem) => f.id === record.id)[0]
});
// 输入框ref
const inputRef = useTemplateRef<InstanceType<typeof Input>>('rename_input');
// 编辑文件名的输入框是否出现
const isEditNameInputShow = computed(() => isEditing.value && contextItem.value.id === record.id);
/**
 * 当编辑编辑文件名的输入框出现时，自动获取焦点
 */
watchEffect(() => {
  if (isEditNameInputShow.value) inputRef.value?.focus();
});
/**
 * 文件名编辑完成事件
 */
const finishEdit = async () => {
  const pid = fileListData.value.parent.id;
  checkName().then(async name => {
    switch (editOp.value) {
      case 'create':
        await createFolder({ name, pid });
        return;
      case 'change':
        await renameItem(name);
        return;
    }
  }).catch((r) => {
    r && Message.warning(r);
  }).finally(() => {
    isEditing.value = false;
  });
};
</script>

<template>
  <td>
    <!--    为了避免表头也有右键菜单事件，所以把右键菜单加到了每个单元格上，而不是整个表格-->
    <normal-file-contextmenu>
      <div class="min-h-14 flex items-center">
        <template v-if="column.dataIndex==='name'">
          <div class="w-full pr-5 flex justify-start items-center">
            <component :is="FileIcon[record.mediaType as IFileItem['mediaType']]"
                       class="scale-150 mr-3" />
            <template v-if="isEditNameInputShow">
              <a-input v-model="name"
                       style="width:160px"
                       ref="rename_input"
                       @blur="finishEdit"
                       @keyup.enter="inputRef!.blur()"
              ></a-input>
              <span v-if="record.mediaType!=='folder'">.{{ record.name.split('.').slice(-1)[0] }}</span>
            </template>
            <span v-else class="cursor-pointer hover:text-h-blue select-none"
            >{{ record[column.dataIndex!]
              }}</span>
          </div>
        </template>
        <!--      放到这里计算，以防扰乱排序；文件夹没有大小，不显示-->
        <span class="pr-5"
              v-else>{{ column.dataIndex === 'size' && record.mediaType !== 'folder' ? calcFileSize(record[column.dataIndex!]) : record[column.dataIndex!]
          }}</span>
      </div>
    </normal-file-contextmenu>
  </td>
</template>


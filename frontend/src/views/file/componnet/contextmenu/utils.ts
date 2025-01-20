import { reqNewFolderName } from '@/api/file';
import { calcCurrentDatetime, calcUniqueFileId } from '@/utils/calc';
import type { IFileItem } from '@/types/file';
import { useFileStore } from '@/stores';

const { fileListData, contextItem, isEditing, editOp } = storeToRefs(useFileStore());

/**
 * 新建文件夹
 */
export const createFolder = async () => {
  const resp = await reqNewFolderName(fileListData.value.parent.id ?? '');
  const file = {
    id: calcUniqueFileId(fileListData.value.records.map(f => f.id)),
    name: resp.data,
    updateTime: calcCurrentDatetime(),
    mediaType: 'folder'
  } as IFileItem;
  contextItem.value = file; // 更新右键文件
  editOp.value = 'create';
  fileListData.value.records.unshift(file); // 添加到开头
  isEditing.value = true; // 开始编辑
};

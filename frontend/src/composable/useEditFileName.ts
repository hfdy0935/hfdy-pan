import { Message } from '@arco-design/web-vue';
import { useFileStore } from '@/stores';
import type { IFileItem } from '@/types/file';


interface Props {
  file: IFileItem;
}

export function useEditFileName(props: Props) {
  const { file } = props;
  const isFolder = file.mediaType === 'folder';
  // 初始名，没后缀
  let initName = isFolder ? file.name : file.name.split('.').slice(-0, 1).join('.');
  const name = ref(initName); // 输入框双向绑定的文件名，不包含后缀
  const { fileListData, folderList, editOp, fileList } = storeToRefs(useFileStore());

  /**
   * 初始化输入框中的文件名
   */
  const doInitName = () => {
    name.value = initName;
  };

  /**
   * 改名之前检查
   */
  const checkName = () => {
    return new Promise<string>((resolve, reject) => {
      // 如果改名且名和原来一样
      if (name.value.trim() === initName && editOp.value == 'change') reject();
      if (!name.value.trim()) {
        name.value = initName;
        reject(`文件${isFolder ? '夹' : ''}名不能为空`);
      }
      if (/[\/\\]/.test(name.value)) {
        name.value = initName;
        reject(`文件${isFolder ? '夹' : ''}名不能包含'/'和'\\'`);
      }
      // 检查文件/文件夹名不能重复
      // 原来有一个假的文件夹记录，这里判断是否>1
      if (isFolder && folderList.value.filter(f => f.name === name.value).length > 1) {
        name.value = initName;
        reject(`同一文件夹下的文件夹名不能重复`);
      } else if (!isFolder && fileList.value.some(f => f.name === name.value)) {
        name.value = initName;
        reject(`同一文件夹下的文件名不能重复`);
      }
      // 验证成功，可以改名
      const suffix = isFolder ? '' : ('.' + file.name.split('.').slice(-1)[0]);
      resolve(name.value + suffix); // 返回新的文件名，有后缀
    });
  };
  return {
    name, doInitName, checkName
  };
}

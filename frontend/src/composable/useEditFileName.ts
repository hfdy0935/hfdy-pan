import { type IFileItem, type ITreeData } from '@/types/file';
import { findFatherNodeById } from './useFolderLevelTree';
import { Message } from '@arco-design/web-vue';
import { isFolder } from '@/utils/file';
import { useBaseFileStore, useFileItemStore, useFileNodeStore } from '@/stores/modules/file'
import type { Callback } from '@/stores/modules/file';

interface Props {
    fileOrNode: IFileItem | ITreeData;
}


export function useEditFileName({ fileOrNode }: Props) {
    // 初始名
    const initName = fileOrNode.name;
    const name = ref(initName); // 输入框双向绑定的文件名
    const { editNameOp } = storeToRefs(useBaseFileStore())
    const { itemList } = storeToRefs(useFileItemStore())
    const { filteredTreeData } = storeToRefs(useFileNodeStore());
    const { finishCreateFolder, finishRename } = useBaseFileStore()

    /**
     * 初始化输入框中的文件名
     */
    const resetName = () => {
        name.value = initName;
    };
    /**
     * 改名时校验，then提交
     * @param peerNames 兄弟文件/文件夹的名数组
     * @param cb
     * @returns 
     */
    const _checkNameThenCommit = (peerNames: string[], id: string, cb?: Callback) => {
        return new Promise<string>((resolve, reject) => {
            // 如果改名且名和原来一样
            if (name.value.trim() === initName && editNameOp.value == 'change') reject();
            if (!name.value.trim()) {
                cb?.fail?.()
                if (editNameOp.value === 'create') filteredTreeData.value.shift();
                reject(`文件${isFolder(fileOrNode) ? '夹' : ''}名不能为空`);
            }
            if (/[\/\\]/.test(name.value)) {
                cb?.fail?.()
                if (editNameOp.value === 'create') filteredTreeData.value.shift();
                reject(`文件${isFolder(fileOrNode) ? '夹' : ''}名不能包含'/'和'\\'`);
            }
            // 检查文件/文件夹名不能重复，排除自己
            if (peerNames.filter(p => p === name.value).length > 1) {
                reject('同一文件夹下的文件和文件夹不能重名');
            }
            resolve(name.value);
        }).then(async name => {
            switch (editNameOp.value) {
                case 'create':
                    return await finishCreateFolder({ name, pid: id, ...cb });
                case 'change':
                    await finishRename({ name, id, ...cb });
                    return;
            }
        })
            .catch(r => {
                r && Message.warning(r);
            })
    }

    /**
     * tree页面改名校验
     * @param id 操作的id，新建文件夹是父级文件夹的id，修改名是该文件/文件夹的id
     * @param ok
     * @returns 
     */
    const handleFinishEidtInTree = (id: string, cb?: Callback) => {
        const father = findFatherNodeById(filteredTreeData.value, null, fileOrNode.id)
        _checkNameThenCommit(father?.children.map(c => c.name) ?? [], id, cb)
    }

    /**
     * 操作的id，新建文件夹是父级文件夹的id，修改名是该文件/文件夹的id
     * list/block页面改名校验
     */
    const handleFinishEidtInListBlock = (id: string, cb: Callback) => {
        _checkNameThenCommit(itemList.value.records.map(r => r.name), id, cb)
    };
    return {
        name,
        resetName,
        handleFinishEidtInTree,
        handleFinishEidtInListBlock,
    };
}


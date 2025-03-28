import { TransCodeStatusEnum, type FolderLevelInfoVO, type ITreeData } from '@/types/file';
import { getIconByFilename } from '@/constants/fileIcon';
import { reqFolderLevelInfo } from '@/api/file';
import { Message } from '@arco-design/web-vue';
import { useAppStore } from '@/stores/modules/app';

/**
 * 树的映射字段
 */
export const fieldNames = {
    key: 'id',
    title: 'name',
    children: 'children',
    icon: 'icon',
    disabled: 'disabled',
};

// 模拟根节点id，不能为空，不然匹配不到
export const FAKE_ROOT_ID = 'root';
/**
 * 根据id找到树节点
 * @param nodes 
 * @param id 
 * @param allowBlankRoot 是否考虑根节点 
 * @returns 
 */
export const findNodeById = (nodes: ITreeData[], id: string, allowBlankRoot: boolean = true): ITreeData | null => {
    for (const node of nodes) {
        if (node.id === id || (allowBlankRoot && id === '' && node.id === FAKE_ROOT_ID)) return node;
        if (node.children.length) {
            const res = findNodeById(node.children, id);
            if (res) return res;
        }
    }
    return null;
};


/**
 * 根据id找到父节点
 * @param nodes 根节点列表
 * @param father 根节点的父节点，可为空
 * @param id 要找的id
 * @param allowBlankRoot 树需要渲染根节点时为true
 * @returns 
 */
export function findFatherNodeById(nodes: ITreeData[], father: ITreeData | null, id: string, allowBlankRoot: boolean = true): ITreeData | null {
    for (const node of nodes) {
        if (node.id === id || (allowBlankRoot && id === '' && node.id === FAKE_ROOT_ID)) return father;
        if (node.children.length) {
            const res = findFatherNodeById(node.children, node, id);
            if (res) return res;
        }
    }
    return null;
}

/**
 * 根据id找到搜索路径
 * @param nodes 
 * @param id 
 * @param allowBlankRoot 是否考虑根节点 
 * @returns 
 */
export const findSearchPathsById = (nodes: ITreeData[], id: string, allowBlankRoot: boolean = true): ITreeData[] => {
    for (const node of nodes) {
        if (node.id === id || (allowBlankRoot && id === '' && node.id === FAKE_ROOT_ID)) {
            return [node]
        }
        if (node.children.length) {
            const res = findSearchPathsById(node.children, id, allowBlankRoot);
            if (res.length) return [node, ...res]
        }
    }
    return []
};


/**
 * 根据ids数组删除节点，删除需要找到原来的ref变量，直接从ocmputed上删页面不更新
 * @param ids 
 * @returns 
 */
export function removeNodeByIds(nodes: FolderLevelInfoVO[], ids: string[]) {
    for (let i = nodes.length - 1; i >= 0; i--) {
        const node = nodes[i]
        if (ids.includes(nodes[i].id)) {
            nodes.splice(i, 1)
        }
        else if (node.children?.length) {
            removeNodeByIds(node.children, ids);
        }
    }
}

/**
 * 递归获取所有节点的id数组
 * @param nodes 
 */
export function getAllNodeIds(nodes: ITreeData[], res: string[] = []): string[] {
    for (const node of nodes) {
        res.push(node.id)
        if (node.children) {
            getAllNodeIds(node.children, res);
        }
    }
    return res
}


/**
 * 根据filter过滤节点
 * @param nodes 
 * @param filter 
 * @returns 
 */
export function filterFlatNodes(nodes: ITreeData[], filter: (node: ITreeData) => boolean): ITreeData[] {
    let res: ITreeData[] = []
    for (const node of nodes) {
        if (filter(node)) res.push(node)
        if (node.children) {
            res = [...res, ...filterFlatNodes(node.children, filter)]
        }
    }
    return res
}

/**
 * 当前用户的文件层级数据
 */
export function useFolderLevelTree() {
    // 原始文件夹层级数据
    const rawLevel = ref<FolderLevelInfoVO[]>([]);
    const appStore = useAppStore()
    // 计算树组件显示的数据
    const calcFileInfo = (data: FolderLevelInfoVO, isFileDisabled: boolean = true): ITreeData => {
        const children = data.children ? [...data.children.map(f => calcFileInfo(f, isFileDisabled))] : [];
        return {
            ...data,
            children,
            icon: () => h((data.mediaType === 'folder' ? appStore.FileIcons.folder : getIconByFilename(data.name)), {
                style: {
                    minWidth: '16px'
                }
            }),
            disabled: isFileDisabled && data.mediaType !== 'folder',
        };
    };

    // 树显示的数据,禁用文件夹
    const treeDataBanFile = computed<ITreeData[]>(() => rawLevel.value.map(f => calcFileInfo(f)));
    // 树显示的数据,文件夹可用
    const treeDataAllowFile = computed<ITreeData[]>(() => rawLevel.value.map(f => calcFileInfo(f, false)));
    // 树显示的数据，文件夹禁用，显示根节点
    const treeDataBanFileWithRoot = computed<ITreeData[]>(() => [
        {
            id: FAKE_ROOT_ID,
            name: '/',
            children: treeDataBanFile.value,
            icon: () => appStore.FileIcons.folder,
            level: '',
            disabled: false,
            status: TransCodeStatusEnum.NO_NEED,
            category: 'all',
            mediaType: 'folder',
            lyricPath: ''
        },
    ]);
    // 获取层级数据
    const updateTreeLayerData = async () => {
        try {
            const res = await reqFolderLevelInfo();
            if (res.code === 200) {
                rawLevel.value = res.data;
            } else Message.error(res.message);
        } catch {
            Message.error('获取文件层级失败');
        }
    }
    // 已选择节点的id
    const selectedIds = ref<string[]>([]);
    // 计算已选择节点的选择路径
    const selectedPaths = computed(() => findSearchPathsById(treeDataAllowFile.value, selectedIds.value[0], false))
    return {
        FAKE_ROOT_ID,
        rawLevel,
        treeDataBanFile,
        treeDataAllowFile,
        treeDataBanFileWithRoot,
        updateTreeLayerData,
        selectedIds,
        selectedPaths
    };
}

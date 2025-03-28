import {
    type IItemCategory,
    type FileDetailVO,
    type ItemListDTO,
    type ItemListVO,
    type IFileItem,
    type MoveItemDTO,
    TransCodeStatusEnum,
    type IItemMediaType,
    type ITreeData,
} from '@/types/file';
import { reqDeleteFile, reqFileList, reqMoveItem, reqPreviewFile, reqCreateFolder, reqRenameItem } from '@/api/file';
import { Message, type FileItem } from '@arco-design/web-vue';
import { OrderType } from '@/enums';
import { calcCurrentDatetime, calcUniqueFileId, calcUniqueFilename } from '@/utils/calc';
import type { ChunksUploader } from '@/layout/component/header/utils';
import { useBreadcrumb } from '@/composable/useBreadcrumb';
import { useTable } from '@/composable/useTable';
import { findNodeById, removeNodeByIds, useFolderLevelTree } from '@/composable/useFolderLevelTree';
import type { UnwrapRef } from 'vue';
import { calcFileLevel, isFile, isFolder, isVideo } from '@/utils/file';
import AudioViewer from '@/component/file-previewer/audio-viewer.vue';
import VideoViewer from '@/component/file-previewer/video-viewer.vue';
import ExcelViewer from '@/component/file-previewer/excel-viewer.vue';
import ImageViewer from '@/component/file-previewer/image-viewer.vue';
import PdfViewer from '@/component/file-previewer/pdf-viewer.vue';
import PptViewer from '@/component/file-previewer/ppt-viewer.vue';
import WordViewer from '@/component/file-previewer/word-viewer.vue';
import type { Component } from 'vue';
import CodeViewer from '@/component/file-previewer/code-viewer.vue';
import UnknownViewer from '@/component/file-previewer/unknown-viewer.vue';
import { useUserStore } from './user';
import { useAppStore } from './app';

export interface Callback {
    ok?(): any, fail?(): any, err?(): any
}

export const useBaseFileStore = defineStore('baseFileStore', () => {
    const { userInfo } = storeToRefs(useUserStore());
    const { updateUserInfo } = useUserStore();
    // 面包屑导航
    const { breadcrumbs, switchTo, replaceExcludeFirst } = useBreadcrumb<IFileItem | ITreeData>()
    // 当前目录的父目录
    const currParentFolder = computed<IFileItem | ITreeData>(() => breadcrumbs.value[breadcrumbs.value.length - 1])
    // 布局
    const layoutType = ref<'block' | 'list' | 'tree'>('block');
    const isTree = computed(() => layoutType.value === 'tree')
    // 是否到达最大层级
    const isMaxFolderLevel = computed(() => calcFileLevel(currParentFolder.value) >= userInfo.value.maxFolderLevel)
    // 正在编辑名的文件/文件夹/节点id，空字符串表示未编辑状态
    const editingId = ref('')
    // 编辑类型，新建文件夹还是改名
    const editNameOp = ref<'create' | 'change'>('change');
    // 右键菜单是否显示
    const isContextmenuShow = ref(false)
    // 公共查询参数
    const baseQuery = ref<Pick<ItemListDTO, 'keyword'>>({
        keyword: '',
    })
    /**
     * 点击后准备改名
     * @param id 
     * @param op 
     */
    const prepareEditName = (id: string, op: UnwrapRef<typeof editNameOp>) => {
        editingId.value = id
        editNameOp.value = op
    }
    /**
     * 发送请求新建文件夹
     * @param param0 
     */
    const finishCreateFolder = async ({ pid, name, ok, fail, err }: {
        pid: string, name: string
    } & Callback) => {
        try {
            const resp = await reqCreateFolder({ pid, name });
            if (resp.code === 200) {
                Message.success('创建成功');
                ok?.()
            } else {
                Message.error(resp.message);
                fail?.()
            }
        } catch {
            Message.error('创建失败');
            err?.()
        } finally {
            editingId.value = ''
        }
    }

    /**
     * 发送请求重命名
     * @param param0 
     */
    const finishRename = async ({ id, name, ok, fail, err }: {
        id: string
        name: string
    } & Callback) => {
        try {
            const resp = await reqRenameItem({ id, name });
            if (resp.code === 200) {
                Message.success('修改成功');
                ok?.()
            } else {
                Message.error(resp.message);
                fail?.();
            }
        } catch {
            Message.error('修改失败');
            err?.();
        } finally {
            editingId.value = ''
        }
    }

    /**
     * 发送请求删除文件/文件夹
     * @param param0 
     */
    const deleteFiles = async ({ completely, ids, ok, fail, err }: {
        completely: boolean
        ids: string[]
    } & Callback) => {
        try {
            const resp = await reqDeleteFile(ids, completely);
            if (resp.code === 200) {
                Message.success('删除成功');
                ok?.()
                updateUserInfo();
            } else {
                Message.error(resp.message);
                fail?.()
            }
        } catch (e) {
            Message.error('删除失败');
            err?.()
        }
    }
    /**
     * 粘贴文件
     * @param param0 
     * @returns 
     */
    const pasteFiles = async ({
        peers, targetId, ok, fail, err, op, filesNeedMove
    }: {
        /**
         * 目标文件夹的文件/文件夹列表，用来验证名不重复
         */
        peers: (IFileItem | ITreeData)[]
        /**
         * 目标文件夹id
         */
        targetId: string
        /**
         * 操作类型
         */
        op: MoveItemDTO['op']
        /**
         * 需要粘贴的文件列表
         */
        filesNeedMove: (IFileItem | ITreeData)[]
    } & Callback) => {
        if (!filesNeedMove.length) {
            Message.info('请先选择要复制/剪切的文件/文件夹');
            return;
        }// 和当前文件夹下的文件/文件夹不能重复（list和block布局时前端只能判断在同一页的）
        const names1 = peers.map(i => i.name);
        const ids2 = filesNeedMove.map(i => i.name);
        if (new Set([...names1, ...ids2]).size !== names1.length + ids2.length) {
            Message.warning('文件/文件夹名不能和目标文件夹下的文件/文件夹重复');
            return;
        }
        const opName = op === 'copy' ? '复制' : '移动';
        try {
            const resp = await reqMoveItem({
                idList: filesNeedMove.map(i => i.id),
                targetId,
                op: op,
            });
            if (resp.code === 200) {
                Message.success(`${opName}成功`);
                // 在清空之前调用
                ok?.();
                filesNeedMove = [];
                updateUserInfo();
            } else {
                Message.error(resp.message);
                fail?.()
            }
        } catch {
            Message.error(`${opName}失败`);
            err?.()
        }
    }
    return {
        breadcrumbs, switchTo, replaceExcludeFirst, currParentFolder, isMaxFolderLevel,
        layoutType, isTree,
        editingId, editNameOp, isContextmenuShow,
        baseQuery,
        prepareEditName,
        finishCreateFolder, finishRename,
        deleteFiles,
        pasteFiles
    }

})

export const useFileItemStore = defineStore('fileItemStore', () => {
    const route = useRoute()
    const {
        selectedIds,
        contextmenuItem,
        contextmenuType, clearSelected
    } = useTable<IFileItem>()
    const selectedFiles = computed<IFileItem[]>(() => itemList.value.records.filter(r => selectedIds.value.includes(r.id)))
    const { breadcrumbs, isContextmenuShow, baseQuery, isTree, isMaxFolderLevel } = storeToRefs(useBaseFileStore())
    const { switchTo: switchBreadcrumbTo, prepareEditName, deleteFiles, pasteFiles } = useBaseFileStore()
    // 请求文件列表请求体体
    const query = ref<Omit<ItemListDTO, 'keyword'>>({
        category: 'all',
        page: 1,
        pageSize: 10,
        pid: '',
        orderByUpdateTime: OrderType.DEFAULT,
        orderBySize: OrderType.DEFAULT
    })
    // 所有文件/文件夹列表数据
    const itemList = ref<ItemListVO>({
        total: 0,
        parent: {
            id: '',
            name: ' ',
            updateTime: '',
            pid: '',
            level: '',
            category: 'all',
            mediaType: 'folder',
            status: TransCodeStatusEnum.NO_NEED,
            lyricPath: ''
        },
        records: [],
        category: 'all',
        page: query.value.page,
        pageSize: query.value.pageSize,
        keyword: baseQuery?.value.keyword ?? '',
        orderByUpdateTime: OrderType.DEFAULT,
        orderBySize: OrderType.DEFAULT
    });
    // 文件夹列表
    const folderList = computed(() => itemList.value.records.filter(isFolder))
    // 文件列表
    const fileList = computed(() => itemList.value.records.filter(isFile))
    // pid是当前breadcrumb导航的最后一个id
    watch(breadcrumbs, value => query.value.pid = value[value.length - 1].id)
    /**
     * 发送请求更新文件列表
     * @param reqParams
     */
    const updateFileItem = async (reqParams?: Partial<ItemListDTO>) => {
        try {
            const resp = await reqFileList({
                ...query.value,
                ...baseQuery.value,
                ...(reqParams || {}),
            });
            if (resp.code === 200) {
                itemList.value = resp.data;
                switchBreadcrumbTo(resp.data.parent)
                return true;
            } else Message.error('获取文件失败 ' + (resp.message ?? ''));
        } catch {
            Message.error('获取文件失败');
        }
    };
    /**
     * 查询参数变化时发送请求获取文件
     */
    watch([
        () => query.value.category,
        () => query.value.page,
        () => query.value.pid,
        () => query.value.pageSize,
        () => query.value.orderByUpdateTime,
        () => query.value.orderBySize,
    ], () => {
        // tree中修改breadcrumb时会修改query的pid，此时不在list和block页面
        // 如果不请求的话页面会跳一下
        !isTree.value && updateFileItem()
    }, { immediate: true })
    watch(() => route.params.category, value => {
        clearSelected()
        value && (query.value.category = value as IFileItem['category'])
    })
    /**
     * 搜索关键词变化时重新请求
     */
    watch(baseQuery, () => {
        if (!isTree.value) updateFileItem()
    }, { deep: true })
    // 选中的文件>1个且当前右键点在了选中的文件上，视为要操作多个
    const shouldHandleGroup = computed(() => selectedFiles.value.length > 1 && selectedFiles.value.some(f => f.id === contextmenuItem.value?.id));
    /**
     * 点击新建文件夹
     */
    const prepareCreateFolder = async () => {
        if (isMaxFolderLevel.value) {
            Message.warning('创建失败，已达到最大深度');
            return;
        }
        // 用到的属性只有这么多
        const file = {
            id: calcUniqueFileId(itemList.value.records.map(f => f.id)),
            name: calcUniqueFilename(itemList.value.records.map(f => f.name)),
            updateTime: calcCurrentDatetime(),
            mediaType: 'folder',
        } as IFileItem;
        prepareEditName(file.id, 'create')
        // 添加到当前页开头
        itemList.value.records.unshift(file);
    }
    /**
     * 删除文件/文件夹
     * @param completely 
     */
    const doDelete = async (completely: boolean) => {
        const ids = shouldHandleGroup.value ? selectedFiles.value.map(f => f.id) : [contextmenuItem.value!.id]
        deleteFiles({
            completely,
            ids,
            ok() {
                // 本地删除记录
                itemList.value.records = itemList.value.records.filter(r => !ids.includes(r.id))
            }
        })
    }

    // 复制还是剪切
    const pasteOp = ref<MoveItemDTO['op']>('copy')
    // 需要粘贴的文件id列表
    const filesNeedMove = ref<(IFileItem | ITreeData)[]>([]);

    /**
     * 准备剪切
     * @param items 
     */
    const prepareCut = (items: IFileItem[]) => {
        pasteOp.value = 'cut'
        filesNeedMove.value = items
    }
    /**
     * 准备复制
     * @param items 
     */
    const prepareCopy = (items: IFileItem[]) => {
        pasteOp.value = 'copy'
        filesNeedMove.value = items
    }

    /**
     * 粘贴
     */
    const doPaste = async () => {
        pasteFiles({
            peers: itemList.value.records,
            targetId: itemList.value.parent.id,
            ok: () => {
                updateFileItem()
                filesNeedMove.value = []
            },
            op: pasteOp.value,
            filesNeedMove: filesNeedMove.value
        })
    }
    return {
        selectedIds,
        selectedFiles,
        contextmenuItem,
        contextmenuType,
        isContextmenuShow,
        clearSelected,
        query, itemList, folderList, fileList,
        updateFileItem, shouldHandleGroup,
        prepareCreateFolder, doDelete,
        filesNeedMove, prepareCut, prepareCopy, doPaste
    }
})


export const useFileNodeStore = defineStore('fileNodeStore', () => {
    const {
        selectedIds,
        contextmenuItem,
        contextmenuType
    } = useTable<ITreeData>()
    const { FileIcons } = storeToRefs(useAppStore())
    const { baseQuery, breadcrumbs } = storeToRefs(useBaseFileStore())
    const selectedFiles = computed<ITreeData[]>(() => selectedIds.value.map(i => findNodeById(treeData.value, i)).filter(i => i !== null))
    const { prepareEditName, deleteFiles, pasteFiles } = useBaseFileStore()
    const { treeDataAllowFile: treeData, rawLevel: rawTreeData, selectedIds: treeSelectedIds, selectedPaths: treeSelectedPaths, updateTreeLayerData: updateTreeData } = useFolderLevelTree()
    /**
     * 遍历搜索树的node
     */
    function _searchData(keyword: string) {
        const loop = (data: ITreeData[]) => {
            const result: ITreeData[] = [];
            data.forEach(item => {
                if (item.name.toLowerCase().includes(keyword.toLowerCase())) {
                    result.push({ ...item });
                } else if (item.children) {
                    const filterData = loop(item.children);
                    if (filterData.length) {
                        result.push({
                            ...item,
                            children: filterData
                        })
                    }
                }
            })
            return result;
        }
        return loop(treeData.value);
    }
    /**
     * 根据keyword搜索后显示的树数据
     */
    const filteredTreeData = computed(() => {
        if (!baseQuery.value.keyword) return treeData.value;
        return _searchData(baseQuery.value.keyword);
    })
    /**
     * 点击新建文件夹
     * @param parent 传了表示在该parent下新建，不传回去找breadcrumb的最后一项，在它下面新建
     * @returns 
     */
    const prepareCreateFolder = (parent?: ITreeData) => {
        const _parent = parent ?? findNodeById(treeData.value, breadcrumbs.value[breadcrumbs.value.length - 1].id) ?? {
            children: treeData.value
        }
        const node: ITreeData = {
            id: calcUniqueFileId(_parent.children.map(c => c.id)),
            name: calcUniqueFilename(_parent.children.map(c => c.name)),
            children: [],
            level: '',
            status: TransCodeStatusEnum.NO_NEED,
            category: 'all',
            mediaType: 'folder',
            icon: () => FileIcons.value.folder,
            disabled: false,
            lyricPath: ''
        }
        prepareEditName(node.id, 'create')
        // 添加到树节点开头
        _parent.children.unshift(node)
    }
    /**
     * 删除文件/文件夹
     * @param completely 
     * @param ids
     */
    const doDelete = async (completely: boolean, ids: string[]) => {
        deleteFiles({
            completely,
            ids,
            ok() {
                // 本地删除记录，要在原始响应式对象上删
                removeNodeByIds(rawTreeData.value, ids)
            }
        })
    }

    /**
     * 拖拽释放时剪切
     * @param peers 父文件夹的子文件/文件夹列表
     * @param targetId 父文件夹id
     * @param nodeNeedMove 需要移动的节点
     */
    const doDropCut = (peers: ITreeData[], targetId: string, nodeNeedMove: ITreeData) => {
        pasteFiles({
            peers, targetId, op: 'cut', filesNeedMove: [nodeNeedMove],
            ok: updateTreeData
        })
    }

    return {
        treeData, rawTreeData, treeSelectedIds, treeSelectedPaths, updateTreeData,
        selectedIds,
        selectedFiles,
        contextmenuItem,
        contextmenuType,
        filteredTreeData,
        prepareCreateFolder, doDelete, doDropCut
    }
})

export interface IUploadChunks {
    /**
     * 文件md5
     */
    md5: string;
    fileItem: FileItem;
    /**
     * 总分块数
     */
    totalChunks: number;
    /**
     * 已上传的分块索引数组
     */
    uploadedChunkIndexs: number[];
    /**
     * 该文件在所有上传的文件中的索引
     */
    index: number;
    /***
     * 状态
     */
    status: 'parse' | 'upload' | 'finish' | 'pause' | 'error';
    uploader?: ChunksUploader;
    /**
     * 要上传到哪个文件夹下，保存上传时的文件夹id，以防恢复上传的时候文件夹找错
     */
    pid: string;
    /**
     * 层级路径
     */
    level: string
}

export const useUploadFileStore = defineStore('uploadFileStore', () => {
    // 上传列表显示总的的文件
    const uploadFileList = ref<IUploadChunks[]>([]);
    // 已上传的文件uid
    const uploadedFileUidList = ref<string[]>([]);
    return { uploadFileList, uploadedFileUidList };
});

export const useFileDetailStore = defineStore('fileDetailStore', () => {
    const detail = ref<FileDetailVO>({} as FileDetailVO);
    const isDetailModalShow = ref<boolean>(false);
    const openDetailModal = (data: FileDetailVO) => {
        detail.value = data;
        isDetailModalShow.value = true;
    };
    return { detail, isDetailModalShow, openDetailModal };
});

export const useViewFileStore = defineStore('viewFileStore', () => {
    const isModalShow = ref(false);
    // 当前预览的文件
    const file = ref<IFileItem | null>(null);
    const data = ref<Blob | null>(null);
    // 要显示的文件类型
    const mediaType = ref<IItemMediaType>('text');
    const spinning = ref(false);
    const route = useRoute();
    let control = new AbortController();
    const refreshControl = () => {
        control.abort()
        control = new AbortController()
    }
    watch(isModalShow, (newValue, oldValue) => {
        if (oldValue && !newValue) {
            refreshControl()
        }
    });

    const getData = async () => {
        if (!file.value) return;
        // 视频未转码警告
        if (isVideo(file.value) && file.value.status !== TransCodeStatusEnum.OK) {
            Message.warning('视频未转码，传输速度和质量会受影响，转码后食用更佳');
        }
        // 如果是unknown、zip类型的数据，先不请求，等确认以text打开了再请求
        if (['unknown', 'zip'].includes(file.value.mediaType)) return;
        spinning.value = true;
        try {
            data.value = null;
            // 考虑到打开分享页面，也需要验证文件是不是该分享下的，携带路径参数shareId
            const resp = await reqPreviewFile(file.value.id, control, route.params.shareId as string);
            data.value = resp;
        } catch (e: any) {
            e.message !== 'canceled' && Message.error('获取文件信息失败');
        } finally {
            spinning.value = false;
        }
    };

    const openPreviewModal = async (item: IFileItem) => {
        file.value = item;
        // 先开对话框，显示加载中
        isModalShow.value = true;
        await getData();
    };
    const modalType: Partial<Record<IItemMediaType, {
        component: Component,
        needFullscreen: boolean
    }>> = {
        video: {
            component: VideoViewer,
            needFullscreen: false
        },
        audio: {
            component: AudioViewer,
            needFullscreen: false
        },
        image: {
            component: ImageViewer,
            needFullscreen: false
        },
        zip: {
            component: UnknownViewer,
            needFullscreen: true
        },
        text: {
            component: CodeViewer,
            needFullscreen: true
        },
        ppt: {
            component: PptViewer,
            needFullscreen: true
        },
        pptx: {
            component: PptViewer,
            needFullscreen: true
        },
        pdf: {
            component: PdfViewer,
            needFullscreen: true
        },
        xlsx: {
            component: ExcelViewer,
            needFullscreen: true
        },
        csv: {
            component: CodeViewer,
            needFullscreen: true
        },
        doc: {
            component: WordViewer,
            needFullscreen: true
        },
        docx: {
            component: WordViewer,
            needFullscreen: true
        },
        code: {
            component: CodeViewer,
            needFullscreen: true
        },
        md: {
            component: CodeViewer,
            needFullscreen: true
        },
        unknown: {
            component: UnknownViewer,
            needFullscreen: true
        }
    }
    const currentItem = computed(() => modalType[file.value?.mediaType ?? 'unknown'])

    return { isModalShow, file, spinning, openPreviewModal, data, getData, mediaType, refreshControl, currentItem };
});



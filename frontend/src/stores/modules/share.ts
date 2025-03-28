import { reqShareFileDTO } from "@/api/share"
import { reqGetMyShare } from "@/api/share"
import { useBreadcrumb } from "@/composable/useBreadcrumb"
import { useFolderLevelTree } from "@/composable/useFolderLevelTree"
import { useShareConfig } from "@/composable/useShareFile"
import type { GetShareFileVO, IFileItem, ITreeData } from "@/types/file"
import type { GetMyShareVO } from "@/types/share"
import { Message } from "@arco-design/web-vue"

/**
 * 创建分享
 */
export const useSetShareStore = defineStore('setShareFileStore', () => {
    const sharedFiles = ref<(IFileItem | ITreeData)[]>([]);
    const show = ref(false);
    const openShareFileModal = (files: (IFileItem | ITreeData)[]) => {
        sharedFiles.value = [...files];
        show.value = true;
    };
    return { sharedFiles, show, openShareFileModal };
});


/**
 * 修改分享
 */
export const useUpdateShareStore = defineStore('updateShareStore', () => {
    // 当前操作的分享id
    const shareId = ref('')
    // 分享的数据
    const shareData = ref<GetMyShareVO>([])
    // 更新分享的数据
    const updateData = async () => {
        try {
            const resp = await reqGetMyShare()
            if (resp.code === 200) shareData.value = resp.data
            else Message.error(resp.message)
        } catch {
            Message.error('获取文件列表失败')
        }
    }
    // 分享配置
    const { config, formattedConfig } = useShareConfig()
    // 分享包括的文件
    const { treeDataAllowFile, selectedIds, selectedPaths, updateTreeLayerData } = useFolderLevelTree()
    return {
        shareId, shareData, updateData, config, formattedConfig, treeDataAllowFile, selectedIds, selectedPaths, updateTreeLayerData
    }
})

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
/**
 * 获取分享
 */
export const useGetShareStore = defineStore('getShareStore', () => {
    const route = useRoute();
    // 当前页的数据
    const sharedFiles = ref<GetShareFileVO>({} as GetShareFileVO);
    // 是否加载中
    const spinning = ref(false);
    const { breadcrumbs, switchTo } = useBreadcrumb<IFileItem>()
    // 分享的id
    const shareId = computed(() => route.params.shareId as string);
    // 输入的提取码
    const pwd = ref('')
    // 选中行文件的id
    const selectedIds = ref<string[]>([]);
    // 选中的文件
    const selectedFiles = computed<IFileItem[]>(() => sharedFiles.value?.records.filter(f => selectedIds.value.includes(f.id)) ?? []);
    // 是否应该处理多行
    const shouldHandleGroup = computed(() => selectedIds.value.includes(contextItem.value.id));
    // 右键菜单的行
    const contextItem = ref<IFileItem>({} as IFileItem);
    // 树对话框
    const treeModalShow = ref(false)
    // 打开位置
    const modalPos = ref<'top' | 'bottom'>('top')
    /**
     * 切换文件夹
     * @param param0 
     */
    const updateFolder = async ({
        pid,
        pwdErrorCB,
        okCb,
    }: {
        // 父文件夹id
        pid?: string;
        // 密码错误回调
        pwdErrorCB?: () => void;
        // 成功回调
        okCb?: (data: GetShareFileVO) => void;
    } = {}) => {
        try {
            spinning.value = true;
            const resp = await reqShareFileDTO({
                id: shareId.value,
                pwd: pwd.value || (route.params.pwd as string),
                pid,
            });
            if (resp.code === 200) {
                sharedFiles.value = resp.data;
                switchTo(resp.data.parent)
                okCb?.(resp.data);
            } else {
                if (resp.code === 632) pwdErrorCB?.();
                else Message.error(resp.message);
            }
        } catch {
            Message.error('获取失败');
        } finally {
            spinning.value = false;
        }
    };

    return {
        sharedFiles,
        updateFolder,
        breadcrumbs,
        spinning,
        shareId,
        pwd,
        selectedIds,
        selectedFiles,
        shouldHandleGroup,
        contextItem,
        treeModalShow,
        modalPos
    };
});

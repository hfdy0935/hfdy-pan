import { type IItemMediaType } from "@/types/file";
import { isFolder } from "@/utils/file";
import type { ComputedRef } from 'vue'
export function useTable<T extends Record<'mediaType', IItemMediaType>>() {
    // 选中的行id
    const selectedIds = ref<string[]>([]);
    /**
     * 清空已选择的文件
     */
    const clearSelected = () => {
        selectedIds.value = []
    }
    // 右键菜单打开时点的的行
    const contextmenuItem = ref<T | null>(null);
    // 右键菜单选的是文件还是文件夹
    const contextmenuType = computed<'folder' | 'file' | null>(() =>
        contextmenuItem.value === null ? null : isFolder(contextmenuItem.value) ? 'folder' : 'file'
    );


    return {
        selectedIds,
        contextmenuItem: contextmenuItem as Ref<T | null>,
        contextmenuType: contextmenuType as ComputedRef<'folder' | 'file' | null>,
        clearSelected
    };
}
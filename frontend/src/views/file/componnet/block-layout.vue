<template>
    <normal-file-contextmenu v-if="itemList.records.length">
        <a-space wrap>
            <div v-for="item in itemList.records" :key="item.id" @contextmenu=" contextmenuItem = item"
                @dblclick="openFileOrFolder(item)"
                class="w-28 h-28 flex flex-col justify-around items-center group truncate cursor-pointer rounded transition-all"
                :class="`${isItemHover(item.id) ? isDark ? 'bg-[#333]' : 'bg-[#eee]' : ''} ${isDark ? 'hover:bg-[#333]' : 'hover:bg-[#eee]'}`">
                <file-icon-name :item :icon-style="{ transform: 'scale(1.6)' }"
                    :name-style="{ fontSize: '12px', textAlign: 'center' }" :input-width="100"></file-icon-name>
                <a-checkbox class="-mt-3" v-model:model-value="checked[item.id]"
                    @change="checkChange(item)"></a-checkbox>
            </div>
        </a-space>
        <div class="flex justify-end mr-3 mb-3">
            <a-pagination :total="itemList.total" v-model:current="query.page" v-model:page-size="query.pageSize"
                show-page-size />
        </div>
    </normal-file-contextmenu>
    <blank-file-contextmenu v-else />
</template>

<script setup lang="ts">
import { useBaseFileStore, useFileItemStore } from '@/stores/modules/file'
import { useUserStore } from '@/stores/modules/user';
import { useAppStore } from '@/stores/modules/app';
import FileIconName from './table-cell-item/file-icon-name.vue';
import { type IFileItem } from '@/types/file';
import NormalFileContextmenu from './table-cell-item/contextmenu/normal.vue';
import BlankFileContextmenu from './table-cell-item/contextmenu/blank.vue';
import { Message } from '@arco-design/web-vue';
import { calcFileLevel, isFolder } from '@/utils/file';
import { useViewFileStore } from '@/stores/modules/viewFile';


const { currParentFolder } = storeToRefs(useBaseFileStore())
const { itemList, query, contextmenuItem, selectedIds, isContextmenuShow } = storeToRefs(useFileItemStore());
const { userInfo } = storeToRefs(useUserStore());
const { openPreviewModal } = useViewFileStore();
const { isDark } = storeToRefs(useAppStore())

// 决定该文件背景是否应该显示为悬浮时的状态，用于多选操作
const isItemHover = (id: string): boolean => {
    return isContextmenuShow.value && selectedIds.value.includes(id) && selectedIds.value.includes(contextmenuItem.value?.id ?? '');
};
const route = useRoute()

/**
 * 双击文件夹/文件打开
 */
const openFileOrFolder = async (item: IFileItem) => {
    if (isFolder(item)) {
        if (calcFileLevel(currParentFolder.value) === userInfo.value.maxFolderLevel) {
            Message.warning('已达到最大深度，不支持打开');
            return;
        }
        query.value = {
            ...query.value,
            page: 1,
            pid: item.id
        }
    } else openPreviewModal(item);
};

/**
 * 是否勾选
 */
const checked = ref<{ [key: string]: boolean }>(
    itemList.value.records.reduce(
        (prev, curr) => ({
            ...prev,
            [curr.id]: selectedIds.value.some(id => id == curr.id),
        }),
        {},
    )
);
const checkChange = (item: IFileItem) => {
    const index = selectedIds.value.findIndex(f => f === item.id);
    if (index === -1) selectedIds.value.push(item.id);
    else selectedIds.value.splice(index, 1);
};
watch(selectedIds, value => {
    for (const record of itemList.value.records) {
        checked.value[record.id] = value.includes(record.id)
    }
})

</script>

<style scoped></style>

<script setup lang="ts">
import { TransCodeStatusEnum } from '@/types/file';
import FileOperation from './componnet/top-operation/file-operation.vue';
import { useBaseFileStore, useFileItemStore, } from '@/stores/modules/file'
import { useAppStore } from '@/stores/modules/app';
import FileBreadcrumb from './componnet/top-operation/file-breadcrumb.vue';
import FileViewModal from './componnet/file-view-modal/index.vue';
import FileDetailModal from './componnet/table-cell-item/contextmenu/file-detail-modal.vue';
import { reqFileStatus } from '@/api/file';
import FileShareModal from './componnet/table-cell-item/contextmenu/file-share-modal.vue';
import { isVideo } from '@/utils/file';
import type { Component, UnwrapRef } from 'vue';


const { textColor, bgColor } = storeToRefs(useAppStore());
const { itemList, query } = storeToRefs(useFileItemStore())
const { updateFileItem } = useFileItemStore()
const { layoutType, isTree } = storeToRefs(useBaseFileStore());
// 布局变化时，如果旧的是tree，重新获取itemList
watch(layoutType, async (_, o) => {
    if (o === 'tree') await updateFileItem()
})
// 切换文件分类时把页码重置为1
onBeforeRouteUpdate(to => {
    // if (/\/file\//.test(to.fullPath)) 
    query.value.page = 1;
});
/**
 * 轮询转码结果
 */
let timer: number = -1;

watch(
    itemList,
    () => {
        clearInterval(timer);
        if (isTree.value) return
        timer = setInterval(async () => {
            const needGetStatusFiles = itemList.value.records.filter(i => isVideo(i) && i.status === TransCodeStatusEnum.ING);
            if (needGetStatusFiles.length === 0) {
                clearInterval(timer);
                return;
            }
            const resp = await reqFileStatus(needGetStatusFiles.map(f => f.id));
            if (resp.code === 200) {
                itemList.value.records.forEach(f => {
                    const item = resp.data.find(i => i.id === f.id);
                    if (item) f.status = item.status;
                });
                // 如果视频都转码成功，停掉计时器
                if (itemList.value.records.filter(i => isVideo(i)).every(i => i.status === TransCodeStatusEnum.OK)) {
                    clearInterval(timer);
                }
            }
        }, 10000);
    },
    {
        immediate: true,
    },
);

onBeforeMount(() => {
    clearInterval(timer);
});

const LayoutComp: Record<UnwrapRef<typeof layoutType>, Component> = {
    list: defineAsyncComponent(() => import('./componnet/list-layout.vue')),
    block: defineAsyncComponent(() => import('./componnet/block-layout.vue')),
    tree: defineAsyncComponent(() => import('./componnet/tree-layout.vue'))
}
</script>

<template>
    <section class="h-full pl-2 flex flex-col" :style="{ color: textColor, backgroundColor: bgColor }">
        <div class="pt-3 pr-3">
            <file-operation />
            <file-breadcrumb />
            <a-divider></a-divider>
        </div>
        <main class="h-[90%]  overflow-auto">
            <component :is="LayoutComp[layoutType]"></component>
        </main>
        <file-view-modal></file-view-modal>
        <file-detail-modal></file-detail-modal>
        <file-share-modal></file-share-modal>
    </section>
</template>

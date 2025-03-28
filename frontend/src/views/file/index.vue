<script setup lang="ts">
import { TransCodeStatusEnum, type TableFileList } from '@/types/file';
import FileOperation from './componnet/top-operation/file-operation.vue';
import { useBaseFileStore, useFileItemStore,  } from '@/stores/modules/file'
import { useAppStore } from '@/stores/modules/app';
import FileBreadcrumb from './componnet/top-operation/file-breadcrumb.vue';
import FileViewModal from './componnet/file-view-modal/index.vue';
import FileDetailModal from './componnet/table-cell-item/contextmenu/file-detail-modal.vue';
import { reqFileStatus } from '@/api/file';
import FileShareModal from './componnet/table-cell-item/contextmenu/file-share-modal.vue';
import GridLayout from './componnet/block-layout.vue';
import ListLayout from './componnet/list-layout.vue';
import TreeLayout from './componnet/tree-layout.vue';
import { isVideo } from '@/utils/file';


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
 * 要显示的文件列表，在请求数据的基础上提取列表、增加key字段
 */
const showedFileList = computed<TableFileList>(
    () =>
        itemList.value.records?.map(f => {
            return {
                ...f,
                key: f.id,
            };
        }) ?? [],
);
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

</script>

<template>
    <section class="h-full pl-3 flex flex-col" :style="{ color: textColor, backgroundColor: bgColor }">
        <div class="pt-3 pr-3">
            <file-operation />
            <file-breadcrumb />
            <a-divider></a-divider>
        </div>
        <main class="h-[90%] overflow-auto pt-3">
            <list-layout v-if="layoutType === 'list'" />
            <grid-layout v-else-if="layoutType === 'block'"></grid-layout>
            <tree-layout v-else />
        </main>
        <file-view-modal></file-view-modal>
        <file-detail-modal></file-detail-modal>
        <file-share-modal></file-share-modal>
    </section>
</template>

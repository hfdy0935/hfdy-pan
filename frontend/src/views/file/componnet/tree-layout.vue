<template>
    <a-row class="h-full">
        <a-col flex="300px" class="h-full pr-1 overflow-hidden">
            <a-tree :data="filteredTreeData" :fieldNames v-model:selected-keys="treeSelectedIds"
                v-model:expanded-keys="expandNodeIds" v-model:checked-keys="selectedIds" check-strictly
                @select="handleSelect" :draggable="!editingId" blockNode checkable @drop="handleDrop">
                <template #title="node">
                    <tree-node-item :node />
                </template>
                <template #drag-icon></template>
                <template #extra="node">
                    <component
                        v-if="isFolder(node) && (editingId !== node.id) && calcFileLevel(node) < userInfo.maxFolderLevel"
                        @click="startCreateFolderExpand(node)" :is="Icons.newFolder" class="hover:text-h-blue">
                    </component>
                </template>
            </a-tree>
        </a-col>
        <a-col flex="auto" class="h-full relative overflow-hidden">
            <display-comp :data :file :getData :comp="currentItem?.component" />
        </a-col>
    </a-row>
</template>

<script setup lang="ts">
import { fieldNames, findFatherNodeById } from '@/composable/useFolderLevelTree';
import { useBaseFileStore, useFileNodeStore ,useViewFileStore} from '@/stores/modules/file'
import { useUserStore } from '@/stores/modules/user';
import { useAppStore } from '@/stores/modules/app';
import { type TreeNodeData } from '@arco-design/web-vue';
import DisplayComp from './file-view-modal/display-comp.vue';
import { type ITreeData } from '@/types/file';
import TreeNodeItem from './tree-children/tree-node-item.vue';
import { isFolder } from '@/utils/file';
import { calcFileLevel } from '@/utils/file';

const { userInfo } = storeToRefs(useUserStore())
const { breadcrumbs, editingId } = storeToRefs(useBaseFileStore())
const { replaceExcludeFirst } = useBaseFileStore()
const {
    treeSelectedIds,
    treeData,
    selectedIds,
    filteredTreeData,
    treeSelectedPaths,
} = storeToRefs(useFileNodeStore())
const { updateTreeData, doDropCut, prepareCreateFolder } = useFileNodeStore()

const { file, data, currentItem } = storeToRefs(useViewFileStore())
const { getData, refreshControl } = useViewFileStore()
const { Icons } = storeToRefs(useAppStore())

// 展开的节点ids
const expandNodeIds = ref<string[]>([])
watchEffect(() => {
    updateTreeData()
})
onBeforeMount(() => {
    const len = breadcrumbs.value.length
    // 如果是从list或block页面来的，根据breadcrumb更新树中选中的id
    treeSelectedIds.value = len === 1 ? [] : [breadcrumbs.value[len - 1].id]
})
onBeforeUnmount(() => file.value = null)
// 切换展开状态
const toggleExpandStatus = (id: string) => {
    if (expandNodeIds.value.includes(id)) {
        expandNodeIds.value = expandNodeIds.value.filter(i => i !== id)
    } else {
        expandNodeIds.value.push(id)
    }
}
/**
 * 点击选择事件
 * @param _ 
 * @param param1 
 */
const handleSelect = async (ids: (string | number)[], { node }: { node?: TreeNodeData }) => {
    const node1 = node as ITreeData
    const id1 = ids[0] as string
    // 点击的是文件夹
    if (isFolder(node1)) {
        toggleExpandStatus(node1.id)
        replaceExcludeFirst(...treeSelectedPaths.value)
        // 不显示文件
        file.value = null
    }
    else {
        treeSelectedIds.value = [id1]
        // 点击文件，发送请求获取
        file.value = {
            ...node1,
            updateTime: '',
            pid: '',
        }
        // 找到文件的路径，修改breadcrumb，如果没有就清空
        replaceExcludeFirst(...treeSelectedPaths.value.slice(0, -1))
        refreshControl()
        await getData()
    }
}

//拖拽移动
const handleDrop = ({ dragNode, dropNode, dropPosition }: {
    dragNode: TreeNodeData, dropNode: TreeNodeData,
    /**
     * -1前，0上，1后
     */
    dropPosition: number
}) => {
    const drag = dragNode as ITreeData
    const drop = dropNode as ITreeData
    // 放到文件夹内
    if (isFolder(drop) && dropPosition === 0) {
        doDropCut(drop.children, drop.id, drag)
        if (!expandNodeIds.value.includes(drop.id))
            expandNodeIds.value.push(drop.id)
    } else {
        // 放到同级目录下
        const parent = findFatherNodeById(treeData.value, null, drop.id)
        // 如果拖到了原来的目录
        const peers = parent?.children ?? treeData.value
        if (peers.some(p => p.id === drag.id)) return
        doDropCut(peers, parent?.id ?? '', drag)
    }
}


// 开始新建文件夹并且展开父文件夹
const startCreateFolderExpand = (node: ITreeData) => {
    expandNodeIds.value.push(node.id)
    prepareCreateFolder(node)
}
</script>

<style scoped></style>
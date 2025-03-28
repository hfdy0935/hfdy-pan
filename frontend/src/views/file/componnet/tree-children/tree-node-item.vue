<template>
    <a-input allow-clear ref="rename_input" v-if="editingId === node.id" v-model:model-value="name"
        @keyup.enter="inputRef!.blur()" @blur="handleFinishEdit"></a-input>
    <template v-else>
        <tree-contextmenu :node>
            <div :style="{ backgroundColor: shouldHover(node) ? '#F2F3F5' : '' }" class="w-44 rounded truncate">
                <span v-if="index = getMatchIndex(node.name), index < 0" @contextmenu="handleContextmenu">
                    {{ node.name }}
                </span>
                <span v-else @contextmenu="handleContextmenu">
                    <span>
                        {{ node.name.slice(0, index) }}
                    </span>
                    <span :style="{ color: HBlue }">
                        {{ node.name.slice(index, index + baseQuery.keyword?.length!) }}
                    </span>
                    <span>
                        {{ node.name.slice(index + baseQuery.keyword?.length!) }}
                    </span>
                </span>
            </div>
        </tree-contextmenu>
    </template>
</template>

<script setup lang="ts">
import { Input } from '@arco-design/web-vue';
import { type ITreeData } from '@/types/file';
import { HBlue } from '@/constants';
import { useEditFileName } from '@/composable/useEditFileName';
import { findFatherNodeById, removeNodeByIds } from '@/composable/useFolderLevelTree';
import { useBaseFileStore, useFileNodeStore } from '@/stores/modules/file'
import type { Callback } from '@/stores/modules/file';
import TreeContextmenu from './tree-contextmenu.vue'


const { node } = defineProps<{
    node: ITreeData
}>()

const { baseQuery, editingId, isContextmenuShow, editNameOp } = storeToRefs(useBaseFileStore())
const { treeData, contextmenuItem, selectedIds, } = storeToRefs(useFileNodeStore())
const { updateTreeData } = useFileNodeStore()



const pid = findFatherNodeById(treeData.value, null, node.id)?.id ?? ''
const { name, handleFinishEidtInTree, resetName } = useEditFileName({ fileOrNode: node });

/**
 * 搜索匹配索引匹配
 * @param name 
 */
function getMatchIndex(name: string) {
    if (!baseQuery.value.keyword) return -1;
    return name.toLowerCase().indexOf(baseQuery.value.keyword.toLowerCase());
}
let index: number

// 右键菜单
const handleContextmenu = () => {
    contextmenuItem.value = node
}
// 是否视为悬浮
const shouldHover = (node: ITreeData) => {
    return isContextmenuShow.value && selectedIds.value.includes(node.id) && selectedIds.value.includes(contextmenuItem.value?.id ?? '')
}
// 输入框ref
const inputRef = useTemplateRef<InstanceType<typeof Input>>('rename_input');
watchEffect(() => {
    inputRef.value?.focus()
    inputRef.value?.$el.querySelector('input').select()
})

// 编辑名完成
const handleFinishEdit = () => {
    const id = editNameOp.value === 'create' ? pid : node.id
    const cb: Callback = editNameOp.value === 'create' ? {
        ok: updateTreeData, fail: () => removeNodeByIds(treeData.value, [node.id]), err: () => removeNodeByIds(treeData.value, [node.id])
    } : {
        ok: () => node.name = name.value, fail: resetName, err: resetName
    }
    handleFinishEidtInTree(id, cb)
}
</script>

<style scoped></style>
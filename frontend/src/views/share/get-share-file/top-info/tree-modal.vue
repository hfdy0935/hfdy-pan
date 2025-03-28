<template>
    <a-modal v-model:visible="modalShow" title="选择转存路径" :footer="false">
        <a-tree :data="treeDataBanFileWithRoot" :fieldNames v-model:selected-keys="selectedTreeIds"
            v-model:checked-keys="selectedTreeIds" @select="handleSelect" check-strictly :multiple="false">
        </a-tree>
        <div class="flex justify-end mt-6">
            <a-button type="primary" @click="doSave">保存</a-button>
        </div>
    </a-modal>
</template>

<script setup lang="ts">
import { reqSaveToMyPan } from '@/api/file';
import { Message, type TreeNodeData } from '@arco-design/web-vue';
import { FAKE_ROOT_ID, fieldNames, useFolderLevelTree } from '@/composable/useFolderLevelTree';
import { useGetShareStore } from '@/stores/modules/share';
import type { ITreeData } from '@/types/file';


const modalShow = defineModel<boolean>()
const { shareId, selectedFiles, selectedIds, contextItem, shouldHandleGroup, modalPos } = storeToRefs(useGetShareStore());
const { selectedIds: selectedTreeIds, treeDataBanFileWithRoot, updateTreeLayerData } = useFolderLevelTree();
watchEffect(updateTreeLayerData)
// 是否通过不重名验证
let isPassValidate = false;
// 选择节点时，校验是否重名
const handleSelect = (_: any, { node }: { node?: TreeNodeData }) => {
    const targetNames = selectedFiles.value.map(f => f.name);
    const nodeChildNames = (node as ITreeData).children?.map(c => c.name);
    if (new Set([...targetNames, ...nodeChildNames]).size < targetNames.length + nodeChildNames.length) {
        Message.warning('同一目录下的文件/文件夹名不能重复');
        isPassValidate = false;
    } else isPassValidate = true;
};
/**
 * 保存，必须有
 */
const doSave = async () => {
    if (selectedTreeIds.value.length === 0) {
        Message.warning('请先选择要保存到哪个文件夹');
        return;
    }
    if (!isPassValidate) {
        Message.warning('同一目录下的文件/文件夹名不能重复');
        return;
    }
    const srcIds = modalPos.value === 'bottom' ? shouldHandleGroup.value ? selectedIds.value : [contextItem.value.id] : selectedIds.value
    try {
        if (!srcIds.length) {
            Message.warning('请选择要保存的文件/文件夹');
            return;
        }
        // 复原根节点
        const to = selectedTreeIds.value[0] === FAKE_ROOT_ID ? '' : selectedTreeIds.value[0]
        const resp = await reqSaveToMyPan({
            shareId: shareId.value,
            srcIds,
            to,
        });
        if (resp.code === 200) Message.success('转存成功');
        else Message.error(resp.message);
    } catch {
        Message.error('转存失败');
    } finally {
        modalShow.value = false;
    }
};
</script>

<style scoped></style>

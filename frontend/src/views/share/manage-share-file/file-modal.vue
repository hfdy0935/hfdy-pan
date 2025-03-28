<template>
    <a-modal v-model:visible="show" @ok="doUpdateFiles" :width="480">
        <template #title>
            修改分享文件&nbsp;
            <a-tooltip content="文件夹和文件可分别添加">
                <icon-info-circle />
            </a-tooltip>
        </template>
        <a-tree :data="treeDataAllowFile" :fieldNames v-model:checked-keys="selectedIds"
            v-model:selected-keys="selectedIds" checkable multiple check-strictly>
        </a-tree>
    </a-modal>
</template>

<script setup lang="ts">
import { reqUpdateMyShareFiles } from '@/api/share';
import { fieldNames } from '@/composable/useFolderLevelTree';
import { useUpdateShareStore } from '@/stores/modules/share';

import { Message } from '@arco-design/web-vue';

const { shareId, selectedIds, treeDataAllowFile } = storeToRefs(useUpdateShareStore())
const { updateData } = useUpdateShareStore()

const show = ref(false)

const doUpdateFiles = async () => {
    try {
        const resp = await reqUpdateMyShareFiles(selectedIds.value, shareId.value);
        if (resp.code === 200) {
            Message.success('修改成功');
            await updateData()
        } else Message.error(resp.message);
    } catch {
        Message.error('修改失败');
    }
};


defineExpose({
    open() {
        show.value = true
    }
})
</script>

<style scoped></style>
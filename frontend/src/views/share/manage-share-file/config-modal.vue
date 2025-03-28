<template>
    <a-modal v-model:visible="show" title="修改分享设置" @ok="doUpdateOptions" :width="480">
        <a-form :model="config" auto-label-width>
            <a-form-item label="永久有效">
                <a-switch v-model:model-value="config.noDdl"></a-switch>
            </a-form-item>
            <a-form-item :disabled="config.noDdl">
                <template #label>
                    有效期
                    <a-tooltip content="从当前时间算起">
                        <icon-info-circle />
                    </a-tooltip>
                </template>
                <a-input-number :min="1" :max="100" v-model="config.value"></a-input-number>
            </a-form-item>
            <a-form-item label="单位" :disabled="config.noDdl">
                <a-select v-model:model-value="config.expire" :options="showedExpireOptions"></a-select>
            </a-form-item>
            <a-form-item label="提取码">
                <a-input-password v-model.trim="config.pwd" placeholder="不设置表示公开" allowClear
                    :max-length="12"></a-input-password>
            </a-form-item>
        </a-form>
    </a-modal>
</template>

<script setup lang="ts">
import { reqUpdateMyShareOptions } from '@/api/share';
import { showedExpireOptions } from '@/composable/useShareFile';
import { useUpdateShareStore } from '@/stores/modules/share';
import { Message } from '@arco-design/web-vue';


const { shareId, config, formattedConfig } = storeToRefs(useUpdateShareStore())
const { updateData } = useUpdateShareStore()


const show = ref(false)

// 修改分享
const doUpdateOptions = async () => {
    try {
        const resp = await reqUpdateMyShareOptions({
            id: shareId.value,
            ...formattedConfig.value
        });
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
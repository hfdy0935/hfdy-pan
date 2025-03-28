<template>
    <a-modal v-model:visible="showModal" title="请输入提取码" :footer="false" :width="480">
        <a-input v-model:modelValue="pwd" allowClear></a-input>
        <div class="float-right mt-3">
            <a-button type="primary" @click="doGetShareFile">提取</a-button>
        </div>
    </a-modal>
</template>

<script setup lang="ts">
import { useGetShareStore } from '@/stores/modules/share';
import { Message } from '@arco-design/web-vue';


const showModal = defineModel<boolean>()
const { pwd } = storeToRefs(useGetShareStore())
const { updateFolder } = useGetShareStore()
const doGetShareFile = () =>
    updateFolder({
        pwdErrorCB() {
            if (pwd.value) Message.error('提取码错误');
            else Message.warning('请输入提取码');
        },
        okCb() {
            Message.success('提取成功');
            showModal.value = false;
        },
    });
const route = useRoute();
const router = useRouter();
// 初始化获取一次
onMounted(() => {
    pwd.value = route.query.pwd as string;
    doGetShareFile();
});
watch(pwd, newValue => {
    router.replace({
        query: { pwd: newValue },
    });
});
</script>

<style scoped></style>
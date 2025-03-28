<template>
    <div class="flex flex-col justify-center items-center">
        <a-empty>
            <template #image>
                <icon-exclamation-circle-fill />
            </template>
            暂不支持查看该类型的文件
        </a-empty>
        <div class="mt-3" v-if="mayBeOpenAsText">
            <a-button type="primary" @click="openAsPlainText">尝试以文本形式打开？</a-button>
        </div>
    </div>
</template>

<script setup lang="ts">
import { useViewFileStore } from '@/stores/modules/file';
import type { IItemMediaType } from '@/types/file';


const { file } = storeToRefs(useViewFileStore())
const { getData } = useViewFileStore()
// 不能用文本打开的
const shouldNotOpenAsText: IItemMediaType[] = [
    'zip'
]
const mayBeOpenAsText = computed(() => !shouldNotOpenAsText.includes(file.value?.mediaType ?? 'unknown'))
const openAsPlainText = async () => {
    // 改成txt后发送请求获取
    file.value!.mediaType = 'text'
    await getData()
}
</script>

<style scoped></style>
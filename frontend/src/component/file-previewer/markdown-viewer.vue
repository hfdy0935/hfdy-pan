<template>
    <MdPreview :modelValue="content" style="width: 100%;height: 100%;" />
</template>

<script setup lang="ts">
import { useViewFileStore } from '@/stores/modules/file';
import { MdPreview } from 'md-editor-v3';
import 'md-editor-v3/lib/preview.css';

const { data } = storeToRefs(useViewFileStore())
const content = ref('')
watchEffect(async () => {
    if (!data.value) return
    const text = await data.value.text()
    text && (content.value = text)
})
</script>

<style scoped></style>
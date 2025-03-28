<template>
    <VueOfficePpt :src />
</template>

<script setup lang="ts">
// @ts-ignore
import VueOfficePpt from '@vue-office/pptx/lib/v3/vue-office-pptx.mjs'
import { useViewFileStore } from '@/stores/modules/file';

const { data } = storeToRefs(useViewFileStore())
const src = ref('')
watchEffect(async () => {
    if (!data.value) return
    const text = URL.createObjectURL(data.value)
    text && (src.value = text)
})

</script>

<style scoped>
:deep(.pptx-preview-wrapper) {
    height: 100% !important;
    width: 100% !important;
    overflow: auto;
}
</style>
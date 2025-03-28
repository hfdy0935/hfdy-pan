<template>
    <VueOfficeExcel :src style="width: 100%;" />
</template>
<script setup lang="ts">
// @ts-ignore
import VueOfficeExcel from '@vue-office/excel/lib/v3/vue-office-excel.mjs'
import '@vue-office/excel/lib/v3/index.css'
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
    height: 88vh !important;
}
</style>
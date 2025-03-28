<template>
    <VueOfficePdf :src="url" style="height: 100%;width: 100%;" />
</template>
<script setup lang="ts">
// @ts-ignore
import VueOfficePdf from '@vue-office/pdf/lib/v3/vue-office-pdf.mjs'
import { useViewFileStore } from '@/stores/modules/file';

const { data } = storeToRefs(useViewFileStore())
const url = ref('')
watchEffect(async () => {
    if (!data.value) return
    const text = URL.createObjectURL(data.value)
    text && (url.value = text)
})

</script>

<style scoped>
:deep(.pptx-preview-wrapper) {
    height: 88vh !important;
}
</style>
<template>
    <VueOfficeDocx :src="url" />
</template>

<script setup lang="ts">
// @ts-ignore
import VueOfficeDocx from '@vue-office/docx/lib/v3/vue-office-docx.mjs'
import '@vue-office/docx/lib/v3/index.css'
import { useViewFileStore } from '@/stores/modules/viewFile';

const { data } = storeToRefs(useViewFileStore())
const url = ref('')
watchEffect(async () => {
    if (!data.value) return
    const text = URL.createObjectURL(data.value)
    text && (url.value = text)
})

</script>

<style scoped></style>
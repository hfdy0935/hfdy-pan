<template>
    <div class="flex justify-center">
        <img :src></img>
    </div>
</template>

<script setup lang="ts">
import { useViewFileStore } from '@/stores/modules/viewFile';



const { data } = storeToRefs(useViewFileStore())

const src = computed(() => {
    if (!data.value) return ''
    return URL.createObjectURL(data.value)
})
</script>

<style scoped></style>
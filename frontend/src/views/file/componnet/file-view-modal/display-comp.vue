<template>
    <a-spin :loading="spinning" tip="加载中..."
        style="height: 100%;width: 100%;position: absolute;display: flex;justify-content: center;">
        <component v-if="file" :is="comp"></component>
    </a-spin>
</template>

<script setup lang="ts">
import { useViewFileStore } from '@/stores/modules/file'
import type { Component } from 'vue';

const { spinning, file } = storeToRefs(useViewFileStore())

defineProps<{
    comp?: Component
}>()
</script>

<style scoped></style>
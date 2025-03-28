<template>
    <img :src="logo" alt="hfdy云盘" class="w-16 mr-2" />
    <h1 class="sm:block text-2xl" :class="`${hideTitle ? 'hidden' : ''}`">hfdy云盘</h1>
</template>

<script setup lang="ts">
import logo from '@/assets/logo.svg';

const { hideTitle = true } = defineProps<{
    hideTitle?: boolean;
}>()
</script>

<style scoped></style>
<template>
    <a-breadcrumb separator=">" class="select-none flex flex-wrap">
        <a-breadcrumb-item @click="clickBreadcrumb(breadcrumbs[0].id)">
            <icon-home :size="18" class="hover:text-h-blue cursor-pointer" />
        </a-breadcrumb-item>
        <a-breadcrumb-item v-for="b in breadcrumbs.slice(1)" :key="b.id" @click="clickBreadcrumb(b.id)">
            <div class="hover:text-h-blue cursor-pointer">
                {{ (b as IFileItem).name }}
            </div>
        </a-breadcrumb-item>
    </a-breadcrumb>
</template>

<script setup lang="ts">

import { useGetShareStore } from '@/stores/modules/share';
import type { IFileItem } from '@/types/file';

const { breadcrumbs, sharedFiles } = storeToRefs(useGetShareStore())
const { updateFolder } = useGetShareStore()

const clickBreadcrumb = async (pid?: string) => {
    // 不在该界面时才能去该界面
    if (sharedFiles.value.parent.id === pid) return;
    await updateFolder({ pid });
};
</script>

<style scoped></style>
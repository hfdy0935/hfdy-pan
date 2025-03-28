<template>
    <div class="flex justify-around items-center px-2">
        <a-list :bordered="false">
            <a-list-item class="list-demo-item" action-layout="vertical">
                <a-list-item-meta>
                    <template #title>
                        <span class="text-lg"> 分享者：{{ sharedFiles.username }} </span>
                    </template>
                    <template #description>
                        <div class="my-2">创建时间： {{ sharedFiles.createTime }}</div>
                        <div class="my-2">围观次数： {{ sharedFiles.visitNum }}</div>
                    </template>
                    <template #avatar>
                        <a-avatar :image-url="sharedFiles.avatar" class="scale-[2.2] mr-6"></a-avatar>
                    </template>
                </a-list-item-meta>
            </a-list-item>
        </a-list>
        <div class="flex flex-col">
            <a-button type="primary" class="my-3" @click="modalPos = 'top'; treeModalShow = true">转存
                <template #icon>
                    <icon-save />
                </template>
            </a-button>
            <a-button type="primary" status="success" @click="fileExtractModalShow = true">提取
                <template #icon>
                    <icon-link />
                </template>
            </a-button>
        </div>
    </div>
    <tree-modal v-model="treeModalShow" />
    <file-extract-modal v-model="fileExtractModalShow" />
</template>

<script setup lang="ts">
import { useGetShareStore } from '@/stores/modules/share';
import FileExtractModal from './file-extract-modal.vue';
import TreeModal from './tree-modal.vue';

const { sharedFiles, treeModalShow, modalPos } = storeToRefs(useGetShareStore())
const fileExtractModalShow = ref(false)
</script>

<style scoped></style>

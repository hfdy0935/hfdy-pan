<template>
    <a-modal v-model:visible="isModalShow" :footer="false" :closable="false" :fullscreen="currentItem?.needFullscreen"
        :body-style="{ padding: 0, height: '100%' }" :width="460" unmount-on-close>
        <display-comp :comp="currentItem?.component" />
        <template #title>
            <a-tooltip content="返回">
                <icon-backward class="hover:text-h-blue scale-150 absolute top-4 left-6" @click="isModalShow = false" />
            </a-tooltip>
            <component v-if="file" :is="getIconByFilename(file?.name)" class="scale-[2] m-5">
            </component>
            {{ file?.name }}&nbsp;
            <a-trigger>
                <icon-question-circle class="hover:text-h-blue cursor-pointer" />
                <template #content>
                    <a-list :style="{ boxShadow: '0 0 5px #165DFF', ...style }">
                        <template #header>
                            提示
                        </template>
                        <a-list-item v-for="item of description" :key="item">
                            {{ item }}
                        </a-list-item>
                    </a-list>
                </template>
            </a-trigger>
        </template>
    </a-modal>
</template>

<script setup lang="ts">
import { getIconByFilename } from '@/constants/fileIcon';
import { useAppStore } from '@/stores/modules/app';
import { useViewFileStore } from '@/stores/modules/viewFile';
import DisplayComp from './display-comp.vue';

const { isModalShow, file, currentItem } = storeToRefs(useViewFileStore())
const { textColor, bgColor } = storeToRefs(useAppStore())
const style = computed(() => ({
    color: textColor.value,
    backgroundColor: bgColor.value
}))
// 说明
const description = [
    '1. office下的部分内容渲染可能有偏差，可下载后在本地查看',
    '2. @vue-office/excel不支持解析csv，所以csv放在了code中，用monaco预览',
    '3. 视频等待转码完毕食用更佳'
]


</script>

<style scoped></style>
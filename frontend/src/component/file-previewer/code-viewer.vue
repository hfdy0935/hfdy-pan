<template>
    <div class="h-full w-full">
        <a-space class="h-[6%] ml-6 pt-1" wrap>
            <a-form-item label="主题">
                <a-select v-model:model-value="theme" style="width: 120px;">
                    <a-option>vs</a-option>
                    <a-option>vs-dark</a-option>
                    <a-option>hc-black</a-option>
                </a-select>
            </a-form-item>
            &emsp;
            <a-form-item label="语言">
                <a-select v-model:model-value="language" style="width: 120px;">
                    <a-option v-for="item in languageOptions" :key="item" :value="item">{{ item }}</a-option>
                </a-select>
            </a-form-item>
            &emsp;
            <a-form-item v-if="isMd" label="显示">
                <a-radio-group type="button" v-model:model-value="mdShowType">
                    <a-radio value="monaco">monaco</a-radio>
                    <a-radio value="md-viewer">md-viewer</a-radio>
                </a-radio-group>
            </a-form-item>
        </a-space>
        <MdEditor v-show="isMd && mdShowType === 'md-viewer'" :model-value="code" read-only
            :toolbars="['catalog', 'previewOnly', 'preview', 'fullscreen']" :theme="mdPreviewTheme"
            style="height: 94%;" />
        <VueMonacoEditor v-show="!(isMd && mdShowType === 'md-viewer')" v-model:value="code" :options :language
            class="max-h-[94%]" />
    </div>
</template>

<script setup lang="ts">
import { useAppStore } from '@/stores/modules/app';
import { useViewFileStore } from '@/stores/modules/viewFile';
import { VueMonacoEditor } from '@guolao/vue-monaco-editor';
import { MdEditor } from 'md-editor-v3';
import 'md-editor-v3/lib/style.css';

const { file, data } = storeToRefs(useViewFileStore())
const { isDark } = storeToRefs(useAppStore())

const code = ref('')
const theme = ref<'vs' | 'vs-dark' | 'hc-black'>('vs')
const language = ref<string>('python')
watch(isDark, value => {
    if (value && theme.value === 'vs') theme.value = 'vs-dark'
    else if (!value) theme.value = 'vs'
})
watchEffect(async () => {
    if (!data.value) return
    const text = await data.value.text()
    if (text) {
        code.value = text
        language.value = guessLanguageByFilename(file.value?.name ?? '')
    }
})
const options = computed(() => ({
    colorDecorators: true,
    readOnly: true,
    theme: theme.value
}))

const languageOptions = ['c', 'cpp', 'csharp', 'go', 'html', 'java', 'javascript', 'json', 'jsx', 'kotlin', 'markdown', 'ruby', 'rust', 'python', 'swift', 'tsx', 'txt', 'typescript', 'xml', 'yaml']
// 根据文件名获取语言类型
function guessLanguageByFilename(filename: string): string {
    const extensionMap: { [key: string]: string } = {
        c: 'c',
        cpp: 'cpp',
        cs: 'csharp',
        go: 'go',
        html: 'html',
        java: 'java',
        js: 'javascript',
        json: 'json',
        kt: 'kotlin',
        md: 'markdown',
        rb: 'ruby',
        rs: 'rust',
        py: 'python',
        swift: 'swift',
        ts: 'typescript',
        yaml: 'yaml',
        yml: 'yaml',
        xml: 'xml',
        vue: 'typescript',
        jsx: 'javascript',
        tsx: 'typescript'
    };
    const extension = filename.split('.').pop()?.toLowerCase();
    return extension && extensionMap[extension] ? extensionMap[extension] : 'txt';
}

// markdown显示时
const isMd = computed(() => file.value?.mediaType === 'md')
const mdShowType = ref<'monaco' | 'md-viewer'>('monaco')
const mdPreviewTheme = computed<'light' | 'dark'>(() => theme.value === 'vs' ? 'light' : 'dark')
</script>

<style scoped></style>
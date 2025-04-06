import {
    type IFileItem,
    TransCodeStatusEnum,
    type IItemMediaType,
} from '@/types/file';
import { reqPreviewFile } from '@/api/file';
import { Message } from '@arco-design/web-vue';
import { isVideo } from '@/utils/file';
import type { Component } from 'vue';

export const useViewFileStore = defineStore('viewFileStore', () => {
    const isModalShow = ref(false);
    // 当前预览的文件
    const file = ref<IFileItem | null>(null);
    const data = ref<Blob | null>(null);
    // 要显示的文件类型
    const mediaType = ref<IItemMediaType>('text');
    const spinning = ref(false);
    const route = useRoute();
    let control = new AbortController();
    const refreshControl = () => {
        control.abort()
        control = new AbortController()
    }
    watch(isModalShow, (newValue, oldValue) => {
        if (oldValue && !newValue) {
            refreshControl()
        }
    });

    const getData = async () => {
        if (!file.value) return;
        // 视频未转码警告
        if (isVideo(file.value) && file.value.status !== TransCodeStatusEnum.OK) {
            Message.warning('视频未转码，传输速度和质量会受影响，转码后食用更佳');
        }
        // 如果是unknown、zip类型的数据，先不请求，等确认以text打开了再请求
        if (['unknown', 'zip'].includes(file.value.mediaType)) return;
        spinning.value = true;
        try {
            data.value = null;
            // 考虑到打开分享页面，也需要验证文件是不是该分享下的，携带路径参数shareId
            const resp = await reqPreviewFile(file.value.id, control, route.params.shareId as string);
            data.value = resp;
        } catch (e: any) {
            if (e.message !== 'canceled') Message.error('获取文件信息失败');
        } finally {
            spinning.value = false;
        }
    };

    const openPreviewModal = async (item: IFileItem) => {
        file.value = item;
        // 先开对话框，显示加载中
        isModalShow.value = true;
        await getData();
    };
    const modalType: Partial<Record<IItemMediaType, {
        component: Component,
        needFullscreen: boolean
    }>> = {
        video: {
            component: defineAsyncComponent(() => import('@/component/file-previewer/video-viewer.vue')),
            needFullscreen: false
        },
        audio: {
            component: defineAsyncComponent(() => import('@/component/file-previewer/audio-viewer.vue')),
            needFullscreen: false
        },
        image: {
            component: defineAsyncComponent(() => import('@/component/file-previewer/image-viewer.vue')),
            needFullscreen: false
        },
        zip: {
            component: defineAsyncComponent(() => import('@/component/file-previewer/unknown-viewer.vue')),
            needFullscreen: true
        },
        text: {
            component: defineAsyncComponent(() => import('@/component/file-previewer/code-viewer.vue')),
            needFullscreen: true
        },
        ppt: {
            component: defineAsyncComponent(() => import('@/component/file-previewer/ppt-viewer.vue')),
            needFullscreen: true
        },
        pptx: {
            component: defineAsyncComponent(() => import('@/component/file-previewer/ppt-viewer.vue')),
            needFullscreen: true
        },
        pdf: {
            component: defineAsyncComponent(() => import('@/component/file-previewer/pdf-viewer.vue')),
            needFullscreen: true
        },
        xlsx: {
            component: defineAsyncComponent(() => import('@/component/file-previewer/excel-viewer.vue')),
            needFullscreen: true
        },
        csv: {
            component: defineAsyncComponent(() => import('@/component/file-previewer/code-viewer.vue')),
            needFullscreen: true
        },
        doc: {
            component: defineAsyncComponent(() => import('@/component/file-previewer/word-viewer.vue')),
            needFullscreen: true
        },
        docx: {
            component: defineAsyncComponent(() => import('@/component/file-previewer/word-viewer.vue')),
            needFullscreen: true
        },
        code: {
            component: defineAsyncComponent(() => import('@/component/file-previewer/code-viewer.vue')),
            needFullscreen: true
        },
        md: {
            component: defineAsyncComponent(() => import('@/component/file-previewer/code-viewer.vue')),
            needFullscreen: true
        },
        unknown: {
            component: defineAsyncComponent(() => import('@/component/file-previewer/unknown-viewer.vue')),
            needFullscreen: true
        }
    }
    const currentItem = computed(() => modalType[file.value?.mediaType ?? 'unknown'])

    return { isModalShow, file, spinning, openPreviewModal, data, getData, mediaType, refreshControl, currentItem };
});


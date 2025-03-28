// 文件的icon
import { useAppStore } from '@/stores/modules/app';
import type { IItemCategory, IItemMediaType } from '@/types/file';
import type { VNode } from 'vue';

/**
 * 单个文件的icon
 */
export const adaptedFileIcons: (isDark: boolean) => Record<IItemMediaType, VNode> = isDark => {
    return {
        // 音视频图片
        video: <img src='/src/assets/icon/wenjianleixing-biaozhuntu-shipinwenjian.png' width='12' />,
        audio: <img src='/src/assets/icon/yinlewenjian.png' width='16' />,
        image: <img src='/src/assets/icon/tupianwenjian.png' width='16' />,
        // 文档
        text: <img src='/src/assets/icon/text.png' width='16' />,
        ppt: <img src='/src/assets/icon/pptx.png' width='16' />,
        pptx: <img src='/src/assets/icon/pptx.png' width='16' />,
        pdf: <img src='/src/assets/icon/pdfwenjian.png' width='16' />,
        xlsx: <img src='/src/assets/icon/xlsx.png' width='16' />,
        csv: <img src='/src/assets/icon/csv.png' width='16' />,
        doc: <img src='/src/assets/icon/docx.png' width='16' />,
        docx: <img src='/src/assets/icon/docx.png' width='16' />,
        code: <img src='/src/assets/icon/a-appround6.png' width='16' />,
        md: <img src={`/src/assets/icon/${isDark ? 'markdown-dark' : 'markdown'}.png`} width='16' />,
        // 压缩包
        zip: <img src='/src/assets/icon/yasuobao.png' width='16' />,
        // 文件夹
        folder: <img src='/src/assets/icon/wenjianjia.png' width='16' />,
        // 其他文件
        unknown: <img src={`/src/assets/icon/${isDark ? 'file' : 'file-dark'}.png`} width='16' />,
    };
};
/**
 * 根据文件名获取媒体类型，文件夹需要额外判断
 * @param filename
 * @returns
 */
export function getMediaTypeByFilename(filename: string): IItemMediaType {
    // 获取文件扩展名（小写）
    const extension = filename.substring(filename.lastIndexOf('.')).toLowerCase();

    // 根据扩展名映射到媒体类型
    let mediaType: IItemMediaType = 'unknown'; // 默认为未知类型

    switch (extension) {
        case '.mp4':
        case '.avi':
        case '.mov':
        case '.mkv':
        case '.flv':
            mediaType = 'video';
            break;
        case '.mp3':
        case '.wav':
            mediaType = 'audio';
            break;
        case '.jpg':
        case '.jpeg':
        case '.png':
        case '.gif':
        case '.bmp':
        case '.ico':
        case '.webp':
        case '.svg':
            mediaType = 'image';
            break;
        case '.txt':
            mediaType = 'text';
            break;
        case '.md':
            mediaType = 'md';
            break;
        case '.doc':
            mediaType = 'doc';
            break;
        case '.docx':
            mediaType = 'docx';
            break;
        case '.ppt':
            mediaType = 'ppt';
            break;
        case '.pptx':
            mediaType = 'pptx';
            break;
        case '.pdf':
            mediaType = 'pdf';
            break;
        case '.xlsx':
            mediaType = 'xlsx';
            break;
        case '.csv':
            mediaType = 'csv';
            break;
        case '.py':
        case '.js':
        case '.ts':
        case '.vue':
        case '.jsx':
        case '.tsx':
        case '.java':
        case '.cpp':
        case '.c':
        case '.cs':
        case '.php':
        case '.rb':
        case '.go':
        case '.rs':
        case '.swift':
        case '.kt':
        case '.sh':
        case '.json':
        case '.toml':
        case '.yml':
        case '.yaml':
        case 'tmol':
        case '.sql':
        case '.json':
        case '.yml':
        case '.yaml':
        case '.html':
        case '.xml':
            mediaType = 'code';
            break;
        case '.zip':
        case '.rar':
        case '.tar':
        case '.gz':
            mediaType = 'zip';
            break;
        case '.folder':
            mediaType = 'folder';
            break;
        default:
            mediaType = 'unknown';
    }
    return mediaType;
}

/**
 * 根据文件名获取图标
 * @param filename
 * @returns
 */
export function getIconByFilename(filename: string): VNode {
    const store = useAppStore();
    return store.FileIcons[getMediaTypeByFilename(filename)];
}

export function getCategoryByFilename(filename: string): IItemCategory {
    const extension = filename.substring(filename.lastIndexOf('.')).toLowerCase();
    const mediaType = getMediaTypeByFilename(extension);
    switch (mediaType) {
        case 'video':
        case 'audio':
        case 'image':
            return mediaType;
        case 'text':
        case 'ppt':
        case 'pptx':
        case 'pdf':
        case 'xlsx':
        case 'csv':
        case 'doc':
        case 'docx':
        case 'code':
        case 'md':
            return 'docs';
        default:
            return 'others';
    }
}

export const adaptedThemeIcons = (isDark: boolean) => {
    const iconBasePath = '/src/assets/icon/';
    const iconSuffix = isDark ? '-dark' : '';

    return {
        open: <img src={`${iconBasePath}yulan${iconSuffix}.png`} width='13' class='inline' />,
        detail: <img src={`${iconBasePath}xiangqing${iconSuffix}.png`} width='13' class='inline' />,
        cut: <img src={`${iconBasePath}jianqiewenjian${iconSuffix}.png`} width='13' class='inline' />,
        newFolder: <img src={`${iconBasePath}xinjianwenjianjia${iconSuffix}.png`} width='13' class='inline' />,
        deleteCompletely: <img src={`${iconBasePath}shanchu.png`} width='13' class='inline' />,
        contextmenuLayout: <img src={`${iconBasePath}layout-5-line${iconSuffix}.png`} width='13' class='inline' />,
        listLayout: <img src={`${iconBasePath}liebiao${iconSuffix}.png`} width='13' class='inline' />,
        blockLayout: <img src={`${iconBasePath}yingyongzhongxin${iconSuffix}.png`} width='13' class='inline' />,
        treeLayout: <img src={`${iconBasePath}shuzhuangtu${iconSuffix}.png`} width='13' class='inline' />,
    };
};

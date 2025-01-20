// 文件的icon
import type { IFileItem } from '@/types/file';
import type { VNode } from 'vue';
import { Icon } from '@arco-design/web-vue';

// iconfont的项目
const IconFont = Icon.addFromIconFontCn({ src: 'https://at.alicdn.com/t/c/font_4812686_h4fmytve1fb.js' });
/**
 * 单个文件的icon
 */
export const FileIcon: Record<IFileItem['mediaType'], VNode> = {
  // 音视频图片
  video: <IconFont type="icon-wenjianleixing-biaozhuntu-shipinwenjian" />,
  audio: <IconFont type="icon-yinlewenjian" />,
  image: <IconFont type="icon-tupianwenjian" />,
  // 文档
  text: <IconFont type="icon-text" />,
  ppt: <IconFont type="icon-pptx" />,
  pptx: <IconFont type="icon-pptx" />,
  pdf: <IconFont type="icon-pdfwenjian" />,
  xlsx: <IconFont type="icon-xlsx" />,
  csv: <IconFont type="icon-csv" />,
  doc: <IconFont type="icon-docx" />,
  docx: <IconFont type="icon-docx" />,
  code: <IconFont type="icon-a-appround6" />,
  md: <IconFont type="icon-markdown" />,
  // 压缩包
  zip: <IconFont type="icon-yasuobao" />,
  // 文件夹
  folder: <IconFont type="icon-wenjianjia" />,
  // 其他文件
  unknown: <IconFont type="icon-file" />
};

/**
 * 其他icon
 */
export const Icons = {
  open: <IconFont type="icon-yulan" />,
  detail: <IconFont type="icon-xiangqing" />
};

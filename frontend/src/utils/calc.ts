/**
 * 根据文件大小(B)计算合适的字符串表示
 * @param size_
 */
export function calcFileSize(size_: number | string): string {
  let size = +size_;
  if (size === 0) return '0 B';

  const units = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
  let unitIndex = 0;

  while (size >= 1024 && unitIndex < units.length - 1) {
    unitIndex++;
    size /= 1024;
  }

  // If the size is less than 1 but we're not at bytes, adjust back.
  if (size < 1 && unitIndex > 0) {
    size *= 1024;
    unitIndex--;
  }

  // Round to one decimal place for KB and above, keep exact value for bytes.
  const roundedSize = unitIndex === 0 ? size : parseFloat(size.toFixed(1));

  return `${roundedSize} ${units[unitIndex]}`;
}


/**
 * 获取不重复的文件夹名
 * @param nameList
 */
export function calcUniqueFilename(nameList: string[]): string {
  console.log(nameList);
  const prefix = '新建文件夹';
  let start = 1;
  while (nameList.includes(prefix + start)) {
    start++;
  }
  return prefix + start;
}

/**
 * 计算不重复的id，用于表格的key，真正的id需要后端生成
 * @param idList
 */
export function calcUniqueFileId(idList: string[]) {
  let start = 1;
  while (idList.includes(start + '')) start++;
  return start + '';
}


/**
 * 计算当前时间字符串
 */
export function calcCurrentDatetime(): string {
  const now = new Date();

  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0'); // Months are zero based
  const day = String(now.getDate()).padStart(2, '0');

  const hours = String(now.getHours()).padStart(2, '0');
  const minutes = String(now.getMinutes()).padStart(2, '0');
  const seconds = String(now.getSeconds()).padStart(2, '0');

  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
}


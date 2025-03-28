import { Message } from "@arco-design/web-vue";

export function copyToClipboard(s: string) {
    navigator.clipboard.writeText(s).then(() => {
        Message.success('复制成功')
    }).catch(err => {
        Message.error('复制失败')
    });
}


export function debounce(fn: () => void, delay: number = 2000) {
    let timer: number = 0
    return function () {
        clearTimeout(timer)
        timer = setTimeout(() => fn(), delay)
    }
}
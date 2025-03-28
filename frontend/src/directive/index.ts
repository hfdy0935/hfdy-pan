import type { Directive } from "vue";


let observer: IntersectionObserver
const startObserve = (el: HTMLDivElement, fn: () => Promise<string>) => {
    observer = new IntersectionObserver(async entries => {
        for (const item of entries) {
            // 进入视口并且src没有值
            const target = item.target as HTMLImageElement
            if (item.isIntersecting && !target.src) {
                target.src = await fn()
            }
        }
    })
    observer.observe(el)
}

export const lazyImage: Directive = {
    async mounted(el: HTMLImageElement, { value }: { value: () => Promise<string> }) {
        startObserve(el, value)
    },
    unmounted(el) {
        observer?.unobserve(el)
    }
}
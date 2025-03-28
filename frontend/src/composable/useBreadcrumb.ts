/**
 * 这两个必须有
 */
interface BaseBreadcrumbItem {
    id: string
    name: string
}

export function useBreadcrumb<T extends BaseBreadcrumbItem>() {
    const breadcrumbs = ref<T[]>([])

    /**
     * 切换导航
     * @param item T
     */
    const switchTo = (item: T) => {
        const index = breadcrumbs.value.findIndex(b => b.id === item.id);
        if (index === -1) (breadcrumbs.value as T[]).push(item);
        else breadcrumbs.value.splice(index + 1)
    }

    /**
     * 保留第一个，替换其他的
     * @param items 
     */
    const replaceExcludeFirst = (...items: T[]) => {
        breadcrumbs.value = [breadcrumbs.value[0] as T, ...items]
    }

    return {
        breadcrumbs, switchTo, replaceExcludeFirst
    }
}
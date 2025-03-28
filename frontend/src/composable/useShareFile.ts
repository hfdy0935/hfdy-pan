import type { IMyShareConfig } from "@/types/share"

/**
 * 过期时间的单位
 */
export type IExpireUnit = 'minute' | 'hour' | 'day' | 'week' | 'month' | 'year'


export const expireOptions = {
    minute: { title: '分钟', rate: 60 },
    hour: { title: '小时', rate: 60 * 60 },
    day: { title: '天', rate: 24 * 60 * 60 },
    week: { title: '周', rate: 7 * 24 * 60 * 60 },
    month: { title: '月', rate: 30 * 24 * 60 * 60 },
    year: { title: '年', rate: 365 * 24 * 60 * 60 }
}
export const showedExpireOptions = Object.entries(expireOptions).map(([k, v]) => ({
    value: k,
    label: v.title
}))
/**
 * 分享文件的配置
 */
export interface IShareConfig {
    // 过期时间配置
    expire: IExpireUnit
    // 过期时间数值
    value: number
    // 是否永不过期
    noDdl: boolean
    // 空表示不需要密码
    pwd: string
}
export function useShareConfig() {
    const config = ref<IShareConfig>({
        expire: 'day',
        value: 7,
        noDdl: false,
        pwd: ''
    })
    /**
     * 格式化的配置，可直接用于发送请求
     */
    const formattedConfig = computed<IMyShareConfig>(() => ({
        expire: config.value.noDdl ? -1 : expireOptions[config.value.expire].rate * config.value.value,
        pwd: config.value.pwd
    }))

    return {
        config, formattedConfig
    }
}






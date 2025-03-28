<template>
    <aside class="h-full flex flex-col" :style>
        <div class="flex">
            <div class="w-20">
                <div v-for="(m, i) in menus" :key="m.id" :class="`${i === menuIdx ? 'text-h-blue' : ''} h-20 flex flex-col justify-center
                    items-center select-none cursor-pointer hover:text-h-blue transition-all my-4`"
                    @click="clickMainMenu(m.path, i)">
                    <template v-if="!shouldHiddenMenuItem(m)">
                        <component :is="m.icon" class="scale-[2] mb-4" />
                        <div class='text-md'>{{ m.name }}</div>
                    </template>
                </div>
            </div>
            <div class="w-28" v-if="!isTree && menus[menuIdx].children?.length">
                <div v-for="(c, i) in menus[menuIdx].children" :key="i"
                    :class="`h-14 flex justify-center items-center rounded hover:text-h-blue cursor-pointer select-none ${i === subMenuIdx ? isDark ? 'text-h-blue bg-[#333]' : 'text-h-blue bg-[#eee]' : ''}`"
                    @click="clickSubMenu(c.path, i)">
                    <component :is="c.icon" class="scale-[1.3]" />
                    <div class="ml-3">{{ c.name }}</div>
                </div>
            </div>
        </div>
        <div class="flex flex-col justify-center items-center absolute bottom-10 left-1 text-xs">
            <a-progress type="circle" :percent="+usedRate.toFixed(2)" :color="calcColorByUsedRate(usedRate)"
                :stroke-width="4" />
            <span class="my-2">已用空间</span>
            {{ calcFileSize(userInfo.usedSpace) }}/{{ calcFileSize(userInfo.totalSpace) }}
        </div>
    </aside>
</template>

<script setup lang="ts">
import { useBaseFileStore } from '@/stores/modules/file'
import { useUserStore } from '@/stores/modules/user';
import { useAppStore } from '@/stores/modules/app';
import { menus } from '@/constants/menuData';
import { calcFileSize } from '@/utils/calc';
import { HBlue } from '@/constants';
import type { IMenuItem } from '@/types/home';

const { menuIdx, subMenuIdx, textColor, bgColor, isDark } = storeToRefs(useAppStore());
const { userInfo } = storeToRefs(useUserStore());
const usedRate = computed(() => userInfo.value.usedSpace / userInfo.value.totalSpace);
const router = useRouter();
const { isTree } = storeToRefs(useBaseFileStore())

const style = computed(() => ({ backgroundColor: bgColor.value, color: textColor.value }))
/**
 * 根据使用率得到对应的颜色
 * @param rate
 */
const calcColorByUsedRate = (rate: number) => {
    if (rate < 0.3) return '#00B42A';
    if (rate < 0.5) return HBlue;
    if (rate < 0.8) return '#FF7D00';
    return '#F53F3F';
};

const shouldHiddenMenuItem = (item: IMenuItem) => item.path === '/admin' && !userInfo.value.isAdmin
const clickMainMenu = async (path: string, i: number) => {
    await router.push(path);
    subMenuIdx.value = 0;
    menuIdx.value = i;
}
const clickSubMenu = async (path: string, i: number) => {
    await router.push(path);
    subMenuIdx.value = i
}
</script>

<style scoped></style>
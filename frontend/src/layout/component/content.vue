<template>
  <main class="flex w-full h-full">
    <Transition>
      <side-nav class="hidden lg:flex" />
    </Transition>
    <div class="lg:hidden pl-2 top-12" :style="{ backgroundColor: bgColor, color: textColor }">
      <icon-double-right class="scale-150 mt-10 hover:text-h-blue" @click=" show = true" />
    </div>
    <a-drawer :width="220" placement="left" :closable="false" :footer="false" v-model:visible="show" unmountOnClose
      :drawerStyle="{ backgroundColor: bgColor, color: textColor, borderRight: isDark ? '1px solid #BFBFBF' : '' }">
      <side-nav />
    </a-drawer>
    <section class="flex-1 overflow-auto">
      <router-view></router-view>
    </section>
  </main>
</template>

<script setup lang="ts">
import { useAppStore } from '@/stores/modules/app';
import SideNav from './side-nav.vue'

const show = ref<boolean>(false);
const { isDark, textColor, bgColor } = storeToRefs(useAppStore());
</script>

<style scoped>
.v-enter-from .v-leave-to {
  transform: translateX(-300px)
}

.v-leave-from .v-enter-to {
  transform: translateX(0)
}
</style>
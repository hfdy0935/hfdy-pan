<template>
  <a-layout style="height: 100vh;position: relative;" :style="{ backgroundColor: bgColor }">
    <a-layout-header>
      <h-header></h-header>
    </a-layout-header>
    <a-layout-content class="overflow-auto">
      <Content></Content>
    </a-layout-content>
  </a-layout>
</template>

<script setup lang="ts">
import HHeader from './component/header';
import Content from './component/content.vue';
import { useUserStore } from '@/stores/modules/user';
import { useAppStore } from '@/stores/modules/app';

const { bgColor } = storeToRefs(useAppStore())
const { updateUserInfo } = useUserStore()
watchEffect(updateUserInfo)

</script>

<template>
  <a-layout style="height: 100vh">
    <a-layout-header>
      <h-header></h-header>
    </a-layout-header>
    <a-layout-content class="overflow-auto">
      <Content></Content>
    </a-layout-content>
  </a-layout>
</template>

<script setup lang="ts">
import HHeader from './component/Header';
import Content from './component/Content';
</script>

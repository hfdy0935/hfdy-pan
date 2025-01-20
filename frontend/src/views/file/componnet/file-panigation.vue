<script setup lang="ts">
import { useFileStore } from '@/stores';

defineOptions({
  name: 'FileFooter'
});
const { fileListData, page } = storeToRefs(useFileStore());
watchEffect(() => {
  console.log(page.value);
});
</script>

<template>
  <a-pagination v-if="fileListData.records?.length && fileListData.total>fileListData.records?.length"
                :total="fileListData.total" v-model:current="page" size="small" />
</template>

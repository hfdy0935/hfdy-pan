<template>
    <div class="py-3">
        <div class="flex justify-center">
            <logo :hide-title="false"></logo>
        </div>
        <top-info></top-info>
        <div class="pl-3">
            <bottom-table></bottom-table>
        </div>
    </div>
</template>

<script setup lang="ts">
import TopInfo from './top-info/index.vue';
import BottomTable from './bottom-table/index.vue';
import Logo from '@/component/logo.vue';

document.title = '分享文件';

</script>

<style scoped></style>

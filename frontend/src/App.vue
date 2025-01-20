<template>
    <router-view v-slot="{ Component }">
        <Transition>
            <component :is="Component"></component>
        </Transition>
    </router-view>
</template>

<script setup lang="ts"></script>

<style scoped>
@keyframes kf {
    0% {
        opacity: 100%;
    }
    100% {
        opacity: 0;
    }
}
.v-enter-active {
    animation: kf 0.3s ease reverse;
}
.v-leave-active {
    animation: kf 0.3s ease;
}
</style>

<template>
    <video ref="video" :src :key="data?.type" controls controlslist="nodownload"></video>
</template>

<script setup lang="ts">
import { useUserStore } from '@/stores/modules/user'
import { useViewFileStore } from '@/stores/modules/file';
import Hls from 'hls.js'

const { data } = storeToRefs(useViewFileStore())
const route = useRoute()
const { userInfo } = storeToRefs(useUserStore())

const videoRef = useTemplateRef<HTMLVideoElement>('video')
const src = ref('')
watchEffect(async () => {
    if (!data.value || !videoRef.value) return
    // 其他视频
    if (data.value.type !== 'application/x-mpegurl') {
        src.value = URL.createObjectURL(data.value)
    } else {
        // m3u8，返回的是新的url
        const hls = new Hls({
            xhrSetup(xhr) {
                xhr.setRequestHeader('Authorization', userInfo.value.accessToken)
                xhr.setRequestHeader('shareId', (route.params.shareId as string) ?? '')
            }
        })
        const value = await data.value.text()
        hls.loadSource(value)
        hls.attachMedia(videoRef.value)
    }
})
</script>

<style scoped></style>
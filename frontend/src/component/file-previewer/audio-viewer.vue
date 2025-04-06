<template>
    <div class="w-full h-full overflow-hidden relative"
        :style="{ backgroundImage: `url(${musicBg})`, backgroundSize: 'cover' }">
        <div ref="lyric" :key="file?.lyricPath" id="gc" class="flex flex-col items-center w-full h-full overflow-auto">
            <div class="order-1 text-[white]" v-for="i in blankRows" :key="i"><br /></div>
        </div>
        <div id="mse" ref="player"></div>
    </div>
</template>
<script setup lang="ts">
import Player from 'xgplayer'
import 'xgplayer/dist/index.min.css'
import MusicPreset, { Lyric } from 'xgplayer-music';
import 'xgplayer-music/dist/index.min.css'
import request from '@/utils/request';
import musicBg from '@/assets/image/music-bg.png'
import { useViewFileStore } from '@/stores/modules/viewFile';

const { file, data } = storeToRefs(useViewFileStore())

const playerRef = useTemplateRef<HTMLDivElement>('player')
const lyricRef = useTemplateRef<HTMLDivElement>('lyric')
let audioPlayer: Player | null
// 歌词后面加的空白行数
const blankRows = 10
watch([file, playerRef, data], async () => {
    if (!data.value || !file.value || !playerRef.value || !lyricRef.value) return
    const _url = URL.createObjectURL(data.value)
    audioPlayer = new Player({
        el: playerRef.value,
        url: _url,
        /***以下配置音乐播放器一定要有start***/
        controls: {
            mode: 'flex',
            initShow: true
        },
        marginControls: true,
        mediaType: 'audio',
        preset: [MusicPreset],
        /***以上配置音乐播放器一定要有ended***/
        width: '100%',
        height: '60px',
    })
    if (file.value.lyricPath.length) {
        const resp = await request(file.value.lyricPath)
        const lyric = new Lyric([resp], lyricRef.value)
        lyric.bind(audioPlayer)
        lyric.show()

        const totalLyric = (resp as unknown as string).split('\n')
        audioPlayer.on('seeked', e => {
            // 找到时间上最近的歌词
            let closeestLyricItem: { seekTime: number, i: number } | null = null
            for (let i = 0; i < totalLyric.length; i++) {
                const l = totalLyric[i]
                const minute = +l.slice(1, 3)
                const second = +l.slice(4, 6)
                const msecond = +l.slice(7, 9)
                const seekTime = minute * 60 + second * 1 + msecond * 0.1
                if (!closeestLyricItem) closeestLyricItem = { seekTime, i }
                else if (Math.abs(seekTime - e.currentTime) < Math.abs(closeestLyricItem.seekTime - e.currentTime)) {
                    closeestLyricItem = { seekTime, i }
                }
                if (seekTime > e.currentTime) break
            }
            const ratio = (closeestLyricItem!.i + 1) / totalLyricNum
            lyricRef.value?.scrollTo({
                top: ratio * lyricRef.value?.scrollHeight - 180,
                behavior: 'smooth'
            })
        })
        // 总行数
        const totalLyricNum = (resp as unknown as string).split('\n').length + blankRows
        audioPlayer.on('lyricUpdate', (res) => {
            const ratio = ((res.idx + 1) / totalLyricNum)
            lyricRef.value?.scrollTo({
                top: ratio * lyricRef.value?.scrollHeight - 180,
                behavior: 'smooth'
            })
        });
    }

})
onBeforeUnmount(() => {
    audioPlayer?.destroy()
})

</script>


<style scoped>
:deep(.xgplayer-lrcWrap) {
    display: flex;
    flex-direction: column;
    width: 100%;
    text-align: center;
    line-height: 32px;
    color: white !important;
}

:deep(.xgplayer-lyric-item-active) {
    color: rgb(49, 194, 124) !important;
    font-weight: bold;
}

:deep(.xgplayer-fullscreen) {
    display: none;
}

:deep(.xgplayer-cssfullscreen) {
    display: none;
}

#mse {
    position: absolute;
    bottom: 0;
    left: 0;
    z-index: 9;
}
</style>
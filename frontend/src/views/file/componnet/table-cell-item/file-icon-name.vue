<template>
    <a-badge :style="iconStyle">
        <template #content>
            <a-tooltip :content="fileStatus.desc" v-if="isVideo">
                <component :is="fileStatus.comp" @click="transVideCode" :size="10" :spin="fileStatus.spin"
                    class="cursor-pointer" :style="{ color: fileStatus.color }">
                </component>
            </a-tooltip>
        </template>
        <img v-if="isImage(item)" v-lazy="reqImage" class="w-8 h-6"></img>
        <component v-else :is="icon" class="w-6" />
    </a-badge>
    <template v-if="editingId === item.id">
        <a-input v-model:model-value="name" :style="{ ...nameStyle, width: `${inputWidth ?? 120}px` }"
            ref="rename_input" @blur="handleFinishEdit" @keyup.enter="inputRef!.blur()"></a-input>
    </template>
    <div v-else class="w-full truncate" :style="nameStyle">
        <span class="group-hover:text-h-blue cursor-pointer hover:text-h-blue select-none" @dblclick="openFileOrFolder">
            {{ name }}
        </span>
    </div>
</template>

<script setup lang="ts">
import { reqPreviewFile, reqTransVideoCode } from '@/api/file'
import { useEditFileName } from '@/composable/useEditFileName'
import { HBlue } from '@/constants'
import { getIconByFilename } from '@/constants/fileIcon'
import { useBaseFileStore, useFileItemStore, useViewFileStore } from '@/stores/modules/file'
import { useUserStore } from '@/stores/modules/user';
import { useAppStore } from '@/stores/modules/app';
import type { Callback } from '@/stores/modules/file'
import { TransCodeStatusEnum, type IFileItem } from '@/types/file'
import { calcFileLevel, isFolder, isImage } from '@/utils/file'
import { Input, Message } from '@arco-design/web-vue'
import { IconCheck, IconClockCircle, IconClose, IconMinus } from '@arco-design/web-vue/es/icon'
import type { CSSProperties } from 'vue'

// 文件状态图标
interface Status {
    comp: Component
    desc: string
    color?: string
    spin?: boolean
}

const { item } = defineProps<{
    item: IFileItem,
    iconStyle?: CSSProperties
    nameStyle?: CSSProperties
    inputWidth?: number
}>()
const appStore = useAppStore()
const { editingId, editNameOp, currParentFolder } = storeToRefs(useBaseFileStore())
const { itemList, contextmenuItem, query } = storeToRefs(useFileItemStore());
const { clearSelected, updateFileItem } = useFileItemStore()
const { openPreviewModal } = useViewFileStore()
const { userInfo } = storeToRefs(useUserStore())
const { name, handleFinishEidtInListBlock, resetName } = useEditFileName({
    fileOrNode: itemList.value.records.filter((f: IFileItem) => f.id === item.id)[0]
});


const isVideo = computed(() => item.mediaType === 'video')

const fileStatus = computed<Status>(() => {
    switch (item.status) {
        case TransCodeStatusEnum.NO_NEED: return {
            desc: '无需转码', comp: IconMinus
        }
        case TransCodeStatusEnum.ING: return {
            desc: '转码中', comp: IconClockCircle, color: HBlue, spin: true
        }
        case TransCodeStatusEnum.OK: return {
            desc: '转码成功', comp: IconCheck, color: 'green'
        }
        case TransCodeStatusEnum.ERR: return {
            desc: '转码失败，点击重试', comp: IconClose, color: 'red'
        }
    }
})
// 文件图标
const icon = computed(() => {
    if (item.mediaType === 'folder') return appStore.FileIcons.folder
    return getIconByFilename(item.name)
})

// 输入框ref
const inputRef = useTemplateRef<InstanceType<typeof Input>>('rename_input');
// 是否正在编辑当前文件/文件夹名
const isInputting = computed(() => editingId.value && contextmenuItem.value?.id === item.id);
/**
 * 当编辑编辑文件名的输入框出现时，自动获取焦点
 */
watchEffect(() => {
    inputRef.value?.focus();
    // 拿到内部的input
    inputRef.value?.$el.querySelector('input').select()
});

/**
 * 发送请求转码视频
 */
const transVideCode = () => {
    // 转码失败才能请求转码
    if (item.status !== TransCodeStatusEnum.ERR) return
    Message.success('转码请求已提交，请耐心等待')
    reqTransVideoCode(item.id)
    item.status = TransCodeStatusEnum.ING
}

/**
 * 双击文件/文件夹夹打开
 */
const openFileOrFolder = async () => {
    if (isFolder(item)) {
        // 达到最大深度不打开
        if (calcFileLevel(currParentFolder.value) === userInfo.value.maxFolderLevel) {
            Message.warning('已达到最大深度，不支持打开');
            return;
        }
        query.value = {
            ...query.value,
            page: 1,
            pid: item.id
        }
        clearSelected()
    }
    else openPreviewModal(item)
}

// 编辑名完成
const handleFinishEdit = () => {
    const id = editNameOp.value === 'create' ? itemList.value.parent.id : contextmenuItem.value!.id
    const cb: Callback = editNameOp.value === 'create' ? {
        ok: updateFileItem, fail: resetName, err: resetName
    } : {
        ok() {
            for (const file of itemList.value.records) {
                if (file.id === item.id) file.name = name.value
            }
        }, fail: resetName, err: resetName
    }
    handleFinishEidtInListBlock(id, cb)
}

/* ----------------------------------- 图片 ----------------------------------- */
const imageSrc = ref('')
const reqImage = async () => {
    const resp = await reqPreviewFile(item.id, new AbortController())
    imageSrc.value = URL.createObjectURL(resp)
    return imageSrc.value
}
</script>

<style scoped></style>

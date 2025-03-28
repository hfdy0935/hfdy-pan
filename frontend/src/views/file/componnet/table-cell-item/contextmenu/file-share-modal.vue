<template>
    <a-modal v-model:visible="show" title="文件分享" :footer="false" unmount-on-close :width="480">
        共 {{ sharedFiles.length }} 项
        <a-list :data="sharedFiles" class="mb-5" scrollbar :max-height="400">
            <template #item="{ item, index }: { item: IFileItem, index: number }">
                <a-row class="h-5 m-5">
                    <a-col :span="2">
                        {{ index + 1 }}
                    </a-col>
                    <a-col :span="2">
                        <component
                            :is="item.mediaType === 'folder' ? appStore.FileIcons.folder : getIconByFilename(item.name)"
                            class="scale-150"> </component>
                    </a-col>
                    <a-col :span="19" class="truncate">{{ item.name }}</a-col>
                    <a-col :span="1">
                        <a-button type="text" shape="circle" @click="sharedFiles.splice(index, 1)">
                            <icon-close />
                        </a-button>
                    </a-col>
                </a-row>
            </template>
        </a-list>
        <a-form :model="config" auto-label-width>
            <a-form-item label="永久有效">
                <a-switch v-model:model-value="config.noDdl"></a-switch>
            </a-form-item>
            <a-form-item label="有效期" :disabled="config.noDdl">
                <a-input-number :min="1" :max="100" v-model="config.value"></a-input-number>
            </a-form-item>
            <a-form-item label="单位" :disabled="config.noDdl">
                <a-select v-model:model-value="config.expire" :options="showedExpireOptions"></a-select>
            </a-form-item>
            <a-form-item label="提取码">
                <a-input-password v-model.trim="config.pwd" placeholder="不设置表示公开" allowClear
                    :max-length="12"></a-input-password>
            </a-form-item>
        </a-form>
        <div class="flex justify-end mt-6">
            <a-popconfirm content="确认要分享的文件及信息" @ok="doSahareFile">
                <a-button type="primary" :disabled="sharedFiles.length === 0">分享</a-button>
            </a-popconfirm>
        </div>
    </a-modal>
</template>

<script setup lang="tsx">
import { reqShareFile } from '@/api/share';
import { expireOptions, useShareConfig } from '@/composable/useShareFile';
import { useAppStore } from '@/stores/modules/app';
import type { IFileItem } from '@/types/file';
import { copyToClipboard } from '@/utils';
import { Message, Modal } from '@arco-design/web-vue';
import { showedExpireOptions } from '@/composable/useShareFile';
import { getIconByFilename } from '@/constants/fileIcon';
import { useSetShareStore } from '@/stores/modules/share';

const { show, sharedFiles } = storeToRefs(useSetShareStore());
const appStore = useAppStore()
const { config, formattedConfig } = useShareConfig()
// 分享
const doSahareFile = async () => {
    if (sharedFiles.value.length === 0) {
        Message.warning('请选择要分享的文件');
        return;
    }
    try {
        const resp = await reqShareFile({
            ids: sharedFiles.value.map(f => f.id),
            ...formattedConfig.value
        });
        if (resp.code === 200) {
            const link = location.protocol + '//' + location.host + '/share/' + resp.data.shareId + (resp.data.pwd ? `?pwd=${resp.data.pwd}` : '');
            const expireUnit = expireOptions[config.value.expire].title
            Modal.info({
                title: '分享成功',
                content: () => (
                    <a-card bodyStyle={{ padding: 0 }}>
                        <div class='text-wrap'>
                            &emsp;&emsp;我用hfdy云盘分享了一些文件，有效期{config.value.value}
                            {expireUnit}，快来通过
                            <a-link
                                style={{ 'word-break': 'break-all', 'white-space': 'normal' }}
                                href={link}
                                target='_blank'
                                v-slots={{
                                    icon: () => <icon-share-internal />,
                                }}
                            >
                                {link}
                            </a-link>
                            查看吧~
                        </div>
                        <div class='flex justify-center my-6'>
                            <a-button
                                type='primary'
                                status='success'
                                onClick={() => {
                                    copyToClipboard(`我用hfdy云盘分享了一些文件，有效期${config.value.value}${expireUnit}，快来通过${link}查看吧~
                            `);
                                }}
                            >
                                复制
                            </a-button>
                            <a-button
                                class='ml-4'
                                type='primary'
                                onClick={() => {
                                    copyToClipboard(link);
                                }}
                            >
                                仅复制链接
                            </a-button>
                        </div>
                    </a-card>
                ),
                footer: false,
            });
            Message.success('分享成功');
        } else Message.error(resp.message);
    } catch {
        Message.error('分享失败');
    }
};
</script>

<style scoped></style>

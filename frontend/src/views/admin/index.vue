<template>
    <div>
        <a-table :columns class="m-3 mt-8" :data hoverable :pagination="false" v-model:selectedKeys="selectedIds">
            <template #td="{ column, record, rowIndex }">
                <table-cell :column :record :rowIndex @delete-user="doDeleteUser" @openModal="openModal" />
            </template>
            <template #empty>
                <a-empty description="还没有任何文件~"></a-empty>
            </template>
        </a-table>
        <a-modal title="修改用户信息" v-model:visible="modalData.show" :closable="false" @ok="doUpdate">
            <a-form :model="modalData">
                <a-form-item label="状态">
                    <a-radio-group type="button" v-model:model-value="modalData.userInfo.status">
                        <a-radio :value="0">封禁</a-radio>
                        <a-radio :value="1">正常</a-radio>
                    </a-radio-group>
                </a-form-item>
                <a-form-item label="身份">
                    <a-radio-group type="button" v-model:model-value="modalData.userInfo.isVip">
                        <a-radio :value="0">普通用户</a-radio>
                        <a-radio :value="1">&emsp;vip&emsp;</a-radio>
                    </a-radio-group>
                </a-form-item>
                <a-form-item label="上传并发数">
                    <a-input-number :min="1" v-model:model-value="modalData.userInfo.uploadLimit"
                        style="width: 240px;"></a-input-number>&emsp;个
                </a-form-item>
                <a-form-item label="下载速度">
                    <a-input-number :min="1" v-model:model-value="modalData.userInfo.downloadSpeed"
                        style="width: 240px;"></a-input-number>&emsp;KB/s
                </a-form-item>
            </a-form>
        </a-modal>
    </div>
</template>

<script setup lang="ts">
import { reqUpdateUser, reqUserList } from '@/api/admin';
import type { UserListVO } from '@/types/admin';
import { Message, type TableColumnData } from '@arco-design/web-vue';
import TableCell from './table-cell.vue';

const data = ref<UserListVO[]>([])
watchEffect(async () => {
    const resp = await reqUserList()
    if (resp.code === 200) data.value = resp.data
})

const columns: TableColumnData[] = [
    {
        title: '序号',
        dataIndex: 'index',
        width: 60,
    },
    {
        title: '昵称',
        dataIndex: 'nickName',
    },
    {
        title: '邮箱',
        dataIndex: 'email',
    },
    {
        title: '注册时间',
        dataIndex: 'registerTime',
    },
    {
        title: '上次登录时间',
        dataIndex: 'lastLoginTime',
    },
    {
        title: '状态',
        dataIndex: 'status',
    },
    {
        title: '已使用空间',
        dataIndex: 'usedSpace',
    },
    {
        title: '用户身份',
        dataIndex: 'isVip',
    },
    {
        title: '上传并发数量限制',
        dataIndex: 'uploadLimit',
    },
    {
        title: '下载速度',
        dataIndex: 'downloadSpeed',
    },
    {
        title: '操作',
        dataIndex: 'operation',
        fixed: 'right' as const
    },
].map(el => ({ ...el, align: 'center' }));
const selectedIds = ref<string[]>([])


/**
 * 表格数据中删除user，不发送请求
 * @param idx 
 */
const doDeleteUser = (idx: number) => {
    data.value.splice(idx, 1)
}

/* -------------------------------- 更新用户信息对话框 ------------------------------- */

const modalData = reactive<{
    show: boolean, idx: number, userInfo: UserListVO
}>({
    show: false,
    idx: -1,
    userInfo: {} as UserListVO
})
const openModal = (idx: number) => {
    modalData.idx = idx
    modalData.userInfo = JSON.parse(JSON.stringify(data.value[idx]))
    modalData.userInfo.downloadSpeed=+(modalData.userInfo.downloadSpeed/1024).toFixed(2) // 下载速度从B转为KB
    modalData.show = true
}

const doUpdate = async () => {
    const { userInfo: { isVip, status, uploadLimit, downloadSpeed, id } } = modalData
    const downloadSpeedAsB=downloadSpeed * 1024
    const resp = await reqUpdateUser({
        isVip, status, uploadLimit, userId: id, downloadSpeed: downloadSpeedAsB
    })
    if (resp.code === 200) {
        Message.success('修改成功')
        data.value[modalData.idx] = { ...data.value[modalData.idx], isVip, status, uploadLimit, downloadSpeed:downloadSpeedAsB }
    }
    else Message.error('修改失败')
}
</script>

<style scoped></style>
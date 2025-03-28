<template>
    <a-table v-if="data.length" :columns class="m-3 mt-10" :scroll="{ x: 700 }" :data hoverable :pagination="false">
        <template #td="{ column, record, rowIndex }">
            <table-cell :column :record :rowIndex @update="id => data = data.filter(d => d.id !== id)"
                @open="openRecoverModal" />
        </template>
    </a-table>
    <a-empty v-else class="mt-36" />
    <a-modal v-model:visible="show" @ok="doRecover" title="选择恢复到哪个文件夹">
        <a-tree :data="treeData" :fieldNames v-model:selected-keys="selectedIds" @select="doSelectFolder"
            check-strictly></a-tree>
    </a-modal>
</template>

<script setup lang="ts">
import TableCell from './table-cell.vue';
import type { IRecycle } from '@/types/recycle';
import { reqGetRecycleFiles, reqRecoverFiles } from '@/api/recycle';
import { Message, type TableColumnData, type TreeNodeData } from '@arco-design/web-vue';
import { fieldNames, findNodeById, useFolderLevelTree } from '@/composable/useFolderLevelTree';
import type { ITreeData } from '@/types/file';


const {
    FAKE_ROOT_ID,
    treeDataBanFileWithRoot: treeData,
    selectedIds,
    updateTreeLayerData
} = useFolderLevelTree()
const data = ref<IRecycle[]>([]);

/**
 * 更新用户回收站数据
 */
watchEffect(async () => {
    try {
        const resp = await reqGetRecycleFiles();
        if (resp.code === 200) data.value = resp.data;
        else Message.error(resp.message);
    } catch {
        Message.error('获取回收站信息失败');
    }
});

const columns: TableColumnData[] = [
    {
        title: '序号',
        dataIndex: 'index',
        width: 60,
    },
    {
        title: '文件名',
        dataIndex: 'name',
        width: 180,
    },
    {
        title: '路径',
        dataIndex: 'level',
        width: 180,
    },
    {
        title: '创建时间',
        dataIndex: 'createTime',
        width: 180,
    },
    {
        title: '删除时间',
        dataIndex: 'deleteTime',
        width: 180,
    },
    {
        title: '操作',
        dataIndex: 'operation',
        width: 120,
    },
].map(o => ({ ...o, align: 'center' }));
// 树的对话框
const show = ref(false)
// 要恢复的文件
const targetFile = ref<IRecycle>();

/**
 * 打开选择恢复到哪个文件夹的对话框
 * @param recycle 要恢复的文件记录
 */
const openRecoverModal = async (recycle: IRecycle) => {
    await updateTreeLayerData()
    const node = findNodeById(treeData.value, recycle.pid);
    // 默认恢复到原来的父文件夹
    selectedIds.value = [recycle.pid || FAKE_ROOT_ID];
    // 如果树数据没有recycle.pid，说明他的父文件夹被删了
    if (!node) selectedIds.value = [];
    targetFile.value = recycle;
    show.value = true;
    // 初始化时判断是否和父文件夹的文件重名
    if (node && node.children?.map((n: ITreeData) => n.name).includes(recycle.name)) {
        selectedIds.value = []
    }
};
// 点击时校验是否和点的文件夹重名
const doSelectFolder = (_: any, { node }: { node?: TreeNodeData }) => {
    const existNames = (node as ITreeData).children?.map(c => c.name);
    if (existNames?.includes(targetFile.value!.name)) {
        Message.warning('同一目录下的文件/文件夹名不能重复');
        selectedIds.value = []
    }
};

/**
 * 复原
 */
const doRecover = async () => {
    if (!selectedIds.value.length) {
        Message.warning('请选择要恢复到哪个文件夹');
        return false;
    }
    try {
        const pid = selectedIds.value[0] === FAKE_ROOT_ID ? '' : selectedIds.value[0];
        const resp = await reqRecoverFiles([targetFile.value!.id], pid);
        if (resp.code === 200) {
            Message.success('恢复成功');
            data.value = data.value.filter(d => d.id !== targetFile.value?.id)
        } else Message.error(resp.message);
    } catch {
        Message.error('恢复失败');
    }
};
</script>

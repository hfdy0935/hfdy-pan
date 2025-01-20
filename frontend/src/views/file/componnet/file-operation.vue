<script setup lang="ts">
import { useFileStore } from '@/stores';
import type { FileCategory } from '@/types/file';
import { Message } from '@arco-design/web-vue';

defineOptions({
  name: 'FileOperation'
});
const route = useRoute();
const { keyword, page, orderBySize, orderByUpdateTime } = storeToRefs(useFileStore());
const { getFileListData } = useFileStore();
const refresh = async () => {
  if (await getFileListData())
    Message.success('刷新成功');
};
</script>

<template>
  <a-row class="w-full flex justify-around items-center pt-4">
    <a-col :xs="24" :lg="16">
      <div class="w-full lg:max-w-[800px] flex justify-around flex-wrap my-6">
        <a-button type="primary" status="success" class="md:w-28 xl:w-36">上传
          <template #icon>
            <icon-upload :stroke-width="6" :size="16" />
          </template>
        </a-button>
        <!--        <a-button type="primary" class="md:w-28 xl:w-36" status="success">新建文件夹-->
        <!--          <template #icon>-->
        <!--            <icon-folder :stroke-width="6" :size="16" />-->
        <!--          </template>-->
        <!--        </a-button>-->
        <a-button type="primary" class="md:w-28 xl:w-36" status="warning">批量移动
          <template #icon>
            <icon-drag-arrow :stroke-width="6" :size="16" />
          </template>
        </a-button>
        <a-button type="primary" class="md:w-28 xl:w-36" status="danger">批量删除
          <template #icon>
            <icon-delete :stroke-width="6" :size="16" />
          </template>
        </a-button>
      </div>
    </a-col>
    <a-col :xs="24" :lg="8" class="flex justify-center lg:justify-end lg:pr-10 items-center my-6">
      <a-input-search class="max-w-[300px]" v-model.trim="keyword" placeholder="输入搜索内容" allow-clear />
      <a-tooltip content="刷新" @click="refresh">
        <icon-refresh :stroke-width="5" :size="20" class="ml-2 cursor-pointer hover:text-h-blue" />
      </a-tooltip>
    </a-col>
  </a-row>
</template>


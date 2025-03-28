<script setup lang="ts">
import { type TableColumnData, type TableData } from '@arco-design/web-vue';
import type { IFileItem } from '@/types/file';
import { calcFileSize } from '@/utils/calc';
import NormalFileContextmenu from './contextmenu/normal.vue'
import FileIconComp from './file-icon-name.vue'
import { useFileItemStore } from '@/stores/modules/file'


interface Props {
    column: TableColumnData;
    record: TableData;
    rowIndex: number;
}

const { column, record } = defineProps<Props>();
const { query } = storeToRefs(useFileItemStore());
</script>

<template>
    <td>
        <!--    为了避免表头也有右键菜单事件，所以把右键菜单加到了每个单元格上，而不是整个表格-->
        <normal-file-contextmenu>
            <div class="min-h-14 flex items-center">
                <template v-if="column.dataIndex === 'name'">
                    <file-icon-comp :item="(record as IFileItem)" :name-style="{ marginLeft: '6px' }"
                        :input-width="160">
                    </file-icon-comp>
                </template>
                <div v-else-if="column.dataIndex === 'updateTime'" class="min-w-36 ml-3">
                    {{ record[column.dataIndex!] }}
                </div>
                <div v-else-if="column.dataIndex === 'index'" class="w-full text-center">
                    {{ (query.page - 1) * query.pageSize + rowIndex + 1 }}
                </div>
                <span class="mx-4" v-else>{{
                    column.dataIndex === 'size' && record.mediaType !== 'folder' ?
                        calcFileSize(record[column.dataIndex!]) :
                        record[column.dataIndex!]
                }}</span>
            </div>
        </normal-file-contextmenu>
    </td>
</template>

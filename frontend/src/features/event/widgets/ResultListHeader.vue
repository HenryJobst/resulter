<script setup lang="ts">
import type { ResultList } from '@/features/event/model/result_list'
import type { Race } from '@/features/race/model/race'
import { useI18n } from 'vue-i18n'

const props = defineProps<{ resultList: ResultList, race: Race }>()
const { t } = useI18n()
</script>

<template>
    <div>
        {{ props.race?.name }}
    </div>
    <div>
        {{ t('labels.created') }}
    </div>
    <div class="ml-2">
        {{ props.resultList?.createTime.toLocaleString() }}
    </div>
    <div class="ml-2">
        {{ props.resultList?.status }}
    </div>
</template>

<style scoped>
div {
  margin-left: 2px;
}
</style>

<script setup lang="ts">
import { useMessageDetailStore } from '@/features/common/stores/useMessageDetailStore.ts'
import Dialog from 'primevue/dialog'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const messageDetailStore = useMessageDetailStore()
</script>

<template>
    <Dialog
        v-model:visible="messageDetailStore.visible"
        :header="t('messages.detail')"
        modal
        dismissable-mask
        :style="{ width: '25rem' }"
        :breakpoints="{ '1199px': '75vw', '575px': '90vw' }"
        @close="messageDetailStore.hide"
    >
        <p>{{ messageDetailStore.getDetails() }}</p>
    </Dialog>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { useEventStore } from '@/features/event/store/event.store'

const { t } = useI18n() // same as `useI18n({ useScope: 'global' })`

const store = useEventStore()
store.loadEventsAction()
</script>

<template>
  <RouterView></RouterView>
</template>

<style scoped></style>
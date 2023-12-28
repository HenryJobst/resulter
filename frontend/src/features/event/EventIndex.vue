<script setup lang="ts">
import { useEventStore } from '@/features/event/store/event.store'

const store = useEventStore()
store.loadEventsAction()
</script>

<template>
  <RouterView></RouterView>
</template>

<style scoped></style>

<script setup lang="ts">
import { useAuthStore } from '@/features/keycloak/store/auth.store'

const props = defineProps<{ id: string; locale?: string }>()
const authStore = useAuthStore()
</script>

<template></template>

<style scoped></style>

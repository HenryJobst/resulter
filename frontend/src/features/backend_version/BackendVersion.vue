<script setup lang="ts">
import axiosInstance from '@/features/keycloak/services/api'
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const backendVersion = ref(t('labels.loading'))
const backendVersionText = computed(() => t('labels.backend_version', { version: backendVersion.value }))

onMounted(async () => {
    await axiosInstance.get('/version')
        .then(response => backendVersion.value = response.data)
        .catch((error) => {
            console.error('Failed to load backend version:', error)
            backendVersion.value = t('labels.error_loading')
        })
})
</script>

<template>
    {{ backendVersionText }}
</template>

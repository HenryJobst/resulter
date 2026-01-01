<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useBackendVersion } from './services/version.service'

const { t } = useI18n()

// Use Tanstack Query to fetch backend version
const { data: backendVersion, isLoading, isError } = useBackendVersion()

// Compute display text based on query state
const backendVersionText = computed(() => {
    if (isLoading.value) {
        return t('labels.backend_version', { version: t('labels.loading') })
    }
    if (isError.value) {
        return t('labels.backend_version', { version: t('labels.error_loading') })
    }
    return t('labels.backend_version', { version: backendVersion.value || '' })
})
</script>

<template>
    {{ backendVersionText }}
</template>

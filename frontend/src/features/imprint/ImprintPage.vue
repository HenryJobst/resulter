<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const { t, locale } = useI18n()

const imprintText = computed(() => {
    const key = `VITE_IMPRESS_TEXT_${locale.value.toUpperCase()}`
    return import.meta.env[key] || t('labels.defaultImprintText')
})
</script>

<template>
    <div class="about">
        <div class="max-w-4xl mx-auto py-12 px-4 sm:px-6 lg:px-8">
            <h1 class="text-3xl font-extrabold text-gray-900 mb-4">
                {{ t('navigations.imprint') }}
            </h1>
            <p v-html="imprintText" />
        </div>
    </div>
</template>

<style scoped>
</style>

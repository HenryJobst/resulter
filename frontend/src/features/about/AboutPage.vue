<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t } = useI18n() // same as `useI18n({ useScope: 'global' })`
</script>

<template>
    <div class="about">
        <div class="max-w-4xl mx-auto py-12 px-4 sm:px-6 lg:px-8">
            <h1 class="text-3xl font-extrabold text-gray-900 mb-4">
                {{ t('about.title') }}
            </h1>
            <p class="mt-2 text-lg text-gray-600">
                {{ t('about.description') }}
            </p>
            <p class="mt-2 text-lg text-gray-600">
                {{ t('about.features') }}
            </p>
            <p class="mt-2 text-lg text-gray-600">
                {{ t('about.futurePlans') }}
            </p>
            <p class="mt-8 text-lg text-gray-900">
                {{ t('about.contact') }}
                <a :href="`mailto:${t('about.emailAddress')}`" class="text-indigo-600 hover:text-indigo-900">
                    {{ t('about.emailAddress') }}
                </a>
            </p>
        </div>
    </div>
</template>

<style></style>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t } = useI18n() // same as `useI18n({ useScope: 'global' })`
</script>

<template>
    <div class="about bg-adaptive min-h-screen py-8">
        <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
            <!-- Main Card -->
            <div class="bg-adaptive-secondary rounded-xl shadow-lg border border-adaptive p-6">
                <!-- Header with Logo and Title -->
                <div class="flex flex-col md:flex-row items-center gap-6 mb-4 pb-3 border-b border-adaptive">
                    <img
                        src="@/assets/Logo_Resulter_400px.webp"
                        alt="Resulter Logo"
                        class="w-24 h-24"
                    >
                    <div class="flex-1 text-center md:text-left">
                        <h1 class="text-2xl md:text-3xl font-bold text-adaptive mb-2">
                            {{ t('about.title') }}
                        </h1>
                        <p class="text-base text-adaptive-secondary">
                            {{ t('about.description') }}
                        </p>
                    </div>
                </div>

                <!-- Features Section -->
                <div class="mb-3 pb-5 border-b border-adaptive">
                    <h2 class="text-base font-semibold text-adaptive mb-3">
                        {{ t('about.features_heading') }}
                    </h2>
                    <div class="flex flex-wrap gap-2">
                        <div class="flex items-center gap-2 bg-adaptive px-3 py-2 rounded-lg border border-adaptive">
                            <i class="pi pi-upload text-primary-600 text-sm" />
                            <span class="text-sm text-adaptive">{{ t('about.feature_event_results') }}</span>
                        </div>
                        <div class="flex items-center gap-2 bg-adaptive px-3 py-2 rounded-lg border border-adaptive">
                            <i class="pi pi-trophy text-primary-600 text-sm" />
                            <span class="text-sm text-adaptive">{{ t('about.feature_cup_results') }}</span>
                        </div>
                        <div class="flex items-center gap-2 bg-adaptive px-3 py-2 rounded-lg border border-adaptive">
                            <i class="pi pi-file-pdf text-primary-600 text-sm" />
                            <span class="text-sm text-adaptive">{{ t('about.feature_certificates') }}</span>
                        </div>
                        <div class="flex items-center gap-2 bg-adaptive px-3 py-2 rounded-lg border border-adaptive">
                            <i class="pi pi-chart-line text-primary-600 text-sm" />
                            <span class="text-sm text-adaptive">{{ t('about.feature_split_analysis') }}</span>
                        </div>
                    </div>
                </div>

                <!-- Contact Section -->
                <div class="flex items-center justify-center gap-4">
                    <div class="flex items-center gap-2 text-adaptive">
                        <i class="pi pi-envelope text-primary-600" />
                        <span class="font-medium">{{ t('about.contact') }}</span>
                    </div>
                    <a
                        :href="`mailto:${t('about.emailAddress')}`"
                        class="inline-flex items-center gap-2 px-4 py-2 bg-primary-600 hover:bg-primary-700 text-white text-sm font-medium rounded-lg shadow transition-all duration-200"
                    >
                        <i class="pi pi-send" />
                        {{ t('about.emailAddress') }}
                    </a>
                </div>
            </div>
        </div>
    </div>
</template>

<style></style>

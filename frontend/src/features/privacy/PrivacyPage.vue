<script setup lang="ts">
import Card from 'primevue/card'
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'

const { t, locale } = useI18n()
const router = useRouter()

const privacyText = computed(() => {
    const key = `VITE_PRIVACY_TEXT_${locale.value.toUpperCase()}`
    return import.meta.env[key] || t('labels.defaultPrivacyText')
})

function showImprint() {
    router.push({ name: 'imprint-page', params: { locale: locale.value } })
}
</script>

<template>
    <div class="privacy-page">
        <div class="max-w-4xl mx-auto py-8 px-4">
            <!-- Page Header -->
            <div class="page-header mb-6">
                <div class="flex items-center gap-3 mb-3">
                    <div class="icon-wrapper">
                        <i class="pi pi-shield text-2xl" />
                    </div>
                    <h1 class="text-3xl font-bold">
                        {{ t('navigations.privacy') }}
                    </h1>
                </div>
            </div>

            <!-- Privacy Content Card -->
            <Card class="privacy-card">
                <template #content>
                    <div class="privacy-content" v-html="privacyText" />

                    <!-- Contact reference to imprint -->
                    <div class="contact-hint mt-6 pt-6 border-t border-adaptive">
                        <p class="text-sm text-adaptive-secondary">
                            {{ t('labels.privacyContactHint') }}
                            <a
                                href="#"
                                class="text-primary-600 hover:text-primary-700 font-medium"
                                @click.prevent="showImprint"
                            >{{ t('navigations.imprint') }}</a>.
                        </p>
                    </div>
                </template>
            </Card>
        </div>
    </div>
</template>

<style scoped>
.privacy-page {
    min-height: calc(100vh - 200px);
}

.icon-wrapper {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 56px;
    height: 56px;
    border-radius: 12px;
    background: linear-gradient(135deg, rgba(59, 130, 246, 0.1) 0%, rgba(59, 130, 246, 0.05) 100%);
    border: 1px solid rgba(59, 130, 246, 0.2);
    color: rgb(59, 130, 246);
}

.privacy-card {
    border-radius: 12px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.privacy-content {
    line-height: 1.8;
    color: rgb(55, 65, 81);
}

.privacy-content :deep(h1) {
    font-size: 1.875rem;
    font-weight: 700;
    margin-top: 2rem;
    margin-bottom: 1rem;
    color: rgb(17, 24, 39);
}

.privacy-content :deep(h2) {
    font-size: 1.5rem;
    font-weight: 600;
    margin-top: 1.5rem;
    margin-bottom: 0.75rem;
    color: rgb(31, 41, 55);
}

.privacy-content :deep(h3) {
    font-size: 1.25rem;
    font-weight: 600;
    margin-top: 1.25rem;
    margin-bottom: 0.5rem;
    color: rgb(55, 65, 81);
}

.privacy-content :deep(h1:first-child),
.privacy-content :deep(h2:first-child),
.privacy-content :deep(h3:first-child) {
    margin-top: 0;
}

.privacy-content :deep(p) {
    margin-bottom: 1rem;
}

.privacy-content :deep(p:last-child) {
    margin-bottom: 0;
}

.privacy-content :deep(a) {
    color: rgb(59, 130, 246);
    text-decoration: none;
    font-weight: 500;
    transition: color 0.2s;
}

.privacy-content :deep(a:hover) {
    color: rgb(37, 99, 235);
    text-decoration: underline;
}

.privacy-content :deep(ul),
.privacy-content :deep(ol) {
    margin-left: 1.5rem;
    margin-bottom: 1rem;
}

.privacy-content :deep(li) {
    margin-bottom: 0.5rem;
}

.privacy-content :deep(strong),
.privacy-content :deep(b) {
    font-weight: 600;
    color: rgb(17, 24, 39);
}

.privacy-content :deep(hr) {
    border: none;
    border-top: 1px solid rgb(229, 231, 235);
    margin: 2rem 0;
}

@media (max-width: 768px) {
    .privacy-page {
        padding: 1rem;
    }

    .icon-wrapper {
        width: 48px;
        height: 48px;
    }

    .privacy-content :deep(h1) {
        font-size: 1.5rem;
    }

    .privacy-content :deep(h2) {
        font-size: 1.25rem;
    }

    .privacy-content :deep(h3) {
        font-size: 1.125rem;
    }
}

@media (prefers-color-scheme: dark) {
    .icon-wrapper {
        background: linear-gradient(135deg, rgba(59, 130, 246, 0.15) 0%, rgba(59, 130, 246, 0.08) 100%);
        border-color: rgba(59, 130, 246, 0.3);
    }

    .privacy-content {
        color: rgb(209, 213, 219);
    }

    .privacy-content :deep(h1) {
        color: rgb(243, 244, 246);
    }

    .privacy-content :deep(h2) {
        color: rgb(229, 231, 235);
    }

    .privacy-content :deep(h3) {
        color: rgb(209, 213, 219);
    }

    .privacy-content :deep(strong),
    .privacy-content :deep(b) {
        color: rgb(243, 244, 246);
    }

    .privacy-content :deep(a) {
        color: rgb(96, 165, 250);
    }

    .privacy-content :deep(a:hover) {
        color: rgb(147, 197, 253);
    }

    .privacy-content :deep(hr) {
        border-color: rgb(55, 65, 81);
    }
}
</style>

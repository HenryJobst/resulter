<script setup lang="ts">
import Card from 'primevue/card'
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const { t, locale } = useI18n()

const imprintText = computed(() => {
    const key = `VITE_IMPRESS_TEXT_${locale.value.toUpperCase()}`
    return import.meta.env[key] || t('labels.defaultImprintText')
})
</script>

<template>
    <div class="imprint-page">
        <div class="max-w-4xl mx-auto py-8 px-4">
            <!-- Page Header -->
            <div class="page-header mb-6">
                <div class="flex items-center gap-3 mb-3">
                    <div class="icon-wrapper">
                        <i class="pi pi-info-circle text-2xl" />
                    </div>
                    <h1 class="text-3xl font-bold">
                        {{ t('navigations.imprint') }}
                    </h1>
                </div>
            </div>

            <!-- Imprint Content Card -->
            <Card class="imprint-card">
                <template #content>
                    <div class="imprint-content" v-html="imprintText" />
                </template>
            </Card>
        </div>
    </div>
</template>

<style scoped>
.imprint-page {
    min-height: calc(100vh - 200px);
}

/* Icon Wrapper */
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

/* Imprint Card */
.imprint-card {
    border-radius: 12px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

/* Imprint Content */
.imprint-content {
    line-height: 1.8;
    color: rgb(55, 65, 81);
}

.imprint-content :deep(h1) {
    font-size: 1.875rem;
    font-weight: 700;
    margin-top: 2rem;
    margin-bottom: 1rem;
    color: rgb(17, 24, 39);
}

.imprint-content :deep(h2) {
    font-size: 1.5rem;
    font-weight: 600;
    margin-top: 1.5rem;
    margin-bottom: 0.75rem;
    color: rgb(31, 41, 55);
}

.imprint-content :deep(h3) {
    font-size: 1.25rem;
    font-weight: 600;
    margin-top: 1.25rem;
    margin-bottom: 0.5rem;
    color: rgb(55, 65, 81);
}

.imprint-content :deep(h1:first-child),
.imprint-content :deep(h2:first-child),
.imprint-content :deep(h3:first-child) {
    margin-top: 0;
}

.imprint-content :deep(p) {
    margin-bottom: 1rem;
}

.imprint-content :deep(p:last-child) {
    margin-bottom: 0;
}

.imprint-content :deep(a) {
    color: rgb(59, 130, 246);
    text-decoration: none;
    font-weight: 500;
    transition: color 0.2s;
}

.imprint-content :deep(a:hover) {
    color: rgb(37, 99, 235);
    text-decoration: underline;
}

.imprint-content :deep(ul),
.imprint-content :deep(ol) {
    margin-left: 1.5rem;
    margin-bottom: 1rem;
}

.imprint-content :deep(li) {
    margin-bottom: 0.5rem;
}

.imprint-content :deep(strong),
.imprint-content :deep(b) {
    font-weight: 600;
    color: rgb(17, 24, 39);
}

.imprint-content :deep(em),
.imprint-content :deep(i) {
    font-style: italic;
}

.imprint-content :deep(code) {
    background-color: rgb(243, 244, 246);
    padding: 0.125rem 0.375rem;
    border-radius: 0.25rem;
    font-size: 0.875rem;
    font-family: monospace;
}

.imprint-content :deep(pre) {
    background-color: rgb(243, 244, 246);
    padding: 1rem;
    border-radius: 0.5rem;
    overflow-x: auto;
    margin-bottom: 1rem;
}

.imprint-content :deep(pre code) {
    background-color: transparent;
    padding: 0;
}

.imprint-content :deep(hr) {
    border: none;
    border-top: 1px solid rgb(229, 231, 235);
    margin: 2rem 0;
}

.imprint-content :deep(blockquote) {
    border-left: 4px solid rgb(59, 130, 246);
    padding-left: 1rem;
    margin-left: 0;
    margin-right: 0;
    margin-bottom: 1rem;
    color: rgb(75, 85, 99);
    font-style: italic;
}

/* Responsive */
@media (max-width: 768px) {
    .imprint-page {
        padding: 1rem;
    }

    .icon-wrapper {
        width: 48px;
        height: 48px;
    }

    .imprint-content :deep(h1) {
        font-size: 1.5rem;
    }

    .imprint-content :deep(h2) {
        font-size: 1.25rem;
    }

    .imprint-content :deep(h3) {
        font-size: 1.125rem;
    }
}

/* Dark Mode Support */
@media (prefers-color-scheme: dark) {
    .icon-wrapper {
        background: linear-gradient(135deg, rgba(59, 130, 246, 0.15) 0%, rgba(59, 130, 246, 0.08) 100%);
        border-color: rgba(59, 130, 246, 0.3);
    }

    .imprint-content {
        color: rgb(209, 213, 219);
    }

    .imprint-content :deep(h1) {
        color: rgb(243, 244, 246);
    }

    .imprint-content :deep(h2) {
        color: rgb(229, 231, 235);
    }

    .imprint-content :deep(h3) {
        color: rgb(209, 213, 219);
    }

    .imprint-content :deep(strong),
    .imprint-content :deep(b) {
        color: rgb(243, 244, 246);
    }

    .imprint-content :deep(a) {
        color: rgb(96, 165, 250);
    }

    .imprint-content :deep(a:hover) {
        color: rgb(147, 197, 253);
    }

    .imprint-content :deep(code) {
        background-color: rgb(55, 65, 81);
        color: rgb(243, 244, 246);
    }

    .imprint-content :deep(pre) {
        background-color: rgb(55, 65, 81);
    }

    .imprint-content :deep(hr) {
        border-color: rgb(55, 65, 81);
    }

    .imprint-content :deep(blockquote) {
        border-left-color: rgb(59, 130, 246);
        color: rgb(156, 163, 175);
    }
}
</style>

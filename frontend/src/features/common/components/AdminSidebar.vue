<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'

const props = defineProps<{
    collapsed: boolean
    currentLocale: string
}>()

const { t } = useI18n()
const router = useRouter()
const route = useRoute()

interface AdminMenuItem {
    key: string
    label: string
    icon: string
    route: string
}

const adminMenuItems = computed<AdminMenuItem[]>(() => [
    {
        key: 'organisations',
        label: t('navigations.organisations'),
        icon: 'pi-building',
        route: 'organisation-list',
    },
    {
        key: 'persons',
        label: t('navigations.persons'),
        icon: 'pi-user',
        route: 'person-list',
    },
    {
        key: 'media',
        label: t('navigations.media-files'),
        icon: 'pi-images',
        route: 'media-list',
    },
    {
        key: 'certificates',
        label: t('navigations.certificates'),
        icon: 'pi-file-pdf',
        route: 'certificate-list',
    },
])

function navigateTo(routeName: string) {
    router.push({ name: routeName, params: { locale: props.currentLocale } })
}

function isActive(routeName: string): boolean {
    return route.name === routeName
}
</script>

<template>
    <aside
        class="admin-sidebar flex-shrink-0 bg-adaptive-secondary border-r border-adaptive transition-all duration-300 overflow-hidden" :class="[
            { 'w-60': !collapsed, 'w-16': collapsed },
        ]"
    >
        <div class="flex flex-col h-full py-4">
            <!-- Admin Header -->
            <div class="px-4 mb-4" :class="[{ 'text-center': collapsed }]">
                <h3
                    v-if="!collapsed"
                    class="text-sm font-semibold text-adaptive-secondary uppercase tracking-wider"
                >
                    {{ t('labels.administration') }}
                </h3>
                <i v-else class="pi pi-cog text-adaptive-secondary text-xl" />
            </div>

            <!-- Menu Items -->
            <nav class="flex-1 space-y-1 px-2">
                <button
                    v-for="item in adminMenuItems"
                    :key="item.key"
                    class="w-full flex items-center gap-3 px-3 py-2.5 rounded-lg transition-all duration-200 text-left" :class="[
                        {
                            'bg-primary-100 text-primary-700': isActive(item.route),
                            'text-adaptive-secondary hover:bg-adaptive-tertiary': !isActive(item.route),
                            'justify-center': collapsed,
                        },
                    ]"
                    :title="collapsed ? item.label : ''"
                    @click="navigateTo(item.route)"
                >
                    <i class="pi text-lg" :class="[item.icon]" />
                    <span v-if="!collapsed" class="font-medium">{{ item.label }}</span>
                </button>
            </nav>
        </div>
    </aside>
</template>

<style scoped>
.admin-sidebar {
    min-height: calc(100vh - 64px); /* 64px = top navbar height */
}

/* Smooth width transition */
.admin-sidebar {
    will-change: width;
}
</style>

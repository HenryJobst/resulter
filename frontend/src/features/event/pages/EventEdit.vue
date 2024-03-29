<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { computed } from 'vue'
import { useAuthStore } from '@/features/keycloak/store/auth.store'
import GenericEdit from '@/features/generic/pages/GenericEdit.vue'
import EventForm from '@/features/event/widgets/EventForm.vue'
import { eventService } from '@/features/event/services/event.service'
import type { SportEvent } from '@/features/event/model/sportEvent'

const props = defineProps<{ id: string, locale?: string }>()

const { t } = useI18n()
const authStore = useAuthStore()
const queryKey: string[] = ['events']
const entityLabel: string = 'event'
const editLabel = computed(() => t('messages.edit_entity', { entity: t('labels.event') }))
</script>

<template>
    <GenericEdit
        :entity-service="eventService"
        :query-key="queryKey"
        :entity-id="props.id"
        :entity-label="entityLabel"
        :edit-label="editLabel"
        router-prefix="event"
        :changeable="authStore.isAdmin"
    >
        <template #default="{ formData }">
            <EventForm
                v-if="formData"
                v-model="formData.data"
                :event="formData.data as SportEvent"
                :entity-service="eventService"
                :query-key="queryKey"
            />
        </template>
    </GenericEdit>
</template>

<style scoped></style>

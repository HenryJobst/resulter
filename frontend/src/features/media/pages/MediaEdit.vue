<script setup lang="ts">
import type { Media } from '@/features/media/model/media'
import GenericEdit from '@/features/generic/pages/GenericEdit.vue'
import { useAuthStore } from '@/features/keycloak/store/auth.store'
import { mediaService } from '@/features/media/services/media.service'
import MediaForm from '@/features/media/widgets/MediaForm.vue'
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const props = defineProps<{ id: string, locale?: string }>()

const { t } = useI18n()
const authStore = useAuthStore()
const queryKey: string[] = ['media']
const entityLabel: string = 'media'
const editLabel = computed(() => t('messages.edit_entity', { entity: t('labels.media') }))
</script>

<template>
    <GenericEdit
        :entity-service="mediaService"
        :query-key="queryKey"
        :entity-id="props.id"
        :entity-label="entityLabel"
        :edit-label="editLabel"
        router-prefix="media"
        :visible="authStore.isAdmin"
    >
        <template #default="{ formData }">
            <MediaForm
                v-if="formData"
                v-model="formData.data"
                :media="formData.data as Media"
                :entity-service="mediaService"
                :query-key="queryKey"
            />
        </template>
    </GenericEdit>
</template>

<style scoped></style>

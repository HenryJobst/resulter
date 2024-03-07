<script setup lang="ts">
import { useAuthStore } from '@/features/keycloak/store/auth.store'
import { useI18n } from 'vue-i18n'
import { computed } from 'vue'
import GenericEdit from '@/features/generic/pages/GenericEdit.vue'
import MediaForm from '@/features/media/widgets/MediaForm.vue'
import { mediaService } from '@/features/media/services/media.service'
import type { Media } from '@/features/media/model/media'

const props = defineProps<{ id: string; locale?: string }>()

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
    :router-prefix="'media'"
    :changeable="authStore.isAdmin"
  >
    <template v-slot:default="{ formData }">
      <MediaForm
        :v-if="formData"
        :media="formData as Media"
        :entity-service="mediaService"
        :query-key="queryKey"
        :v-model="formData"
      />
    </template>
  </GenericEdit>
</template>

<style scoped></style>

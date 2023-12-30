<script setup lang="ts">
import Button from 'primevue/button'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Spinner from '@/components/SpinnerComponent.vue'
import ErrorMessage from '@/components/ErrorMessage.vue'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/features/keycloak/store/auth.store'
import { useQuery } from '@tanstack/vue-query'
import { OrganisationService } from '@/features/organisation/services/organisation.service'

const { t } = useI18n() // same as `useI18n({ useScope: 'global' })`

const authStore = useAuthStore()

const reload = () => {}
const deleteOrganisation = (id: number) => {
  console.log(id)
}

const organisationQuery = authStore.isAdmin
  ? useQuery({
      queryKey: ['organisations'],
      queryFn: () => OrganisationService.getAll(t)
    })
  : null

/*
if (!authStore.isAdmin) {
  const router = useRouter()
  await router.replace({ name: 'event-list' }).then()
}*/
</script>

<template v-if="authStore.isAdmin">
  <h1>{{ t('labels.organisation', 2) }}</h1>
  <div class="flex justify-content-between my-4">
    <div class="flex justify-content-start">
      <router-link :to="{ name: 'organisation-new' }" v-if="authStore.isAdmin">
        <Button icon="pi pi-plus" :label="t('labels.new')" outlined></Button>
      </router-link>
    </div>
    <Button
      v-if="authStore.isAdmin"
      icon="pi pi-refresh"
      :label="t('labels.reload')"
      outlined
      severity="secondary"
      @click="reload"
    />
  </div>

  <div>
    <span v-if="organisationQuery?.status.value === 'pending'">
      {{ t('messages.loading') }}
      <Spinner />
    </span>
    <span v-else-if="organisationQuery?.status.value === 'error'">
      <ErrorMessage
        :message="t('messages.error', { message: organisationQuery?.error.value?.message })"
      />
    </span>
    <div v-else-if="organisationQuery?.data" class="card">
      <DataTable :value="organisationQuery?.data.value" class="p-datatable-sm">
        <Column field="id" :header="t('labels.no')" />
        <Column field="name" :header="t('labels.name')" />
        <Column field="shortName" :header="t('labels.short_name')" />
        <Column field="type" :header="t('labels.type')" />
        <Column class="text-right">
          <template #body="slotProps">
            <router-link :to="{ name: 'organisation-edit', params: { id: slotProps.data.id } }">
              <Button
                icon="pi pi-pencil"
                class="mr-2"
                :label="t('labels.edit')"
                outlined
                v-if="authStore.isAdmin"
              />
            </router-link>
            <Button
              icon="pi pi-trash"
              severity="danger"
              outlined
              :label="t('labels.delete')"
              @click="deleteOrganisation(slotProps.data.id)"
              v-if="authStore.isAdmin"
            />
          </template>
        </Column>
      </DataTable>
    </div>
  </div>
</template>

<style scoped></style>

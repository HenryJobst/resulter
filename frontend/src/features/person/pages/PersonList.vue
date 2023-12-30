<script setup lang="ts">
import Button from 'primevue/button'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/features/keycloak/store/auth.store'
import { useQuery } from '@tanstack/vue-query'
import { PersonService } from '@/features/person/services/person.service'
import ErrorMessage from '@/components/ErrorMessage.vue'
import Spinner from '@/components/SpinnerComponent.vue'

const { t } = useI18n() // same as `useI18n({ useScope: 'global' })`

const authStore = useAuthStore()

const reload = () => {}
const deletePerson = (id: number) => {}

const personQuery = authStore.isAdmin
  ? useQuery({
      queryKey: ['persons'],
      queryFn: () => PersonService.getAll(t)
    })
  : null

/*if (!authStore.isAdmin) {
  const router = useRouter()
  await router.replace({ name: 'event-list' }).then()
}
*/
</script>

<template v-if="authStore.isAdmin">
  <h1>{{ t('labels.person', 2) }}</h1>
  <div class="flex justify-content-between my-4">
    <div class="flex justify-content-start">
      <router-link :to="{ name: 'person-new' }" v-if="authStore.isAuthenticated">
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

  <!--ErrorMessage :message="store.errorMessage"></ErrorMessage-->
  <!--Spinner v-if="store.loadingPersons"></Spinner-->

  <div>
    <span v-if="personQuery?.status.value === 'pending'">
      {{ t('messages.loading') }}
      <Spinner />
    </span>
    <span v-else-if="personQuery?.status.value === 'error'">
      <ErrorMessage
        :message="t('messages.error', { message: personQuery?.error.value?.message })"
      />
    </span>
    <div v-else-if="personQuery?.data" class="card">
      <DataTable :value="personQuery?.data.value" class="p-datatable-sm">
        <Column field="id" :header="t('labels.no')" />
        <Column field="name" :header="t('labels.name')" />
        <Column class="text-right">
          <template #body="slotProps">
            <router-link :to="{ name: 'person-edit', params: { id: slotProps.data.id } }">
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
              @click="deletePerson(slotProps.data.id)"
              v-if="authStore.isAdmin"
            />
          </template>
        </Column>
      </DataTable>
    </div>
  </div>
</template>

<style scoped></style>

<script setup lang="ts">
import type { Person } from '@/features/person/model/person'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/features/keycloak/store/auth.store'

const authStore = useAuthStore()

const personSubmitHandler = (person: Omit<Person, 'id'>) => {
  console.log(person)
  //store.createPersonAction(person)
}

const { t } = useI18n() // same as `useI18n({ useScope: 'global' })`

const router = useRouter()
const redirectBack = async () => {
  await router.replace({ name: 'person-list' })
}
</script>

<template>
  <div>
    <h2>{{ t('messages.new_person') }}</h2>

    <!--Spinner v-if="store.loadingPersons"></Spinner-->

    <!--PersonForm v-if="!store.loadingPersons" @person-submit="personSubmitHandler">
      <Button
        type="submit"
        :label="t('labels.create')"
        outlined
        v-if="authStore.isAuthenticated"
      ></Button>
      <Button
        class="ml-2"
        severity="secondary"
        type="reset"
        :label="t('labels.back')"
        outlined
        @click="redirectBack"
      ></Button>
    </PersonForm-->
  </div>
</template>

<style scoped></style>

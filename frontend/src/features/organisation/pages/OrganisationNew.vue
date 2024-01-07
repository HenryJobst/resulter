<script setup lang="ts">
import type { Organisation } from '@/features/organisation/model/organisation'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'

const organisationSubmitHandler = (organisation: Omit<Organisation, 'id'>) => {
  console.log(organisation)
  //store.createOrganisationAction(organisation)
}

const { t } = useI18n() // same as `useI18n({ useScope: 'global' })`

const router = useRouter()
const redirectBack = async () => {
  await router.replace({ name: 'organisation-list' })
}
</script>

<template>
  <div>
    <h2>{{ t('messages.new_organisation') }}</h2>

    <!--Spinner v-if="store.loadingOrganisations"></Spinner-->

    <!--OrganisationForm v-if="!store.loadingOrganisations" @organisation-submit="organisationSubmitHandler">
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
    </OrganisationForm-->
  </div>
</template>

<style scoped></style>

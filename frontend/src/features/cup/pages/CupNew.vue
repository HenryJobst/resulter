<script setup lang="ts">
import type { Cup } from '@/features/cup/model/cup'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/features/keycloak/store/auth.store'

const authStore = useAuthStore()

const cupSubmitHandler = (cup: Omit<Cup, 'id'>) => {
  console.log(cup)
  //store.createCupAction(cup)
}

const { t } = useI18n() // same as `useI18n({ useScope: 'global' })`

const router = useRouter()
const redirectBack = async () => {
  await router.replace({ name: 'cup-list' })
}
</script>

<template>
  <div>
    <h2>{{ t('messages.new_cup') }}</h2>

    <!--Spinner v-if="store.loadingCups"></Spinner-->

    <!--CupForm v-if="!store.loadingCups" @cup-submit="cupSubmitHandler">
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
    </CupForm-->
  </div>
</template>

<style scoped></style>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/features/keycloak/store/auth.store'
import { SUPPORT_LOCALES } from './i18n'

const router = useRouter()
const { t, locale } = useI18n()
const authStore = useAuthStore()

// Verwalten des aktuellen Lokalisierungswertes
const currentLocale = ref(locale.value)

// Synchronisation, um die Lokalisierung vom Router-Pfad zu ändern
watch(router.currentRoute, (route) => {
  currentLocale.value = route.params.locale as string
})

// Änderung der Lokalisierung, gehen Sie zur Lokalisierungsroute
watch(currentLocale, (val) => {
  router.push({
    name: router.currentRoute.value.name!,
    params: { locale: val }
  })
})
</script>

<template>
  <div class="flex flex-col h-full">
    <header class="flex justify-between items-center bg-gray-200 p-4">
      <!-- Logo und Menüeinträge -->
      <div class="flex items-center">
        <img
          :alt="t('labels.logo')"
          class="mr-6"
          src="@/assets/Logo_Resulter.png"
          :width="'60'"
          :height="'60'"
        />
        <nav>
          <ul class="flex text-2xl">
            <li class="mr-4">
              <router-link :to="{ name: 'start-page', params: { locale } }">
                {{ t('navigations.start') }}
              </router-link>
            </li>
            <li class="mr-4">
              <router-link :to="{ name: 'event-list', params: { locale } }">
                {{ t('navigations.events') }}
              </router-link>
            </li>
            <li class="mr-4">
              <router-link :to="{ name: 'about-page', params: { locale } }">
                {{ t('navigations.about') }}
              </router-link>
            </li>
            <!-- Weitere Menüeinträge hier hinzufügen -->
          </ul>
        </nav>
      </div>
      <!-- Sprachauswahl -->
      <div class="flex flex-row flex-wrap">
        <a href="#" class="text-xl" v-if="!authStore.isAuthenticated" @click="authStore.login()">
          {{ t('navigations.login') }}
        </a>
        <a href="#" class="text-xl" v-if="authStore.isAuthenticated" @click="authStore.logout()">
          {{ t('navigations.logout') }}
        </a>
        <div class="flex flex-row flex-nowrap ml-4">
          <label class="mr-2 mt-1" for="locale-select">{{ t('labels.language') }}</label>
          <select id="locale-select" class="form-select" v-model="currentLocale">
            <option
              v-for="optionLocale in SUPPORT_LOCALES"
              :key="optionLocale"
              :value="optionLocale"
            >
              {{ optionLocale }}
            </option>
          </select>
        </div>
      </div>
    </header>

    <div class="flex-1 m-4">
      <router-view />
    </div>
  </div>
</template>

<style scoped></style>

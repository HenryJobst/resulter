<script setup lang="ts">
import { onMounted, type Ref, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/features/keycloak/store/auth.store'
import { SUPPORT_LOCALES } from './i18n'
import { VueQueryDevtools } from '@tanstack/vue-query-devtools'
import Toast from 'primevue/toast'
import { type PrimeVueLocaleOptions, usePrimeVue } from 'primevue/config'

const router = useRouter()
const { t, locale } = useI18n()
const authStore = useAuthStore()

const currentLocale = ref(locale.value)
const fullUrl = ref('')

interface LocaleMessages {
  [key: string]: PrimeVueLocaleOptions
}

const primeVueLocales: Ref<LocaleMessages> = ref({})

const primevue = usePrimeVue()

onMounted(() => {
  fullUrl.value = window.location.href
})

// Synchronisation, um die Lokalisierung vom Router-Pfad zu ändern
watch(router.currentRoute, (route) => {
  currentLocale.value = route.params.locale as string
  fullUrl.value = window.location.href
})

const switchPrimeVueLocale = async (locale: string) => {
  if (!locale) return
  // noinspection SpellCheckingInspection
  if (!primeVueLocales.value[locale]) {
    try {
      const filename = `./locales/primevue/${locale}.json`
      const module = await import(filename)
      primeVueLocales.value[locale] = module['default'][locale]
    } catch (error) {
      console.error('Failed to load locale:', error)
    }
  }

  const primeVueLocale = primeVueLocales.value[locale]
  Object.assign(primevue.config.locale!, primeVueLocale)

  /*
    primevue.config.locale!.startsWith = primeVueLocale.startsWith
    primevue.config.locale!.contains = primeVueLocale.contains
    primevue.config.locale!.notContains = primeVueLocale.notContains
    primevue.config.locale!.endsWith = primeVueLocale.endsWith
    primevue.config.locale!.equals = primeVueLocale.equals
    primevue.config.locale!.notEquals = primeVueLocale.notEquals
    primevue.config.locale!.noFilter = primeVueLocale.noFilter
    //primevue.config.locale!.filter = primeVueLocale.filter
    primevue.config.locale!.lt = primeVueLocale.lt
    primevue.config.locale!.lte = primeVueLocale.lte
    primevue.config.locale!.gt = primeVueLocale.gt
    primevue.config.locale!.gte = primeVueLocale.gte
    primevue.config.locale!.dateIs = primeVueLocale.dateIs
    primevue.config.locale!.dateIsNot = primeVueLocale.dateIsNot
    primevue.config.locale!.dateBefore = primeVueLocale.dateBefore
    primevue.config.locale!.dateAfter = primeVueLocale.dateAfter
    //primevue.config.locale!.custom = primeVueLocale.custom
    primevue.config.locale!.clear = primeVueLocale.clear
    primevue.config.locale!.apply = primeVueLocale.apply
    primevue.config.locale!.matchAll = primeVueLocale.matchAll
    primevue.config.locale!.matchAny = primeVueLocale.matchAny
    primevue.config.locale!.addRule = primeVueLocale.addRule
    primevue.config.locale!.removeRule = primeVueLocale.removeRule
    primevue.config.locale!.accept = primeVueLocale.accept
    primevue.config.locale!.reject = primeVueLocale.reject
    primevue.config.locale!.choose = primeVueLocale.choose
    primevue.config.locale!.upload = primeVueLocale.upload
    primevue.config.locale!.cancel = primeVueLocale.cancel
    primevue.config.locale!.completed = primeVueLocale.completed
    primevue.config.locale!.pending = primeVueLocale.pending
    primevue.config.locale!.fileSizeTypes = primeVueLocale.fileSizeTypes
    primevue.config.locale!.dayNames = primeVueLocale.dayNames
    primevue.config.locale!.dayNamesShort = primeVueLocale.dayNamesShort
    primevue.config.locale!.dayNamesMin = primeVueLocale.dayNamesMin
    primevue.config.locale!.monthNames = primeVueLocale.monthNames
    primevue.config.locale!.monthNamesShort = primeVueLocale.monthNamesShort
    primevue.config.locale!.chooseYear = primeVueLocale.chooseYear
    primevue.config.locale!.chooseMonth = primeVueLocale.chooseMonth
    primevue.config.locale!.chooseDate = primeVueLocale.chooseDate
    primevue.config.locale!.prevDecade = primeVueLocale.prevDecade
    primevue.config.locale!.nextDecade = primeVueLocale.nextDecade
    primevue.config.locale!.prevYear = primeVueLocale.prevYear
    primevue.config.locale!.nextYear = primeVueLocale.nextYear
    primevue.config.locale!.prevMonth = primeVueLocale.prevMonth
    primevue.config.locale!.nextMonth = primeVueLocale.nextMonth
    primevue.config.locale!.prevHour = primeVueLocale.prevHour
    primevue.config.locale!.nextHour = primeVueLocale.nextHour
    primevue.config.locale!.prevMinute = primeVueLocale.prevMinute
    primevue.config.locale!.nextMinute = primeVueLocale.nextMinute
    primevue.config.locale!.prevSecond = primeVueLocale.prevSecond
    primevue.config.locale!.nextSecond = primeVueLocale.nextSecond
    primevue.config.locale!.am = primeVueLocale.am
    primevue.config.locale!.pm = primeVueLocale.pm
    primevue.config.locale!.today = primeVueLocale.today
    //primevue.config.locale!.now = primeVueLocale.now
    primevue.config.locale!.weekHeader = primeVueLocale.weekHeader
    primevue.config.locale!.firstDayOfWeek = primeVueLocale.firstDayOfWeek
    primevue.config.locale!.showMonthAfterYear = primeVueLocale.showMonthAfterYear
    primevue.config.locale!.dateFormat = primeVueLocale.dateFormat
    primevue.config.locale!.weak = primeVueLocale.weak
    primevue.config.locale!.medium = primeVueLocale.medium
    primevue.config.locale!.strong = primeVueLocale.strong
    primevue.config.locale!.passwordPrompt = primeVueLocale.passwordPrompt
    primevue.config.locale!.emptyFilterMessage = primeVueLocale.emptyFilterMessage
    primevue.config.locale!.searchMessage = primeVueLocale.searchMessage
    primevue.config.locale!.selectionMessage = primeVueLocale.selectionMessage
    primevue.config.locale!.emptySelectionMessage = primeVueLocale.emptySelectionMessage
    primevue.config.locale!.emptySearchMessage = primeVueLocale.emptySearchMessage
    primevue.config.locale!.emptyMessage = primeVueLocale.emptyMessage
    */
}

watch(currentLocale, (val) => {
  router.push({
    name: router.currentRoute.value.name!,
    params: { locale: val }
  })
  switchPrimeVueLocale(val)
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
          <ul class="flex flex-row flex-wrap text-2xl">
            <li class="mr-4">
              <router-link :to="{ name: 'start-page', params: { locale } }">
                {{ t('navigations.start') }}
              </router-link>
            </li>
            <li class="mr-4">
              <router-link :to="{ name: 'cup-list', params: { locale } }">
                {{ t('navigations.cups') }}
              </router-link>
            </li>
            <li class="mr-4">
              <router-link :to="{ name: 'event-list', params: { locale } }">
                {{ t('navigations.events') }}
              </router-link>
            </li>
            <li class="mr-4" v-if="authStore.isAdmin">
              <router-link :to="{ name: 'organisation-list', params: { locale } }">
                {{ t('navigations.organisations') }}
              </router-link>
            </li>
            <li class="mr-4" v-if="authStore.isAdmin">
              <router-link :to="{ name: 'person-list', params: { locale } }">
                {{ t('navigations.persons') }}
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
        <a
          href="#"
          class="text-xl mr-4"
          v-if="!authStore.isAuthenticated"
          @click="authStore.login(fullUrl, currentLocale)"
        >
          {{ t('navigations.login') }}
        </a>
        <a
          href="#"
          class="text-xl mr-4"
          v-if="authStore.isAuthenticated"
          @click="authStore.logout()"
        >
          {{ t('navigations.logout') }}
        </a>
        <div class="flex flex-row flex-nowrap">
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
      <Toast />
      <router-view />
      <VueQueryDevtools />
    </div>

    <footer class="flex justify-between items-center bg-gray-200 p-4">
      <div class="flex items-center">
        <div v-if="authStore.authenticated" class="ml-4">
          {{ t('labels.login_user', { username: authStore.user.username }) }}
        </div>
      </div>
    </footer>
  </div>
</template>

<style scoped></style>

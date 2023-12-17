<script lang="ts">
import { defineComponent, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { SUPPORT_LOCALES } from './i18n'

export default defineComponent({
  name: 'App',
  setup() {
    const router = useRouter()
    const { t, locale } = useI18n() // same as `useI18n({ useScope: 'global' })`

    /**
     * select locale value for language select form
     *
     * If you use the vue-i18n composer `locale` property directly, it will be re-rendering component when this property is changed,
     * before dynamic import was used to asynchronously load and apply locale messages
     * To avoid this, use the another locale reactive value.
     */
    const currentLocale = ref(locale.value)

    // sync to switch locale from router locale path
    watch(router.currentRoute, (route) => {
      currentLocale.value = route.params.locale as string
    })

    /**
     * when change the locale, go to locale route
     *
     * when the changes are detected, load the locale message and set the language via vue-router navigation guard.
     * change the vue-i18n locale too.
     */
    watch(currentLocale, (val) => {
      router.push({
        name: router.currentRoute.value.name!,
        params: { locale: val }
      })
    })

    return { t, locale, currentLocale, supportLocales: SUPPORT_LOCALES }
  }
})
</script>

<template>
  <div class="flex flex-col h-full">
    <header class="flex justify-between items-center bg-gray-200 p-4">
      <!-- Logo und Menüeinträge -->
      <div class="flex items-center">
        <img alt="Logo" class="mr-6" src="@/assets/Logo_Resulter.png" width="60" height="60" />
        <nav>
          <ul class="flex text-2xl">
            <li class="mr-4">
              <router-link :to="{ name: 'start-page', params: { locale } }">
                {{ t('navigations.start') }}
              </router-link>
            </li>
            <li class="mr-4">
              <router-link :to="{ name: 'event-index', params: { locale } }">
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
      <div>
        <label class="mr-2" for="locale-select">{{ t('labels.language') }}</label>
        <select id="locale-select" class="form-select" v-model="currentLocale">
          <option v-for="optionLocale in supportLocales" :key="optionLocale" :value="optionLocale">
            {{ optionLocale }}
          </option>
        </select>
      </div>
    </header>

    <!-- Body -->
    <body>
      <div class="flex-1 m-4">
        <router-view />
      </div>
    </body>
  </div>
  <header></header>
  <body></body>
</template>

<style scoped></style>
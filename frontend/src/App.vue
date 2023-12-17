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
  <header>
    <div class="wrapper">
      <nav class="flex flex-row md:flex-nowrap lg:flex-nowrap xl:flex-nowrap">
        <img
          alt="Resulter Logo"
          class="logo"
          src="@/assets/Logo_Resulter.png"
          width="50"
          height="50"
        />
        <div class="navigation">
          <router-link :to="{ name: 'start-page', params: { locale } }">
            {{ t('navigations.start') }}
          </router-link>
          <router-link :to="{ name: 'event-index', params: { locale } }">
            {{ t('navigations.events') }}
          </router-link>
          <router-link :to="{ name: 'about-page', params: { locale } }">
            {{ t('navigations.about') }}
          </router-link>
        </div>
        <form class="language">
          <label for="locale-select">{{ t('labels.language') }}</label>
          <select id="locale-select" v-model="currentLocale">
            <option
              v-for="optionLocale in supportLocales"
              :key="optionLocale"
              :value="optionLocale"
            >
              {{ optionLocale }}
            </option>
          </select>
        </form>
      </nav>
    </div>
  </header>
  <body>
    <router-view />
  </body>
</template>

<style scoped>
header {
  line-height: 1.5;
  max-height: 100vh;
}

.logo {
  display: block;
  margin: 0 auto 2rem;
}

nav {
  width: 100%;
  font-size: 16px;
  text-align: center;
  margin-top: 2rem;
}

nav a.router-link-exact-active {
  color: var(--color-text);
}

nav a.router-link-exact-active:hover {
  background-color: transparent;
}

nav a {
  display: inline-block;
  padding: 0 1rem;
  border-left: 1px solid var(--color-border);
}

nav a:first-of-type {
  border: 0;
}

@media (min-width: 768px) {
  header {
    display: flex;
    place-items: center;
    padding-right: calc(var(--section-gap) / 2);
  }

  .logo {
    margin: 0 2rem 0 0;
  }

  header .wrapper {
    display: flex;
    place-items: flex-start;
    flex-wrap: wrap;
  }
}

nav {
  display: inline-flex;
}

.navigation {
  margin-right: 1rem;
}

.language label {
  margin-right: 1rem;
}
</style>
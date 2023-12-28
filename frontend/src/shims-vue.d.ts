import 'vue'
import { PrimeVueConfiguration } from 'primevue/config'

declare module '*.vue' {
  import { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

declare module '@vue/runtime-core' {
  interface ComponentCustomProperties {
    $primevue: PrimeVueConfiguration
  }
}

import 'vue'
import type { PrimeVueConfiguration } from 'primevue/config'

declare module '*.vue' {
    import type { DefineComponent } from 'vue'

    const component: DefineComponent<NonNullable<unknown>, NonNullable<unknown>, any>
    export default component
}

declare module '@vue/runtime-core' {
    interface ComponentCustomProperties {
        $primevue: PrimeVueConfiguration
    }
}

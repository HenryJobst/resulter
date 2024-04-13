import { defaultConfig, plugin } from '@formkit/vue'
import { primeInputs } from '@sfxcode/formkit-primevue'

export function formkitInstall(app: any, i18n: any) {
    app.use(plugin, defaultConfig({
        inputs: primeInputs,
        locale: i18n.global.locale.value,
    }))
}

// tests/unit.setup.ts
import { config } from '@vue/test-utils'
import { createI18n } from 'vue-i18n'

const i18n = createI18n({ locale: 'en' })
config.global.plugins = [i18n]

config.global.mocks = {
    $t: (tKey: string) => tKey,
}

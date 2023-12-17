/**
 * global type definitions
 */

import en from './locales/en.json'

type MessageSchema = typeof en

declare module 'vue-i18n' {
  // define the locale messages schema
  export interface DefineLocaleMessage extends MessageSchema {}
}

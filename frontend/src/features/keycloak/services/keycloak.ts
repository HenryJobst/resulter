import type { KeycloakConfig } from 'keycloak-js'
import Keycloak from 'keycloak-js'

function getOptions(): KeycloakConfig {
    return {
        url: import.meta.env.VITE_KEYCLOAK_URL as string,
        clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID as string,
        realm: import.meta.env.VITE_KEYCLOAK_REALM as string,
    }
}

const options: KeycloakConfig = getOptions()

function getKeycloak() {
    if (!options.url)
        console.log('keycloak.ts:getKeycloak:Missing options.url')

    if (!options.clientId)
        console.log('keycloak.ts:getKeycloak:Missing options.clientId')

    if (!options.realm)
        console.log('keycloak.ts:getKeycloak:Missing options.realm')

    return new Keycloak(options)
}

const keycloak = getKeycloak()

let authenticated: boolean = false
let store: any = null // Define the store type based on your store structure

async function init(onInitCallback: () => void): Promise<void> {
    try {
        authenticated = await keycloak.init({
            flow: 'hybrid',
            onLoad: 'check-sso',
            silentCheckSsoRedirectUri: `${location.origin}/silent-check-sso.html`,
        })
    }
    finally {
        onInitCallback()
    }
}

async function initStore(storeInstance: any): Promise<void> {
    // Define the storeInstance type based on your store structure
    store = storeInstance
    store.initOauth(keycloak)

    if (!authenticated) {
        // Implement a more integrated user experience for unauthenticated users
        // alert('Not authenticated')
    }
}

async function login(url?: string, locale?: string): Promise<Keycloak | undefined> {
    keycloak.login({ redirectUri: url, locale }).then()
    return keycloak
}

async function logout(url?: string): Promise<void> {
    keycloak.logout({ redirectUri: url }).then()
}

async function refreshToken(): Promise<Keycloak | undefined> {
    const minValidity: number = Math.floor((keycloak.tokenParsed?.exp ?? 0) - Date.now() / 1000)
    await keycloak.updateToken(minValidity > 0 ? minValidity : 30)
    return keycloak
}

const KeycloakService = {
    callInit: init,
    callInitStore: initStore,
    callLogin: login,
    callLogout: logout,
    callTokenRefresh: refreshToken,
}

export default KeycloakService

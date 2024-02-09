import Keycloak from 'keycloak-js'

interface KeycloakOptions {
  url: string
  clientId: string
  realm: string
}

function getOptions() {
  console.log(
    import.meta.env.VITE_KEYCLOAK_URL,
    import.meta.env.VITE_KEYCLOAK_CLIENT_ID,
    import.meta.env.VITE_KEYCLOAK_REALM
  )
  return {
    url: import.meta.env.VITE_KEYCLOAK_URL as string,
    clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID as string,
    realm: import.meta.env.VITE_KEYCLOAK_REALM as string
  }
}

const options: KeycloakOptions = getOptions()

function getKeycloak() {
  if (!options.url) {
    console.log('Missing options.url')
  }
  if (!options.clientId) {
    console.log('Missing options.clientId')
  }
  if (!options.realm) {
    console.log('Missing options.realm')
  }
  return new Keycloak(options)
}

const keycloak = getKeycloak()

let authenticated: boolean = false
let store: any = null // Define the store type based on your store structure

// Custom error handling function
function handleError(error: Error, message: string): void {
  console.error(message)
  console.error(error)
  console.error(options)
  // Implement additional logging or user feedback mechanisms here
}

async function init(onInitCallback: () => void): Promise<void> {
  try {
    authenticated = await keycloak.init({
      flow: 'hybrid',
      onLoad: 'check-sso',
      silentCheckSsoRedirectUri: `${location.origin}/silent-check-sso.html`
    })
    onInitCallback()
  } catch (error) {
    handleError(error as Error, 'Keycloak init failed')
  }
}

async function initStore(storeInstance: any): Promise<void> {
  // Define the storeInstance type based on your store structure
  try {
    store = storeInstance
    store.initOauth(keycloak)

    if (!authenticated) {
      // Implement a more integrated user experience for unauthenticated users
      // alert('Not authenticated')
    }
  } catch (error) {
    handleError(error as Error, 'Keycloak init failed')
  }
}

async function login(url?: string, locale?: string): Promise<Keycloak | undefined> {
  try {
    keycloak.login({ redirectUri: url, locale: locale }).then()
    return keycloak
  } catch (error) {
    handleError(error as Error, 'Failed to refresh token')
  }
}

async function logout(url?: string): Promise<void> {
  keycloak.logout({ redirectUri: url }).then()
}

async function refreshToken(): Promise<Keycloak | undefined> {
  try {
    const minValidity: number = Math.floor((keycloak.tokenParsed?.exp ?? 0) - Date.now() / 1000)
    await keycloak.updateToken(minValidity > 0 ? minValidity : 30)
    return keycloak
  } catch (error) {
    handleError(error as Error, 'Failed to refresh token')
  }
}

const KeycloakService = {
  callInit: init,
  callInitStore: initStore,
  callLogin: login,
  callLogout: logout,
  callTokenRefresh: refreshToken
}

export default KeycloakService

import Keycloak from 'keycloak-js'

interface KeycloakOptions {
  url: string
  clientId: string
  realm: string
}

const options: KeycloakOptions = {
  url: import.meta.env.VITE_KEYCLOAK_URL as string,
  clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID as string,
  realm: import.meta.env.VITE_KEYCLOAK_REALM as string
}

const keycloak = new Keycloak(options)

let authenticated: boolean = false
let store: any = null // Define the store type based on your store structure

// Custom error handling function
function handleError(error: Error, message: string): void {
  console.error(message)
  console.error(error)
  // Implement additional logging or user feedback mechanisms here
}

async function init(onInitCallback: () => void): Promise<void> {
  try {
    authenticated = await keycloak.init({
      /*onLoad: 'login-required'*/
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

function logout(url: string): void {
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
  callLogout: logout,
  callTokenRefresh: refreshToken
}

export default KeycloakService

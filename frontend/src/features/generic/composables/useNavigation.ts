import { useRouter } from 'vue-router'

export function useNavigation() {
    const router = useRouter()

    function navigateTo(route_name: string) {
        router.replace({ name: `${route_name}` }).then()
    }

    return { navigateTo }
}

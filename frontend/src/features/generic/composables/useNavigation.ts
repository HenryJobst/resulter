import { useRoute, useRouter } from 'vue-router'

export function useNavigation() {
    const router = useRouter()
    const route = useRoute()

    function navigateTo(route_name: string) {
        router.replace({
            name: `${route_name}`,
            params: { locale: route.params.locale as string },
        }).then()
    }

    return { navigateTo }
}

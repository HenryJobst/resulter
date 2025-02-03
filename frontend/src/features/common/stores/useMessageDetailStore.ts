import { defineStore } from 'pinia'

export const useMessageDetailStore = defineStore('messageDetailStore', {
    state: () => ({
        visible: false as boolean,
        currentDetails: '' as string,
    }),
    getters: {
        isVisible: (state): boolean => state.visible,
        getDetails: state => (): string => {
            return state.currentDetails
        },
    },
    actions: {
        hide(): void {
            this.currentDetails = ''
            this.visible = false
        },
        show(details: string): void {
            this.currentDetails = details
            this.visible = true
        },
    },
})

import { describe, expect, it } from 'vitest'
import { nextTick, ref } from 'vue'
import { useLayoutHistory } from '@/features/certificate/model/useLayoutHistory'

function setup() {
    const value = ref('a')
    const key = ref<number | undefined>(1)
    let time = 0
    const history = useLayoutHistory(() => value.value, (v) => {
        value.value = v
    }, () => key.value, () => time)
    const edit = async (v: string, at: number) => {
        time = at
        value.value = v
        await nextTick()
    }
    return { value, key, history, edit }
}

describe('useLayoutHistory', () => {
    it('undoes and redoes separate edits', async () => {
        const { value, history, edit } = setup()
        await edit('b', 1000)
        await edit('c', 3000)
        history.undo()
        expect(value.value).toBe('b')
        history.undo()
        expect(value.value).toBe('a')
        expect(history.canUndo.value).toBe(false)
        history.redo()
        await nextTick()
        expect(value.value).toBe('b')
        expect(history.canRedo.value).toBe(true)
    })

    it('merges quick edits (a drag) into one step', async () => {
        const { value, history, edit } = setup()
        await edit('b', 1000)
        await edit('c', 1100)
        await edit('d', 1200)
        history.undo()
        expect(value.value).toBe('a')
        expect(history.canUndo.value).toBe(false)
    })

    it('forgets the redo steps after a new edit and starts over for another certificate', async () => {
        const { value, key, history, edit } = setup()
        await edit('b', 1000)
        history.undo()
        await nextTick()
        await edit('x', 3000)
        expect(history.canRedo.value).toBe(false)
        key.value = 2
        value.value = 'loaded'
        await nextTick()
        expect(history.canUndo.value).toBe(false)
    })
})

import assert from 'node:assert/strict'
import { test } from 'node:test'
import { anchors, blocks, checkRepository, compare } from './docs-parity.mjs'

test('a missing table row is reported', () => {
    const report = compare('## T\n\n| a | b |\n| - | - |\n| 1 | 2 |\n| 3 | 4 |\n', '## T\n\n| a | b |\n| - | - |\n| 1 | 2 |\n')
    assert.ok(report.problems.some(p => p.includes('table rows')), report.problems.join())
})

test('a missing list item is reported', () => {
    const report = compare('## L\n\n- one\n- two\n- three\n', '## L\n\n- one\n- two\n')
    assert.ok(report.problems.some(p => p.includes('list items')))
})

test('a missing link target is reported', () => {
    const report = compare('## L\n\nsee [a](a.md) and [b](b.md)\n', '## L\n\nsiehe [a](a.de.md)\n')
    assert.ok(report.problems.some(p => p.includes('link targets')))
})

test('a different number of headings is reported', () => {
    const report = compare('# A\n\n## B\n', '# A\n')
    assert.deepEqual(report.problems, ['3 headings against 2'])
})

test('a code block that lost a line is reported, a moved block is not', () => {
    assert.ok(compare('# A\n\n```\nx\ny\n```\n', '# A\n\n```\nx\n```\n').problems.some(p => p.includes('code blocks')))
    assert.deepEqual(compare('# A\n\n## B\n\n```\nx\n```\n\n## C\n', '# A\n\n## B\n\n## C\n\n```\nx\n```\n').problems, [])
})

test('code in a block is not counted as a table, list or link', () => {
    const report = compare('# A\n\n```\n- item\n| a |\n[x](y)\n```\n', '# A\n\n```\n- Punkt\n| a |\n[x](y)\n```\n')
    assert.deepEqual(report.problems, [])
    assert.deepEqual(blocks('```\na\n```\n```\nb\nc\n```'), [1, 2])
})

test('anchors follow the GitHub rules', () => {
    const set = anchors('# Title\n\n## ❌ Broken thing\n\n## Same\n\n## Same\n')
    assert.ok(set.has('-broken-thing') && set.has('same') && set.has('same-1'))
})

test('a document without its other language is reported', () => {
    const files = { 'a.md': '# A\n\n> [Deutsch](a.de.md) · **English**\n' }
    const result = checkRepository(Object.keys(files), f => files[f], () => true)
    assert.ok(result.problems.some(p => p.includes('no German version')))
})

test('links point to the version of the own language', () => {
    const files = {
        'a.md': '# A\n\n> [Deutsch](a.de.md) · **English**\n\nsee [b](b.md)\n',
        'a.de.md': '# A\n\n> **Deutsch** · [English](a.md)\n\nsiehe [b](b.md)\n',
        'b.md': '# B\n\n> [Deutsch](b.de.md) · **English**\n',
        'b.de.md': '# B\n\n> **Deutsch** · [English](b.md)\n',
    }
    const result = checkRepository(Object.keys(files), f => files[f], f => f in files)
    assert.ok(result.problems.some(p => p.startsWith('a.de.md') && p.includes('links to the English')), result.problems.join('\n'))
    assert.equal(result.problems.length, 1)
})

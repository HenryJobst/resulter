#!/usr/bin/env node
/**
 * Checks that every Markdown document exists in English (`name.md`) and German (`name.de.md`)
 * and that both versions have the same shape (docs/adr/0021).
 *
 * The check cannot read meaning. It counts what a reader can count: headings, tables and their
 * rows, list items and link targets per section, and the lines of every code block per document.
 * A section that lost one of them renders a different page, and that is what it reports.
 * Not checked: word count, the wording of headings, prose and meaning.
 *
 * Usage: node scripts/docs-parity.mjs        (exit code 1 on problems)
 */
import { execFileSync } from 'node:child_process'
import { existsSync, readFileSync } from 'node:fs'
import { dirname, join, normalize, relative, resolve } from 'node:path'
import process from 'node:process'
import { fileURLToPath } from 'node:url'

const root = resolve(dirname(fileURLToPath(import.meta.url)), '..')
// documents that are intentionally single-language (repository-relative paths); keep this empty
export const IGNORED = new Set()

const isGerman = name => name.endsWith('.de.md')
export const siblingOf = name => (isGerman(name) ? `${name.slice(0, -6)}.md` : `${name.slice(0, -3)}.de.md`)

// ---- shape of a document ----

function isFence(line) {
    return line.trimStart().startsWith('```')
}

/** One heading and everything below it, up to the next heading. */
export function sections(text) {
    const result = [{ heading: '', body: [] }]
    let fence = false
    for (const line of text.split('\n')) {
        if (isFence(line)) {
            fence = !fence
            result.at(-1).body.push(line)
        }
        else if (!fence && /^#{1,6} /.test(line)) {
            result.push({ heading: line.trim(), body: [] })
        }
        else {
            result.at(-1).body.push(line)
        }
    }
    return result
}

function listMarker(trimmed, lastOrdered) {
    if (/^[-*+] /.test(trimmed)) {
        return { kind: 'bullet', number: 0 }
    }
    const m = /^(\d+)[.)] /.exec(trimmed)
    if (!m) {
        return { kind: 'prose', number: lastOrdered }
    }
    const number = Number(m[1])
    // a wrapped sentence that happens to start with "5)" is no item
    return number === 1 || number > lastOrdered ? { kind: 'ordered', number } : { kind: 'prose', number: lastOrdered }
}

export function shape(lines) {
    const s = { tables: 0, tableRows: [], listItems: 0, links: 0, inlineCode: 0 }
    let fence = false
    let inTable = false
    let lastOrdered = 0
    for (const line of lines) {
        if (isFence(line)) {
            fence = !fence
            continue
        }
        if (fence) {
            continue
        }
        const trimmed = line.trimStart()
        if (trimmed.startsWith('|')) {
            if (inTable) {
                s.tableRows[s.tableRows.length - 1]++
            }
            else {
                s.tables++
                s.tableRows.push(1)
                inTable = true
            }
            continue
        }
        inTable = false
        s.links += line.split('](').length - 1
        s.inlineCode += Math.floor((line.split('`').length - 1) / 2)
        const marker = listMarker(trimmed, lastOrdered)
        if (marker.kind === 'bullet') {
            s.listItems++
            lastOrdered = 0
        }
        else if (marker.kind === 'ordered') {
            s.listItems++
            lastOrdered = marker.number
        }
    }
    return s
}

/** Line counts of every code block, in document order. */
export function blocks(text) {
    const lengths = []
    let fence = false
    for (const line of text.split('\n')) {
        if (isFence(line)) {
            fence = !fence
            if (fence) {
                lengths.push(0)
            }
        }
        else if (fence) {
            lengths[lengths.length - 1]++
        }
    }
    return lengths
}

const same = (a, b) => a.length === b.length && a.every((v, i) => v === b[i])

/** Compares two versions of a document; returns the problems and advisory hints. */
export function compare(source, translated) {
    const report = { problems: [], hints: [] }
    const left = sections(source)
    const right = sections(translated)
    if (left.length !== right.length) {
        report.problems.push(`${left.length} headings against ${right.length}`)
        return report
    }
    left.forEach((ours, i) => {
        const a = shape(ours.body)
        const b = shape(right[i].body)
        const where = `section ${i} \`${ours.heading}\``
        if (a.tables !== b.tables) {
            report.problems.push(`${where}: ${a.tables} tables against ${b.tables}`)
        }
        if (!same(a.tableRows, b.tableRows)) {
            report.problems.push(`${where}: table rows [${a.tableRows}] against [${b.tableRows}]`)
        }
        if (a.listItems !== b.listItems) {
            report.problems.push(`${where}: ${a.listItems} list items against ${b.listItems}`)
        }
        if (a.links !== b.links) {
            report.problems.push(`${where}: ${a.links} link targets against ${b.links}`)
        }
        if (a.inlineCode !== b.inlineCode) {
            report.hints.push(`${where}: ${a.inlineCode} inline code spans against ${b.inlineCode}`)
        }
    })
    // a block may sit under a neighbouring heading in one language, so compare over the document
    const x = blocks(source)
    const y = blocks(translated)
    if (!same(x, y) && !same([...x].sort((p, q) => p - q), [...y].sort((p, q) => p - q))) {
        report.problems.push(`code blocks with [${x}] lines against [${y}], counted over the document`)
    }
    return report
}

// ---- links ----

/** GitHub anchor names of the headings of a document (duplicates get -1, -2, ...). */
export function anchors(text) {
    const seen = new Map()
    const result = new Set()
    for (const section of sections(text).slice(1)) {
        const base = section.heading
            .replace(/^#+\s+/, '')
            .replace(/[`*_~]/g, '')
            .toLowerCase()
            .replace(/[^\p{L}\p{N}_\s-]/gu, '')
            .replace(/\s/g, '-')
        const n = seen.get(base) ?? 0
        seen.set(base, n + 1)
        result.add(n === 0 ? base : `${base}-${n}`)
    }
    return result
}

function links(text) {
    const found = []
    let fence = false
    text.split('\n').forEach((line, i) => {
        if (isFence(line)) {
            fence = !fence
        }
        if (fence) {
            return
        }
        const noCode = line.replace(/`[^`]*`/g, '')
        for (const m of noCode.matchAll(/\]\(([^)\s]*)\)/g)) {
            found.push({ target: m[1], line: i + 1, text: line })
        }
    })
    return found
}

// ---- the repository ----

function markdownFiles() {
    const out = execFileSync('git', ['ls-files', '-co', '--exclude-standard', '*.md'], { cwd: root, encoding: 'utf8' })
    return out.split('\n').filter(f => f && !f.includes('node_modules') && !IGNORED.has(f))
}

export function checkRepository(files = markdownFiles(), read = f => readFileSync(join(root, f), 'utf8'), exists = f => existsSync(join(root, f))) {
    const problems = []
    const hints = []
    const known = new Set(files)
    const bilingual = new Set(files.filter(f => known.has(siblingOf(f))).map(f => (isGerman(f) ? siblingOf(f) : f)))

    for (const file of files) {
        const text = read(file)
        const sibling = siblingOf(file)
        const german = isGerman(file)

        if (!known.has(sibling)) {
            problems.push(`${file}: no ${german ? 'English' : 'German'} version \`${sibling}\``)
        }
        else {
            // the language switch near the top links to the sibling
            const top = text.split('\n').slice(0, 8).join('\n')
            const name = sibling.split('/').at(-1)
            if (!top.includes(`](${name})`)) {
                problems.push(`${file}: no language switch to \`${name}\` in the first lines`)
            }
            if (!german) {
                const report = compare(text, read(sibling))
                problems.push(...report.problems.map(p => `${file} / ${sibling}: ${p}`))
                hints.push(...report.hints.map(h => `${file} / ${sibling}: ${h}`))
            }
        }

        // links: targets exist, anchors exist, and a version links to the version of its own language
        for (const { target, line, text: lineText } of links(text)) {
            if (/^(?:https?:|mailto:|#)/.test(target) && !target.startsWith('#')) {
                continue
            }
            const [path, anchor] = target.split('#')
            const resolved = path ? normalize(join(dirname(file), path)) : file
            if (path && !exists(resolved)) {
                problems.push(`${file}:${line}: link target \`${path}\` does not exist`)
                continue
            }
            if (anchor && resolved.endsWith('.md') && known.has(resolved) && !anchors(read(resolved)).has(anchor)) {
                problems.push(`${file}:${line}: anchor \`#${anchor}\` not found in \`${resolved}\``)
            }
            // a language switch or a line that names both languages links to the other version on purpose
            const isSwitch = /English|Englisch|Deutsch|German|·/.test(lineText)
            if (!isSwitch && resolved.endsWith('.md') && bilingual.has(isGerman(resolved) ? siblingOf(resolved) : resolved)) {
                if (german && !isGerman(resolved)) {
                    problems.push(`${file}:${line}: German document links to the English \`${relative('.', resolved)}\``)
                }
                if (!german && isGerman(resolved)) {
                    problems.push(`${file}:${line}: English document links to the German \`${relative('.', resolved)}\``)
                }
            }
        }
    }
    return { problems, hints, documents: files.length }
}

if (process.argv[1] && resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
    const { problems, hints, documents } = checkRepository()
    for (const hint of hints) {
        console.log(`hint: ${hint}`)
    }
    for (const problem of problems) {
        console.error(`error: ${problem}`)
    }
    if (problems.length > 0) {
        console.error(`\n${problems.length} problem(s) in ${documents} Markdown documents.`)
        process.exit(1)
    }
    console.log(`${documents} Markdown documents, every English/German pair has the same shape.`)
}

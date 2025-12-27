import antfu from '@antfu/eslint-config'

export default antfu(
    {
        stylistic: {
            indent: 4, // 4, or 'tab'
            quotes: 'single', // or 'double'
            semi: false, // or true
            width: 100,
        },

        // TypeScript and Vue are auto-detected, you can also explicitly enable them:
        typescript: true,
        vue: true,

        // `.eslintignore` is no longer supported in Flat config, use `ignores` instead
        ignores: [
            '**/*.md/**', // Ignore code blocks in markdown files
        ],

        formatters: false,
        unocss: false,
    },
    {
        rules: {
            'no-console': 'off',
        },
    },
)

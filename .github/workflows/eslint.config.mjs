import eslintPluginYml from 'eslint-plugin-yml'

export default [
  ...eslintPluginYml.configs['flat/standard'],
  {
    rules: {
      // override/add rules settings here, such as:
      // 'yml/rule-name': 'error'
      'indent': ['error', 2], // 2 Leerzeichen für Einrückung
      'switch-colon-spacing': ['error', { 'after': true, 'before': false }],
      'key-spacing': ['error', { 'beforeColon': false, 'afterColon': true }],
      'no-console': 'off',
      'quotes': ['error', 'single'],
      'yml/quotes': ['error', { 'prefer': 'single', 'avoidEscape': true }]
    }
  }
]

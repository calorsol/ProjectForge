import { ref } from 'vue'

const KEY = 'ppm_theme'
// 'light' = 正常主题，'dark' = 暗黑主题；默认正常
export const theme = ref(localStorage.getItem(KEY) === 'dark' ? 'dark' : 'light')

export function applyTheme(t) {
  theme.value = t === 'dark' ? 'dark' : 'light'
  localStorage.setItem(KEY, theme.value)
  document.documentElement.classList.toggle('dark', theme.value === 'dark')
}

export function initTheme() { applyTheme(theme.value) }

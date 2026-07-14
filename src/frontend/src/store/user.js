import { defineStore } from 'pinia'

const TOKEN_KEY = 'ppm_token'
const USER_KEY = 'ppm_user'
const readUser = () => { try { return JSON.parse(localStorage.getItem(USER_KEY)) } catch { return null } }

export const useUserStore = defineStore('user', {
  state: () => ({ token: localStorage.getItem(TOKEN_KEY) || '', user: readUser() }),
  getters: { isLoggedIn: state => Boolean(state.token), isAdmin: state => state.user?.role === 'ADMIN' },
  actions: {
    setSession(token, user) { this.token = token; this.user = user; localStorage.setItem(TOKEN_KEY, token); localStorage.setItem(USER_KEY, JSON.stringify(user)) },
    clearSession() { this.token = ''; this.user = null; localStorage.removeItem(TOKEN_KEY); localStorage.removeItem(USER_KEY) }
  }
})

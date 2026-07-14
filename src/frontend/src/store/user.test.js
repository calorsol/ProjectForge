import { beforeEach, describe, expect, it } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useUserStore } from './user'

describe('user store', () => {
  beforeEach(() => { localStorage.clear(); setActivePinia(createPinia()) })

  it('persists the token and authenticated user after login', () => {
    const store = useUserStore()
    store.setSession('jwt-token', { id: 1, username: 'admin', role: 'ADMIN' })

    expect(store.token).toBe('jwt-token')
    expect(store.user.username).toBe('admin')
    expect(localStorage.getItem('ppm_token')).toBe('jwt-token')
  })
})

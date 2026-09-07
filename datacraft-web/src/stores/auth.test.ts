// @vitest-environment jsdom
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useAuthStore } from './auth'
import * as authApi from '../api/auth'

vi.mock('../api/auth')

describe('auth store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
    vi.resetAllMocks()
  })

  it('persists token and user after successful login', async () => {
    vi.mocked(authApi.login).mockResolvedValue({
      accessToken: 'token', tokenType: 'Bearer', expiresIn: 3600,
      user: { id: 1, username: 'admin', displayName: '管理员', roles: ['ADMIN'] },
    })
    const store = useAuthStore()

    await store.login('admin', 'secret')

    expect(store.token).toBe('token')
    expect(store.user?.username).toBe('admin')
    expect(localStorage.getItem('datacraft.auth.token')).toBe('token')
  })

  it('clears persisted session when logout or restore fails', async () => {
    localStorage.setItem('datacraft.auth.token', 'stale')
    vi.mocked(authApi.me).mockRejectedValue(new Error('401'))
    const store = useAuthStore()

    await store.restoreSession()
    expect(store.token).toBeNull()
    expect(localStorage.getItem('datacraft.auth.token')).toBeNull()
  })
})

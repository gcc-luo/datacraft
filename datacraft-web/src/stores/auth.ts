import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { login as loginApi, me as meApi, menus as menusApi } from '../api/auth'
import { AUTH_TOKEN_KEY } from '../api/http'
import type { LoginResponse, MenuDto, UserSummary } from '../types/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem(AUTH_TOKEN_KEY))
  const user = ref<UserSummary | null>(null)
  const menuTree = ref<MenuDto[]>([])
  const ready = ref(false)
  const isAuthenticated = computed(() => Boolean(token.value && user.value))

  function applySession(response: LoginResponse) {
    token.value = response.accessToken
    user.value = response.user
    localStorage.setItem(AUTH_TOKEN_KEY, response.accessToken)
  }

  function clearSession() {
    token.value = null
    user.value = null
    menuTree.value = []
    localStorage.removeItem(AUTH_TOKEN_KEY)
  }

  async function login(username: string, password: string) {
    const response = await loginApi(username, password)
    applySession(response)
    menuTree.value = await menusApi()
  }

  async function restoreSession() {
    if (!token.value) {
      ready.value = true
      return
    }
    try {
      user.value = await meApi()
      menuTree.value = await menusApi()
    } catch {
      clearSession()
    } finally {
      ready.value = true
    }
  }

  function logout() {
    clearSession()
    ready.value = true
  }

  window.addEventListener('datacraft:auth-expired', clearSession)

  return { token, user, menuTree, ready, isAuthenticated, login, restoreSession, logout, clearSession }
})

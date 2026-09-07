import axios from 'axios'

export const AUTH_TOKEN_KEY = 'datacraft.auth.token'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10_000,
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem(AUTH_TOKEN_KEY)
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

http.interceptors.response.use(undefined, (error) => {
  if (error.response?.status === 401) {
    localStorage.removeItem(AUTH_TOKEN_KEY)
    window.dispatchEvent(new Event('datacraft:auth-expired'))
  }
  return Promise.reject(error)
})

export default http

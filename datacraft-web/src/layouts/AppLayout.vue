<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()
const collapsed = ref(false)

const menuIconPaths: Record<string, string[]> = {
  home: ['M3 10.5 12 3l9 7.5', 'M5.5 9.5V21h13V9.5', 'M9.5 21v-6h5v6'],
  database: ['M4 5.5C4 3.84 7.58 2.5 12 2.5s8 1.34 8 3-3.58 3-8 3-8-1.34-8-3Z', 'M4 5.5v6c0 1.66 3.58 3 8 3s8-1.34 8-3v-6', 'M4 11.5v6c0 1.66 3.58 3 8 3s8-1.34 8-3v-6'],
  grid: ['M4 4h6v6H4z', 'M14 4h6v6h-6z', 'M4 14h6v6H4z', 'M14 14h6v6h-6z'],
  flow: ['M5 4h4v4H5z', 'M15 16h4v4h-4z', 'M15 4h4v4h-4z', 'M5 16h4v4H5z', 'M9 6h3a3 3 0 0 1 3 3v7', 'M15 6h-3a3 3 0 0 0-3 3v7'],
  check: ['m5 12 4 4L19 6', 'M12 22a10 10 0 1 0 0-20 10 10 0 0 0 0 20Z'],
  clock: ['M12 7v5l3 2', 'M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z'],
  settings: ['M12 8.5a3.5 3.5 0 1 0 0 7 3.5 3.5 0 0 0 0-7Z', 'm19.4 15 .1.1a2 2 0 1 1-2.8 2.8l-.1-.1a2 2 0 0 0-3.4 1.4v.3a2 2 0 1 1-4 0v-.2a2 2 0 0 0-3.4-1.5l-.1.1a2 2 0 1 1-2.8-2.8l.1-.1A2 2 0 0 0 4.4 12a2 2 0 0 0-1.4-1.9 2 2 0 1 1 1.5-3.7l.1.1A2 2 0 0 0 8 5.1a2 2 0 0 0 1-2.6 2 2 0 1 1 3.8 0 2 2 0 0 0 1 2.6 2 2 0 0 0 3.4 1.4l.1-.1a2 2 0 1 1 2.8 2.8l-.1.1A2 2 0 0 0 19.6 10a2 2 0 0 0 1.4 1.9 2 2 0 1 1-1.5 3.7l-.1-.1Z'],
  menu: ['M5 7h14', 'M5 12h14', 'M5 17h14'],
}

function menuIcon(item: { code: string; icon?: string }) {
  return menuIconPaths[item.icon || item.code] || menuIconPaths.menu
}

function logout() {
  auth.logout()
  void router.push({ name: 'login' })
}
</script>

<template>
  <div class="app-shell" :class="{ 'app-shell--collapsed': collapsed }">
    <aside class="app-sidebar">
      <div class="brand"><span class="brand__mark">D</span><span v-if="!collapsed" class="brand__name">DataCraft</span></div>
      <nav class="app-nav" aria-label="主导航">
        <RouterLink v-for="item in auth.menuTree" :key="item.code" class="app-nav__item" :class="{ 'is-active': route.path === item.path }" :to="item.path">
          <span class="app-nav__icon" aria-hidden="true">
            <svg viewBox="0 0 24 24" focusable="false">
              <path v-for="path in menuIcon(item)" :key="path" :d="path" />
            </svg>
          </span>
          <span v-if="!collapsed">{{ item.title }}</span>
        </RouterLink>
      </nav>
      <button class="sidebar-toggle" type="button" @click="collapsed = !collapsed">{{ collapsed ? '→' : '←' }}</button>
    </aside>
    <section class="app-main">
      <header class="app-header">
        <div><p class="app-header__eyebrow">CONTROL PLANE</p><h1>DataCraft 控制台</h1></div>
        <div class="user-menu"><span class="user-menu__avatar">{{ auth.user?.displayName.slice(0, 1) || 'U' }}</span><span>{{ auth.user?.displayName || auth.user?.username }}</span><button type="button" @click="logout">退出</button></div>
      </header>
      <main class="app-content"><RouterView /></main>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()
const collapsed = ref(false)

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
          <span class="app-nav__icon">{{ item.icon?.slice(0, 1).toUpperCase() || '·' }}</span>
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

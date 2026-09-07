<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()
const username = ref('')
const password = ref('')
const submitting = ref(false)
const errorMessage = ref('')

async function submit() {
  errorMessage.value = ''
  submitting.value = true
  try {
    await auth.login(username.value, password.value)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
    await router.push(redirect)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '登录失败，请检查用户名和密码'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <main class="login-page">
    <section class="login-brand"><p class="login-brand__eyebrow">DATA INTEGRATION &amp; GOVERNANCE</p><div class="login-brand__title">把复杂的数据流<br>变成可控的流程</div><p class="login-brand__copy">连接数据 · 编排处理 · 守护质量</p><div class="login-brand__grid"><span>统一连接</span><span>可视编排</span><span>安全治理</span></div></section>
    <section class="login-card"><div class="login-card__heading"><p class="login-card__eyebrow">WELCOME BACK</p><h1>欢迎回来</h1><p>登录 DataCraft 控制台</p></div><form @submit.prevent="submit"><label>用户名<input v-model="username" name="username" autocomplete="username" placeholder="请输入用户名" required></label><label>密码<input v-model="password" name="password" type="password" autocomplete="current-password" placeholder="请输入密码" required></label><div class="login-options"><label class="checkbox"><input type="checkbox"> 记住我</label><span>安全登录</span></div><p v-if="errorMessage" class="login-error" role="alert">{{ errorMessage }}</p><button class="login-submit" type="submit" :disabled="submitting">{{ submitting ? '登录中…' : '登录' }}</button></form><p class="login-card__foot">DataCraft · Control Plane</p></section>
  </main>
</template>

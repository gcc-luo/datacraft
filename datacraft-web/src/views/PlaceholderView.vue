<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const auth = useAuthStore()
const title = computed(() => auth.menuTree.find(item => item.path === route.path)?.title || '功能模块')
</script>

<template><div class="placeholder-view"><div class="placeholder-view__mark">◇</div><p class="page-heading__eyebrow">COMING IN NEXT PHASE</p><h2>{{ title }}</h2><p>该模块已出现在系统菜单中，将在后续 Phase 实现。</p><RouterLink class="back-link" to="/">返回工作台</RouterLink></div></template>

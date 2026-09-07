<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  currentPage: number
  pageSize: number
  total: number
  pageSizes?: number[]
}>(), {
  pageSizes: () => [10, 20, 50],
})

const emit = defineEmits<{
  'update:currentPage': [page: number]
  'update:pageSize': [size: number]
}>()

const totalPages = computed(() => Math.max(1, Math.ceil(props.total / props.pageSize)))
const pageItems = computed<(number | 'ellipsis-start' | 'ellipsis-end')[]>(() => {
  if (totalPages.value <= 7) return Array.from({ length: totalPages.value }, (_, index) => index + 1)

  const items: (number | 'ellipsis-start' | 'ellipsis-end')[] = [1]
  if (props.currentPage > 4) items.push('ellipsis-start')
  const start = Math.max(2, props.currentPage - 1)
  const end = Math.min(totalPages.value - 1, props.currentPage + 1)
  for (let page = start; page <= end; page += 1) items.push(page)
  if (props.currentPage < totalPages.value - 3) items.push('ellipsis-end')
  items.push(totalPages.value)
  return items
})

function selectPage(page: number) {
  emit('update:currentPage', Math.min(Math.max(1, page), totalPages.value))
}

function selectPageSize(event: Event) {
  emit('update:pageSize', Number((event.target as HTMLSelectElement).value))
}
</script>

<template>
  <nav v-if="total > 0" class="pagination-bar" data-testid="pagination-bar" aria-label="分页导航">
    <span class="pagination-bar__total">共 {{ total }} 条</span>
    <label class="pagination-bar__size">每页
      <select :value="pageSize" aria-label="每页条数" @change="selectPageSize"><option v-for="size in pageSizes" :key="size" :value="size">{{ size }} 条</option></select>
    </label>
    <div class="pagination-bar__pages">
      <button data-testid="pagination-prev" type="button" aria-label="上一页" :disabled="currentPage <= 1" @click="selectPage(currentPage - 1)">‹</button>
      <template v-for="item in pageItems" :key="item">
        <span v-if="typeof item === 'string'" class="pagination-bar__ellipsis">…</span>
        <button v-else :data-testid="`pagination-page-${item}`" type="button" :class="{ 'is-current': item === currentPage }" :aria-current="item === currentPage ? 'page' : undefined" @click="selectPage(item)">{{ item }}</button>
      </template>
      <button data-testid="pagination-next" type="button" aria-label="下一页" :disabled="currentPage >= totalPages" @click="selectPage(currentPage + 1)">›</button>
    </div>
  </nav>
</template>

<style scoped>
.pagination-bar { align-items: center; color: #8090aa; display: flex; font-size: 12px; gap: 16px; justify-content: flex-end; margin-top: 14px; }
.pagination-bar__size { align-items: center; display: inline-flex; gap: 6px; }
.pagination-bar__size select { background: #fff; border: 1px solid #dce4f2; border-radius: 6px; color: #334566; font: inherit; padding: 5px 22px 5px 8px; }
.pagination-bar__pages { align-items: center; display: inline-flex; gap: 4px; }
.pagination-bar__pages button { background: #fff; border: 1px solid #dce4f2; border-radius: 6px; color: #61718d; cursor: pointer; min-width: 28px; padding: 5px 7px; }
.pagination-bar__pages button:hover:not(:disabled), .pagination-bar__pages button.is-current { background: #3564d8; border-color: #3564d8; color: #fff; }
.pagination-bar__pages button:disabled { cursor: not-allowed; opacity: .4; }
@media (max-width: 720px) { .pagination-bar { align-items: flex-start; flex-wrap: wrap; justify-content: flex-start; } }
</style>

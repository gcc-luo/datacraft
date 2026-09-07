import { computed, ref, toValue, watch, type MaybeRefOrGetter } from 'vue'

export function usePagination<T>(source: MaybeRefOrGetter<T[]>, defaultPageSize = 10) {
  const currentPage = ref(1)
  const pageSize = ref(defaultPageSize)
  const total = computed(() => toValue(source).length)
  const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)))
  const paginatedItems = computed(() => {
    const start = (currentPage.value - 1) * pageSize.value
    return toValue(source).slice(start, start + pageSize.value)
  })

  function setPage(page: number) {
    currentPage.value = Math.min(Math.max(1, page), totalPages.value)
  }

  function setPageSize(size: number) {
    if (!Number.isFinite(size) || size <= 0) return
    pageSize.value = size
    currentPage.value = 1
  }

  function resetPage() {
    currentPage.value = 1
  }

  watch([total, pageSize], () => {
    if (currentPage.value > totalPages.value) currentPage.value = totalPages.value
  })

  return { currentPage, pageSize, total, totalPages, paginatedItems, setPage, setPageSize, resetPage }
}

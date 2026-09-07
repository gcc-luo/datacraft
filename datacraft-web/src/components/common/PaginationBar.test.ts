// @vitest-environment jsdom
import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import PaginationBar from './PaginationBar.vue'

describe('PaginationBar', () => {
  it('renders count and emits the selected page', async () => {
    const wrapper = mount(PaginationBar, {
      props: { currentPage: 1, pageSize: 10, total: 25 },
    })

    expect(wrapper.text()).toContain('共 25 条')
    expect(wrapper.get('[data-testid="pagination-prev"]').attributes('disabled')).toBeDefined()

    await wrapper.get('[data-testid="pagination-page-2"]').trigger('click')
    expect(wrapper.emitted('update:currentPage')?.at(-1)).toEqual([2])
  })
})

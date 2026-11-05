import { defineStore } from 'pinia'

export const useAppStore = defineStore('app', {
  state: () => ({
    sidebarCollapsed: localStorage.getItem('rpzh_sidebar_collapsed') === '1',
    dark: localStorage.getItem('rpzh_theme_dark') === '1'
  }),
  actions: {
    toggleSidebar() {
      this.sidebarCollapsed = !this.sidebarCollapsed
      localStorage.setItem('rpzh_sidebar_collapsed', this.sidebarCollapsed ? '1' : '0')
    },
    toggleTheme() {
      this.dark = !this.dark
      localStorage.setItem('rpzh_theme_dark', this.dark ? '1' : '0')
      this.applyTheme()
    },
    applyTheme() {
      const root = document.documentElement
      if (this.dark) {
        root.classList.add('dark')
      } else {
        root.classList.remove('dark')
      }
    }
  }
})

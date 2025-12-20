import { createRouter, createWebHashHistory } from 'vue-router'
import LoginView from '@/views/Login.vue'
import IndexView from '@/views/Index.vue'

const routes = [
  {
    path: '/',
    name: 'login',
    component: LoginView
  },

  {
    path: '/login',
    name: 'Login',
    component: LoginView
  },

  {
    path: '/index',
    name: 'Index',
    component: IndexView
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

export default router

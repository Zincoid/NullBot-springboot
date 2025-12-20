import { createRouter, createWebHashHistory } from 'vue-router'
import LoginView from '@/views/Login.vue'
import RegisterView from '@/views/Register.vue'
import IndexView from '@/views/Index.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: LoginView
  },

  {
    path: '/index',
    name: 'Index',
    component: IndexView
  },

  {
    path: '/',
    name: 'login',
    component: LoginView
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

export default router

import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../store/user'
import Login from '../views/Login.vue'

// 各页面按需懒加载，工作台首屏不再打包其他页面代码
const router = createRouter({ history: createWebHistory(), routes: [
  { path: '/login', component: Login, meta: { public: true } },
  { path: '/', component: () => import('../views/Workbench.vue') },
  { path: '/projects', component: () => import('../views/Projects.vue') },
  { path: '/projects/:id', component: () => import('../views/ProjectDetail.vue') },
  { path: '/tasks/:id', component: () => import('../views/TaskDetail.vue') },
  { path: '/members', component: () => import('../views/Members.vue'), meta: { admin: true } },
  { path: '/:pathMatch(.*)*', redirect: '/' }
] })
router.beforeEach(to => { const store = useUserStore(); if (!to.meta.public && !store.isLoggedIn) return '/login'; if (to.meta.admin && !store.isAdmin) return '/'; if (to.path === '/login' && store.isLoggedIn) return '/' })
export default router

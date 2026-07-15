import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../store/user'
import Login from '../views/Login.vue'
import Workbench from '../views/Workbench.vue'
import Projects from '../views/Projects.vue'
import ProjectDetail from '../views/ProjectDetail.vue'
import TaskDetail from '../views/TaskDetail.vue'
import Members from '../views/Members.vue'

const router = createRouter({ history: createWebHistory(), routes: [
  { path: '/login', component: Login, meta: { public: true } },
  { path: '/', component: Workbench },
  { path: '/projects', component: Projects },
  { path: '/projects/:id', component: ProjectDetail },
  { path: '/tasks/:id', component: TaskDetail },
  { path: '/members', component: Members, meta: { admin: true } },
  { path: '/:pathMatch(.*)*', redirect: '/' }
] })
router.beforeEach(to => { const store = useUserStore(); if (!to.meta.public && !store.isLoggedIn) return '/login'; if (to.meta.admin && !store.isAdmin) return '/'; if (to.path === '/login' && store.isLoggedIn) return '/' })
export default router

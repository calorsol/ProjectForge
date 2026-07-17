<script setup>
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from './store/user'
import { logout } from './api/auth'
import { theme, applyTheme } from './theme'
import { APP_VERSION } from './version'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const signOut = async () => {
  try { await logout() } finally { userStore.clearSession(); router.replace('/login') }
}
</script>

<template>
  <header v-if="!route.meta.public" class="topbar">
    <div class="brand">📋 个人项目平台</div>
    <nav class="topnav">
      <RouterLink to="/">工作台</RouterLink>
      <RouterLink to="/projects">项目</RouterLink>
      <RouterLink v-if="userStore.isAdmin" to="/members">成员管理</RouterLink>
    </nav>
    <div class="topbar-right">
      <el-switch :model-value="theme === 'dark'" @change="v => applyTheme(v ? 'dark' : 'light')"
        inline-prompt active-text="🌙 暗黑" inactive-text="☀ 正常" :width="64"
        style="--el-switch-on-color:#33353a; --el-switch-off-color:#2b6fd6" />
      <span>版本 {{ APP_VERSION }}</span>
      <span>👤 {{ userStore.user?.username }} · {{ userStore.isAdmin ? '超级管理员' : '普通用户' }}</span>
      <el-link :underline="false" style="color:#fff" @click="signOut">退出</el-link>
    </div>
  </header>
  <RouterView />
</template>

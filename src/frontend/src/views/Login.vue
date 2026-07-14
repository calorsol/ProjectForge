<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, register } from '../api/auth'
import { useUserStore } from '../store/user'

const router = useRouter(); const userStore = useUserStore(); const isRegister = ref(false); const loading = ref(false)
const form = reactive({ username: '', password: '', confirmPassword: '' })
const submit = async () => {
  loading.value = true
  try {
    if (isRegister.value) { await register(form); ElMessage.success('注册成功，请登录'); isRegister.value = false; form.confirmPassword = ''; return }
    const data = await login(form); userStore.setSession(data.token, data.user); router.replace('/')
  } catch (error) { ElMessage.error(error.message) } finally { loading.value = false }
}
</script>
<template><main class="login-page"><el-card class="login-card" shadow="never"><h2>{{ isRegister ? '注册账号' : '个人项目平台' }}</h2><p class="login-sub">{{ isRegister ? '仅需用户名和密码' : '登录你的账号' }}</p><el-form :model="form" label-position="top" @submit.prevent="submit"><el-form-item label="用户名"><el-input v-model.trim="form.username" placeholder="请输入用户名" /></el-form-item><el-form-item label="密码"><el-input v-model="form.password" type="password" show-password placeholder="请输入密码" /></el-form-item><el-form-item v-if="isRegister" label="确认密码"><el-input v-model="form.confirmPassword" type="password" show-password placeholder="请再次输入密码" /></el-form-item><el-button type="primary" native-type="submit" :loading="loading" style="width:100%">{{ isRegister ? '注 册' : '登 录' }}</el-button></el-form><p class="form-switch">{{ isRegister ? '已有账号？' : '还没有账号？' }}<el-link type="primary" :underline="false" @click="isRegister = !isRegister">{{ isRegister ? '去登录' : '注册一个' }}</el-link></p></el-card></main></template>

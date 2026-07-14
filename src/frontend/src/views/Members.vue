<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listUsers, updateUserStatus, updateUserRole, resetPassword, deleteUser } from '../api/user'
import { roleLabel, statusLabel } from '../utils/dict'

const rows = ref([])
const keyword = ref('')
const load = async () => { rows.value = await listUsers({ keyword: keyword.value || undefined }) }
const run = async (fn, ok) => { try { await fn(); ElMessage.success(ok); load() } catch (e) { ElMessage.error(e.message) } }

const toggle = row => run(() => updateUserStatus(row.id, row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'), '已更新状态')
const role = row => run(() => updateUserRole(row.id, row.role === 'ADMIN' ? 'USER' : 'ADMIN'), '已更新角色')
const del = async row => {
  try { await ElMessageBox.confirm(`确认删除用户「${row.username}」？`, '提示', { type: 'warning' }) } catch { return }
  run(() => deleteUser(row.id), '已删除')
}
const reset = async row => {
  let value
  try { ({ value } = await ElMessageBox.prompt(`为「${row.username}」设置新密码（至少6位）`, '重置密码', { inputType: 'password' })) } catch { return }
  if (!value || value.length < 6) { ElMessage.warning('密码至少6位'); return }
  run(() => resetPassword(row.id, value), '密码已重置')
}
onMounted(load)
</script>

<template>
  <main class="page-container">
    <el-card class="page-card">
      <el-alert title="本页面仅超级管理员可见" type="warning" :closable="false" />
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="搜索用户名" style="width:200px" @keyup.enter="load" />
        <el-button @click="load">搜索</el-button>
      </div>
      <el-table :data="rows" style="width:100%" border stripe>
        <el-table-column prop="username" label="用户名" min-width="160" show-overflow-tooltip />
        <el-table-column label="角色" min-width="130">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'info'" size="small">{{ roleLabel(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" min-width="180" />
        <el-table-column prop="lastLoginAt" label="最近登录" min-width="180" />
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button link @click="reset(row)">重置密码</el-button>
            <el-button link @click="toggle(row)">{{ row.status === 'ENABLED' ? '禁用' : '启用' }}</el-button>
            <el-button link @click="role(row)">{{ row.role === 'ADMIN' ? '设为普通' : '设为管理员' }}</el-button>
            <el-button link type="danger" @click="del(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </main>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getProject } from '../api/project'
import { listTasks, changeStatus, deleteTask } from '../api/task'
import TaskTable from '../components/TaskTable.vue'
import TaskModal from '../components/TaskModal.vue'

const route = useRoute()
const router = useRouter()
const projectId = Number(route.params.id)
const project = ref(null)
const rows = ref([])
const dialog = ref(false)
const editing = ref(null)

const load = async () => {
  project.value = await getProject(projectId)
  rows.value = await listTasks({ projectId })
}
const act = async (row, action) => {
  try { await changeStatus(row.id, action); ElMessage.success('操作成功'); load() }
  catch (e) { ElMessage.error(e.message) }
}
const openNew = () => { editing.value = { projectId }; dialog.value = true }
const openEdit = row => { editing.value = { ...row }; dialog.value = true }
const remove = async row => {
  try { await ElMessageBox.confirm(`确认删除任务「${row.title}」？`, '提示', { type: 'warning' }) } catch { return }
  await deleteTask(row.id); ElMessage.success('已删除'); load()
}
onMounted(load)
</script>

<template>
  <main class="page-container">
    <el-card class="page-card">
      <div class="breadcrumb">
        <a @click="router.push('/projects')">项目</a>
        <span class="sep">/</span>
        <span>{{ project?.name || '' }}</span>
        <el-tag v-if="project" size="small" style="margin-left:8px">{{ project.code }}</el-tag>
      </div>
      <div class="toolbar">
        <span style="color:#86909c;font-size:13px">共 {{ rows.length }} 个任务</span>
        <span class="spacer" />
        <el-button @click="router.push('/projects')">返回项目列表</el-button>
        <el-button type="primary" @click="openNew">＋ 新建任务</el-button>
      </div>
      <TaskTable :rows="rows" :show-project="false" @action="act" @edit="openEdit" @delete="remove" @open="row => router.push('/tasks/' + row.id)" />
    </el-card>
    <TaskModal v-model="dialog" :task="editing" @saved="load" />
  </main>
</template>

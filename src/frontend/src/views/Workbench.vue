<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { changeStatus, workbenchTasks, deleteTask } from '../api/task'
import { listProjects } from '../api/project'
import TaskTable from '../components/TaskTable.vue'
import TaskModal from '../components/TaskModal.vue'

const router = useRouter()
const tab = ref('todo')
const rows = ref([])
const projects = ref([])
const dialog = ref(false)
const editing = ref(null)

// 筛选条件
const fKeyword = ref('')
const fProject = ref(null)
const fType = ref('')
const fRange = ref(null)

const load = async () => { rows.value = await workbenchTasks(tab.value) }
const resetFilters = () => { fKeyword.value = ''; fProject.value = null; fType.value = ''; fRange.value = null }

const filteredRows = computed(() => rows.value.filter(r => {
  if (fKeyword.value && !(r.title || '').toLowerCase().includes(fKeyword.value.toLowerCase())) return false
  if (fProject.value != null && r.projectId !== fProject.value) return false
  if (fType.value && r.type !== fType.value) return false
  if (fRange.value && fRange.value.length === 2) {
    if (!r.planStartAt) return false
    if (r.planStartAt < fRange.value[0] || r.planStartAt > fRange.value[1]) return false
  }
  return true
}))

const act = async (row, action) => {
  try { await changeStatus(row.id, action); ElMessage.success('操作成功'); load() }
  catch (e) { ElMessage.error(e.message) }
}
const openNew = () => { editing.value = null; dialog.value = true }
const openEdit = row => { editing.value = { ...row }; dialog.value = true }
const remove = async row => {
  try { await ElMessageBox.confirm(`确认删除任务「${row.title}」？`, '提示', { type: 'warning' }) } catch { return }
  await deleteTask(row.id); ElMessage.success('已删除'); load()
}
onMounted(async () => { await load(); projects.value = await listProjects() })
</script>

<template>
  <main class="page-container">
    <el-card class="page-card">
      <el-tabs v-model="tab" @tab-change="load">
        <el-tab-pane label="待办 (进行中/未开始)" name="todo" />
        <el-tab-pane label="已暂停" name="paused" />
        <el-tab-pane label="已完成" name="done" />
        <el-tab-pane label="全部" name="all" />
      </el-tabs>
      <div class="toolbar" style="flex-wrap:wrap">
        <el-input v-model="fKeyword" placeholder="搜索任务名称" clearable style="width:180px" />
        <el-select v-model="fProject" placeholder="全部项目" clearable style="width:150px">
          <el-option v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
        </el-select>
        <el-select v-model="fType" placeholder="全部类型" clearable style="width:130px">
          <el-option v-for="x in ['开发', '修复', '测试', '调研', '杂务']" :key="x" :label="x" :value="x" />
        </el-select>
        <el-date-picker v-model="fRange" type="datetimerange" value-format="YYYY-MM-DD HH:mm:ss"
          range-separator="至" start-placeholder="计划开始 从" end-placeholder="到" style="width:360px" />
        <el-button @click="resetFilters">重置</el-button>
        <span class="spacer" />
        <el-button type="primary" @click="openNew">＋ 新建任务</el-button>
      </div>
      <TaskTable :rows="filteredRows" @action="act" @edit="openEdit" @delete="remove" @open="row => router.push('/tasks/' + row.id)" />
    </el-card>
    <TaskModal v-model="dialog" :task="editing" @saved="load" />
  </main>
</template>

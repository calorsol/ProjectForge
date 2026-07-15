<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTask, changeStatus } from '../api/task'
import { taskAttachments } from '../api/attachment'
import { listNotes, addNote, deleteNote } from '../api/note'
import { taskStatusLabel, taskStatusType, priorityLabel, priorityType } from '../utils/dict'
import { BACKEND_ORIGIN } from '../api/client'
import RichEditor from '../components/RichEditor.vue'
import TaskModal from '../components/TaskModal.vue'

const route = useRoute()
const router = useRouter()
const taskId = Number(route.params.id)
const task = ref(null)
const files = ref([])
const notes = ref([])
const expanded = ref([])          // 当前展开的备注 id
const adding = ref(false)
const draft = ref('')
const dialog = ref(false)

const load = async () => {
  task.value = await getTask(taskId)
  files.value = (await taskAttachments(taskId)).filter(a => a.kind === 'FILE')
  notes.value = await listNotes(taskId)
  expanded.value = notes.value.map(n => n.id)   // 默认展开
}
const act = async action => {
  try { await changeStatus(taskId, action); ElMessage.success('操作成功'); load() }
  catch (e) { ElMessage.error(e.message) }
}
const submitNote = async () => {
  const text = (draft.value || '').replace(/<[^>]*>/g, '').trim()
  if (!text) { ElMessage.warning('请输入备注内容'); return }
  await addNote(taskId, draft.value)
  draft.value = ''; adding.value = false
  ElMessage.success('备注已添加'); load()
}
const removeNote = async n => {
  try { await ElMessageBox.confirm('确认删除这条备注？', '提示', { type: 'warning' }) } catch { return }
  await deleteNote(n.id); ElMessage.success('已删除'); load()
}
const plain = html => (html || '').replace(/<[^>]*>/g, '').trim()
const fileUrl = f => BACKEND_ORIGIN + f.url
onMounted(load)
</script>

<template>
  <main class="page-container" v-if="task">
    <!-- 顶部条 -->
    <div class="detail-head">
      <el-button @click="router.back()">↩ 返回</el-button>
      <span class="tid">{{ task.id }}</span>
      <span class="ttitle">{{ task.title }}</span>
      <el-tag :type="taskStatusType(task.status)" size="small">{{ taskStatusLabel(task.status) }}</el-tag>
      <span class="spacer" />
      <el-button v-if="task.status === 'TODO'" @click="act('start')">开始</el-button>
      <el-button v-if="task.status === 'DOING'" @click="act('pause')">暂停</el-button>
      <el-button v-if="task.status === 'PAUSED'" @click="act('resume')">继续</el-button>
      <el-button v-if="['DOING','PAUSED'].includes(task.status)" type="success" @click="act('finish')">完成</el-button>
      <el-button v-if="task.status === 'DONE'" @click="act('reopen')">重开</el-button>
      <el-button type="primary" @click="dialog = true">编辑</el-button>
    </div>

    <div class="detail-body">
      <!-- 左栏 -->
      <div class="detail-main">
        <el-card class="page-card">
          <div class="sec-title">任务描述</div>
          <div v-if="plain(task.description)" class="rich-content" v-html="task.description" />
          <el-empty v-else description="暂无描述" :image-size="60" />
        </el-card>

        <el-card class="page-card" style="margin-top:16px">
          <div class="sec-title">附件信息</div>
          <div v-if="files.length">
            <div v-for="f in files" :key="f.id" style="margin:6px 0">
              <a :href="fileUrl(f)" target="_blank" class="file-link">📎 {{ f.fileName }}</a>
            </div>
          </div>
          <el-empty v-else description="暂无附件" :image-size="60" />
        </el-card>

        <el-card class="page-card" style="margin-top:16px">
          <div class="sec-title">
            备注 / 历史记录
            <el-button link type="primary" style="float:right" @click="adding = !adding">
              {{ adding ? '取消' : '＋ 添加备注' }}
            </el-button>
          </div>

          <div v-if="adding" style="margin-bottom:16px">
            <RichEditor v-model="draft" height="200px" />
            <div style="margin-top:8px; text-align:right">
              <el-button @click="adding = false; draft = ''">取消</el-button>
              <el-button type="primary" @click="submitNote">提交备注</el-button>
            </div>
          </div>

          <!-- 备注可折叠：点标题栏收起/展开 -->
          <el-collapse v-if="notes.length" v-model="expanded">
            <el-collapse-item v-for="n in notes" :key="n.id" :name="n.id">
              <template #title>
                <span class="note-head">
                  <b>{{ n.authorName }}</b>
                  <span class="note-time">{{ n.createdAt }}</span>
                  <span class="note-preview">{{ plain(n.content).slice(0, 40) }}{{ plain(n.content).length > 40 ? '…' : '' }}</span>
                </span>
              </template>
              <div class="rich-content" v-html="n.content" />
              <div style="text-align:right">
                <el-button link type="danger" @click="removeNote(n)">删除该备注</el-button>
              </div>
            </el-collapse-item>
          </el-collapse>
          <el-empty v-else description="暂无备注" :image-size="60" />
        </el-card>
      </div>

      <!-- 右栏 -->
      <div class="detail-side">
        <el-card class="page-card">
          <div class="sec-title">基础信息</div>
          <div class="kv"><span>所属项目</span><b>{{ task.projectName || '默认项目' }}</b></div>
          <div class="kv"><span>任务类型</span><b>{{ task.type || '-' }}</b></div>
          <div class="kv"><span>任务状态</span><el-tag :type="taskStatusType(task.status)" size="small">{{ taskStatusLabel(task.status) }}</el-tag></div>
          <div class="kv"><span>优先级</span><el-tag :type="priorityType(task.priority)" size="small">{{ priorityLabel(task.priority) }}</el-tag></div>
          <div class="kv"><span>截止时间</span><b :class="{ overdue: task.overdue }">{{ task.dueAt || '-' }}</b></div>
        </el-card>

        <el-card class="page-card" style="margin-top:16px">
          <div class="sec-title">时间信息</div>
          <div class="kv"><span>计划启动时间</span><b>{{ task.planStartAt || '-' }}</b></div>
          <div class="kv"><span>截止时间</span><b>{{ task.dueAt || '-' }}</b></div>
          <div class="kv"><span>实际完成时间</span><b>{{ task.finishedAt || '-' }}</b></div>
          <div class="kv"><span>创建时间</span><b>{{ task.createdAt || '-' }}</b></div>
        </el-card>
      </div>
    </div>

    <TaskModal v-model="dialog" :task="task" @saved="load" />
  </main>
</template>

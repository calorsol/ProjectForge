<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTask, getTaskHistory, changeStatus } from '../api/task'
import { taskAttachments, uploadAttachment, deleteAttachment } from '../api/attachment'
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
const history = ref([])
const descOpen = ref(['desc'])      // 描述默认展开，可收起
const expanded = ref([])            // 展开中的备注 id
const bottomTab = ref('notes')
const adding = ref(false)
const draft = ref('')
const dialog = ref(false)

const load = async () => {
  task.value = await getTask(taskId)
  files.value = (await taskAttachments(taskId)).filter(a => a.kind === 'FILE')
  notes.value = await listNotes(taskId)
  history.value = await getTaskHistory(taskId)
  expanded.value = notes.value.map(n => n.id)
}
const act = async action => {
  try { await changeStatus(taskId, action); ElMessage.success('操作成功'); load() }
  catch (e) { ElMessage.error(e.message) }
}
const submitNote = async () => {
  if (!plain(draft.value)) { ElMessage.warning('请输入备注内容'); return }
  await addNote(taskId, draft.value)
  draft.value = ''; adding.value = false
  ElMessage.success('备注已添加'); load()
}
const removeNote = async n => {
  try { await ElMessageBox.confirm('确认删除这条备注？', '提示', { type: 'warning' }) } catch { return }
  await deleteNote(n.id); ElMessage.success('已删除'); load()
}
const uploadFile = async f => {
  try { await uploadAttachment(f.raw, 'FILE', taskId); ElMessage.success('附件已上传'); load() }
  catch (e) { ElMessage.error(e.message) }
}
const removeFile = async f => {
  try { await ElMessageBox.confirm(`确认删除附件「${f.fileName}」？`, '提示', { type: 'warning' }) } catch { return }
  await deleteAttachment(f.id); ElMessage.success('已删除'); load()
}
const plain = html => (html || '').replace(/<[^>]*>/g, '').trim()
const fileUrl = f => BACKEND_ORIGIN + f.url
onMounted(load)
</script>

<template>
  <main class="page-container" v-if="task">
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
      <div class="detail-main">
        <!-- 任务描述：可收起 -->
        <el-card class="page-card">
          <el-collapse v-model="descOpen">
            <el-collapse-item name="desc">
              <template #title><span class="sec-title-inline">任务描述</span></template>
              <div v-if="plain(task.description)" class="rich-content" v-html="task.description" />
              <span v-else class="muted">暂无描述</span>
            </el-collapse-item>
          </el-collapse>
        </el-card>

        <!-- 附件：没有附件时只留一个小上传按钮 -->
        <el-card class="page-card" style="margin-top:16px">
          <div class="sec-title">
            附件信息
            <el-upload :auto-upload="false" :show-file-list="false" multiple style="float:right" @change="uploadFile">
              <el-button size="small">＋ 上传附件</el-button>
            </el-upload>
          </div>
          <div v-if="files.length">
            <div v-for="f in files" :key="f.id" class="file-row">
              <a :href="fileUrl(f)" target="_blank" class="file-link">📎 {{ f.fileName }}</a>
              <el-button link type="danger" size="small" @click="removeFile(f)">删除</el-button>
            </div>
          </div>
        </el-card>

        <!-- 备注 + 历史记录 -->
        <el-card class="page-card" style="margin-top:16px">
          <el-tabs v-model="bottomTab">
            <el-tab-pane label="备注" name="notes">
              <div style="margin-bottom:12px">
                <el-button v-if="!adding" link type="primary" @click="adding = true">＋ 添加备注</el-button>
                <div v-else>
                  <RichEditor v-model="draft" height="200px" />
                  <div style="margin-top:8px; text-align:right">
                    <el-button @click="adding = false; draft = ''">取消</el-button>
                    <el-button type="primary" @click="submitNote">提交备注</el-button>
                  </div>
                </div>
              </div>
              <el-collapse v-if="notes.length" v-model="expanded">
                <el-collapse-item v-for="n in notes" :key="n.id" :name="n.id">
                  <template #title>
                    <span class="note-head"><b>{{ n.authorName }}</b><span class="note-time">{{ n.createdAt }}</span></span>
                  </template>
                  <div class="rich-content" v-html="n.content" />
                  <div style="text-align:right">
                    <el-button link type="danger" @click="removeNote(n)">删除该备注</el-button>
                  </div>
                </el-collapse-item>
              </el-collapse>
              <span v-else class="muted">暂无备注</span>
            </el-tab-pane>

            <el-tab-pane :label="`历史记录 (${history.length})`" name="history">
              <ol v-if="history.length" class="history-list">
                <li v-for="h in history" :key="h.id">
                  <span class="note-time">{{ h.createdAt }}</span>
                  由 <b>{{ h.userName || '系统' }}</b> <span class="act-name">{{ h.action }}</span>
                  <div v-if="h.detail" class="history-detail">{{ h.detail }}</div>
                </li>
              </ol>
              <span v-else class="muted">暂无历史记录</span>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </div>

      <div class="detail-side">
        <el-card class="page-card">
          <div class="sec-title">基础信息</div>
          <div class="kv"><span>所属项目</span><b>{{ task.projectName || '默认项目' }}</b></div>
          <div class="kv"><span>任务类型</span><b>{{ task.type || '-' }}</b></div>
          <div class="kv"><span>任务状态</span><el-tag :type="taskStatusType(task.status)" size="small">{{ taskStatusLabel(task.status) }}</el-tag></div>
          <div class="kv"><span>优先级</span><el-tag :type="priorityType(task.priority)" size="small">{{ priorityLabel(task.priority) }}</el-tag></div>
        </el-card>

        <el-card class="page-card" style="margin-top:16px">
          <div class="sec-title">时间信息</div>
          <div class="kv"><span>计划启动时间</span><b>{{ task.planStartAt || '-' }}</b></div>
          <div class="kv"><span>截止时间</span><b :class="{ overdue: task.overdue }">{{ task.dueAt || '-' }}</b></div>
          <div class="kv"><span>实际完成时间</span><b>{{ task.finishedAt || '-' }}</b></div>
          <div class="kv"><span>创建时间</span><b>{{ task.createdAt || '-' }}</b></div>
        </el-card>
      </div>
    </div>

    <TaskModal v-model="dialog" :task="task" @saved="load" />
  </main>
</template>

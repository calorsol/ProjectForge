<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTask, getTaskHistory, changeStatus, updateTask } from '../api/task'
import { taskAttachments, uploadAttachment, deleteAttachment } from '../api/attachment'
import { listNotes, addNote, updateNote, deleteNote } from '../api/note'
import { taskStatusLabel, taskStatusType, priorityLabel, priorityType } from '../utils/dict'
import { BACKEND_ORIGIN } from '../api/client'
import { defineAsyncComponent } from 'vue'
import TaskModal from '../components/TaskModal.vue'
import FinishDialog from '../components/FinishDialog.vue'
const RichEditor = defineAsyncComponent(() => import('../components/RichEditor.vue'))

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
const finishDialog = ref(false)
const editingNoteId = ref(null)   // 正在编辑的备注 id
const editDraft = ref('')
const descEditing = ref(false)    // 描述是否处于编辑态
const descDraft = ref('')

const load = async () => {
  task.value = await getTask(taskId)
  files.value = (await taskAttachments(taskId)).filter(a => a.kind === 'FILE')
  notes.value = await listNotes(taskId)
  history.value = await getTaskHistory(taskId)
  expanded.value = notes.value.map(n => n.id)
}
const act = async action => {
  if (action === 'finish') { finishDialog.value = true; return }
  try { await changeStatus(taskId, action); ElMessage.success('操作成功'); load() }
  catch (e) { ElMessage.error(e.message) }
}
const submitNote = async () => {
  if (!plain(draft.value)) { ElMessage.warning('请输入备注内容'); return }
  await addNote(taskId, draft.value)
  draft.value = ''; adding.value = false
  ElMessage.success('备注已添加'); load()
}
// 直接在详情页就地编辑任务描述（不必打开编辑弹窗）
const startEditDesc = () => {
  descDraft.value = task.value.description || ''
  descEditing.value = true
  if (!descOpen.value.includes('desc')) descOpen.value.push('desc')
}
const cancelEditDesc = () => { descEditing.value = false; descDraft.value = '' }
const saveDesc = async () => {
  const t = task.value
  try {
    await updateTask(taskId, {
      title: t.title, projectId: t.projectId, type: t.type, priority: t.priority,
      planStartAt: t.planStartAt, dueAt: t.dueAt, description: descDraft.value,
      attachmentIds: files.value.map(f => f.id)
    })
    ElMessage.success('描述已保存'); cancelEditDesc(); load()
  } catch (e) { ElMessage.error(e.message) }
}

const startEditNote = n => {
  editingNoteId.value = n.id
  editDraft.value = n.content
  if (!expanded.value.includes(n.id)) expanded.value.push(n.id)   // 编辑时确保展开
}
const cancelEditNote = () => { editingNoteId.value = null; editDraft.value = '' }
const saveNote = async n => {
  if (!plain(editDraft.value)) { ElMessage.warning('备注内容不能为空'); return }
  try {
    await updateNote(n.id, editDraft.value)
    ElMessage.success('备注已更新'); cancelEditNote(); load()
  } catch (e) { ElMessage.error(e.message) }
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
              <template #title>
                <span class="sec-title-inline">任务描述</span>
                <el-button v-if="!descEditing" link type="primary" style="margin-left:12px" @click.stop="startEditDesc">编辑</el-button>
              </template>
              <div v-if="descEditing">
                <RichEditor v-model="descDraft" height="300px" />
                <div style="margin-top:8px; text-align:right">
                  <el-button @click="cancelEditDesc">取消</el-button>
                  <el-button type="primary" @click="saveDesc">保存</el-button>
                </div>
              </div>
              <template v-else>
                <div v-if="plain(task.description)" class="rich-content" v-html="task.description" />
                <span v-else class="muted">暂无描述，点击上方「编辑」添加</span>
              </template>
            </el-collapse-item>
          </el-collapse>
        </el-card>

        <!-- 备注 / 历史记录 / 附件信息：操作按钮统一放在右上角 -->
        <el-card class="page-card" style="margin-top:16px">
          <div class="tabs-wrap">
            <div class="tabs-actions">
              <el-button v-if="bottomTab === 'notes' && !adding" link type="primary" @click="adding = true">＋ 添加备注</el-button>
              <el-button v-if="bottomTab === 'notes' && adding" link @click="adding = false; draft = ''">取消添加</el-button>
              <el-upload v-if="bottomTab === 'files'" :auto-upload="false" :show-file-list="false" multiple @change="uploadFile">
                <el-button link type="primary">＋ 上传附件</el-button>
              </el-upload>
            </div>
            <el-tabs v-model="bottomTab">
            <el-tab-pane label="备注" name="notes">
              <div v-if="adding" style="margin-bottom:12px">
                <RichEditor v-model="draft" height="200px" />
                <div style="margin-top:8px; text-align:right">
                  <el-button @click="adding = false; draft = ''">取消</el-button>
                  <el-button type="primary" @click="submitNote">提交备注</el-button>
                </div>
              </div>
              <el-collapse v-if="notes.length" v-model="expanded">
                <el-collapse-item v-for="n in notes" :key="n.id" :name="n.id">
                  <template #title>
                    <span class="note-head"><b>{{ n.authorName }}</b><span class="note-time">{{ n.createdAt }}</span></span>
                  </template>
                  <!-- 编辑中显示富文本编辑器，否则渲染正文 -->
                  <div v-if="editingNoteId === n.id">
                    <RichEditor v-model="editDraft" height="200px" />
                    <div style="margin-top:8px; text-align:right">
                      <el-button @click="cancelEditNote">取消</el-button>
                      <el-button type="primary" @click="saveNote(n)">保存</el-button>
                    </div>
                  </div>
                  <template v-else>
                    <div class="rich-content" v-html="n.content" />
                    <div style="text-align:right">
                      <el-button link type="primary" @click="startEditNote(n)">编辑</el-button>
                      <el-button link type="danger" @click="removeNote(n)">删除该备注</el-button>
                    </div>
                  </template>
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

            <el-tab-pane :label="`附件信息 (${files.length})`" name="files">
              <div v-if="files.length">
                <div v-for="f in files" :key="f.id" class="file-row">
                  <a :href="fileUrl(f)" target="_blank" class="file-link">📎 {{ f.fileName }}</a>
                  <el-button link type="danger" size="small" @click="removeFile(f)">删除</el-button>
                </div>
              </div>
              <span v-else class="muted">暂无附件，点击右上角「上传附件」添加</span>
            </el-tab-pane>
            </el-tabs>
          </div>
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
    <FinishDialog v-model="finishDialog" :task-id="taskId" :task-title="task.title" @finished="load" />
  </main>
</template>

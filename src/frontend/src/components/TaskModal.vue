<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { createTask, updateTask } from '../api/task'
import { listProjects } from '../api/project'
import { uploadAttachment, deleteAttachment, taskAttachments } from '../api/attachment'
import RichEditor from './RichEditor.vue'

const props = defineProps({ modelValue: Boolean, task: Object })
const emit = defineEmits(['update:modelValue', 'saved'])
const projects = ref([])
const files = ref([])
const empty = () => ({ title: '', projectId: null, type: '', priority: 'MEDIUM', planStartAt: null, dueAt: null, description: '' })
const form = reactive(empty())

onMounted(async () => { projects.value = await listProjects() })

// 每次打开弹窗时重置表单与附件；编辑时回填已有附件（仅普通文件附件，正文图片已内嵌在描述里）
watch(() => props.modelValue, async open => {
  if (!open) return
  const t = props.task
  Object.assign(form, empty(), t ? { title: t.title, projectId: t.projectId, type: t.type, priority: t.priority, planStartAt: t.planStartAt, dueAt: t.dueAt, description: t.description } : {})
  files.value = t?.id ? (await taskAttachments(t.id)).filter(a => a.kind === 'FILE') : []
})

const upload = async f => {
  try { files.value.push(await uploadAttachment(f.raw, 'FILE')) }
  catch (e) { ElMessage.error(e.message) }
}
const remove = async f => { await deleteAttachment(f.id); files.value = files.value.filter(x => x.id !== f.id) }

const save = async () => {
  if (!form.title.trim()) { ElMessage.warning('请填写任务名称'); return }
  const data = { ...form, attachmentIds: files.value.map(x => x.id) }
  try {
    if (props.task?.id) await updateTask(props.task.id, data)
    else await createTask(data)
    ElMessage.success('已保存')
    emit('saved'); emit('update:modelValue', false)
  } catch (e) { ElMessage.error(e.message) }
}
</script>

<template>
  <el-dialog :model-value="modelValue" :title="task?.id ? '编辑任务' : '新建任务'" width="76%" style="max-width:1280px" top="4vh" @close="emit('update:modelValue', false)">
    <el-form label-width="90px">
      <el-form-item label="任务名称" required><el-input v-model="form.title" /></el-form-item>
      <el-form-item label="所属项目">
        <el-select v-model="form.projectId" clearable placeholder="默认项目">
          <el-option v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="任务类型">
        <el-select v-model="form.type" clearable>
          <el-option v-for="x in ['开发', '修复', '测试', '调研', '杂务']" :key="x" :label="x" :value="x" />
        </el-select>
      </el-form-item>
      <el-form-item label="优先级">
        <el-radio-group v-model="form.priority">
          <el-radio value="HIGH">高</el-radio><el-radio value="MEDIUM">中</el-radio><el-radio value="LOW">低</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="时间计划">
        <el-date-picker v-model="form.planStartAt" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="计划开始" />
        <span style="margin:0 8px">至</span>
        <el-date-picker v-model="form.dueAt" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="截止时间" />
      </el-form-item>
      <el-form-item label="任务描述">
        <RichEditor v-model="form.description" height="440px" />
      </el-form-item>
      <el-form-item label="附件">
        <div style="width:100%">
          <el-upload :auto-upload="false" :show-file-list="false" multiple @change="upload">
            <el-button>上传文件</el-button>
            <template #tip><span style="color:#909399;font-size:12px;margin-left:10px">文档、压缩包等任意文件；图片建议直接粘贴进上面的描述里</span></template>
          </el-upload>
          <div style="margin-top:6px">
            <el-tag v-for="f in files" :key="f.id" closable style="margin:4px 4px 0 0" @close="remove(f)">📎 {{ f.fileName }}</el-tag>
          </div>
        </div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" @click="save">保存</el-button>
    </template>
  </el-dialog>
</template>

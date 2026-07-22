<script setup>
import { defineAsyncComponent, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { changeStatus } from '../api/task'
import { addNote } from '../api/note'
const RichEditor = defineAsyncComponent(() => import('./RichEditor.vue'))

const props = defineProps({ modelValue: Boolean, taskId: [Number, String], taskTitle: String })
const emit = defineEmits(['update:modelValue', 'finished'])

const note = ref('')
const submitting = ref(false)
const now = () => {
  const d = new Date(), p = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
}
const finishTime = ref('')
watch(() => props.modelValue, o => { if (o) { note.value = ''; finishTime.value = now() } })

const plain = h => (h || '').replace(/<[^>]*>/g, '').trim()
const confirm = async () => {
  submitting.value = true
  try {
    await changeStatus(props.taskId, 'finish')
    if (plain(note.value)) await addNote(props.taskId, '<p>✅ <strong>完成备注</strong></p>' + note.value)
    ElMessage.success('任务已完成')
    emit('finished'); emit('update:modelValue', false)
  } catch (e) { ElMessage.error(e.message) } finally { submitting.value = false }
}
</script>

<template>
  <el-dialog :model-value="modelValue" title="完成任务" width="640px" @close="emit('update:modelValue', false)">
    <el-form label-width="100px">
      <el-form-item label="任务">
        <span style="font-weight:500">{{ taskTitle }}</span>
      </el-form-item>
      <el-form-item label="实际完成时间">
        <span>{{ finishTime }}</span>
      </el-form-item>
      <el-form-item label="完成备注">
        <div style="width:100%">
          <RichEditor v-model="note" height="220px" />
          <div class="muted" style="margin-top:4px">记录一下这次完成的结果（可选，会保存到该任务的备注中）</div>
        </div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="confirm">完成</el-button>
    </template>
  </el-dialog>
</template>

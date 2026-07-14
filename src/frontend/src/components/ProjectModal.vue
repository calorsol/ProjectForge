<script setup>
import { reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { createProject, updateProject, listProjects } from '../api/project'

const props = defineProps({ modelValue: Boolean, project: Object })
const emit = defineEmits(['update:modelValue', 'saved'])
const empty = () => ({ name: '', code: '', sortNo: 1, description: '' })
const form = reactive(empty())

// 打开弹窗时：编辑回填原值；新建则排序号默认为「当前项目数量 + 1」
watch(() => props.modelValue, async open => {
  if (!open) return
  const p = props.project
  if (p?.id) {
    Object.assign(form, empty(), { name: p.name, code: p.code, sortNo: p.sortNo, description: p.description })
  } else {
    const list = await listProjects()
    Object.assign(form, empty(), { sortNo: list.length + 1 })
  }
})

const save = async () => {
  if (!form.name.trim() || !form.code.trim()) { ElMessage.warning('请填写项目名称和项目编号'); return }
  try {
    const data = props.project?.id ? await updateProject(props.project.id, form) : await createProject(form)
    ElMessage.success('已保存')
    emit('saved', data); emit('update:modelValue', false)
  } catch (e) { ElMessage.error(e.message) }
}
</script>

<template>
  <el-dialog :model-value="modelValue" :title="project ? '编辑项目' : '创建项目'" width="720px" @close="emit('update:modelValue', false)">
    <el-form label-width="90px">
      <el-form-item label="项目名称" required><el-input v-model="form.name" /></el-form-item>
      <el-form-item label="项目编号" required><el-input v-model="form.code" /></el-form-item>
      <el-form-item label="排序号"><el-input-number v-model="form.sortNo" :min="0" /></el-form-item>
      <el-form-item label="项目描述"><el-input v-model="form.description" type="textarea" :rows="8" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" @click="save">保存</el-button>
    </template>
  </el-dialog>
</template>

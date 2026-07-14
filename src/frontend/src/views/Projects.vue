<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listProjects, deleteProject } from '../api/project'
import { projectStatusLabel } from '../utils/dict'
import ProjectModal from '../components/ProjectModal.vue'
const router = useRouter()
const rows=ref([]), keyword=ref(''), status=ref(''), dialog=ref(false), editing=ref(null)
const load=async()=>{ rows.value=await listProjects({keyword:keyword.value||undefined,status:status.value||undefined}) }
const open=p=>{editing.value=p;dialog.value=true}
const remove=async p=>{ try{ await ElMessageBox.confirm(`确认删除项目「${p.name}」？项目下的任务不会被删除。`,'提示',{type:'warning'}) }catch{ return } await deleteProject(p.id); ElMessage.success('已删除'); load() }
onMounted(load)
</script>
<template><main class="page-container"><el-card class="page-card"><div class="toolbar"><el-input v-model="keyword" placeholder="搜索项目名称" style="width:200px" @keyup.enter="load"/><el-select v-model="status" placeholder="全部状态" clearable style="width:130px"><el-option label="进行中" value="ACTIVE"/><el-option label="已完成" value="DONE"/><el-option label="已归档" value="ARCHIVED"/></el-select><el-button @click="load">搜索</el-button><span class="spacer"/><el-button type="primary" @click="open(null)">＋ 创建项目</el-button></div><el-table :data="rows" style="width:100%" border stripe row-class-name="clickable-row" @row-click="row => router.push('/projects/'+row.id)"><el-table-column prop="name" label="项目名称" min-width="200" show-overflow-tooltip><template #default="{row}"><el-link type="primary" :underline="false">{{row.name}}</el-link></template></el-table-column><el-table-column prop="code" label="项目编号" min-width="150" show-overflow-tooltip/><el-table-column label="状态" min-width="100"><template #default="{row}"><el-tag size="small" type="warning">{{projectStatusLabel(row.status)}}</el-tag></template></el-table-column><el-table-column prop="taskTotal" label="任务总数" min-width="100"/><el-table-column prop="taskOpen" label="未完成" min-width="90"/><el-table-column prop="createdAt" label="创建时间" min-width="170"/><el-table-column label="操作" width="150" fixed="right"><template #default="{row}"><el-button link type="primary" @click.stop="open(row)">编辑</el-button><el-button link type="danger" @click.stop="remove(row)">删除</el-button></template></el-table-column></el-table></el-card><ProjectModal v-model="dialog" :project="editing" @saved="load"/></main></template>

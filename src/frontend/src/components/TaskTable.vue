<script setup>
import { taskStatusLabel, taskStatusType, priorityLabel, priorityType } from '../utils/dict'
defineProps({ rows: { type: Array, default: () => [] }, showProject: { type: Boolean, default: true } })
const emit = defineEmits(['action', 'edit', 'delete', 'open'])

// 行背景：已完成且不晚于截止=提前完成(绿)；逾期(未完成过期 或 完成晚于截止)=红
const rowClass = ({ row }) => {
  let cls = 'clickable-row'
  if (row.status === 'DONE' && row.dueAt && row.finishedAt) {
    cls += row.finishedAt <= row.dueAt ? ' row-early' : ' row-late'
  } else if (row.overdue) {
    cls += ' row-late'
  }
  return cls
}
</script>

<template>
  <el-table :data="rows" style="width:100%" border stripe :row-class-name="rowClass" @row-click="row => emit('open', row)">
    <el-table-column prop="title" label="任务名称" min-width="220" show-overflow-tooltip />
    <el-table-column v-if="showProject" prop="projectName" label="所属项目" min-width="140" show-overflow-tooltip />
    <el-table-column prop="type" label="类型" min-width="90" />
    <el-table-column label="优先级" min-width="90">
      <template #default="{ row }">
        <el-tag :type="priorityType(row.priority)" size="small">{{ priorityLabel(row.priority) }}</el-tag>
      </template>
    </el-table-column>
    <el-table-column label="计划开始" min-width="165">
      <template #default="{ row }">{{ row.planStartAt || '-' }}</template>
    </el-table-column>
    <el-table-column label="截止时间" min-width="180">
      <template #default="{ row }">
        <span :class="{ overdue: row.overdue }">{{ row.dueAt || '-' }}{{ row.overdue ? ' 逾期' : '' }}</span>
      </template>
    </el-table-column>
    <el-table-column label="状态" min-width="95">
      <template #default="{ row }">
        <el-tag :type="taskStatusType(row.status)" size="small">{{ taskStatusLabel(row.status) }}</el-tag>
      </template>
    </el-table-column>
    <el-table-column label="操作" width="280" fixed="right">
      <template #default="{ row }">
        <el-button v-if="row.status === 'TODO'" link @click.stop="emit('action', row, 'start')">开始</el-button>
        <el-button v-if="row.status === 'DOING'" link @click.stop="emit('action', row, 'pause')">暂停</el-button>
        <el-button v-if="row.status === 'PAUSED'" link @click.stop="emit('action', row, 'resume')">继续</el-button>
        <el-button v-if="['DOING', 'PAUSED'].includes(row.status)" link type="success" @click.stop="emit('action', row, 'finish')">完成</el-button>
        <el-button v-if="row.status === 'DONE'" link @click.stop="emit('action', row, 'reopen')">重开</el-button>
        <el-button v-if="['TODO', 'DOING', 'PAUSED'].includes(row.status)" link @click.stop="emit('action', row, 'cancel')">取消</el-button>
        <el-button link type="primary" @click.stop="emit('edit', row)">编辑</el-button>
        <el-button link type="danger" @click.stop="emit('delete', row)">删除</el-button>
      </template>
    </el-table-column>
  </el-table>
</template>

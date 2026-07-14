// 状态、优先级、角色等枚举的中文与标签样式映射
export const TASK_STATUS = {
  TODO:     { label: '未开始', type: 'info' },
  DOING:    { label: '进行中', type: 'warning' },
  PAUSED:   { label: '已暂停', type: 'info' },
  DONE:     { label: '已完成', type: 'success' },
  CANCELED: { label: '已取消', type: 'info' }
}
export const taskStatusLabel = s => TASK_STATUS[s]?.label || s
export const taskStatusType  = s => TASK_STATUS[s]?.type || 'info'

export const PRIORITY = { HIGH: '高', MEDIUM: '中', LOW: '低' }
export const priorityLabel = p => PRIORITY[p] || p
export const priorityType  = p => (p === 'HIGH' ? 'danger' : p === 'LOW' ? 'info' : 'warning')

export const PROJECT_STATUS = { ACTIVE: '进行中', DONE: '已完成', ARCHIVED: '已归档' }
export const projectStatusLabel = s => PROJECT_STATUS[s] || s

export const roleLabel   = r => (r === 'ADMIN' ? '超级管理员' : '普通用户')
export const statusLabel = s => (s === 'ENABLED' ? '启用' : '禁用')

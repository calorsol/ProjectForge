import client from './client'
export const listTasks=params=>client.get('/tasks',{params})
export const workbenchTasks=tab=>client.get('/workbench/tasks',{params:{tab}})
export const getTask=id=>client.get(`/tasks/${id}`)
export const createTask=data=>client.post('/tasks',data)
export const updateTask=(id,data)=>client.put(`/tasks/${id}`,data)
export const deleteTask=id=>client.delete(`/tasks/${id}`)
export const changeStatus=(id,action)=>client.patch(`/tasks/${id}/status`,{action})

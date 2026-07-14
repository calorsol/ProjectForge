import client from './client'
export const listProjects = params => client.get('/projects', { params })
export const createProject = data => client.post('/projects', data)
export const updateProject = (id, data) => client.put(`/projects/${id}`, data)
export const getProject = id => client.get(`/projects/${id}`)
export const deleteProject = id => client.delete(`/projects/${id}`)

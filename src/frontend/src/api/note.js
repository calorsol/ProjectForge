import client from './client'
export const listNotes = taskId => client.get(`/tasks/${taskId}/notes`)
export const addNote = (taskId, content) => client.post(`/tasks/${taskId}/notes`, { content })
export const updateNote = (id, content) => client.put(`/notes/${id}`, { content })
export const deleteNote = id => client.delete(`/notes/${id}`)

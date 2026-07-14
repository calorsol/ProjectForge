import axios from 'axios'

export const API_BASE = 'http://127.0.0.1:8080/api'
export const BACKEND_ORIGIN = API_BASE.replace(/\/api$/, '')
const client = axios.create({ baseURL: API_BASE })
client.interceptors.request.use(config => { const token = localStorage.getItem('ppm_token'); if (token) config.headers.Authorization = `Bearer ${token}`; return config })
client.interceptors.response.use(response => {
  const body = response.data
  if (body.code !== 0) return Promise.reject(new Error(body.message || '请求失败'))
  return body.data
}, error => Promise.reject(new Error(error.response?.data?.message || '网络请求失败')))
export default client

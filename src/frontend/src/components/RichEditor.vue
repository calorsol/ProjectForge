<script setup>
import '@wangeditor/editor/dist/css/style.css'
import { onBeforeUnmount, shallowRef, ref, watch } from 'vue'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import { ElMessage } from 'element-plus'
import { uploadAttachment } from '../api/attachment'
import { BACKEND_ORIGIN } from '../api/client'

const props = defineProps({ modelValue: { type: String, default: '' }, height: { type: String, default: '260px' } })
const emit = defineEmits(['update:modelValue'])

const editorRef = shallowRef()
const valueHtml = ref(props.modelValue || '')

watch(() => props.modelValue, v => { if ((v || '') !== valueHtml.value) valueHtml.value = v || '' })
watch(valueHtml, v => emit('update:modelValue', v))

const toolbarConfig = { excludeKeys: ['group-video', 'fullScreen', 'insertImage'] }
const editorConfig = {
  placeholder: '请输入任务描述，可直接 Ctrl+V 粘贴截图（图片会内嵌进正文）',
  MENU_CONF: {
    uploadImage: {
      async customUpload(file, insertFn) {
        try {
          const r = await uploadAttachment(file, 'IMAGE')
          insertFn(BACKEND_ORIGIN + r.url, r.fileName, BACKEND_ORIGIN + r.url)
        } catch (e) { ElMessage.error('图片上传失败：' + e.message) }
      }
    }
  }
}

const handleCreated = editor => { editorRef.value = editor }
onBeforeUnmount(() => { editorRef.value?.destroy() })
</script>

<template>
  <div style="border:1px solid var(--app-border); border-radius:4px; width:100%; overflow:hidden;">
    <Toolbar :editor="editorRef" :default-config="toolbarConfig" mode="default" style="border-bottom:1px solid var(--app-border);" />
    <Editor v-model="valueHtml" :default-config="editorConfig" mode="default" :style="{ height: height, 'overflow-y': 'auto' }" @on-created="handleCreated" />
  </div>
</template>

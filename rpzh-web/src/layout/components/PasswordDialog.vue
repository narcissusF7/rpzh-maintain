<template>
  <el-dialog
    :model-value="modelValue"
    title="修改密码"
    width="420px"
    append-to-body
    @update:model-value="(v) => emit('update:modelValue', v)"
    @closed="reset"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="88px">
      <el-form-item label="账号">
        <el-input :model-value="userStore.userInfo.username || ''" disabled />
      </el-form-item>
      <el-form-item label="新密码" prop="newpassWord">
        <el-input v-model="form.newpassWord" type="password" show-password placeholder="请输入新密码" />
      </el-form-item>
      <el-form-item label="确认密码" prop="notarizePassword">
        <el-input
          v-model="form.notarizePassword"
          type="password"
          show-password
          placeholder="请再次输入新密码"
        />
      </el-form-item>
      <div class="form-tip">密码中不能包含中文，修改后请使用新密码重新登录。</div>
    </el-form>

    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" :loading="loading" @click="submit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { updatePassword } from '@/api/user'
import { useUserStore } from '@/store/modules/user'

const props = defineProps({
  modelValue: { type: Boolean, default: false }
})
const emit = defineEmits(['update:modelValue'])

const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  newpassWord: '',
  notarizePassword: ''
})

const validateConfirm = (rule, value, callback) => {
  if (value !== form.newpassWord) {
    callback(new Error('两次输入的密码不一致'))
    return
  }
  callback()
}

const rules = {
  newpassWord: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  notarizePassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

function reset() {
  form.newpassWord = ''
  form.notarizePassword = ''
  formRef.value?.clearValidate()
}

async function submit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  const username = userStore.userInfo.username
  if (!username) {
    ElMessage.warning('未获取到当前账号，请重新登录')
    return
  }

  loading.value = true
  try {
    await updatePassword(username, form.newpassWord, form.notarizePassword)
    ElMessage.success('密码修改成功')
    emit('update:modelValue', false)
  } catch (e) {
    // 错误提示已在请求拦截器统一处理
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-wrapper">
    <div class="login-card">
      <div class="login-title">养护管理系统</div>
      <div class="login-subtitle">rpzh-maintain · 系统管理端</div>

      <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="submit">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入账号" clearable>
            <template #prefix><el-icon><User /></el-icon></template>
          </el-input>
        </el-form-item>

        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password>
            <template #prefix><el-icon><Lock /></el-icon></template>
          </el-input>
        </el-form-item>

        <el-form-item prop="verificationCode">
          <div style="display: flex; gap: 10px; width: 100%">
            <el-input v-model="form.verificationCode" placeholder="请输入验证码" clearable />
            <div style="width: 120px; flex-shrink: 0">
              <img
                v-if="captchaUrl"
                :src="captchaUrl"
                class="captcha-img"
                alt="验证码"
                @click="refreshCaptcha"
              />
              <div v-else class="captcha-placeholder" @click="refreshCaptcha">
                {{ captchaLoading ? '加载中…' : captchaError || '点击获取' }}
              </div>
            </div>
          </div>
        </el-form-item>

        <el-button type="primary" size="large" style="width: 100%" :loading="loading" @click="submit">
          登 录
        </el-button>

        <el-button
          v-if="demoEnabled"
          text
          size="small"
          style="width: 100%; margin-top: 10px; margin-left: 0"
          @click="demoLogin"
        >
          后端登录不可用？本地点一次进入
        </el-button>

        <el-button
          v-if="platformSsoEnabled"
          type="success"
          plain
          size="large"
          style="width: 100%; margin-top: 12px; margin-left: 0"
          :loading="ssoLoading"
          @click="platformSsoLogin"
        >
          中台账号登录
        </el-button>
      </el-form>

      <div class="login-tip">
        测试账号 <code>rpzh</code> / <code>123456</code>，另有 admin、zhangwei、lina、wangqiang、chenjing（密码相同）。
        后端暂未校验图形验证码，填任意值即可。
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCaptcha } from '@/api/auth'
import { useUserStore } from '@/store/modules/user'
import { usePermissionStore } from '@/store/modules/permission'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const permissionStore = usePermissionStore()

const demoEnabled = import.meta.env.VITE_ENABLE_DEMO_LOGIN === 'true'
const platformSsoEnabled = import.meta.env.VITE_ENABLE_PLATFORM_SSO === 'true'
const ssoLoading = ref(false)

const formRef = ref()
const loading = ref(false)
const captchaUrl = ref('')
const captchaLoading = ref(false)
const captchaError = ref('')

const form = reactive({
  username: '',
  password: '',
  uuidCode: '',
  verificationCode: ''
})

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  verificationCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

function genUuid() {
  // 后端只要求 uuidCode 非空且能作为 redis key，这里生成一个随机串即可
  if (window.crypto?.randomUUID) return window.crypto.randomUUID()
  return `web-${Date.now()}-${Math.random().toString(16).slice(2, 10)}`
}

async function refreshCaptcha() {
  form.uuidCode = genUuid()
  captchaLoading.value = true
  captchaError.value = ''

  try {
    const blob = await getCaptcha(form.uuidCode)
    if (blob && blob.type && blob.type.indexOf('image') === -1) {
      throw new Error('验证码接口返回的不是图片')
    }
    if (captchaUrl.value) URL.revokeObjectURL(captchaUrl.value)
    captchaUrl.value = URL.createObjectURL(blob)
  } catch (e) {
    captchaUrl.value = ''
    captchaError.value = '验证码不可用'
  } finally {
    captchaLoading.value = false
  }
}

function afterLogin() {
  permissionStore.reset()
  const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
  router.replace(redirect)
}

async function submit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.login(form)
    ElMessage.success('登录成功')
    afterLogin()
  } catch (e) {
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}

function demoLogin() {
  userStore.demoLogin('admin')
  ElMessage.success('已进入演示模式')
  afterLogin()
}

/** 中台账号登录：整页跳转到后端 /user/sso/login，由后端重定向到中台授权页 */
function platformSsoLogin() {
  ssoLoading.value = true
  const base = import.meta.env.VITE_API_BASE || '/api'
  window.location.href = `${base}/user/sso/login`
}

onMounted(() => {
  // 中台 SSO 回跳：URL 上携带 ?token=bearer xxx（或 ?error=...）
  const q = route.query
  if (typeof q.error === 'string' && q.error) {
    ElMessage.error('中台登录失败，请重试或使用本地账号')
    refreshCaptcha()
    return
  }
  if (typeof q.token === 'string' && q.token) {
    try {
      userStore.ssoLogin(q.token)
      ElMessage.success('中台登录成功')
      afterLogin()
      return
    } catch (e) {
      ElMessage.error('中台登录回跳处理失败')
    }
  }
  refreshCaptcha()
})
</script>

<template>
  <div class="app-page">
    <el-row :gutter="12">
      <el-col :xs="24" :md="8">
        <div class="app-card profile-card">
          <el-avatar :size="72" :src="profile.avatar">{{ avatarText }}</el-avatar>
          <div class="name">{{ profile.nickname || profile.username || userStore.displayName }}</div>
          <div class="account">@{{ profile.username || userStore.userInfo.username || '-' }}</div>
          <el-divider />
          <div class="meta-row"><span>所属部门</span><b>{{ profile.deptname || '-' }}</b></div>
          <div class="meta-row"><span>角色</span><b>{{ roleText }}</b></div>
          <div class="meta-row"><span>最后登录</span><b>{{ formatTime(profile.loginDate) }}</b></div>
        </div>
      </el-col>

      <el-col :xs="24" :md="16">
        <div class="app-card panel">
          <div class="panel-title">基本资料</div>
          <el-alert
            v-if="userStore.demoMode"
            type="warning"
            :closable="false"
            show-icon
            title="当前是免登录模式，后端取不到登录用户，保存资料可能失败"
            style="margin-bottom: 12px"
          />

          <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
            <el-form-item label="账号">
              <el-input :model-value="form.userName" disabled />
            </el-form-item>
            <el-form-item label="昵称" prop="nickName">
              <el-input v-model="form.nickName" placeholder="请输入昵称" />
            </el-form-item>
            <el-form-item label="手机号" prop="phonenumber">
              <el-input v-model="form.phonenumber" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="性别">
              <el-radio-group v-model="form.sex">
                <el-radio v-for="item in SEX_OPTIONS" :key="item.value" :value="item.value">
                  {{ item.label }}
                </el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="submitting" @click="submit">保存</el-button>
              <el-button @click="loadProfile">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getProfile, updateProfile } from '@/api/user'
import { formatTime } from '@/utils/form'
import { SEX_OPTIONS } from '@/utils/dict'
import { useUserStore } from '@/store/modules/user'

const userStore = useUserStore()
const formRef = ref()
const submitting = ref(false)
const profile = ref({})

const form = reactive({
  userName: '',
  nickName: '',
  phonenumber: '',
  email: '',
  sex: '2'
})

const rules = {
  nickName: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  phonenumber: [{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
}

const avatarText = computed(() => (profile.value.nickname || profile.value.username || '游').slice(0, 1))

const roleText = computed(() => {
  const names = profile.value.roleNames
  if (Array.isArray(names) && names.length) return names.join('、')
  return '-'
})

async function loadProfile() {
  try {
    const data = await getProfile()
    profile.value = data || {}
  } catch (e) {
    // 免登录模式下接口可能拿不到登录用户，退回本地缓存
    profile.value = { ...userStore.userInfo }
  }

  Object.assign(form, {
    userName: profile.value.username || userStore.userInfo.username || '',
    nickName: profile.value.nickname || userStore.userInfo.nickname || '',
    phonenumber: profile.value.phonenumber || '',
    email: profile.value.email || '',
    sex: profile.value.sex != null ? String(profile.value.sex) : '2'
  })
}

async function submit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await updateProfile({
      userName: form.userName,
      nickName: form.nickName,
      phonenumber: form.phonenumber,
      email: form.email,
      sex: form.sex
    })
    ElMessage.success('保存成功')
    userStore.userInfo = { ...userStore.userInfo, nickname: form.nickName, email: form.email, phonenumber: form.phonenumber }
    loadProfile()
  } catch (e) {
    // 拦截器已提示
  } finally {
    submitting.value = false
  }
}

onMounted(loadProfile)
</script>

<style scoped>
.profile-card {
  padding: 24px 20px;
  text-align: center;
}

.profile-card .name {
  margin-top: 10px;
  font-size: 17px;
  font-weight: 600;
}

.profile-card .account {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 2px;
}

.meta-row {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  padding: 6px 0;
  color: var(--el-text-color-secondary);
}

.meta-row b {
  color: var(--el-text-color-primary);
  font-weight: 500;
}

.panel {
  padding: 16px 20px 4px;
}

.panel-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 14px;
}
</style>

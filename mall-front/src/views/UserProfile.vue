<template>
  <div class="profile-page">
    <h2 class="page-title">个人中心</h2>

    <div class="profile-layout">
      <!-- 基本信息 -->
      <section class="profile-card">
        <h3 class="card-title">
          <el-icon :size="18"><User /></el-icon>基本信息
        </h3>
        <div class="input-group">
          <label class="input-label">昵称</label>
          <el-input v-model="profileForm.nickname" placeholder="请输入昵称" class="custom-input" />
        </div>
        <div class="input-group">
          <label class="input-label">手机号</label>
          <el-input v-model="profileForm.phone" placeholder="请输入手机号" class="custom-input" />
        </div>
        <div class="input-group">
          <label class="input-label">邮箱</label>
          <el-input v-model="profileForm.email" placeholder="请输入邮箱" class="custom-input" />
        </div>
        <button class="save-btn" :class="{ loading: profileLoading }" :disabled="profileLoading" @click="handleUpdateProfile">
          <span v-if="!profileLoading">保存修改</span>
          <span v-else class="btn-spinner"></span>
        </button>
      </section>

      <!-- 修改密码 -->
      <section class="profile-card">
        <h3 class="card-title">
          <el-icon :size="18"><Lock /></el-icon>修改密码
        </h3>
        <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules">
          <div class="input-group">
            <label class="input-label">旧密码</label>
            <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入旧密码" class="custom-input" />
          </div>
          <div class="input-group">
            <label class="input-label">新密码</label>
            <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码" class="custom-input" />
          </div>
          <div class="input-group">
            <label class="input-label">确认密码</label>
            <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请确认新密码" class="custom-input" />
          </div>
          <button class="save-btn secondary" :class="{ loading: pwdLoading }" :disabled="pwdLoading" @click.prevent="handleChangePwd">
            <span v-if="!pwdLoading">修改密码</span>
            <span v-else class="btn-spinner"></span>
          </button>
        </el-form>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { getCurrentUser } from '../api/user'
import request from '../api/request'

const store = useUserStore()
const profileLoading = ref(false)
const pwdLoading = ref(false)
const pwdFormRef = ref(null)

const profileForm = reactive({
  nickname: '',
  phone: '',
  email: '',
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const validateConfirmPwd = (rule, value, callback) => {
  if (value !== pwdForm.newPassword) {
    callback(new Error('两次密码不一致'))
  } else {
    callback()
  }
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度6-100位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPwd, trigger: 'blur' },
  ],
}

const fetchProfile = async () => {
  const res = await getCurrentUser()
  profileForm.nickname = res.data.nickname || ''
  profileForm.phone = res.data.phone || ''
  profileForm.email = res.data.email || ''
}

const handleUpdateProfile = async () => {
  profileLoading.value = true
  try {
    await request.put('/user/profile', {
      nickname: profileForm.nickname || undefined,
      phone: profileForm.phone || undefined,
      email: profileForm.email || undefined,
    })
    ElMessage.success('修改成功')
  } finally {
    profileLoading.value = false
  }
}

const handleChangePwd = async () => {
  const valid = await pwdFormRef.value.validate().catch(() => false)
  if (!valid) return
  pwdLoading.value = true
  try {
    await request.put('/user/password', {
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword,
    })
    ElMessage.success('密码修改成功，请重新登录')
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
    setTimeout(() => store.logout(), 1500)
  } finally {
    pwdLoading.value = false
  }
}

onMounted(fetchProfile)
</script>

<style scoped>
.profile-page { max-width: 720px; }

.page-title {
  font-size: 22px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 24px;
  letter-spacing: -0.3px;
}

.profile-layout {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.profile-card {
  background: #fff;
  border-radius: 14px;
  padding: 24px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  border: 1px solid #f0efed;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 20px;
  padding-bottom: 14px;
  border-bottom: 1px solid #f0efed;
}

.card-title .el-icon { color: #FF6B35; }

.input-group {
  margin-bottom: 16px;
}

.input-label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: #666;
  margin-bottom: 6px;
}

.custom-input {
  --el-input-border-radius: 10px;
  --el-input-bg-color: #f8f7f5;
  --el-input-border-color: transparent;
  --el-input-hover-border-color: #e0ded9;
  --el-input-focus-border-color: #FF6B35;
}

.custom-input :deep(.el-input__wrapper) {
  padding: 4px 14px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.02);
}

.custom-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #FF6B35, 0 2px 8px rgba(255,107,53,0.08);
}

.save-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 28px;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, #FF6B35 0%, #FF8F5E 100%);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 0.5px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 3px 10px rgba(255,107,53,0.2);
  margin-top: 4px;
}

.save-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 18px rgba(255,107,53,0.3);
}

.save-btn:disabled { opacity: 0.6; cursor: not-allowed; }
.save-btn.loading { pointer-events: none; }

.save-btn.secondary {
  background: #1a1a2e;
  box-shadow: 0 3px 10px rgba(0,0,0,0.15);
}

.save-btn.secondary:hover:not(:disabled) {
  box-shadow: 0 6px 18px rgba(0,0,0,0.25);
}

.btn-spinner {
  width: 18px; height: 18px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }

@media (max-width: 768px) {
  .profile-card { padding: 18px; }
}
</style>

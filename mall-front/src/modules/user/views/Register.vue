<template>
  <div class="auth-wrapper">
    <div class="auth-split">
      <!-- 左侧品牌区 -->
      <div class="auth-brand">
        <div class="brand-overlay"></div>
        <div class="brand-content">
          <div class="brand-logo">
            <span class="logo-icon">&#9670;</span>
            <h1>MALL</h1>
          </div>
          <p class="brand-tagline">开启品质之旅</p>
          <p class="brand-desc">创建账号，发现更多惊喜好物</p>
          <div class="brand-steps">
            <div class="step">
              <span class="step-num">01</span>
              <span class="step-text">填写信息</span>
            </div>
            <div class="step-divider"></div>
            <div class="step">
              <span class="step-num">02</span>
              <span class="step-text">验证注册</span>
            </div>
            <div class="step-divider"></div>
            <div class="step">
              <span class="step-num">03</span>
              <span class="step-text">开始购物</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧表单区 -->
      <div class="auth-form">
        <div class="form-inner">
          <div class="form-header">
            <h2>创建账号</h2>
            <p>注册 Mall 商城，享受品质购物</p>
          </div>

          <el-form ref="formRef" :model="form" :rules="rules" @keyup.enter="handleRegister">
            <div class="input-group">
              <label class="input-label">用户名</label>
              <el-input
                v-model="form.username"
                placeholder="设置登录用户名"
                :prefix-icon="User"
                class="custom-input"
              />
            </div>

            <div class="input-group">
              <label class="input-label">密码</label>
              <el-input
                v-model="form.password"
                type="password"
                placeholder="设置登录密码"
                :prefix-icon="Lock"
                show-password
                class="custom-input"
              />
            </div>

            <div class="input-group">
              <label class="input-label">确认密码</label>
              <el-input
                v-model="form.confirmPassword"
                type="password"
                placeholder="再次输入密码"
                :prefix-icon="Lock"
                show-password
                class="custom-input"
              />
            </div>

            <div class="input-group">
              <label class="input-label">手机号 <span class="optional">（选填）</span></label>
              <el-input
                v-model="form.phone"
                placeholder="方便物流联系"
                :prefix-icon="Phone"
                class="custom-input"
              />
            </div>

            <div class="input-group">
              <label class="input-label">注册类型</label>
              <el-radio-group v-model="form.role" class="role-options">
                <el-radio :label="0">买家</el-radio>
                <el-radio :label="1">商家</el-radio>
              </el-radio-group>
            </div>

            <button
              type="button"
              class="submit-btn"
              :class="{ loading: loading }"
              :disabled="loading"
              @click="handleRegister"
            >
              <span v-if="!loading">注 册</span>
              <span v-else class="btn-spinner"></span>
            </button>
          </el-form>

          <div class="form-footer">
            <span>已有账号？</span>
            <router-link to="/login" class="link">去登录</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { User, Lock, Phone } from '@element-plus/icons-vue'
import { useUserStore } from '../../../stores/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  phone: '',
  role: 0,
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度3-50位', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度6-100位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
}

const handleRegister = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await userStore.handleRegister({
      username: form.username,
      password: form.password,
      phone: form.phone || undefined,
      role: Number(form.role),
    })
    ElMessage.success('注册成功，请登录')
  } catch {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-wrapper {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f3f0;
  padding: 20px;
}

.auth-split {
  display: flex;
  width: 960px;
  min-height: 640px;
  background: #fff;
  border-radius: 20px;
  overflow: hidden;
  box-shadow:
    0 4px 6px -1px rgba(0,0,0,0.02),
    0 10px 15px -3px rgba(0,0,0,0.03),
    0 25px 50px -12px rgba(0,0,0,0.08);
}

/* ===== 左侧品牌区 ===== */
.auth-brand {
  flex: 1;
  position: relative;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 40%, #0f3460 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.brand-overlay {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 20% 30%, rgba(255,107,53,0.15) 0%, transparent 50%),
    radial-gradient(circle at 80% 70%, rgba(255,107,53,0.08) 0%, transparent 50%);
}

.brand-content {
  position: relative;
  z-index: 1;
  text-align: center;
  padding: 60px 40px;
}

.brand-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 20px;
}

.logo-icon {
  font-size: 36px;
  color: #FF6B35;
  filter: drop-shadow(0 0 20px rgba(255,107,53,0.4));
  animation: logoPulse 3s ease-in-out infinite;
}

@keyframes logoPulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.8; transform: scale(1.05); }
}

.brand-logo h1 {
  font-size: 42px;
  font-weight: 300;
  letter-spacing: 8px;
  color: #fff;
  margin: 0;
}

.brand-tagline {
  font-size: 18px;
  color: rgba(255,255,255,0.8);
  margin: 0 0 8px;
  letter-spacing: 2px;
  font-weight: 300;
}

.brand-desc {
  font-size: 14px;
  color: rgba(255,255,255,0.45);
  margin: 0 0 48px;
}

.brand-steps {
  display: flex;
  flex-direction: column;
  gap: 0;
  align-items: center;
}

.step {
  display: flex;
  align-items: center;
  gap: 14px;
}

.step-num {
  font-size: 28px;
  font-weight: 200;
  color: #FF6B35;
  letter-spacing: 2px;
  font-family: 'Georgia', serif;
}

.step-text {
  font-size: 13px;
  color: rgba(255,255,255,0.55);
  letter-spacing: 1px;
}

.step-divider {
  width: 1px;
  height: 20px;
  background: linear-gradient(to bottom, rgba(255,107,53,0.4), transparent);
  margin: 4px 0 4px 17px;
  align-self: flex-start;
}

/* ===== 右侧表单区 ===== */
.auth-form {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 50px 48px;
}

.form-inner {
  width: 100%;
  max-width: 360px;
}

.form-header {
  margin-bottom: 32px;
}

.form-header h2 {
  font-size: 28px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 8px;
  letter-spacing: -0.5px;
}

.form-header p {
  font-size: 14px;
  color: #999;
  margin: 0;
}

/* 输入组 */
.input-group {
  margin-bottom: 18px;
}

.input-label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: #666;
  margin-bottom: 6px;
  letter-spacing: 0.3px;
}

.optional {
  font-weight: 400;
  color: #bbb;
  font-size: 12px;
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
  transition: all 0.25s ease;
}

.custom-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #FF6B35, 0 2px 8px rgba(255,107,53,0.08);
}

.custom-input :deep(.el-input__prefix) {
  color: #bbb;
}

.role-options { display: flex; gap: 18px; height: 32px; align-items: center; }

/* 提交按钮 */
.submit-btn {
  width: 100%;
  height: 48px;
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, #FF6B35 0%, #FF8F5E 100%);
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 2px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 15px rgba(255,107,53,0.25);
  margin-top: 8px;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(255,107,53,0.35);
}

.submit-btn:active {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(255,107,53,0.2);
}

.submit-btn:disabled {
  cursor: not-allowed;
  opacity: 0.7;
}

.submit-btn.loading {
  pointer-events: none;
}

.btn-spinner {
  display: inline-block;
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 底部链接 */
.form-footer {
  text-align: center;
  margin-top: 28px;
  font-size: 14px;
  color: #999;
}

.link {
  color: #FF6B35;
  text-decoration: none;
  font-weight: 500;
  margin-left: 4px;
  transition: color 0.2s;
}

.link:hover {
  color: #e55a2b;
}

/* 响应式 */
@media (max-width: 768px) {
  .auth-split {
    flex-direction: column;
    width: 100%;
    min-height: auto;
  }

  .auth-brand {
    padding: 40px 20px;
    min-height: 180px;
  }

  .brand-content {
    padding: 20px;
  }

  .brand-logo h1 {
    font-size: 28px;
  }

  .brand-desc {
    margin-bottom: 0;
  }

  .brand-steps {
    display: none;
  }

  .auth-form {
    padding: 32px 24px;
  }
}
</style>

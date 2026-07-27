<template>
  <div class="home-container">
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <h1>Mall 商城</h1>
        </div>
        <div class="header-right">
          <span class="user-info" v-if="store.user">
            <el-avatar :size="32" icon="UserFilled" />
            <span class="username">{{ store.user.nickname || store.user.username }}</span>
          </span>
          <el-button type="danger" text @click="store.logout">退出登录</el-button>
        </div>
      </el-header>
      <el-main>
        <div class="welcome">
          <h2>欢迎回来，{{ store.user?.nickname || store.user?.username }}</h2>
          <p>登录成功！以下是您的账号信息：</p>
          <el-descriptions :column="2" border style="margin-top: 24px; max-width: 600px;">
            <el-descriptions-item label="用户ID">{{ store.user?.id }}</el-descriptions-item>
            <el-descriptions-item label="用户名">{{ store.user?.username }}</el-descriptions-item>
            <el-descriptions-item label="昵称">{{ store.user?.nickname || '-' }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ store.user?.phone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="角色">{{ store.user?.role === 0 ? '普通用户' : '管理员' }}</el-descriptions-item>
            <el-descriptions-item label="状态">{{ store.user?.status === 1 ? '正常' : '禁用' }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { useUserStore } from '../stores/user'

const store = useUserStore()
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background: #f5f7fa;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 24px;
  height: 60px;
}

.header-left h1 {
  font-size: 20px;
  color: #667eea;
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #303133;
}

.username {
  font-size: 14px;
}

.welcome {
  max-width: 800px;
  margin: 60px auto;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.welcome h2 {
  font-size: 24px;
  color: #303133;
  margin-bottom: 8px;
}

.welcome p {
  color: #909399;
  font-size: 14px;
}
</style>
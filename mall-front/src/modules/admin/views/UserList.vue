<template>
  <section class="user-page">
    <div class="filter-card">
      <el-form :inline="true" :model="query" @submit.prevent="search">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" clearable placeholder="用户名、昵称或手机号" @keyup.enter="search" />
        </el-form-item>
        <el-form-item label="账号状态">
          <el-select v-model="query.status" clearable placeholder="全部状态" class="filter-select">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="query.role" clearable placeholder="全部角色" class="filter-select">
            <el-option label="买家" :value="0" />
            <el-option label="卖家" :value="1" />
            <el-option label="管理员" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="search">查询</el-button>
          <el-button :icon="Refresh" @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-card">
      <div class="table-heading">
        <div>
          <h2>用户列表</h2>
          <p>管理平台用户账号状态；禁用后的账号无法再次登录。</p>
        </div>
        <el-tag effect="plain">共 {{ total }} 位用户</el-tag>
      </div>

      <el-table v-loading="loading" :data="records" stripe>
        <el-table-column label="用户" min-width="185">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="34" :src="row.avatar" icon="UserFilled" />
              <div>
                <strong>{{ row.nickname || row.username }}</strong>
                <span>@{{ row.username }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" min-width="130">
          <template #default="{ row }">{{ row.phone || '-' }}</template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="180">
          <template #default="{ row }">{{ row.email || '-' }}</template>
        </el-table-column>
        <el-table-column label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="roleType(row.role)" effect="light">{{ roleText(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="light">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" min-width="170" />
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              :type="row.status === 1 ? 'danger' : 'success'"
              :disabled="Number(row.id) === Number(store.user?.id)"
              @click="confirmStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchUsers"
          @current-change="fetchUsers"
        />
      </div>
    </div>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Search } from '@element-plus/icons-vue'
import { useUserStore } from '../../../stores/user'
import { getAdminUserPage, updateAdminUserStatus } from '../api/adminUser'

const store = useUserStore()
const loading = ref(false)
const records = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 20, keyword: '', status: undefined, role: undefined })

const roleText = (role) => ({ 0: '买家', 1: '卖家', 2: '管理员' }[Number(role)] || '未知')
const roleType = (role) => ({ 0: '', 1: 'warning', 2: 'danger' }[Number(role)] || 'info')

const fetchUsers = async () => {
  loading.value = true
  try {
    const res = await getAdminUserPage(query)
    records.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const search = () => {
  query.pageNum = 1
  fetchUsers()
}

const reset = () => {
  Object.assign(query, { pageNum: 1, pageSize: 20, keyword: '', status: undefined, role: undefined })
  fetchUsers()
}

const confirmStatus = async (user) => {
  const nextStatus = user.status === 1 ? 0 : 1
  const action = nextStatus === 1 ? '启用' : '禁用'
  await ElMessageBox.confirm(`确认${action}用户“${user.username}”吗？`, '用户状态变更', {
    type: 'warning',
    confirmButtonText: `确认${action}`,
    cancelButtonText: '取消',
  })
  await updateAdminUserStatus(user.id, nextStatus)
  ElMessage.success(`用户已${action}`)
  await fetchUsers()
}

onMounted(fetchUsers)
</script>

<style scoped>
.user-page { max-width: 1280px; margin: 0 auto; }
.filter-card, .table-card { background: #fff; border: 1px solid #e9edf4; border-radius: 14px; box-shadow: 0 3px 12px rgba(32, 50, 80, .025); }
.filter-card { padding: 20px 22px 4px; }
.filter-select { width: 130px; }
.table-card { margin-top: 20px; overflow: hidden; }
.table-heading { display: flex; align-items: center; justify-content: space-between; padding: 22px; }
.table-heading h2 { margin: 0 0 7px; color: #273246; font-size: 17px; }
.table-heading p { margin: 0; color: #99a3b5; font-size: 13px; }
.user-cell { display: flex; align-items: center; gap: 10px; }
.user-cell :deep(.el-avatar) { background: #edf2ff; color: #5b7fe9; }
.user-cell strong, .user-cell span { display: block; }
.user-cell strong { color: #30394a; font-size: 13px; }
.user-cell span { margin-top: 3px; color: #9ca5b4; font-size: 12px; }
.pagination-wrap { display: flex; justify-content: flex-end; padding: 18px 22px; border-top: 1px solid #edf0f5; }
@media (max-width: 760px) { .filter-card :deep(.el-form-item) { margin-right: 0; } .table-heading { align-items: flex-start; gap: 12px; flex-direction: column; } .pagination-wrap { justify-content: center; overflow-x: auto; } }
</style>

<template>
  <div class="address-page" v-loading="loading">
    <div class="page-header">
      <h2 class="page-title">收货地址</h2>
      <button class="add-btn" @click="showDialog(null)">
        <el-icon :size="16"><Plus /></el-icon>
        <span>新增地址</span>
      </button>
    </div>

    <!-- 地址列表 -->
    <div v-if="addresses.length" class="address-list">
      <div v-for="addr in addresses" :key="addr.id" class="addr-card">
        <div class="addr-body">
          <div class="addr-line">
            <strong>{{ addr.receiverName }}</strong>
            <span class="addr-phone">{{ addr.receiverPhone }}</span>
            <span v-if="addr.isDefault === 1" class="default-tag">默认</span>
          </div>
          <p class="addr-text">{{ addr.province }}{{ addr.city }}{{ addr.district }} {{ addr.detail }}</p>
        </div>
        <div class="addr-actions">
          <button class="action-btn edit" @click="showDialog(addr)">
            <el-icon :size="15"><Edit /></el-icon>
          </button>
          <button class="action-btn del" @click="handleDelete(addr.id)">
            <el-icon :size="15"><Delete /></el-icon>
          </button>
        </div>
      </div>
    </div>

    <div v-else class="empty-state">
      <el-icon :size="48" color="#ddd"><MapLocation /></el-icon>
      <p>暂无收货地址</p>
    </div>

    <!-- 编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editAddr ? '编辑地址' : '新增地址'"
      width="480px"
      :close-on-click-modal="false"
    >
      <el-form ref="addrFormRef" :model="addrForm" :rules="addrRules" label-position="top">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="收货人" prop="receiverName">
              <el-input v-model="addrForm.receiverName" placeholder="请输入收货人姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="receiverPhone">
              <el-input v-model="addrForm.receiverPhone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="省份" prop="province">
              <el-select v-model="addrForm.province" filterable clearable placeholder="请选择省份" @change="handleProvinceChange">
                <el-option v-for="option in provinceOptions" :key="option.value" :label="option.label" :value="option.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="城市" prop="city">
              <el-select v-model="addrForm.city" filterable clearable :disabled="!addrForm.province" placeholder="请选择城市" @change="handleCityChange">
                <el-option v-for="option in cityOptions" :key="option.value" :label="option.label" :value="option.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="区/县" prop="district">
              <el-select v-model="addrForm.district" filterable clearable :disabled="!addrForm.city" placeholder="请选择区/县">
                <el-option v-for="option in districtOptions" :key="option.value" :label="option.label" :value="option.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="详细地址" prop="detail">
          <el-input v-model="addrForm.detail" type="textarea" :rows="2" placeholder="街道门牌号等" />
        </el-form-item>
        <el-form-item>
          <label class="switch-label">
            <span>设为默认地址</span>
            <el-switch v-model="addrForm.isDefault" :active-value="1" :inactive-value="0" />
          </label>
        </el-form-item>
      </el-form>
      <template #footer>
        <button class="dialog-cancel" @click="dialogVisible = false">取消</button>
        <button class="dialog-save" :class="{ loading: submitting }" :disabled="submitting" @click="handleSave">
          <span v-if="!submitting">保存</span>
          <span v-else class="btn-spinner"></span>
        </button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, MapLocation } from '@element-plus/icons-vue'
import { pcaTextArr } from 'element-china-area-data'
import { getAddresses, addAddress, updateAddress, deleteAddress } from '../api/address'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const editAddr = ref(null)
const addrFormRef = ref(null)
const addresses = ref([])

const addrForm = reactive({
  receiverName: '',
  receiverPhone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  isDefault: 0,
})

const provinceOptions = pcaTextArr
const cityOptions = computed(() => provinceOptions.find(option => option.value === addrForm.province)?.children || [])
const districtOptions = computed(() => cityOptions.value.find(option => option.value === addrForm.city)?.children || [])

const handleProvinceChange = () => {
  addrForm.city = ''
  addrForm.district = ''
}

const handleCityChange = () => {
  addrForm.district = ''
}

const addrRules = {
  receiverName: [{ required: true, message: '请输入收货人', trigger: 'blur' }],
  receiverPhone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  province: [{ required: true, message: '请输入省份', trigger: 'blur' }],
  city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
  district: [{ required: true, message: '请输入区/县', trigger: 'blur' }],
  detail: [{ required: true, message: '请输入详细地址', trigger: 'blur' }],
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getAddresses()
    addresses.value = res.data || []
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  addrForm.receiverName = ''
  addrForm.receiverPhone = ''
  addrForm.province = ''
  addrForm.city = ''
  addrForm.district = ''
  addrForm.detail = ''
  addrForm.isDefault = 0
}

const showDialog = (addr) => {
  editAddr.value = addr
  if (addr) {
    Object.assign(addrForm, {
      receiverName: addr.receiverName,
      receiverPhone: addr.receiverPhone,
      province: addr.province,
      city: addr.city,
      district: addr.district,
      detail: addr.detail,
      isDefault: addr.isDefault,
    })
  } else {
    resetForm()
  }
  dialogVisible.value = true
}

const handleSave = async () => {
  const valid = await addrFormRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (editAddr.value) {
      await updateAddress(editAddr.value.id, { ...addrForm })
      ElMessage.success('修改成功')
    } else {
      await addAddress({ ...addrForm })
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await fetchList()
  } finally {
    submitting.value = false
  }
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定删除该地址？', '提示', { type: 'warning' }).then(async () => {
    try {
      await deleteAddress(id)
      ElMessage.success('已删除')
      await fetchList()
    } catch { /* handled */ }
  })
}

onMounted(fetchList)
</script>

<style scoped>
.address-page { max-width: 720px; }

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.page-title {
  font-size: 22px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0;
  letter-spacing: -0.3px;
}

.add-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 9px 18px;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, #FF6B35 0%, #FF8F5E 100%);
  color: #fff;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 3px 10px rgba(255,107,53,0.2);
}

.add-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 18px rgba(255,107,53,0.3);
}

/* 地址列表 */
.address-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.addr-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-radius: 14px;
  padding: 18px 20px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  border: 1px solid #f0efed;
  transition: all 0.2s;
  gap: 16px;
}

.addr-card:hover {
  border-color: #e0ded9;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
}

.addr-body { flex: 1; min-width: 0; }

.addr-line {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.addr-line strong {
  font-size: 15px;
  color: #333;
}

.addr-phone {
  font-size: 13px;
  color: #888;
}

.default-tag {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 3px;
  background: rgba(255,107,53,0.08);
  color: #FF6B35;
  font-weight: 500;
}

.addr-text {
  font-size: 13px;
  color: #888;
  margin: 0;
  line-height: 1.5;
}

.addr-actions {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
}

.action-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 8px;
  background: transparent;
  cursor: pointer;
  transition: all 0.2s;
}

.action-btn.edit { color: #aaa; }
.action-btn.edit:hover { color: #FF6B35; background: rgba(255,107,53,0.06); }
.action-btn.del { color: #ccc; }
.action-btn.del:hover { color: #e55a2b; background: rgba(229,90,43,0.06); }

/* 空状态 */
.empty-state {
  background: #fff;
  border-radius: 14px;
  padding: 60px 0;
  text-align: center;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}

.empty-state p {
  font-size: 15px;
  color: #aaa;
  margin-top: 12px;
}

/* 弹窗 */
.switch-label {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  font-size: 14px;
  color: #333;
}

.address-page :deep(.el-select) {
  width: 100%;
}

.dialog-cancel,
.dialog-save {
  padding: 9px 22px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.dialog-cancel {
  background: #f5f3f0;
  color: #666;
}

.dialog-cancel:hover { background: #e8e5e0; }

.dialog-save {
  background: linear-gradient(135deg, #FF6B35 0%, #FF8F5E 100%);
  color: #fff;
  font-weight: 500;
  box-shadow: 0 3px 10px rgba(255,107,53,0.2);
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.dialog-save:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 18px rgba(255,107,53,0.3);
}

.dialog-save:disabled { opacity: 0.6; cursor: not-allowed; }
.dialog-save.loading { pointer-events: none; }

.btn-spinner {
  width: 16px; height: 16px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }

@media (max-width: 768px) {
  .addr-card {
    flex-direction: column;
    align-items: flex-start;
  }

  .addr-actions {
    align-self: flex-end;
  }
}
</style>

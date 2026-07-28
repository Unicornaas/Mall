import request from '../../../shared/api/request'

export const getAdminInventoryPage = (params) => request.get('/admin/inventory', { params })
export const addAdminInventory = (skuId, data) => request.put(`/admin/inventory/${skuId}/add`, data)
export const getAdminInventoryLogs = (skuId) => request.get(`/admin/inventory/${skuId}/logs`)

import request from '../../../shared/api/request'

export const getSellerInventoryPage = (params) => request.get('/seller/inventory', { params })
export const addSellerInventory = (skuId, data) => request.put(`/seller/inventory/${skuId}/add`, data)
export const updateSellerSafetyStock = (skuId, data) => request.put(`/seller/inventory/${skuId}/safety-stock`, data)
export const getSellerInventoryLogs = (skuId) => request.get(`/seller/inventory/${skuId}/logs`)

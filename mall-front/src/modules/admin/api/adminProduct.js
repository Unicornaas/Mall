import request from '../../../shared/api/request'

export const getAdminProductPage = (params) => request.get('/admin/product/spus/page', { params })
export const createAdminProduct = (data) => request.post('/admin/product/spus', data)
export const updateAdminProduct = (id, data) => request.put(`/admin/product/spus/${id}`, data)
export const updateAdminProductStatus = (id, status) => request.put(`/admin/product/spus/${id}/status`, null, { params: { status } })

export const getAdminCategories = () => request.get('/admin/product/categories')
export const createAdminCategory = (data) => request.post('/admin/product/categories', data)
export const updateAdminCategory = (id, data) => request.put(`/admin/product/categories/${id}`, data)

export const getAdminSkus = (spuId) => request.get(`/admin/product/spus/${spuId}/skus`)
export const createAdminSku = (data) => request.post('/admin/product/skus', data)
export const updateAdminSku = (id, data) => request.put(`/admin/product/skus/${id}`, data)

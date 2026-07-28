import request from '../../../shared/api/request'

export const getAdminOrderPage = (params) => request.get('/admin/order/page', { params })
export const getAdminOrderDetail = (id) => request.get(`/admin/order/${id}`)
export const closeAdminOrder = (id) => request.put(`/admin/order/${id}/close`)
export const shipAdminOrder = (id, data) => request.put(`/admin/order/${id}/ship`, data)
export const receiveAdminOrder = (id) => request.put(`/admin/order/${id}/receive`)

import request from '../../../shared/api/request'

export const getSellerOrderPage = (params) => request.get('/seller/orders/page', { params })
export const getSellerOrderDetail = (sellerOrderId) => request.get(`/seller/orders/${sellerOrderId}`)
export const shipSellerOrder = (sellerOrderId, data) => request.put(`/seller/orders/${sellerOrderId}/ship`, data)

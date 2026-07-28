import request from '../../../shared/api/request'

export const getAdminPaymentPage = (params) => request.get('/admin/payment/payments', { params })
export const getAdminRefundPage = (params) => request.get('/admin/payment/refunds', { params })
export const processAdminRefund = (id, data) => request.put(`/admin/payment/refunds/${id}/process`, data)

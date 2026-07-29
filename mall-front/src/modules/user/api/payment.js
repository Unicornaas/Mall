import request from '../../../shared/api/request'

export function createPayment(data) {
  return request.post('/payment/create', data)
}

export function payOrder(orderNo) {
  return request.put(`/payment/pay/${orderNo}`)
}

export function getPaymentStatus(orderNo) {
  return request.get(`/payment/status/${orderNo}`)
}

export function applyRefund(data) {
  return request.post('/payment/refund', data)
}

export function getRefundStatus(orderNo) {
  return request.get(`/payment/refund/status/${orderNo}`)
}

export function getRefundList(orderNo) {
  return request.get(`/payment/refund/list/${orderNo}`)
}

export function getPaymentPage(pageNum = 1, pageSize = 20) {
  return request.get('/payment/page', { params: { pageNum, pageSize } })
}

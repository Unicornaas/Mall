import request from './request'

export function createOrder(data) {
  return request.post('/order', data)
}

export function getOrderDetail(id) {
  return request.get(`/order/${id}`)
}

export function getOrderList(userId) {
  return request.get(`/order/list/${userId}`)
}

export function cancelOrder(id, userId) {
  return request.put(`/order/${id}/cancel`, null, { params: { userId } })
}

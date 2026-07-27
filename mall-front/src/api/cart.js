import request from './request'

export function getCartList(userId) {
  return request.get('/cart/list', { params: { userId } })
}

export function addToCart(data) {
  return request.post('/cart/add', data)
}

export function updateQuantity(id, userId, quantity) {
  return request.put(`/cart/${id}/quantity`, { quantity }, { params: { userId } })
}

export function updateSelected(id, userId, selected) {
  return request.put(`/cart/${id}/selected`, { selected }, { params: { userId } })
}

export function selectAll(userId, selected) {
  return request.put('/cart/select-all', null, { params: { userId, selected } })
}

export function removeCartItem(id, userId) {
  return request.delete(`/cart/${id}`, { params: { userId } })
}

export function batchRemoveCart(data) {
  return request.delete('/cart/batch', { data })
}

export function getCartCount(userId) {
  return request.get('/cart/count', { params: { userId } })
}

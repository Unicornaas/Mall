import request from './request'

export function getInventory(skuId) {
  return request.get(`/inventory/${skuId}`)
}

export function batchGetInventory(skuIds) {
  return request.post('/inventory/batch', { skuIds })
}

export function getInventoryLog(skuId) {
  return request.get(`/inventory/log/${skuId}`)
}

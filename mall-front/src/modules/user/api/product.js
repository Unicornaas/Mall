import request from '../../../shared/api/request'

export function getCategories() {
  return request.get('/product/categories')
}

export function getChildrenCategories(parentId) {
  return request.get(`/product/categories/children/${parentId}`)
}

export function getSpusByCategory(categoryId) {
  return request.get(`/product/spus/category/${categoryId}`)
}

export function getSpuDetail(id) {
  return request.get(`/product/spus/${id}`)
}

export function getSkusBySpu(spuId) {
  return request.get(`/product/skus/spu/${spuId}`)
}

export function getSkuDetail(id) {
  return request.get(`/product/skus/${id}`)
}

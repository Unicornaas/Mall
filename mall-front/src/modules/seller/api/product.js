import request from '../../../shared/api/request'

export const getSellerProductPage = (params) => request.get('/seller/products/page', { params })
export const getSellerProduct = (spuId) => request.get(`/seller/products/${spuId}`)
export const createSellerProduct = (data) => request.post('/seller/products', data)
export const updateSellerProduct = (spuId, data) => request.put(`/seller/products/${spuId}`, data)
export const updateSellerProductStatus = (spuId, status) => request.put(`/seller/products/${spuId}/status`, null, { params: { status } })

export const getSellerSkus = (spuId) => request.get(`/seller/products/${spuId}/skus`)
export const createSellerSku = (spuId, data) => request.post(`/seller/products/${spuId}/skus`, data)
export const updateSellerSku = (skuId, data) => request.put(`/seller/products/skus/${skuId}`, data)
export const updateSellerSkuStatus = (skuId, status) => request.put(`/seller/products/skus/${skuId}/status`, null, { params: { status } })

export const getProductCategories = () => request.get('/product/categories')

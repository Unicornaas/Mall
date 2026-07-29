import request from '../../../shared/api/request'

export const getSellerDashboardOverview = () => request.get('/seller/dashboard/overview')
export const getSellerRecentOrders = (limit = 10) => request.get('/seller/dashboard/recent-orders', { params: { limit } })
export const getSellerStockAlerts = (limit = 10) => request.get('/seller/dashboard/stock-alerts', { params: { limit } })

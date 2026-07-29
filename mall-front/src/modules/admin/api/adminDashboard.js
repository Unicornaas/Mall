import request from '../../../shared/api/request'

export const getAdminDashboardOverview = () => request.get('/admin/dashboard/overview')
export const getAdminDashboardOrderTrend = (days = 7) => request.get('/admin/dashboard/order-trend', { params: { days } })
export const getAdminDashboardLatestOrders = (limit = 10) => request.get('/admin/dashboard/latest-orders', { params: { limit } })
export const getAdminDashboardStockAlerts = (limit = 10) => request.get('/admin/dashboard/stock-alerts', { params: { limit } })

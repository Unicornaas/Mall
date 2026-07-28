import request from '../../../shared/api/request'

export function getAdminUserPage(params) {
  return request.get('/admin/user/page', { params })
}

export function updateAdminUserStatus(id, status) {
  return request.put(`/admin/user/${id}/status`, { status })
}

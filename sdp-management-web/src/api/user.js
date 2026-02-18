import http from '@/utils/http'

const user = {
  list(data) {
    return http.post('/admin/userList', data)
  },
  createUser(data) {
    return http.post('/admin/createUser', data)
  },
  updatePassword(data) {
    return http.put('/admin/updatePassword', data)
  },
  deleteUser(data) {
    return http.post('/admin/deleteUser', data)
  },
}

export default user;
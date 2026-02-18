import http from '@/utils/http'

const login = {
  login(data) {
    return http.post('/admin/login', data)
  },
  logout(data) {
    return http.get('/admin/logout', {
      params: data
    })
  },
  getUserInfoByCookie(data) {
    return http.get('/admin/getUserInfoByCookie', {
      params: data
    })
  },
}

export default login;
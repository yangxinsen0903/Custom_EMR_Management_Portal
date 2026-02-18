import http from '@/utils/http'

const systemManage = {
  queryauthkeylist(data) {
    return http.post('/admin/api/queryauthkeylist', data)
  },
  createauthkey(data) {
    return http.post('/admin/api/createauthkey', data)
  },
  updateauthkey(data) {
    return http.post('/admin/api/updateauthkey', data)
  },
  deleteauthkey(data) {
    return http.post('/admin/api/deleteauthkey', data)
  },
  getGroupedBizConfigs(data) {
    return http.get('/admin/api/getGroupedBizConfigs', {params: data})
  },
  updateBizConfigs(data) {
    return http.post('/admin/api/updateBizConfigs', data)
  },
  listImage(data) {
    return http.get('/admin/image/listImage', {params: data})
  },
  saveImageScript(data) {
    return http.post('/admin/image/saveImageScript', data)
  },
  listImageScript(data) {
    return http.get('/admin/image/listImageScript', {params: data})
  },
  queryconfiglist(data) {
    return http.post('/admin/api/queryconfiglist', data)
  },
  addconfig(data) {
    return http.post('/admin/api/addconfig', data)
  },
  updateconfig(data) {
    return http.post('/admin/api/updateconfig', data)
  },
  deleteconfig(data) {
    return http.post('/admin/api/deleteconfig', data)
  },
}

export default systemManage;
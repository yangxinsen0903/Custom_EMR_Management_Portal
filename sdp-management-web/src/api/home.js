import http from '@/utils/http'

const home = {
  clusterOverview() {
    return http.get('/admin/api/clusterOverview')
  },
  sdpVersionInfo() {
    return http.get('/admin/api/sdpVersionInfo')
  }
}

export default home
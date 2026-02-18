import http from '@/utils/http'

const taskCenter = {
  list(data) {
    return http.post('/admin/api/getjoblist', data)
  },
  listByPlanId(params) {
    return http.get('/admin/api/getVmListByPlanId?planId=' + params.planId, params)
  },
  getjobdetail(data) {
    return http.post('/admin/api/getjobdetail', data)
  },
  retry(params) {
    return http.get('/admin/api/retryactivity', {params})
  },
  getJobQueryParamDict(params) {
    return http.get('/admin/api/getJobQueryParamDict', {params})
  },
}

export default taskCenter;

import http from '@/utils/http'

const operation = {
  getAbnormalVmCleanSummary (data) {
    return http.get('/admin/api/dc/getAbnormalVmCleanSummary', {params: data})
  },
  getAbnormalVmRetrySummary (data) {
    return http.get('/admin/api/dc/getAbnormalVmRetrySummary', {params: data})
  },
  vmDiffStat (data) {
    return http.get('/admin/api/dc/vmDiffStat', {params: data})
  },
  getPlanResultReport (params) {
    return http.get('/admin/api/dc/getPlanResultReport', { params })
  },
  getScaleInFailureResultReport (params) {
    return http.get('/admin/api/dc/getScaleInFailureResultReport', { params })
  },
  getVmStatements (params) {
    return http.get('/admin/api/dc/vmStatements', { params })
  },
  getVmStatementDetails (params) {
    return http.get('/admin/api/dc/vmStatementDetails', { params })
  },
  getClusterVMDelete (params) {
    return http.get('/admin/api/dc/clusterVMDelete', { params })
  },
  getVmReqJobFailed (params) {
    return http.get('/admin/api/dc/vmReqJobFailed', { params })
  },
  getfailedlogs (params) {
    return http.get('/admin/api/getfailedlogs', { params })
  },
  getfailedlogbyid (params) {
    return http.get('/admin/api/getfailedlogbyid', { params })
  },
  checkReportDaily (params) {
    return http.get('/admin/api/dc/checkReportDaily', { params })
  },
  getVmEventList (params) {
    return http.get('/admin/api/getVmEventList', { params })
  },
  queryDestroyTask (data) {
    return http.post('/admin/api/queryDestroyTask', data)
  },
  retryActivity (data) {
    return http.post('/admin/api/retryActivity', data)
  },
  cancelTask (data) {
    return http.post('/admin/api/cancelTask', data)
  },
  queryorderapproval (params) {
    return http.get('/admin/api/queryorderapproval', { params })
  },
  collectClusterInfoList (data) {
    return http.post('/admin/api/collectClusterInfoList', data)
  },
  collectClusterInfoByClusterId (data) {
    return http.post('/admin/api/collectClusterInfoByClusterId', data)
  },
  getAzureCleanedVms (data) {
    return http.post('/admin/api/dc/getAzureCleanedVms', data)
  },
}

export default operation;
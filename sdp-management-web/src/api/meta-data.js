import http from '@/utils/http'

const metaDataApi = {
  selectMetaDataList (data) {
    return http.post('/admin/meta/selectMetaDataList', data)
  },
  deleteMetaDataById (data) {
    return http.post('/admin/meta/deleteMetaDataById', data)
  },
  insertMetaData (data) {
    return http.post('/admin/meta/insertMetaData', data)
  },
  updateMetaData (data) {
    return http.post('/admin/meta/updateMetaData', data)
  },
  azureGetRegionList (data) {
    return http.get('/admin/api/azure/metas/getRegionList', {params: data})
  },
  azureListSubscription (data) {
    return http.get('/admin/api/azure/metas/listSubscription', {params: data})
  },
}

export default metaDataApi;
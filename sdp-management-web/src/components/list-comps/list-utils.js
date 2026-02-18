import {Search, Plus, Delete, Filter, Download, Close, Document, Refresh} from '@element-plus/icons-vue'
import http from '@/utils/http'
import {ElMessage, ElMessageBox} from 'element-plus'
import router from '@/router/index'
import store from "@/store"

// import { showJsDialog } from '../form-comps/form-utils'

export function getButtonIcon(iconName) {
  let comp = null
  switch (iconName) {
    case 'Search':
      comp = Search
      break;
    case 'Plus':
      comp = Plus
      break;
    case 'Delete':
      comp = Delete
      break;
    case 'Filter':
      comp = Filter
      break;
    case 'Download':
      comp = Download
      break;
    case 'Close':
      comp = Close
      break;
    case 'Document':
      comp = Document
      break;
    case 'Refresh':
      comp = Refresh
      break;
  }
  return comp
}

// function eventApi(apiUrl, data) {
//   return new Promise(resolve => {
//     http.post(apiUrl, data).then(res => {
//       if (res.result == true) {
//         resolve()
//       } else {
//         ElMessage.error(res.errorMsg)
//       }
//     })
//   })
// }
//
// export function doEvent(eventAttrs, data) {
//   return new Promise((resolve, reject) => {
//     let type = eventAttrs.type
//
//     if (type == 'api') {
//       let tip = eventAttrs.tip
//       let apiUrl = eventAttrs.apiUrl || ''
//
//       if (!apiUrl) {
//         ElMessage.error('接口不存在！')
//         reject()
//       } else {
//         if (tip) {
//           ElMessageBox.confirm(tip, '提示', {
//             confirmButtonText: '确定',
//             cancelButtonText: '取消',
//             type: 'warning',
//           }).then(() => {
//             eventApi(apiUrl, data).then(resolve)
//           }).catch(() => {
//             reject()
//           })
//         } else {
//           eventApi(apiUrl, data).then(resolve).catch(reject)
//         }
//       }
//     } else if (type == 'formdialog') {
//       showJsDialog({
//         data: data,
//         eventAttrs: eventAttrs,
//       }, resolve)
//     } else if (type == 'nextpage') {
//       let pageUrl = eventAttrs.pageUrl || ''
//       if (pageUrl) {
//         router.push(pageUrl)
//       }
//     }
//   })
// }
//
// export function opBtnShow(btnConf, rowData) {
//   const userInfo = store.state.userInfo || {}
//   let showRule = btnConf.showRule || ''
//   if (showRule) {
//     try {
//       return eval(showRule)
//     } catch (e) {
//       console.log(e)
//       return false
//     }
//   } else {
//     return true
//   }
// }
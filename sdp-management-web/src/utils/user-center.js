import { ElMessage } from 'element-plus'
import loginApi from "@/api/login";

const userCenter = {
  storeUserInfo: null,
  getUserInfo() {
    return new Promise((resolve, reject) => {
      if (this.storeUserInfo) {
        resolve(this.storeUserInfo)
        return ;
      }
      loginApi.getUserInfoByCookie().then(res => {
        if (res.result == true) {
          this.storeUserInfo = res.data || {}
          resolve(this.storeUserInfo)
        } else {
          ElMessage.error(res.errorMsg)
        }
      })
    })
  },
  clearUserInfo() {
    this.storeUserInfo = null
  }
}

export default userCenter
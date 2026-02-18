import store from "@/store";

const permissionCheck = {
  currentPermissionCheck(permission) {
    let baseUserRole = store.state.userInfo.baseUserRole || []
    let roleCode = ''
    if (baseUserRole.length) {
      roleCode = baseUserRole[0].roleCode || ''
    }
    console.log("当前用户的roleCode:", roleCode)

    if (Array.isArray(permission)) {
      for (let i in permission) {
        let per = permission[i]
        if (per == roleCode) {
          console.log("当前用户role匹配成功:", per)
          return true
        }
      }
    } else {
      if (permission == roleCode) {
        console.log("当前用户role匹配成功:", permission)
        return true
      }
    }

    return false
  }
}

export default permissionCheck;

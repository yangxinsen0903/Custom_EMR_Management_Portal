function httpExtend(http) {
  let ex_http ={
    post: function (...arg) {
      return http.post(...arg)
    },
    formDataPost: function (...arg) {
      let url = arg[0]
      let data = {}
      if (arg.length > 1) {
        data = arg[1]
      }
      let params = {}
      if (arg.length > 2) {
        params = arg[2]
      }

      let headers = params.headers || {}
      headers["Content-Type"] = "multipart/form-data; charset=UTF-8"

      params.headers = headers

      return http.post(url, data, params)
    }
  }

  ex_http.__proto__ = http

  return ex_http
}

export default httpExtend
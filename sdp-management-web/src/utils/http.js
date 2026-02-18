import axios from "axios";
// import Qs from "qs";
import config from "../config/config";
// import token from './token'
import { ElMessage } from 'element-plus';
import router from "../router";
import httpExtend from "./http-extend";
import {clearCookie} from "./cookie";

let Axios = axios.create({
  baseURL: config.baseURL,
  timeout: 20000,
  responseType: "json",
  withCredentials: true,
  headers: {
    "Content-Type": "application/json; charset=UTF-8",
  },
});

Axios.interceptors.request.use(
  config => {
    // if (config.method == "post") {
    //   let postData = config.data;
    //   let nArgs = {}
    //
    //   // const _token = token.getToken()
    //   // if (_token && _token !== 'undefined') {
    //   //   nArgs.token = _token
    //   // }
    //   for (let key in postData) {
    //     nArgs[key] = postData[key]
    //   }
    //
    //   let d = JSON.stringify(nArgs, { arrayFormat: "repeat" });
    //   config.data = d;
    // }

    // const _token = token.getToken()
    // if (_token && _token !== 'undefined') {
    //   config.headers["X-ABEI-TOKEN"] = _token
    // }

    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

// 返回状态判断(添加响应拦截器)
Axios.interceptors.response.use(
  res => {
    let { retcode } = res.data
    // token 异常，直接登出
    if (retcode == 404) {
      setTimeout(() => {
        ElMessage.closeAll()
        ElMessage({
          message: '登录已过期，请重新登录！',
          type: 'error'
        });

        clearCookie('sdptoken')
        router.replace('/login')
      }, 10)
      return res.data;
    }
    return res.data
  },
  error => {
    ElMessage({
      message: '请求数据失败，请稍后尝试！',
      type: 'error'
    });
    return Promise.reject(error);
  }
);

const http = httpExtend(Axios)

export default http;

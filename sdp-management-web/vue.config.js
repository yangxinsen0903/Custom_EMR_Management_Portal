
function getFormatNow() {
  let oDate = new Date()

  let oYear = oDate.getFullYear(),
    oMonth = oDate.getMonth() + 1,
    oDay = oDate.getDate(),
    oHour = oDate.getHours() + 8,
    oMinute = oDate.getMinutes(),
    oSecond = oDate.getSeconds(),

    //最后拼接时间
    oTime = oYear + '-' + oMonth + '-' + oDay + ' ' + oHour + ':' + oMinute + ':' + oSecond;
  return oTime;
}

module.exports = {
  publicPath: '/',
  devServer: {
    proxy: {
      "/admin": {
        // target: 'http://172.16.2.70:30460',
        target: 'http://20.191.84.132:8081',
        ws: true,        //如果要代理 websockets，配置这个参数
        secure: false,  // 如果是https接口，需要配置这个参数
        changeOrigin: true,  //是否跨域
        pathRewrite: {
          '^/admin': '/admin'
        }
      }
    }
  },
  chainWebpack(config) {
    config.plugin('html').tap(args => {
      args[0].createDate = '编译时间：' + getFormatNow();
      return args
    })
  }
}
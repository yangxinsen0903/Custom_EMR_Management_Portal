//防抖实现
export function debounce(fn,delay){   //传入所要防抖的方法或者回调与延迟时间
  let timer = null
  //借助闭包，使得变量timer不被回收
  return function() {
    //保存当前环境下的实例对象，this即为引入该方法的那个组件实例对象
    let th = this;
    //保存传入参数
    let args = arguments;
    //第一次timer为null，跳过该判断，执行setTimeout()
    if(timer){
      //之后如果timer存在，则把上一次的销毁，也就是setTimeout()，则里面的函数执行也会抛弃
      clearTimeout(timer)
    }
    timer = setTimeout(()=>{
      //apply()，改变this指向，指向正在操作的组件实例，传入参数
      fn.apply(th, args)
      timer = null
    },delay)
  }
}

//由于使用闭包，使得timer不被回收，在A组件中每次调用该方法时会去判断timer是否存在，如果存在，
//表示上一次输入在等待执行fn()，期间如果继续输入，且在1s内，则会把上一次未执行的
//（setTimeout中的）fn()销毁，重新定时1s，以此来达到输入结束过1s才执行fn()，即触发事件或者发送
//请求。



//节流实现
export function throttle(fn,delay){
  //有效标志，用来判断是否可以允许下一次执行，true表示允许，false表示禁止
  let flag = true
  //借助闭包，使得变量flag不被回收
  return function() {
    //保存当前环境下的实例对象，this即为引入该方法的那个组件实例对象
    let th = this;
    //保存传入参数
    let args = arguments;
    //如果flag为false，不允许执行fn()
    if(!flag){
      //未超过时间间隔，flag无效，不执行fn()
      return false
    }
    fn.apply(th, args)
    //在时间间隔内把状态位flag设为禁止（false）
    flag = false
    setTimeout(() => {
      //超过时间间隔把状态位flag设为允许（true）,这样下次就能执行fn()了
      flag = true
    }, delay)
  }
}
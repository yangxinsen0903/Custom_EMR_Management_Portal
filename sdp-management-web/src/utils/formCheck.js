const FormCheck = {
  // 必填
  required: function (msg = '请输入必填项', trigger = "blur") {
    return { required: true, message: msg, trigger: trigger }
  },
  // 中文数字字母-_
  justChineseNumberLetterZGAnd_: function (msg = '仅支持中文、数字、字母、-、_', trigger = "blur") {
    return {
      validator: (rule, value, callback) => {
        if (value === '' || value === undefined || value === null) {
          return callback();
        }
        var reg = /^[\u4e00-\u9fa5a-zA-Z0-9\-_]*$/
        if (!reg.test(value)) {
          return callback(new Error(msg));
        }
        return callback();
      },
      trigger: trigger
    }
  },
  // 中文数字字母
  justChineseNumberLetter: function (msg = '仅支持中文、数字、字母', trigger = "blur") {
    return {
      validator: (rule, value, callback) => {
        if (value === '' || value === undefined || value === null) {
          return callback();
        }
        var reg = /^[\u4e00-\u9fa5a-zA-Z0-9]*$/
        if (!reg.test(value)) {
          return callback(new Error(msg));
        }
        return callback();
      },
      trigger: trigger
    }
  },
  // 仅支持数字
  justNumber: function (msg = '仅支持数字', trigger = "blur") {
    return {
      validator: (rule, value, callback) => {
        if (value === '' || value === undefined || value === null) {
          return callback();
        }
        var reg = /^(-)[0-9.]|^[0-9.]*$/
        if (!reg.test(value)) {
          return callback(new Error(msg));
        }
        return callback();
      },
      trigger: trigger
    }
  },
  // 仅支持最多两位小数的正数
  justPrice: function (msg = '仅支持数字', trigger = "blur") {
    return {
      validator: (rule, value, callback) => {
        if (value === '' || value === undefined || value === null) {
          return callback();
        }
        var reg = /^(([1-9]{1}\d*)|(0{1}))(\.\d{1,2})?$/
        if (!reg.test(value)) {
          return callback(new Error(msg));
        }
        return callback();
      },
      trigger: trigger
    }
  },
  // 仅支持正整数
  justPositiveInt(msg = '仅支持正整数', trigger = "blur") {
    return {
      validator: (rule, value, callback) => {
        if (value === '' || value === undefined || value === null) {
          return callback();
        }
        let reg = /^[1-9][0-9]*$/;
        if (!reg.test(value)) {
          return callback(new Error(msg));
        }
        return callback();
      },
      trigger: trigger
    }
  },
  // 仅支持自然数
  justNaturalNumber(msg = '仅支持自然数', trigger = "blur") {
    return {
      validator: (rule, value, callback) => {
        if (value === '' || value === undefined || value === null) {
          return callback();
        }
        let reg = /^[0-9]|^[1-9][0-9]*$/;
        if (!reg.test(value)) {
          return callback(new Error(msg));
        }
        return callback();
      },
      trigger: trigger
    }
  },
  // 应包含数字、大小写英文、特殊符号
  complexPassword(msg = '应包含数字、大小写英文、特殊符号', trigger = "blur") {
    return {
      validator: (rule, value, callback) => {
        if (value === '' || value === undefined || value === null) {
          return callback();
        }
        let reg = /(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^a-zA-Z0-9])[\S]/;
        if (!reg.test(value)) {
          return callback(new Error(msg));
        }
        return callback();
      },
      trigger: trigger
    }
  },
  // 仅支持字母和数字
  justLetterAndNumber: function (msg = '仅支持字母和数字', trigger = "blur") {
    return {
      validator: (rule, value, callback) => {
        var reg = /^[0-9a-zA-Z]*$/
        if (!reg.test(value)) {
          return callback(new Error(msg));
        }
        return callback();
      },
      trigger: trigger
    }
  },
  // 仅支持字母和数字和-
  justLetterAndNumberAnd_: function (msg = '仅支持字母、数字和-，并且不能以-开头', trigger = "blur") {
    return {
      validator: (rule, value, callback) => {
        var reg = /^[0-9a-zA-Z][0-9a-zA-Z\-]*$/
        if (!reg.test(value)) {
          return callback(new Error(msg));
        }
        return callback();
      },
      trigger: trigger
    }
  },
  // 正确的手机号
  justPhone: function (msg = '请输入正确的手机号', trigger = "blur") {
    return {
      validator: (rule, value, callback) => {
        if (value == '') {
          return callback();
        }
        var reg = /^1\d{10}$/
        if (!reg.test(value)) {
          return callback(new Error(msg));
        }
        return callback();
      },
      trigger: trigger
    }
  },
  // 正确的身份证号码
  justIdCard: function (msg = '请输入正确的身份证号码', trigger = "blur") {
    return {
      validator: (rule, value, callback) => {
        if (value == '') {
          return callback();
        }
        var reg = /(^[1-9]\d{5}(18|19|([23]\d))\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$)|(^[1-9]\d{5}\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\d{2}$)/
        if (!reg.test(value)) {
          return callback(new Error(msg));
        }
        return callback();
      },
      trigger: trigger
    }
  },
  // 正确的邮箱号
  justEmail: function (msg = '请输入正确的电子邮件', trigger = "blur") {
    return {
      validator: (rule, value, callback) => {
        if (value == '') {
          return callback();
        }
        var reg = /^[a-z0-9]+([._\\-]*[a-z0-9])*@([a-z0-9]+[-a-z0-9]*[a-z0-9]+.){1,63}[a-z0-9]+$/
        if (!reg.test(value)) {
          return callback(new Error(msg));
        }
        return callback();
      },
      trigger: trigger
    }
  },
  // Length limit 长度限制
  lengthLimit: function (min = 0, max = 11, trigger = "blur") {
    return {
      validator: (rule, value, callback) => {
        if (value == '') {
          return callback();
        }
        let str = value + ''
        if (str.length < min) {
          return callback(new Error(`长度不能低于${min}位`));
        } else if (str.length > max) {
          return callback(new Error(`长度不能超过${max}位`));
        }
        return callback();
      },
      trigger: trigger
    }
  },
  // 数据大小问题
  valueIn: function (min = 1, max = 4, trigger = "blur") {
    return {
      validator: (rule, value, callback) => {
        if (value === '' || value == undefined || value == null) {
          return callback();
        }
        let val = parseFloat(value)
        if (val < min) {
          return callback(new Error(`请输入${min}到${max}中的数字`));
        } else if (val > max) {
          return callback(new Error(`请输入${min}到${max}中的数字`));
        }
        return callback();
      },
      trigger: trigger
    }
  },
  // 判断是电话
  isPhone: function (msg = '请输入合法电话号码（包含区号，使用-间隔）', trigger = "blur") {
    let reg= /^((0\d{2,3}-\d{7,8})|(1[3584]\d{9}))$/;
    return {
      validator: (rule, value, callback) => {
        if (!reg.test(value)) {
          return callback(new Error(msg));
        }
        return callback();
      },
      trigger: trigger
    };
  },
  // 判断是IP地址
  isValidIP: function (msg = '请输入合法的IP地址', trigger = "blur") {
    let reg = /^(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])$/
    return {
      validator: (rule, value, callback) => {
        if (!reg.test(value)) {
          return callback(new Error(msg));
        }
        return callback();
      },
      trigger: trigger
    };
  }
}

export default FormCheck

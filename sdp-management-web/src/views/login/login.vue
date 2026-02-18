/**Created by liaoyingchao on 12/22/22.*/

<template>
  <div class="login page-css">
    <div class="login-content">
      <div class="login-title">
        <span class="title">SDP管控平台</span>
      </div>
      <div class="login-panel">
        <div class="panel-title">
          密码登录
        </div>
        <el-form :model="loginInfo" ref="userInfo" :rules="rules">
          <!--      账号密码登录-->
          <el-form-item prop="userName" style="margin-bottom: 24px">
            <el-input v-model="loginInfo.userName" clearable placeholder="请输入用户名">
              <template #prefix>
                <el-icon size="24">
                  <User/>
                </el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="passWord">
            <el-input v-model="loginInfo.password" placeholder="请输入密码" show-password>
              <template #prefix>
                <el-icon size="24">
                  <Lock/>
                </el-icon>
              </template>
            </el-input>
          </el-form-item>
        </el-form>
        <div class="forget-password">
          <div style="margin: 0 3px 0 0; font-size: 14px">
            <el-checkbox v-model="savePwd" label="记住密码"></el-checkbox>
          </div>
          <!--<div style="line-height: 32px; font-size: 14px; color: #353535">忘记密码</div>-->
        </div>
        <el-button type="primary" style="width: 100%" class="dlbtn" :loading="loading" @click="loginBtn">登 录</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
  import {
    User,
    Lock
  } from '@element-plus/icons-vue'
  import {ref, reactive, defineEmits, defineProps, toRefs} from 'vue'
  import loginApi from "../../api/login";
  import {ElMessage, ElMessageBox} from 'element-plus';
  import { useRoute, useRouter } from 'vue-router'
  import {clearCookie, setCookie} from "../../utils/cookie";
  import md5 from 'js-md5'
  import userCenter from "@/utils/user-center";

  const router = useRouter()

  const loginInfo = reactive({
    userName: '',
    password: ''
  })

  const savePwd = ref(false)

  const loading = ref(false)

  function loginBtn() {
    clearCookie()
    userCenter.clearUserInfo();
    loading.value = true
    loginApi.login({
      userName: loginInfo.userName,
      password: md5(md5(loginInfo.userName + loginInfo.password)),
    }).then(res => {
      if (res.result == true) {
        saveStorageAccount()
        ElMessage.success("登录成功！")
        let data = res.data || ''
        setCookie('sdptoken', data, 1);
        setTimeout(() => {
          router.replace({
            path: '/home'
          })
        }, 500)
      } else {
        ElMessage.error(res.errorMsg)
      }
    }).finally(() => {
      loading.value = false
    })
  }

  function saveStorageAccount() {
    let dic = {
      userName: loginInfo.userName,
      password: loginInfo.password,
      savePwd: savePwd.value,
    }

    let str = JSON.stringify(dic)
    str = encodeURIComponent(str)

    window.localStorage.setItem('Login_Account', str)
  }
  
  function getStorageAccount() {
    let str = window.localStorage.getItem('Login_Account')
    if (str) {
      str = decodeURIComponent(str)

      let dic = JSON.parse(str)

      if (dic.savePwd) {
        savePwd.value = dic.savePwd

        loginInfo.userName = dic.userName
        loginInfo.password = dic.password
      }
    }
  }

  getStorageAccount()
</script>

<style lang="stylus" scoped type="text/stylus">
  .login {
    background-image: url('../../assets/login/bg.png')

    .login-content {
      position: absolute;
      left: 50%;
      top: 50%;
      transform: translate(-50%, -50%);

      .login-title {
        margin-bottom: 10px;
        display: flex;
        align-items: center;

        .title {
          font-size: 20px;
          color: #333333;
        }
      }

      .login-panel {
        width: 400px;
        max-height: 60%;
        position: relative;
        background: #ffffff;
        box-shadow: 0px 8px 20px -6px rgba(0, 0, 0, 0.19);
        border-radius: 5px;
        padding: 30px 40px 36px;

        .panel-title {
          color: #333333;
          font-size: 22px;
          margin-bottom: 20px;
        }

        >>> .el-input {
          .el-input__wrapper {
            padding 1px 1px;

            .el-input__prefix {
              position absolute;
              left 0px;
              top 0;
              bottom 0;
              width 40px;
              display flex;
              align-items center;
              justify-content center;

              .el-input__prefix-inner > :last-child {
                margin-right 0;
              }
            }

            .el-input__suffix {
              position absolute;
              right 0px;
              top 0;
              bottom 0;
              width 40px;
              display flex;
              align-items center;
              justify-content center;
              text-align right;

              .el-input__icon {
                margin-left 0;
              }
            }

            .el-input__inner {
              line-height: 42px;
              height: 42px;
              font-size: 14px;
              padding: 0 40px 0 40px;
            }
          }
        }

        .forget-password {
          text-align: right;
          font-size: 14px;
          color: #999999;
          padding: 10px 0 23px;
          cursor: pointer;
          display: flex
          justify-content: space-between
        }

        .dlbtn {
          height: 42px
          background-color: #165bd3;
          border-color: #165bd3;
          font-size: 18px;
          span {
            font-weight 600;
          }
        }
      }
    }
  }
</style>
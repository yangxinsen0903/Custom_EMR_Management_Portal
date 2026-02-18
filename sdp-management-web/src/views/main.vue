/**Created by liaoyingchao on 11/23/22.*/

<template>
  <div class="main">
    <div class="main-left">
      <div class="logo-div">SDP管控平台-V4</div>
      <div class="menu-div">
        <el-menu
            active-text-color="#ffffff"
            background-color="#202132"
            class="el-menu-vertical-demo"
            :default-active="defaultActive"
            text-color="#fff"
            :router="true"
            @select="menuSelect"
        >
          <template v-for="item in menus">
            <template v-if="item.children && item.children.length">
              <el-sub-menu :index="item.path">
                <template #title>
                  <el-icon>
                    <component :is="item.icon"/>
                  </el-icon>
                  <span style="margin-left: 10px;">{{ item.title }}</span>
                </template>
                <el-menu-item :index="subItem.path" v-for="subItem in item.children">
                  <span style="margin-left: 20px;">{{ subItem.title }}</span>
                </el-menu-item>
              </el-sub-menu>
            </template>
            <template v-else>
              <el-menu-item :index="item.path">
                <!--            <img width="12" height="12" :src="item.icon"/>-->
                <el-icon>
                  <component :is="item.icon"/>
                </el-icon>
                <span style="margin-left: 10px;">{{ item.title }}</span>
              </el-menu-item>
            </template>
          </template>
        </el-menu>
      </div>
    </div>
    <div class="main-right">
      <div class="header-div">
        <div class="page-title">
          <div class="page-back" v-if="showBack" @click="backEvent">
            <el-icon>
              <ArrowLeft/>
            </el-icon>
            <span>返回</span>
          </div>
          <div class="title">{{ pageTitle }}</div>
        </div>
        <div class="top-right-div">
          <div class="login-user" v-if="store.state.userInfo.realName">您好 {{ store.state.userInfo.realName }}</div>
          <div class="logout-btn btn" @click="logoutEvent">
            退出登录
          </div>
          <div class="msg-btn btn">
            <el-icon size="18">
              <Bell/>
            </el-icon>
          </div>
        </div>
      </div>
      <div class="main-content">
        <router-view/>
      </div>
    </div>
  </div>
</template>

<script setup>
import {
  ArrowLeft,
  Bell,
  SetUp,
  Odometer,
  Postcard,
  Operation,
  User,
  Setting,
  Document,
} from '@element-plus/icons-vue'
import {ref, watch, shallowRef} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {ElMessage} from 'element-plus';
import PageTitle from "../utils/page-title";
import loginApi from "../api/login";
import {clearCookie} from "../utils/cookie";
import {getButtonIcon} from "@/components/list-comps/list-utils";
import store from "@/store";
import userCenter from "@/utils/user-center";

const route = useRoute()
const router = useRouter()

const menuOptions = [
  {
    title: '概览',
    icon: shallowRef(Odometer),
    path: '/home',
    limits: ['Staff', 'Maintainer', 'Administrator']
  },
  {
    title: '集群列表',
    icon: shallowRef(Postcard),
    path: '/clusterlist',
    limits: ['Staff', 'Maintainer', 'Administrator']
  },
  {
    title: '任务中心',
    icon: shallowRef(Operation),
    path: '/taskcenter',
    limits: ['Staff', 'Maintainer', 'Administrator']
  },
  {
    title: '系统管理',
    icon: shallowRef(Setting),
    path: '/setting',
    limits: ['Administrator'],
    children: [
      {
        title: '用户管理',
        path: '/setting/usermanage',
        limits: ['Administrator'],
      },
      {
        title: 'Shein AK管理',
        path: '/setting/sheinak',
        limits: ['Administrator'],
      },
      {
        title: '业务配置管理',
        path: '/setting/businessconfig',
        limits: ['Administrator'],
      },
      {
        title: '镜像管理',
        path: '/setting/iso',
        limits: ['Administrator'],
      },
      {
        title: '集群默认配置管理',
        path: '/setting/clusterconf',
        limits: ['Administrator'],
      },
    ]
  },
  {
    title: '运维管理',
    icon: shallowRef(SetUp),
    path: '/maintenance',
    limits: ['Maintainer', 'Administrator'],
    children: [
      {
        title: '概览',
        path: '/maintenance/overview',
        limits: ['Maintainer', 'Administrator'],
      },
      {
        title: '三方资源比对',
        path: '/maintenance/vmdifference',
        limits: ['Maintainer', 'Administrator'],
      },
      {
        title: '异步清理VM',
        path: '/maintenance/vmclear',
        limits: ['Maintainer', 'Administrator'],
      },
      {
        title: 'Azure申请资源补偿查询',
        path: '/maintenance/azureapply',
        limits: ['Maintainer', 'Administrator'],
      },
      {
        title: '巡检报告',
        path: '/maintenance/inspectionreport',
        limits: ['Maintainer', 'Administrator'],
      },
      {
        title: '调用三方API异常日志',
        path: '/maintenance/apiabnormal',
        limits: ['Maintainer', 'Administrator'],
      },
      {
        title: 'VM上下线事件管理',
        path: '/maintenance/vmeventlist',
        limits: ['Maintainer', 'Administrator'],
      },
      {
        title: '集群销毁任务列表',
        path: '/maintenance/destroytask',
        limits: ['Maintainer', 'Administrator'],
      },
      {
        title: '工单审批情况',
        path: '/maintenance/workorder',
        limits: ['Maintainer', 'Administrator'],
      },
      {
        title: '集群信息采集管理',
        path: '/maintenance/collectlog',
        limits: ['Maintainer', 'Administrator'],
      },
      {
        title: 'SDP脱管VM列表',
        path: '/metadata/azurecleanedvms',
        limits: ['Maintainer', 'Administrator'],
      },
    ]
  },
  {
    title: '元数据管理',
    icon: shallowRef(Document),
    path: '/metadata',
    limits: ['Administrator'],
    children: [
      {
        title: '数据中心管理',
        path: '/metadata/dataregion',
        limits: ['Maintainer', 'Administrator'],
      },
      {
        title: '可用区管理',
        path: '/metadata/availabilityzone',
        limits: ['Maintainer', 'Administrator'],
      },
      {
        title: '子网管理',
        path: '/metadata/subnet',
        limits: ['Maintainer', 'Administrator'],
      },
      {
        title: '安全组管理',
        path: '/metadata/saftygroup',
        limits: ['Maintainer', 'Administrator'],
      },
      {
        title: 'VMSKU管理',
        path: '/metadata/vmsku',
        limits: ['Maintainer', 'Administrator'],
      },
      {
        title: '磁盘SKU管理',
        path: '/metadata/disksku',
        limits: ['Maintainer', 'Administrator'],
      },
      {
        title: 'Key Vault',
        path: '/metadata/keyvault',
        limits: ['Maintainer', 'Administrator'],
      },
      {
        title: 'ssh密钥管理',
        path: '/metadata/sshkey',
        limits: ['Maintainer', 'Administrator'],
      },
      {
        title: '托管标识管理',
        path: '/metadata/mi',
        limits: ['Maintainer', 'Administrator'],
      },
      {
        title: '日志桶管理',
        path: '/metadata/logs',
        limits: ['Maintainer', 'Administrator'],
      },
    ]
  },
]

const menus = ref([])

const defaultActive = ref('/home')

const pageTitle = PageTitle.title

const showBack = ref(false)

function backEvent() {
  router.go(-1)
}

function getSelectMenu(menuSt) {

  for (let i = 0; i < menus.value.length; i++) {
    let item = menus.value[i]
    if (item.children && item.children.length) {
      for (let i = 0; i < item.children.length; i++) {
        let subItem = item.children[i]
        let isMatch = menuSt.indexOf(subItem.path) == 0
        if (isMatch && menuSt != subItem.path) {
          showBack.value = true
        } else {
          showBack.value = false
        }
        if (isMatch) {
          return subItem
        }
      }
    } else {
      let isMatch = menuSt.indexOf(item.path) == 0
      if (isMatch && menuSt != item.path) {
        showBack.value = true
      } else {
        showBack.value = false
      }
      if (isMatch) {
        return item
      }
    }
  }

  return null
}

function menuSelect(menuSt) {

  let menu = getSelectMenu(menuSt)

  if (menu) {
    defaultActive.value = menu.path
  } else {
    setTimeout(() => {
      router.replace(defaultActive.value)
    }, 0)
  }

  let title = route.meta.title || ''
  PageTitle.setTitle(title)
}

watch(
    () => route.path,
    (newValue, oldValue) => {

      menuSelect(newValue)
    })

function logoutEvent() {
  loginApi.logout().then(res => {
    if (res.result == true) {
      clearCookie('sdptoken')
      router.replace('/login')
    } else {
      ElMessage.error("退出登录失败！")
    }
  })
}

// const userInfo = ref({})
userCenter.getUserInfo().then(userInfo => {
  store.state.userInfo = userInfo || {}

  initMenus()
})

function initMenus() {
  menus.value = []

  let baseUserRole = store.state.userInfo.baseUserRole || []
  if (baseUserRole.length) {
    let roleCode = baseUserRole[0].roleCode || ''

    menuOptions.forEach(menu => {
      if (menu.limits && menu.limits.includes(roleCode)) {
        if (menu.children && menu.children.length) {
          let arr = []
          menu.children.forEach(subMenu => {
            if (subMenu.limits && subMenu.limits.includes(roleCode)) {
              arr.push(subMenu)
            }
          })
          menu.children = arr
        }
        menus.value.push(menu)
      }
    })

    console.log(roleCode)
    console.log(menus.value)

    menuSelect(route.path)
  }
}
</script>

<style lang="stylus" scoped type="text/stylus">
.main {
  width 100%;
  height: 100%;
  overflow hidden;
  background-color #efefef;
  display flex;

  .main-left {
    width 201px;

    .logo-div {
      width 100%;
      height 60px;
      display flex;
      align-items center;
      font-size 20px;
      font-weight bold;
      color white;
      background-color #202132;
      padding-left 20px;
      border-right solid 1px var(--el-menu-border-color);
    }

    .menu-div {
      height calc(100% - 60px);

      .el-menu-item.is-active {
        background-color #315FCE;
      }

      .el-menu {
        height 100%;
        overflow-x hidden;
        overflow-y auto;
      }

      /* 滚动条整体样式 */

      .el-menu::-webkit-scrollbar {
        width: 0px; /* 竖直滚动条宽度 */
        height: 4px; /* 水平滚动条高度 */
      }

      /* 滚动条滑块 */

      .el-menu::-webkit-scrollbar-thumb {
        background: transparent; /* 设置滑块为完全透明 */
        border-radius: 4px; /* 圆角滑块 */
      }

      /* 滚动条轨道（背景） */

      .el-menu::-webkit-scrollbar-track {
        background: transparent; /* 设置轨道为完全透明 */
      }

      ::-webkit-scrollbar-track-piece {
        background-color: transparent; /* 设置轨道为完全透明 不设置不生效 */
      }
    }

  }

  .main-right {
    flex 1;
    height 100%;
    overflow hidden;

    .header-div {
      height 60px;
      display flex;
      align-items center;
      justify-content space-between;
      border-bottom 1px solid #ddd;
      background-color white;

      .page-title {
        font-size 20px;
        padding-left 20px;
        display flex;
        align-items center;

        .page-back {
          display flex;
          align-items center;
          font-size 14px;
          cursor pointer;
          color #333;
          padding-right 15px;
          margin-right 15px;
          border-right 1px solid #ddd;

          span {
            margin-left 5px;
          }

          &:hover {
            color #315FCE;
          }
        }

        .title {
          font-weight bold;
        }
      }

      .top-right-div {
        padding-right 20px;
        display flex;
        align-items center;

        .login-user {
          margin-right 12px;
          font-size 14px;
          color #333;
        }

        .logout-btn {
          margin-right 12px;
          padding 5px 0;
          font-size 14px;
        }

        .msg-btn {
          i {
            display block;
          }
        }

        .btn {
          color #333;
          cursor pointer;

          &:hover {
            color #315FCE;
          }
        }
      }
    }


    .main-content {
      height calc(100% - 60px);
      padding 10px;
      overflow hidden;
      position relative;
    }
  }
}
</style>

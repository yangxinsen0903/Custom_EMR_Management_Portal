/**Created by liaoyingchao on 5/1/22.*/

<template>
  <div class="table-container" v-loading.lock="loading">
    <div class="base-table-main" ref="Ref_Base_Table">
      <slot v-bind:data="{ tableHeight: tableHeight, tableData: tableData, showOperation: showOperation }" v-if="tableHeight"></slot>
    </div>
    <div class="base-table-footer" v-if="hasPagination">
      <div style="flex: 1">
        <slot name="footer"></slot>
      </div>
      <el-pagination
          v-if="hasPagination"
              background
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
              :currentPage="currentPage"
              :page-size="pageSize"
              layout="sizes, total, prev, pager, next"
              :total="total">
      </el-pagination>
    </div>
  </div>
</template>

<script>
  export default {
    name: "table-container",
    components: {},
    props: {
      listApiFunction: {
        type: Function,
        default: null,
      },
      defaultArgs: {
        type: Object,
        default: () => {
          return {}
        },
      },
      operationWidth: {
        type: Number,
        default: 0
      },
      initLoad: {
        type: Boolean,
        default: false
      },
      hasPagination: {
        type: Boolean,
        default: true
      }
    },
    data() {
      return {
        total: 0,
        tableHeight: 0,
        currentPage: 1,
        pageSize: 20,
        tableData: [],
        lastArgs: {},
        showOperation: true,
        zoomRight: 30,
        loading: false,
      }
    },
    methods: {
      zoomOptEvent() {
        this.showOperation = !this.showOperation;
        if (this.showOperation) {
          this.zoomRight = this.operationWidth
        } else {
          this.zoomRight = 30
        }
      },
      handleSizeChange(val) {
        console.log(`每页 ${val} 条`);
        this.pageSize = val;
        let maxIndex = Math.ceil(this.total / this.pageSize)
        if (maxIndex < this.currentPage) {
          this.currentPage = maxIndex
        }
        this.loadData()
      },
      handleCurrentChange(val) {
        this.currentPage = val
        this.loadData()
      },
      filterEvent(args) {
        this.lastArgs = args || {}
        this.currentPage = 1
        this.loadData()
      },
      loadData() {
        if (this.hasPagination) {
          this.lastArgs.pageIndex = this.currentPage
          this.lastArgs.pageSize = this.pageSize
        }

        if (this.listApiFunction && typeof this.listApiFunction == 'function') {
          this.loading = true
          this.listApiFunction(this.lastArgs).then(res => {
            if (res.result == true) {
              this.total = res.total
              this.tableData = []
              this.$nextTick(() => {
                this.tableData = res.data
              })
            } else {
              this.$message.error(res.errorMsg)
            }
          }).finally(() => {
            this.loading = false
          })
        }
      },
      resizeContainer() {
        this.tableHeight = 0
        this.$nextTick(() => {
          let dom = this.$refs.Ref_Base_Table
          this.tableHeight = dom.offsetHeight
        })
      }
    },
    mounted() {
      this.lastArgs = this.defaultArgs || {}
      setTimeout(() => {
        let dom = this.$refs.Ref_Base_Table
        this.tableHeight = dom.offsetHeight
      }, 10)

      if (this.initLoad) {
        this.loadData()
      }

      this.zoomRight = this.operationWidth
    }
  }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style lang="stylus" scoped type="text/stylus">
  .table-container {
    width 100%;
    height 100%;
    display flex;
    flex-direction column;
    .base-table-main {
      width 100%;
      flex 1;
      position relative;
    }
    .base-table-footer {
      padding 10px 10px 0;
      display flex;
      align-items center;
    }
  }
</style>
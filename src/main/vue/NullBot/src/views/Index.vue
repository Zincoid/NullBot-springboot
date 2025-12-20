<template>
  <div>
    <el-container>
      <!-- 头部区域 -->
      <el-header height="auto">
        <el-menu
            ref="menu"
            default-active="1"
            class="el-menu-1"
            mode="horizontal"
            :ellipsis="false"
            style="min-width: 100%;"
        >
          <el-menu-item>
            <h1>NullBot <el-icon size="25">
              <MostlyCloudy />
            </el-icon></h1>
          </el-menu-item>

          <div class="header-center" style="flex: 1; display: flex; align-items: center; justify-content: center;">
            <!-- op=1时显示文件管理相关功能 -->
            <div v-show="op === 1" style="display: flex; align-items: center; gap: 10px; flex-wrap: wrap; justify-content: center;">
              <el-input
                  placeholder="在此目录中搜索"
                  v-model="searchKey"
                  clearable
                  style="width: 600px;"
              >
              </el-input>

              <el-button plain @click="searchFile">
                <el-icon size="15"><Search /></el-icon>搜索
              </el-button>

              <el-upload
                  ref="upload"
                  class="upload"
                  action=""
                  :file-list="uploadFileList"
                  :on-change="handleChange"
                  :auto-upload="false"
                  style="display: inline-flex;"
              >
                <template #trigger>
                  <el-button type="primary" plain>选择文件</el-button>
                </template>
                <el-button class="ml-1" type="success" plain @click="upload">
                  <el-icon size="15"><UploadFilled /></el-icon>上传
                </el-button>
              </el-upload>

              <el-button round plain @click="backDir">
                <el-icon size="15"><RefreshLeft /></el-icon>返回上级
              </el-button>

              <el-button round plain @click="createDir">
                <el-icon size="15"><FolderAdd /></el-icon>新建目录
              </el-button>
            </div>

            <!-- 显示标题 -->
            <!--<h2 v-show="op === 1">文件管理</h2>-->
            <!--<h2 v-show="op === 2">个人中心</h2>-->
            <!--<h2 v-show="op === 3">语录管理</h2>-->
          </div>
          <!-- 右侧用户信息 -->
          <el-menu-item style="margin-left: auto;">
            <el-icon><User /></el-icon>
            <el-text size="large" tag="b">{{ " "+info.username }}</el-text>
          </el-menu-item>
        </el-menu>
      </el-header>

      <el-container>
        <!-- 左侧导航区域 -->
        <el-aside>
          <el-menu
              ref="menu"
              default-active="1"
              class="el-menu-2"
              :ellipsis="false"
              style="min-width: 100%;"
          >
            <h3 align="center"><el-icon>
              <Promotion />
            </el-icon>导航</h3>
            <el-menu-item
                index="1"
                @click="shiftFileManage"
                style="display: flex; justify-content: center; align-items: center;"
            >
              <span><el-icon><Files /></el-icon>文件管理</span>
            </el-menu-item>
            <el-menu-item
                index="3"
                @click="shiftSayingManage"
                style="display: flex; justify-content: center; align-items: center;"
            >
              <span><el-icon><ChatDotSquare /></el-icon>语录管理</span>
            </el-menu-item>
            <el-menu-item
                index="2"
                @click="shiftUserCenter"
                style="display: flex; justify-content: center; align-items: center;"
            >
              <span><el-icon><User /></el-icon>个人中心</span>
            </el-menu-item>
          </el-menu>
        </el-aside>

        <!-- 右侧内容区域 -->
        <el-container style="height: 100%;">
          <el-main style="height: 100%; overflow-y: auto; padding: 20px;">
            <div v-show="op === 1" style="margin-bottom: 15px;">
              <el-icon size="20">
                <HomeFilled />
              </el-icon>
              <el-text size="large">{{ " "+curDir }}</el-text>
            </div>

            <!-- 文件管理 -->
            <div v-show="op === 1">
              <el-table ref="tableData" :data="tableData" style="width: 100%" height="calc(100vh - 250px)">
                <el-table-column type="index" label="序号" width="100"
                                 :index="(pageInfo.current - 1) * pageInfo.size + 1">
                </el-table-column>

                <el-table-column label="文件名" width="600">
                  <template v-slot="scope">
                    {{ scope.row.fileName }}
                  </template>
                </el-table-column>

                <el-table-column label="文件大小" width="250">
                  <template v-slot="scope">
                    {{ scope.row.isDir === 1 ? '/' : (scope.row.fileSize / 1024).toFixed(2) + 'KB' }}
                  </template>
                </el-table-column>

                <el-table-column label="文件类型" width="250">
                  <template v-slot="scope">
                    {{ scope.row.isDir === 1 ? '文件夹' : '文件' }}
                  </template>
                </el-table-column>

                <el-table-column fixed="right" label="操作" width="400">
                  <template v-slot="scope">
                    <el-popconfirm title="确认删除?" @confirm="deleteFile(scope.row)">
                      <template #reference>
                        <el-button type="text" size="small"><el-icon size="17">
                          <Delete />
                        </el-icon>删除</el-button>
                      </template>
                    </el-popconfirm>
                    <el-button type="text" size="small" @click="download(scope.row)"
                               v-if="scope.row.isDir === 0"><el-icon size="17">
                      <Download />
                    </el-icon>下载</el-button>
                    <el-button type="text" size="small" @click="enterDir(scope.row)"
                               v-if="scope.row.isDir === 1"><el-icon size="17">
                      <FolderOpened />
                    </el-icon>进入文件夹</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>

            <!-- 语录管理 -->
            <div v-show="op === 3">
              <el-table ref="sayingTableData" :data="sayingTableData" style="width: 100%" height="calc(100vh - 250px)">
                <el-table-column type="index" label="序号" width="100"
                                 :index="(sayingPageInfo.current - 1) * sayingPageInfo.size + 1">
                </el-table-column>

                <el-table-column label="用户ID" width="150">
                  <template v-slot="scope">
                    {{ scope.row.userId }}
                  </template>
                </el-table-column>

                <el-table-column label="昵称" width="150">
                  <template v-slot="scope">
                    {{ scope.row.userName }}
                  </template>
                </el-table-column>

                <el-table-column label="内容" width="1000">
                  <template v-slot="scope">
                    {{ scope.row.text }}
                  </template>
                </el-table-column>

                <el-table-column label="时间" width="200">
                  <template v-slot="scope">
                    {{ scope.row.time }}
                  </template>
                </el-table-column>

                <el-table-column fixed="right" label="操作" width="150">
                  <template v-slot="scope">
                    <el-popconfirm title="确认删除?" @confirm="deleteSaying(scope.row)">
                      <template #reference>
                        <el-button type="text" size="small"><el-icon size="17">
                          <Delete />
                        </el-icon>删除</el-button>
                      </template>
                    </el-popconfirm>
                  </template>
                </el-table-column>
              </el-table>
            </div>

            <!-- 个人中心 -->
            <div v-show="op === 2" style="max-width: 1000px;">
              <el-descriptions class="info" title="用户信息" :column="1" border>
                <el-descriptions-item label="用户名">
                  <el-tag>{{ info.username }}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="邮箱">
                  <el-tag>{{ info.email }}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="Token">
                  <el-text copyable>{{ token }}</el-text>
                </el-descriptions-item>
              </el-descriptions>

              <div style="margin-top: 20px;">
                <el-button round color="red" @click="logout">
                  <el-icon size="15"><SwitchButton /></el-icon>退出登录
                </el-button>
              </div>
            </div>
          </el-main>

          <!-- 分页区域 -->
          <el-footer height="60px" style="padding: 10px 20px;">
            <div v-show="op === 1" style="text-align: right;">
              <el-pagination
                  background
                  @size-change="handleSizeChange"
                  @current-change="handleCurrentChange"
                  layout="sizes, prev, pager, next"
                  :page-sizes="[10, 20, 30, 40]"
                  :page-size="pageInfo.size"
                  :total="pageInfo.total"
                  :current-page="pageInfo.current"
                  :pager-count="4">
              </el-pagination>
            </div>

            <div v-show="op === 3" style="text-align: right;">
              <el-pagination
                  background
                  @size-change="handleSayingSizeChange"
                  @current-change="handleSayingCurrentChange"
                  layout="sizes, prev, pager, next"
                  :page-sizes="[10, 20, 30, 40]"
                  :page-size="sayingPageInfo.size"
                  :total="sayingPageInfo.total"
                  :current-page="sayingPageInfo.current"
                  :pager-count="4">
              </el-pagination>
            </div>
          </el-footer>
        </el-container>
      </el-container>

      <!-- 搜索对话框 -->
      <el-dialog title="搜索结果" v-model="searchTableVisible" width="55%">
        <el-table ref="searchData" :data="searchData" style="width: 100%">
          <el-table-column type="index" label="序号" width="100">
          </el-table-column>

          <el-table-column label="文件名" width="400">
            <template v-slot="scope">
              {{ scope.row.fileName }}
            </template>
          </el-table-column>

          <el-table-column label="文件大小" width="150">
            <template v-slot="scope">
              {{ scope.row.isDir === 1 ? '/' : (scope.row.fileSize / 1024).toFixed(2) + 'KB' }}
            </template>
          </el-table-column>

          <el-table-column label="文件类型" width="100">
            <template v-slot="scope">
              {{ scope.row.isDir === 1 ? '文件夹' : '文件' }}
            </template>
          </el-table-column>

          <el-table-column fixed="right" label="操作" width="200">
            <template v-slot="scope">
              <el-popconfirm title="确认删除?" @confirm="deleteFile(scope.row)">
                <template #reference>
                  <el-button type="text" size="small"><el-icon size="17">
                    <Delete />
                  </el-icon>删除</el-button>
                </template>
              </el-popconfirm>
              <el-button type="text" @click="download(scope.row)" color="black" size="small" v-if="scope.row.isDir === 0">
                <el-icon size="17"><Download /></el-icon>下载
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-dialog>
    </el-container>
  </div>
</template>

<script>
import {
  ChatDotSquare,
  Delete,
  Download,
  Files, FolderAdd,
  FolderOpened,
  HomeFilled, MostlyCloudy,
  Promotion, RefreshLeft, Search, SwitchButton, UploadFilled,
  User
} from "@element-plus/icons-vue";

export default {
  components: {
    FolderAdd,
    RefreshLeft,
    UploadFilled,
    Search,
    MostlyCloudy,
    SwitchButton, FolderOpened, Download, Delete, HomeFilled, ChatDotSquare, Files, User, Promotion},
  data() {
    return {
      token: 'null',
      info: {
        username: 'null',
        email: 'null'
      },

      searchKey: '',
      searchData: [],

      tableData: [],
      pageInfo: {
        total: 0,
        size: 0,
        current: 0,
        pages: 0
      },

      curDir: '/',
      op: 1,

      uploadFileList: [],

      searchTableVisible: false,

      sayingTableData: [],
      sayingPageInfo: {
        total: 0,
        size: 0,
        current: 0,
        pages: 0
      }
    }
  },

  methods: {
    getInfo() {
      this.$axios.get('/info', {
        headers: {
          token: localStorage.getItem("token")
        }
      }).then(res => {
        if (res.data.code === 200) {
          var info = res.data.data.info
          this.info = JSON.parse(JSON.stringify(info))
        } else if (res.data.code === 400) {
          this.$message.error(res.data.message)
        }
      })
    },

    handleCurrentChange(currentPage) {
      this.getPage(currentPage, this.pageInfo.size)
    },

    handleSizeChange(pageSize) {
      this.getPage(1, pageSize)
    },

    handleSayingCurrentChange(currentPage) {
      this.getSayingPage(currentPage, this.sayingPageInfo.size)
    },

    handleSayingSizeChange(pageSize) {
      this.getSayingPage(1, pageSize)
    },

    shiftFileManage() {
      this.op = 1
    },

    shiftUserCenter() {
      this.op = 2
    },

    shiftSayingManage() {
      this.op = 3
    },

    getPage(currentPage, pageSize) {
      this.$axios({
        url: '/file/' + currentPage + '/' + pageSize,
        headers: {
          'token': localStorage.getItem("token")
        },
        method: 'GET',
        params: {
          curDir: this.curDir,
        }
      }).then(res => {
        this.tableData = JSON.parse(JSON.stringify(res.data.data.filePage.files))
        this.pageInfo.total = res.data.data.filePage.total
        this.pageInfo.size = res.data.data.filePage.pageSize
        this.pageInfo.current = res.data.data.filePage.currentPage
        this.pageInfo.pages = res.data.data.filePage.totalPage
        // console.log('pageInfo:')
        // console.log(this.pageInfo)
        console.log('tableData:')
        console.log(this.tableData)
      })
    },

    searchFile() {
      this.$axios({
        url: '/file/searchFile',
        headers: {
          'token': localStorage.getItem("token")
        },
        method: 'GET',
        params: {
          key: this.searchKey,
          curDir: this.curDir,
        }
      }).then(res => {
        this.searchData = JSON.parse(JSON.stringify(res.data.data.filePage.files))
        this.searchTableVisible = true
        console.log('searchData:')
        console.log(this.searchData)
      })
    },

    deleteFile(file) {
      this.$axios.delete('/file/' + file.id, {
        headers: {
          token: localStorage.getItem("token")
        }
      }).then(res => {
        if (res.data.code === 200) {
          this.$message.success(res.data.message)
          this.getPage(this.pageInfo.current, this.pageInfo.size)

          if (this.searchTableVisible === true) {
            this.searchFile(this.searchKey, this.curDir)
          }
        }
      }).catch(res => {
        this.$message.error("删除失败!")
      })
    },

    upload() {
      if (this.uploadFileList.length === 0) {
        this.$message.warning("未选择文件")
      } else {
        for (let i = 0; i < this.uploadFileList.length; i++) {
          let formData = new FormData()
          formData.append("uploadFile", this.uploadFileList[i].raw)
          formData.append("curDir", this.curDir)
          this.$axios.post("/file/upload", formData, {
            headers: {
              "Content-Type": "multipart/form-data;charset=utf-8",
              token: localStorage.getItem("token")
            },
          }).then(res => {
            if (res.data.code === 200) {
              this.$refs.upload.submit()
              if (this.pageInfo.pages === 0) {
                this.getPage(1, this.pageInfo.size)
              } else {
                this.getPage(this.pageInfo.pages, this.pageInfo.size)
              }
              this.$message.success(res.data.message)
            }
          }).catch(err => {
            this.$message.error("文件上传失败")
          })
        }
      }
    },

    handleChange(file, fileList) {
      this.uploadFileList = fileList;
    },

    download(file) {
      this.$axios.get("/file/download/" + file.id, {
        responseType: "arraybuffer",
        headers: {
          token: localStorage.getItem("token")
        },
      }).then(res => {
        const blob = new Blob([res.data]);
        const elink = document.createElement('a');
        elink.download = file.fileName;
        elink.style.display = 'none';
        elink.href = URL.createObjectURL(blob);
        document.body.appendChild(elink);
        elink.click();
        URL.revokeObjectURL(elink.href); // 释放URL 对象
        document.body.removeChild(elink);
      })
    },

    enterDir(dir) {
      if (this.curDir === "/") {
        this.curDir += dir.fileName
      } else {
        this.curDir += "/" + dir.fileName
      }
      this.getPage(1, this.pageInfo.size)
    },

    backDir() {
      if (this.curDir === "/") {
        this.$message.error("已在根目录")
      } else {
        let index = this.curDir.lastIndexOf('/')
        if (index === 0) {
          this.curDir = "/"
        } else {
          this.curDir = this.curDir.substring(0, index)
        }
      }
      this.getPage(1, this.pageInfo.size)
    },

    createDir() {
      this.$prompt('请输入文件夹名', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPattern: /^(?!.*(\/)|(\\))/,
        inputErrorMessage: "目录中不能包含斜杠"
      }).then(({ value }) => {
        if (!value) {
          this.$message({
            type: 'error',
            message: '文件名不能为空'
          });
        } else {
          this.$axios.post('/file/createDir', {
            curDir: this.curDir !== '' ? this.curDir : '/',
            dirName: value
          }, {
            headers: {
              token: localStorage.getItem("token")
            }
          }).then(res => {
            if (res.data.code === 200) {
              this.$message({
                type: 'success',
                message: value + '创建成功!'
              })
              this.getPage(this.pageInfo.current, this.pageInfo.size)
            } else {
              this.$message({
                type: 'error',
                message: res.data.message
              });
            }
          })
        }
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '取消创建'
        });
      });
    },

    logout() {
      localStorage.clear()
      this.$router.push('/login')
    },

    getSayingPage(currentPage, pageSize) {
      this.$axios({
        url: '/saying/' + currentPage + '/' + pageSize,
        headers: {
          'token': localStorage.getItem("token")
        },
        method: 'GET'
      }).then(res => {
        this.sayingTableData = JSON.parse(JSON.stringify(res.data.data.sayingPage.sayings))
        this.sayingPageInfo.total = res.data.data.sayingPage.total
        this.sayingPageInfo.size = res.data.data.sayingPage.pageSize
        this.sayingPageInfo.current = res.data.data.sayingPage.currentPage
        this.sayingPageInfo.pages = res.data.data.sayingPage.totalPage
        console.log('sayingTableData:')
        console.log(this.sayingTableData)
      })
    },

    deleteSaying(saying) {
      this.$axios.delete('/saying/' + saying.id, {
        headers: {
          token: localStorage.getItem("token")
        }
      }).then(res => {
        if (res.data.code === 200) {
          this.$message.success(res.data.message)
          this.getSayingPage(this.sayingPageInfo.current, this.sayingPageInfo.size)
        }
      }).catch(res => {
        this.$message.error("删除失败!")
      })
    }
  },

  created() {
    this.token = localStorage.getItem("token");
    if (this.token === null) {
      this.$router.push('/login')
      return
    }
    this.getInfo()
    this.getPage(1, 10)
    this.getSayingPage(1, 10)
  },
}

</script>

<style scoped>
.el-menu--horizontal > .el-menu-item:nth-child(1) {
  margin-right: auto;
}
</style>
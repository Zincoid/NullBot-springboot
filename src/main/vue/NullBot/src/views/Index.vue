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
            <!-- op=1时显示搜索功能 -->
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
            </div>
            <!-- op=2时显示问候 -->
            <h3 v-show="op === 2" style="display: flex; align-items: center; gap: 10px; flex-wrap: wrap; justify-content: center;">
              <span>Ciallo～(∠・ω< ) <span style="font-weight: bold;">{{ currentTime }}</span> ⌒☆</span>

            </h3>
            <!-- op=3时显示语录管理 -->
            <h3 v-show="op === 3" style="display: flex; align-items: center; gap: 10px; flex-wrap: wrap; justify-content: center;">
              <el-icon><Comment /></el-icon>语录集
            </h3>
          </div>

          <!-- 右侧用户信息 -->
          <el-sub-menu style="margin-left: auto;">
            <el-menu-item>
              <el-button type="danger" plain style="width: 100%; justify-content: center;" @click="logout">
                <el-icon size="15"><SwitchButton /></el-icon>退出登录
              </el-button>
            </el-menu-item>
            <template #title>
              <el-icon><User /></el-icon>
              <el-text size="large" tag="b">{{ " "+info.username }}</el-text>
            </template>
          </el-sub-menu>

        </el-menu>
      </el-header>

      <el-container>
        <!-- 左侧导航区域 -->
        <el-aside width="200px">
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
            <!-- 文件管理头部 -->
            <div v-show="op === 1" style="margin-bottom: 15px; display: flex; justify-content: space-between; align-items: center;">
              <div style="display: flex; align-items: center;">
                <el-icon size="20">
                  <HomeFilled />
                </el-icon>
                <el-text size="large">{{ " "+curDir }}</el-text>
              </div>

              <!-- 文件操作按钮 -->
              <div style="display: flex; align-items: center;">
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
                    <el-button type="primary" plain >选择文件</el-button>
                  </template>
                </el-upload>
                <el-button class="ml-1" type="success" plain @click="upload">
                  <el-icon size="15"><UploadFilled /></el-icon>上传
                </el-button>
                <el-button round plain @click="backDir">
                  <el-icon size="15"><RefreshLeft /></el-icon>返回上级
                </el-button>
                <el-button round plain @click="createDir">
                  <el-icon size="15"><FolderAdd /></el-icon>新建目录
                </el-button>
              </div>
            </div>

            <!-- 文件管理 -->
            <div v-show="op === 1">
              <el-table ref="tableData" :data="tableData" style="width: 100%" height="calc(100vh - 250px)">
                <el-table-column type="index" label="序号" min-width="80"
                                 :index="(pageInfo.current - 1) * pageInfo.size + 1">
                </el-table-column>

                <el-table-column label="文件名" min-width="300" show-overflow-tooltip>
                  <template v-slot="scope">
                    <div style="display: flex; align-items: center;">
                      <el-icon v-if="scope.row.isDir === 1" style="margin-right: 8px;">
                        <Folder />
                      </el-icon>
                      <el-icon v-else style="margin-right: 8px;">
                        <Document />
                      </el-icon>
                      {{ scope.row.fileName }}
                    </div>
                  </template>
                </el-table-column>

                <el-table-column label="文件大小" min-width="120">
                  <template v-slot="scope">
                    {{ scope.row.isDir === 1 ? '-' : formatFileSize(scope.row.fileSize) }}
                  </template>
                </el-table-column>

                <el-table-column label="文件类型" min-width="100">
                  <template v-slot="scope">
                    <el-tag :type="scope.row.isDir === 1 ? 'info' : 'success'">
                      {{ scope.row.isDir === 1 ? '文件夹' : getFileExtension(scope.row.fileName) }}
                    </el-tag>
                  </template>
                </el-table-column>

                <el-table-column label="修改时间" min-width="160" v-if="tableData[0] && tableData[0].modifyTime">
                  <template v-slot="scope">
                    {{ scope.row.modifyTime || '-' }}
                  </template>
                </el-table-column>

                <el-table-column fixed="right" label="操作" width="220" align="center">
                  <template v-slot="scope">
                    <div style="display: flex; gap: 2px; justify-content: center;">
                      <el-button type="info" plain @click="handlePreview(scope.row)"
                                 v-if="isPreviewable(scope.row)" size="small" title="预览">
                        <el-icon size="14"><Picture /></el-icon>
                      </el-button>
                      <el-button type="warning" plain size="small" @click="handleRename(scope.row)" title="重命名">
                        <el-icon size="14"><Edit /></el-icon>
                      </el-button>
                      <el-button type="primary" plain size="small" @click="enterDir(scope.row)"
                                 v-if="scope.row.isDir === 1" title="进入文件夹">
                        <el-icon size="14"><FolderOpened /></el-icon>
                      </el-button>

                      <el-button type="success" plain size="small" @click="download(scope.row)"
                                 v-if="scope.row.isDir === 0" title="下载">
                        <el-icon size="14"><Download /></el-icon>
                      </el-button>

                      <el-popconfirm title="确认删除吗?" @confirm="deleteFile(scope.row)">
                        <template #reference>
                          <el-button type="danger" plain size="small" title="删除">
                            <el-icon size="14"><Delete /></el-icon>
                          </el-button>
                        </template>
                      </el-popconfirm>
                    </div>
                  </template>
                </el-table-column>
              </el-table>
            </div>

            <!-- 语录管理 -->
            <div v-show="op === 3">
              <el-table ref="sayingTableData" :data="sayingTableData" style="width: 100%" height="calc(100vh - 250px)">
                <el-table-column type="index" label="序号" min-width="80"
                                 :index="(sayingPageInfo.current - 1) * sayingPageInfo.size + 1">
                </el-table-column>

                <el-table-column label="用户ID" min-width="120">
                  <template v-slot="scope">
                    {{ scope.row.userId }}
                  </template>
                </el-table-column>

                <el-table-column label="昵称" min-width="120">
                  <template v-slot="scope">
                    {{ scope.row.userName }}
                  </template>
                </el-table-column>

                <el-table-column label="内容" min-width="750" prop="text" show-overflow-tooltip>
                  <!-- 不设置宽度，自动填充剩余空间 -->
                </el-table-column>

                <el-table-column label="时间" min-width="180">
                  <template v-slot="scope">
                    {{ scope.row.time }}
                  </template>
                </el-table-column>

                <el-table-column fixed="right" label="操作" width="110" align="center">
                  <template v-slot="scope">
                    <el-popconfirm title="确认删除吗?" @confirm="deleteSaying(scope.row)">
                      <template #reference>
                        <el-button type="danger" plain size="small" title="删除">
                          <el-icon size="14"><Delete /></el-icon>
                        </el-button>
                      </template>
                    </el-popconfirm>
                  </template>
                </el-table-column>
              </el-table>
            </div>

            <!-- 个人中心 -->
            <div v-show="op === 2" style="margin-right: 20px;">
              <el-descriptions class="info" title="用户信息" :column="1" border>
                <el-descriptions-item label="用户名">
                  <el-tag>{{ info.username }}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="邮箱">
                  <el-tag>{{ info.email }}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="Token">
                  <el-tag copyable>{{ token }}</el-tag>
                </el-descriptions-item>
              </el-descriptions>

              <div style="margin-top: 10px; display: flex; justify-content: flex-end;">
                <el-button type="danger" round plain @click="logout">
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
        <el-table ref="searchData" :data="searchData" style="width: 100%" stripe>
          <el-table-column type="index" label="序号" width="80">
          </el-table-column>

          <el-table-column label="文件名" min-width="200">
            <template v-slot="scope">
              <div style="white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                {{ scope.row.fileName }}
              </div>
            </template>
          </el-table-column>

          <el-table-column label="文件大小" width="120">
            <template v-slot="scope">
              {{ scope.row.isDir === 1 ? '/' : (scope.row.fileSize / 1024).toFixed(2) + 'KB' }}
            </template>
          </el-table-column>

          <el-table-column label="文件类型" width="100">
            <template v-slot="scope">
              {{ scope.row.isDir === 1 ? '文件夹' : '文件' }}
            </template>
          </el-table-column>

          <el-table-column fixed="right" label="操作" width="250" align="center">
            <template v-slot="scope">
              <el-button type="text" @click="download(scope.row)" size="small" v-if="scope.row.isDir === 0">
                <el-icon size="17"><Download /></el-icon>下载
              </el-button>
              <el-button type="text" @click="handlePreview(scope.row)" size="small" v-if="isPreviewable(scope.row)">
                <el-icon size="17"><Picture /></el-icon>预览
              </el-button>
              <el-popconfirm title="确认删除?" @confirm="deleteFile(scope.row)">
                <template #reference>
                  <el-button type="text" size="small"><el-icon size="17">
                    <Delete />
                  </el-icon>删除</el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
      </el-dialog>

      <!-- 图片/视频预览对话框 -->
      <el-dialog v-model="previewVisible" :title="previewTitle" :destroy-on-close="true" width="70%" top="5vh" center>
        <div style="text-align: center; max-height: 70vh; overflow: auto;">
          <!-- 图片预览 -->
          <el-image
              v-if="previewType === 'image'"
              :src="previewUrl"
              :preview-src-list="[previewUrl]"
              fit="contain"
              style="max-width: 100%; max-height: 65vh;"
              :hide-on-click-modal="true"
          />
          <!-- 视频预览 -->
          <video
              v-else-if="previewType === 'video'"
              :src="previewUrl"
              controls
              autoplay
              style="max-width: 100%; max-height: 65vh;"
          >
            您的浏览器不支持 video 标签。
          </video>
          <audio
              v-else-if="previewType === 'audio'"
              ref="videoPlayer"
              :src="previewUrl"
              controls
              autoplay
              style="max-width: 100%; max-height: 65vh;"
          >
            您的浏览器不支持 audio 标签。
          </audio>
          <!-- 可以在这里扩展 -->
        </div>
      </el-dialog>
    </el-container>
  </div>
</template>

<script>
import {
  ChatDotSquare, Comment,
  Delete, Document,
  Download,
  Files, Folder, FolderAdd,
  FolderOpened,
  HomeFilled, MostlyCloudy, Picture,
  Promotion, RefreshLeft, Search, SwitchButton, UploadFilled,
  User
} from "@element-plus/icons-vue";
import axios from "axios";

export default {
  components: {
    Picture,
    Comment,
    Document,
    Folder,
    FolderAdd,
    RefreshLeft,
    UploadFilled,
    Search,
    MostlyCloudy,
    SwitchButton, FolderOpened, Download, Delete, HomeFilled, ChatDotSquare, Files, User, Promotion},

  data() {
    return {
      currentTime: '',
      timer: null,

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
      },

      previewVisible: false, // 控制预览对话框显示
      previewUrl: '', // 预览文件的完整URL
      previewType: '', // 'image' 或 'video'
      previewTitle: '' // 预览对话框标题
    }
  },

  methods: {
    // 时钟更新
    updateTime() {
      const now = new Date()
      this.currentTime = now.toLocaleTimeString('zh-CN', {
        hour12: false,
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      })
    },

    // 从文件名获取文件扩展名
    getFileExtension(fileName) {
      if (!fileName) return '未知类型'
      // 获取最后一个点之后的部分作为文件扩展名
      const lastDotIndex = fileName.lastIndexOf('.')
      if (lastDotIndex === -1 || lastDotIndex === fileName.length - 1) {
        return '未知类型'
      }
      // 获取后缀并转为大写
      const extension = fileName.substring(lastDotIndex + 1).toUpperCase()
      // 如果后缀太长，可以截断显示
      return extension.length > 8 ? extension.substring(0, 8) + '...' : extension
    },

    // 判断文件是否可预览（根据扩展名）
    isPreviewable(file) {
      if (file.isDir === 1) return false; // 文件夹不可预览
      const fileName = file.fileName.toLowerCase();
      const imageExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp'];
      const videoExtensions = ['.mp4', '.webm', '.ogg', '.mov', '.avi', '.mkv'];
      const audioExtensions = ['.mp3', '.wav', '.flac', '.aac', '.ogg', '.m4a', '.wma', '.opus'];
      return imageExtensions.some(ext => fileName.endsWith(ext)) ||
          videoExtensions.some(ext => fileName.endsWith(ext)) ||
          audioExtensions.some(ext => fileName.endsWith(ext));
    },

    // 处理预览点击事件
    handlePreview(file) {
      // 1. 构造文件预览URL（需要后端的支持）
      const baseUrl = axios.defaults.baseURL + '/preview/';
      this.previewUrl = baseUrl + file.id + '?token=' + localStorage.getItem("token");

      // 2. 判断文件类型
      const fileName = file.fileName.toLowerCase();
      const imageExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp'];
      const videoExtensions = ['.mp4', '.webm', '.ogg', '.mov', '.avi', '.mkv'];
      this.previewType = imageExtensions.some(ext => fileName.endsWith(ext)) ? 'image' : (videoExtensions.some(ext => fileName.endsWith(ext)) ? 'video' : 'audio');

      // 3. 设置对话框标题
      this.previewTitle = `预览 - ${file.fileName}`;

      // 4. 打开对话框
      this.previewVisible = true;
    },

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

    formatFileSize(bytes) {
      if (bytes === 0) return '0 B';
      const k = 1024;
      const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
      const i = Math.floor(Math.log(bytes) / Math.log(k));
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
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
      this.$prompt('请输入目录名', '新建目录', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPattern: /^(?!.*(\/)|(\\))/,
        inputErrorMessage: "目录名不能包含斜杠"
      }).then(({ value }) => {
        if (!value) {
          this.$message({
            type: 'error',
            message: '目录名不能为空'
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

    handleRename(file) {
      this.$prompt('请输入新的文件名', '重命名', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputValue: file.fileName,
        inputPattern: /^[^\\\/:*?"<>|]+$/,
        inputErrorMessage: '文件名不能包含 \\ / : * ? " < > | 等字符'
      }).then(({ value }) => {
        if (!value || value.trim() === '') {
          this.$message.error('文件名不能为空');
          return;
        }

        if (value === file.fileName) {
          this.$message.warning('文件名未更改');
          return;
        }

        this.$axios({
          url: `/file/rename/${file.id}`,
          method: 'GET',
          headers: {
            'token': localStorage.getItem("token")
          },
          params: {
            newFileName: value
          }
        }).then(res => {
          if (res.data.code === 200) {
            this.$message.success('重命名成功');

            // 刷新当前视图
            if (this.op === 1) {
              this.getPage(this.pageInfo.current, this.pageInfo.size);
            } else if (this.searchTableVisible) {
              this.searchFile();
            }
          } else {
            this.$message.error(res.data.message || '重命名失败');
          }
        }).catch(error => {
          console.error('重命名失败:', error);
          this.$message.error('重命名失败');
        });
      }).catch(() => {
        this.$message.info('已取消重命名');
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

  mounted() {
    this.updateTime()
    this.timer = setInterval(this.updateTime, 1000)
  },

  beforeUnmount() {
    if (this.timer) {
      clearInterval(this.timer)
    }
  }
}

</script>

<style scoped>
</style>
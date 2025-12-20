<template>
    <div>
        <el-container>
            <el-header height="120px">
                <el-row>
                    <el-col :span="4">
                        <h1>NullBot <el-icon size="25">
                                <MostlyCloudy />
                            </el-icon></h1>
                    </el-col>
                    <el-col :span="14" align="center">
                        <h1 v-show="op === 1">文件管理</h1>
                        <h1 v-show="op === 2">个人中心</h1>
                    </el-col>
                    <el-col :span="2" :offset="2" align="right">
                        <el-icon size="25">
                            <User />
                        </el-icon>
                        <el-text size="large" tag="b">{{ " "+info.username }}</el-text>
                    </el-col>
                </el-row>
                <el-row>
                    <el-col :span="8" :offset="4" v-show="op === 1">
                        <el-input placeholder="在此目录中搜索" v-model="searchKey" clearable>
                        </el-input>
                    </el-col>
                    <el-col :span="2" v-show="op === 1">
                        <el-button @click="searchFile"><el-icon size="15">
                                <Search />
                            </el-icon>搜索</el-button>
                    </el-col>

                    <el-col :span="3" :offset="1" v-show="op === 1">
                        <el-upload ref="upload" class="upload" action="" :file-list="uploadFileList"
                            :on-change="handleChange" :auto-upload="false">
                            <template #trigger>
                                <el-button type="primary">选择文件</el-button>
                            </template>
                            <el-button class="ml-3" type="success" @click="upload">
                                <el-icon size="18"><UploadFilled /></el-icon>上传到服务器
                            </el-button>
                        </el-upload>
                    </el-col>
                    <el-col :span="4" :offset="1" v-show="op === 1">
                        <el-button round @click="backDir"><el-icon size="18"><RefreshLeft /></el-icon>返回上级目录</el-button>
                        <el-button round @click="createDir"><el-icon size="18"><FolderAdd /></el-icon>新建目录</el-button>
                    </el-col>
                </el-row>
            </el-header>

            <el-main>
                <el-row v-show="op === 1">
                    <el-col :span="20" :offset="3">
                        <el-icon size="20">
                            <HomeFilled />
                        </el-icon>
                        <el-text size="large">{{ " "+curDir }}</el-text>
                    </el-col>
                </el-row>
                <el-row>
                    <el-col :span="3">
                        <h3 align="center"><el-icon>
                                <Promotion />
                            </el-icon>导航</h3>
                        <el-menu ref="menu" default-active="1" @open="handleOpen" @close="handleClose">
                            <el-menu-item index="1" @click="shiftFileManage">
                                <span><el-icon>
                                        <Files />
                                    </el-icon>文件管理</span>
                            </el-menu-item>
                            <el-menu-item index="2" @click="shiftUserCenter">
                                <span><el-icon>
                                        <User />
                                    </el-icon>个人中心</span>
                            </el-menu-item>
                        </el-menu>
                    </el-col>

                    <el-col :span="20" v-show="op === 1">
                        <el-table ref="tableData" :data="tableData" style="width: 100%">
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
                                    <el-button @click="deleteFile(scope.row)" color="red"><el-icon size="17">
                                            <Delete />
                                        </el-icon>删除</el-button>
                                    <el-button @click="download(scope.row)" color="black"
                                        v-if="scope.row.isDir === 0"><el-icon size="17">
                                            <Download />
                                        </el-icon>下载</el-button>
                                    <el-button @click="enterDir(scope.row)" color="lightgrey"
                                        v-if="scope.row.isDir === 1"><el-icon size="17">
                                            <FolderOpened />
                                        </el-icon>进入文件夹</el-button>
                                </template>
                            </el-table-column>
                        </el-table>
                    </el-col>

                    <el-col :span="18" :offset="1" v-show="op === 2">
                        <el-descriptions class="info">
                            <el-descriptions-item label="用户名">
                                <el-tag>{{ info.username }}</el-tag>
                            </el-descriptions-item>
                            <el-descriptions-item label="邮箱">
                                <el-tag>{{ info.email }}</el-tag>
                            </el-descriptions-item>
                            <el-descriptions-item label="Token">
                                <el-tag>{{ token }}</el-tag>
                            </el-descriptions-item>
                        </el-descriptions>

                        <el-col align :span="4" v-show="op === 2">
                            <el-button round color="blue" @click="logout"><el-icon size="15"><SwitchButton /></el-icon>退出登录</el-button>
                            <!-- <el-button round color="crimson" @click="cancelUser"><el-icon size="18"><WarnTriangleFilled /></el-icon>注销账户</el-button> -->
                        </el-col>
                    </el-col>

                </el-row>
            </el-main>

            <el-footer v-show="op === 1">
                <el-row>
                    <el-col :span="6" :offset="18">
                        <el-pagination background @size-change="handleSizeChange" @current-change="handleCurrentChange"
                            layout="sizes, prev, pager, next" :page-sizes="[3, 5, 7, 9]" :page-size="pageInfo.size"
                            :total="pageInfo.total" :current-page="pageInfo.current" :pager-count="4">
                        </el-pagination>
                    </el-col>
                </el-row>
            </el-footer>

            <el-dialog title="搜索结果" v-model="searchTableVisible">
                <el-table ref="searchData" :data="searchData">
                    <el-table-column type="index" label="序号" width="100">
                    </el-table-column>

                    <el-table-column label="文件名" width="300">
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
                            <el-button @click="deleteFile(scope.row)" color="red"><el-icon size="17">
                                    <Delete />
                                </el-icon>删除</el-button>
                            <el-button @click="download(scope.row)" color="black" v-if="scope.row.isDir === 0"><el-icon
                                    size="17">
                                    <Download />
                                </el-icon>下载</el-button>
                        </template>
                    </el-table-column>
                </el-table>
            </el-dialog>
        </el-container>
    </div>
</template>

<script>
export default {
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
                    // console.log('info:')
                    // console.log(this.info)
                    // console.log(this.token)
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

        shiftFileManage() {
            this.op = 1
        },

        shiftUserCenter() {
            this.op = 2
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
                    this.getPage(this.pageInfo.pages, this.pageInfo.size)

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
                    this.$axios.post("/nullbot/file/upload", formData, {
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
        }
    },

    created() {
        this.token = localStorage.getItem("token");
        if (this.token === null) {
            this.$router.push('/login')
            return
        }
        this.getInfo()
        this.getPage(1, 5)
    },
}

</script>

<style scoped></style>
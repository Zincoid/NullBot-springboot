<template>
  <div style="width: 500px;margin: auto">
    <div>
      <h1 align="center">NullBot 管理系统</h1>
    </div>
    <el-form :model="LoginForm" ref="LoginForm" label-width="100px" class="demo-ruleForm">
      <el-form-item label="账号">
        <el-input v-model="LoginForm.id"></el-input>
      </el-form-item>
      <el-form-item label="密码">
        <el-input v-model="LoginForm.password" show-password></el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="submitForm()">登录</el-button>
        <!-- <el-link type="primary" :underline="false" @click="toRegister">前往注册</el-link> -->
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
export default {
  name: "Login",
  data() {
    return {
      LoginForm: {
        id: '',
        password: ''
      },
    }
  },

  methods: {
    submitForm() {
      this.$axios.post('/nullbot/login', this.LoginForm)
          .then(res => {
            console.log(res.data)
            if (res.data.code === 200){
              this.$message.success("登录成功!")
              localStorage.setItem("token", res.data.data.token)
              this.$router.push('/index')
            }else if (res.data.code === 400){
              this.$message.warning(res.data.message)
            }
          })
    },
    // toRegister(){
    //   this.$router.push('/register')
    // }
  },

  created() {
    localStorage.clear();
  }
}
</script>

<style scoped>

</style>
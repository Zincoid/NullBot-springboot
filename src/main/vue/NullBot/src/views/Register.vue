<template>
    <div style="width: 500px;margin: auto">
      <div>
        <h1 align="center">NetDisk 注册</h1>
      </div>
      <el-form :model="LoginForm" :rules="rules" ref="registerForm" label-width="100px" class="demo-ruleForm">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="LoginForm.username"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="LoginForm.email"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="LoginForm.password" show-password></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitForm('registerForm')">注册</el-button>
          <el-link type="primary" :underline="false" @click="toLogin">前往登录</el-link>
        </el-form-item>
      </el-form>
    </div>
</template>

<script>
export default {
  name: "Register",
  data() {
    return {
      LoginForm: {
        username: '',
        password: '',
        email: ''
      },
    }
  },
  methods: {
    submitForm(formName) {
      this.$axios.post('/user/register', this.LoginForm)
          .then(res => {
            console.log(res.data)
            if (res.data.code === 200){
              this.$message.success("注册成功!")
              this.$router.push('/login')
            } else {
              this.$message.warning(res.data.message)
            }
          })
    },
    toLogin(){
      this.$router.push('/login')
    }
  }
}
</script>

<style scoped>

</style>
<template>
  <div style="
    width: 300px;
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
  ">
    <div>
      <h1 align="center" style="margin-bottom: 20px;">&nbsp;&nbsp;&nbsp;&nbsp;Hi! NullBot =]</h1>
    </div>
    <el-form
        :model="LoginForm"
        ref="LoginForm"
        label-width="40px"
    class="demo-ruleForm"
    >
    <el-form-item label="账号">
      <el-input
          placeholder="请输入账号"
          v-model="LoginForm.id"
          style="width: 100%"
      ></el-input>
    </el-form-item>
    <el-form-item label="密码">
      <el-input
          placeholder="请输入密码"
          v-model="LoginForm.password"
          show-password
          style="width: 100%"
      ></el-input>
    </el-form-item>
    <el-form-item>
      <div style="display: flex; justify-content: center; width: 100%;">
        <el-button
            type="primary"
            plain
            @click="submitForm()"
            style="width: 100%;"
        >登录</el-button>
      </div>
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
      this.$axios.post('/login', this.LoginForm)
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
    }
  },

  created() {
    localStorage.clear();
  }
}
</script>

<style scoped>

</style>
<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <h2>Dreams Admin</h2>
          <p>后台管理系统</p>
        </div>
      </template>
      <el-form ref="loginForm" :model="form" :rules="rules" label-width="0">
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="用户名/手机号/邮箱"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            prefix-icon="Lock"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            style="width: 100%"
            :loading="loading"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/api/admin'

const router = useRouter()
const loginForm = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名/手机号/邮箱', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!loginForm.value) return
  await loginForm.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const data = await request.post('/auth/login', {
        username: form.username,
        password: form.password
      })
      // admin.js interceptor 已解包 Result，data = {token, userId, role}
      localStorage.setItem('admin_token', data.token)
      localStorage.setItem('admin_user', JSON.stringify({
        id: data.userId,
        username: form.username,
        role: data.role
      }))
      ElMessage.success('登录成功')
      router.push('/dashboard')
    } catch (err) {
      const msg = err.message || '登录失败，请检查用户名和密码'
      ElMessage.error(msg)
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped lang="scss">
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #6C5CE7, #a855f7);
}

.login-card {
  width: 400px;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);

  .card-header {
    text-align: center;

    h2 {
      font-size: 24px;
      color: #6C5CE7;
      margin-bottom: 4px;
    }

    p {
      font-size: 14px;
      color: #999;
    }
  }
}
</style>

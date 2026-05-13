<template>
  <el-container class="layout-container">
    <!-- Sidebar -->
    <el-aside width="220px" class="sidebar">
      <div class="sidebar-logo">
        <h2>Dreams Admin</h2>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="sidebar-menu"
        background-color="#6C5CE7"
        text-color="rgba(255,255,255,0.7)"
        active-text-color="#ffffff"
        router
      >
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <span>控制台</span>
        </el-menu-item>
        <el-menu-item index="/dashboard/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/dashboard/dreams">
          <el-icon><MoonNight /></el-icon>
          <span>梦境管理</span>
        </el-menu-item>
        <el-menu-item index="/dashboard/categories">
          <el-icon><FolderOpened /></el-icon>
          <span>分类管理</span>
        </el-menu-item>
        <el-menu-item index="/dashboard/discussions">
          <el-icon><ChatDotRound /></el-icon>
          <span>讨论组管理</span>
        </el-menu-item>
        <el-menu-item index="/dashboard/comments">
          <el-icon><Comment /></el-icon>
          <span>评论管理</span>
        </el-menu-item>
        <el-menu-item index="/dashboard/audits">
          <el-icon><Checked /></el-icon>
          <span>内容审核</span>
        </el-menu-item>
        <el-menu-item index="/dashboard/notifications">
          <el-icon><Bell /></el-icon>
          <span>通知管理</span>
        </el-menu-item>
        <el-menu-item index="/dashboard/keywords">
          <el-icon><Search /></el-icon>
          <span>审核关键词</span>
        </el-menu-item>
        <el-menu-item index="/dashboard/logs">
          <el-icon><Document /></el-icon>
          <span>操作日志</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container class="main-container">
      <!-- Header -->
      <el-header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="admin-info">
              <el-icon><User /></el-icon>
              <span class="admin-name">{{ adminName }}</span>
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- Main Content -->
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()

const adminName = computed(() => {
  try {
    const user = JSON.parse(localStorage.getItem('admin_user') || '{}')
    return user.username || '管理员'
  } catch {
    return '管理员'
  }
})

const activeMenu = computed(() => {
  const path = route.path
  if (path === '/dashboard' || path === '/') return '/dashboard'
  return path
})

const currentTitle = computed(() => {
  const matched = route.matched[1]
  return matched?.meta?.title || '控制台'
})

const handleCommand = (command) => {
  if (command === 'logout') {
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_user')
    ElMessage.success('已退出登录')
    router.push('/login')
  }
}
</script>

<style scoped lang="scss">
.layout-container {
  height: 100vh;
}

.sidebar {
  background-color: #6C5CE7;
  overflow-y: auto;

  &::-webkit-scrollbar {
    width: 0;
  }

  .sidebar-logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);

    h2 {
      color: #fff;
      font-size: 18px;
      font-weight: 600;
    }
  }

  .sidebar-menu {
    border-right: none;
  }
}

.main-container {
  display: flex;
  flex-direction: column;
}

.header {
  background-color: #fff;
  border-bottom: 1px solid #e6e8eb;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 60px;

  .header-left {
    flex: 1;
  }

  .header-right {
    .admin-info {
      display: flex;
      align-items: center;
      cursor: pointer;
      color: #333;

      .admin-name {
        margin: 0 4px;
      }
    }
  }
}

.main-content {
  background-color: #f5f7fa;
  padding: 20px;
  overflow-y: auto;
  flex: 1;
}
</style>

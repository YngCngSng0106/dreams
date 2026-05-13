<template>
  <el-container class="layout-container">
    <!-- Sidebar -->
    <el-aside width="220px" class="sidebar">
      <div class="sidebar-logo">
        <h2>Dreams Admin</h2>
      </div>
      <div class="sidebar-menu">
        <div
          v-for="item in menuItems"
          :key="item.path"
          class="menu-item"
          :class="{ active: activeMenu === item.index }"
          @click="navigate(item.path)"
        >
          <span class="menu-icon">{{ item.icon }}</span>
          <span class="menu-text">{{ item.title }}</span>
        </div>
      </div>
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
              <span class="admin-name">{{ adminName }}</span>
              <span class="arrow">▼</span>
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
import { computed, provide } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
provide('router', router)

const menuItems = [
  { index: 'dashboard', path: '/dashboard', title: '控制台', icon: '📊' },
  { index: 'users', path: '/dashboard/users', title: '用户管理', icon: '👤' },
  { index: 'dreams', path: '/dashboard/dreams', title: '梦境管理', icon: '🌙' },
  { index: 'categories', path: '/dashboard/categories', title: '分类管理', icon: '📁' },
  { index: 'discussions', path: '/dashboard/discussions', title: '讨论组管理', icon: '💬' },
  { index: 'comments', path: '/dashboard/comments', title: '评论管理', icon: '✏️' },
  { index: 'audits', path: '/dashboard/audits', title: '内容审核', icon: '✅' },
  { index: 'notifications', path: '/dashboard/notifications', title: '通知管理', icon: '🔔' },
  { index: 'keywords', path: '/dashboard/keywords', title: '审核关键词', icon: '🔍' },
  { index: 'logs', path: '/dashboard/logs', title: '操作日志', icon: '📋' },
]

const adminName = computed(() => {
  try {
    const user = JSON.parse(localStorage.getItem('admin_user') || '{}')
    return user.username || '管理员'
  } catch (e) {
    return '管理员'
  }
})

const activeMenu = computed(() => {
  const path = route.path
  if (path === '/dashboard' || path === '/') return 'dashboard'
  return path.replace('/dashboard/', '')
})

const currentTitle = computed(() => {
  const matched = route.matched[1]
  return matched?.meta?.title || '控制台'
})

const navigate = (path) => {
  router.push(path)
}

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
    .menu-item {
      display: flex;
      align-items: center;
      padding: 0 20px;
      height: 50px;
      cursor: pointer;
      color: rgba(255, 255, 255, 0.7);
      transition: all 0.2s;

      .menu-icon {
        margin-right: 12px;
        font-size: 18px;
        width: 24px;
        text-align: center;
      }

      .menu-text {
        font-size: 14px;
        white-space: nowrap;
      }

      &:hover {
        background-color: rgba(255, 255, 255, 0.1);
        color: #fff;
      }

      &.active {
        background-color: rgba(255, 255, 255, 0.15);
        color: #fff;
      }
    }
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
        margin-right: 4px;
      }

      .arrow {
        font-size: 10px;
        color: #999;
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

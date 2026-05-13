<template>
  <el-card shadow="hover" class="page-card">
    <template #header>
      <div class="card-header">
        <span>用户管理</span>
      </div>
    </template>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input v-model="searchForm.keyword" placeholder="搜索用户名/昵称/邮箱" clearable class="search-input" @keyup.enter="handleSearch" />
      <el-select v-model="searchForm.isBanned" placeholder="全部状态" clearable class="search-select">
        <el-option label="全部状态" value="" />
        <el-option label="正常" value="0" />
        <el-option label="封禁" value="1" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="tableData" border stripe highlight-current-row v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" width="140" />
      <el-table-column prop="nickname" label="昵称" width="140" />
      <el-table-column prop="email" label="邮箱" width="200" />
      <el-table-column prop="createTime" label="注册时间" width="180">
        <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
      </el-table-column>
      <el-table-column prop="isBanned" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.isBanned ? 'danger' : 'success'">{{ row.isBanned ? '封禁' : '正常' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button :type="row.isBanned ? 'success' : 'danger'" size="small" @click="handleBanUnban(row)">
            {{ row.isBanned ? '解封' : '封禁' }}
          </el-button>
          <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrap">
      <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size" :page-sizes="[10, 20, 50, 100]" :total="pagination.total" layout="total, sizes, prev, pager, next, jumper" @size-change="fetchData" @current-change="fetchData" />
    </div>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import admin from '@/api/admin'

const loading = ref(false)
const tableData = ref([])
const searchForm = reactive({ keyword: '', isBanned: '' })
const pagination = reactive({ page: 1, size: 20, total: 0 })

const formatDate = (val) => {
  if (!val) return '-'
  return new Date(val).toLocaleString('zh-CN')
}

const fetchData = async () => {
  loading.value = true
  try {
    const { data } = await admin.get('/admin/users', {
      params: {
        page: pagination.page,
        size: pagination.size,
        keyword: searchForm.keyword,
        isBanned: searchForm.isBanned !== '' ? searchForm.isBanned : undefined
      }
    })
    tableData.value = data.records || data.content || data.list || []
    pagination.total = data.total || data.totalElements || 0
  } catch (err) {
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pagination.page = 1; fetchData() }
const handleReset = () => { searchForm.keyword = ''; searchForm.isBanned = ''; pagination.page = 1; fetchData() }

const handleBanUnban = async (row) => {
  const isBan = !row.isBanned
  const action = isBan ? '封禁' : '解封'
  try {
    await ElMessageBox.confirm(`确定要${action}用户"${row.username}"吗？`, '确认', { type: isBan ? 'warning' : 'info' })
    const url = isBan ? `/admin/users/${row.id}/ban` : `/admin/users/${row.id}/unban`
    await admin.post(url)
    ElMessage.success(`${action}成功`)
    fetchData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(`${action}失败`)
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除用户"${row.username}"吗？此操作不可恢复！`, '警告', { type: 'warning' })
    await admin.post(`/admin/users/${row.id}/delete`)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

onMounted(fetchData)
</script>

<style scoped>
.page-card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1) !important;
  padding: 20px;
}
.card-header { font-size: 18px; font-weight: 600; }
.search-bar { display: flex; gap: 12px; margin-bottom: 20px; flex-wrap: wrap; align-items: center; }
.search-input { width: 280px; }
.search-select { width: 140px; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 20px; }
</style>

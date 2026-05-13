<template>
  <el-card shadow="hover" class="page-card">
    <template #header>
      <div class="card-header">
        <span>讨论组管理</span>
      </div>
    </template>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input v-model="searchForm.keyword" placeholder="搜索讨论组名称" clearable class="search-input" @keyup.enter="handleSearch" />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="tableData" border stripe highlight-current-row v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" min-width="180" />
      <el-table-column label="描述" min-width="200">
        <template #default="{ row }">{{ truncate(row.description, 30) }}</template>
      </el-table-column>
      <el-table-column prop="creatorId" label="创建者ID" width="120" />
      <el-table-column prop="memberCount" label="成员数" width="100" />
      <el-table-column prop="createTime" label="创建时间" width="180">
        <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="handleViewMembers(row)">查看成员</el-button>
          <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrap">
      <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size" :page-sizes="[10, 20, 50, 100]" :total="pagination.total" layout="total, sizes, prev, pager, next, jumper" @size-change="fetchData" @current-change="fetchData" />
    </div>

    <!-- 成员弹窗 -->
    <el-dialog v-model="memberDialogVisible" :title="'讨论组成员 - ' + currentDiscussion?.title" width="500px">
      <el-table :data="memberList" border stripe highlight-current-row v-loading="memberLoading">
        <el-table-column prop="username" label="用户名" width="160" />
        <el-table-column prop="nickname" label="昵称" width="160" />
        <el-table-column prop="joinedAt" label="加入时间" min-width="180">
          <template #default="{ row }">{{ formatDate(row.joinedAt) }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import admin from '@/api/admin'

const loading = ref(false)
const memberLoading = ref(false)
const tableData = ref([])
const memberList = ref([])
const searchForm = reactive({ keyword: '' })
const pagination = reactive({ page: 1, size: 20, total: 0 })
const memberDialogVisible = ref(false)
const currentDiscussion = ref(null)

const formatDate = (val) => {
  if (!val) return '-'
  return new Date(val).toLocaleString('zh-CN')
}

const truncate = (str, len) => {
  if (!str) return '-'
  return str.length > len ? str.substring(0, len) + '...' : str
}

const fetchData = async () => {
  loading.value = true
  try {
    const { data } = await admin.get('/admin/discussions', {
      params: {
        page: pagination.page,
        size: pagination.size,
        keyword: searchForm.keyword
      }
    })
    tableData.value = data.records || data.content || data.list || []
    pagination.total = data.total || data.totalElements || 0
  } catch (err) {
    ElMessage.error('获取讨论组列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pagination.page = 1; fetchData() }
const handleReset = () => { searchForm.keyword = ''; pagination.page = 1; fetchData() }

const handleViewMembers = async (row) => {
  currentDiscussion.value = row
  memberDialogVisible.value = true
  memberLoading.value = true
  try {
    const { data } = await admin.get(`/admin/discussions/${row.id}/members`)
    memberList.value = data || []
  } catch (err) {
    ElMessage.error('获取成员列表失败')
  } finally {
    memberLoading.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除讨论组"${row.title}"吗？此操作不可恢复！`, '警告', { type: 'warning' })
    await admin.post(`/admin/discussions/${row.id}/delete`)
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
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 20px; }
</style>

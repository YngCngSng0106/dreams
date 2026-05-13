<template>
  <el-card shadow="hover" class="page-card">
    <template #header>
      <div class="card-header">
        <span>评论管理</span>
      </div>
    </template>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input v-model="searchForm.keyword" placeholder="搜索评论内容" clearable class="search-input" @keyup.enter="handleSearch" />
      <el-input v-model="searchForm.discussionId" placeholder="讨论组ID" clearable class="search-input-small" @keyup.enter="handleSearch" />
      <el-input v-model="searchForm.userId" placeholder="用户ID" clearable class="search-input-small" @keyup.enter="handleSearch" />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="tableData" border stripe highlight-current-row v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="discussionId" label="讨论组ID" width="120" />
      <el-table-column prop="userId" label="用户ID" width="100" />
      <el-table-column label="内容" min-width="200">
        <template #default="{ row }">{{ truncate(row.content, 50) }}</template>
      </el-table-column>
      <el-table-column label="是否隐藏" width="100">
        <template #default="{ row }">
          <el-tag :type="row.isHidden ? 'info' : 'success'">{{ row.isHidden ? '已隐藏' : '正常' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="时间" width="180">
        <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button :type="row.isHidden ? 'success' : 'warning'" size="small" @click="handleHideUnhide(row)">
            {{ row.isHidden ? '取消隐藏' : '隐藏' }}
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
const searchForm = reactive({ keyword: '', discussionId: '', userId: '' })
const pagination = reactive({ page: 1, size: 20, total: 0 })

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
    const { data } = await admin.get('/admin/comments', {
      params: {
        page: pagination.page,
        size: pagination.size,
        keyword: searchForm.keyword,
        discussionId: searchForm.discussionId || undefined,
        userId: searchForm.userId || undefined
      }
    })
    tableData.value = data.content || data.list || data || []
    pagination.total = data.totalElements || data.total || 0
  } catch (err) {
    ElMessage.error('获取评论列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pagination.page = 1; fetchData() }
const handleReset = () => { searchForm.keyword = ''; searchForm.discussionId = ''; searchForm.userId = ''; pagination.page = 1; fetchData() }

const handleHideUnhide = async (row) => {
  const isHide = !row.isHidden
  try {
    await ElMessageBox.confirm(`确定要${isHide ? '隐藏' : '取消隐藏'}该评论吗？`, '确认', { type: 'info' })
    const url = isHide ? `/admin/comments/${row.id}/hide` : `/admin/comments/${row.id}/unhide`
    await admin.post(url)
    ElMessage.success(`${isHide ? '隐藏' : '取消隐藏'}成功`)
    fetchData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该评论吗？此操作不可恢复！', '警告', { type: 'warning' })
    await admin.post(`/admin/comments/${row.id}/delete`)
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
.search-input { width: 240px; }
.search-input-small { width: 140px; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 20px; }
</style>

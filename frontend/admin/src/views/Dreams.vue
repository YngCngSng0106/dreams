<template>
  <el-card shadow="hover" class="page-card">
    <template #header>
      <div class="card-header">
        <span>梦境管理</span>
      </div>
    </template>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input v-model="searchForm.keyword" placeholder="搜索梦境描述" clearable class="search-input" @keyup.enter="handleSearch" />
      <el-select v-model="searchForm.categoryId" placeholder="全部分类" clearable class="search-select">
        <el-option label="全部分类" value="" />
        <el-option v-for="c in categoryList" :key="c.id" :label="c.name" :value="String(c.id)" />
      </el-select>
      <el-input v-model="searchForm.userId" placeholder="作者ID" clearable class="search-input-small" @keyup.enter="handleSearch" />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="tableData" border stripe highlight-current-row v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="categoryName" label="分类" width="120">
        <template #default="{ row }">{{ row.categoryName || row.category?.name || '-' }}</template>
      </el-table-column>
      <el-table-column label="梦境描述" min-width="200">
        <template #default="{ row }">{{ truncate(row.description, 50) }}</template>
      </el-table-column>
      <el-table-column prop="userId" label="作者ID" width="100" />
      <el-table-column label="是否置顶" width="90">
        <template #default="{ row }">
          <el-tag v-if="row.isPinned" type="warning">置顶</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="日期" width="180">
        <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="handleDetail(row)">查看详情</el-button>
          <el-button :type="row.isPinned ? 'info' : 'warning'" size="small" @click="handlePinUnpin(row)">
            {{ row.isPinned ? '取消置顶' : '置顶' }}
          </el-button>
          <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrap">
      <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size" :page-sizes="[10, 20, 50, 100]" :total="pagination.total" layout="total, sizes, prev, pager, next, jumper" @size-change="fetchData" @current-change="fetchData" />
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="梦境详情" width="600px">
      <div v-if="currentDetail" class="detail-content">
        <p><strong>分类：</strong>{{ currentDetail.categoryName || currentDetail.category?.name || '-' }}</p>
        <p><strong>描述：</strong>{{ currentDetail.description }}</p>
        <p><strong>作者ID：</strong>{{ currentDetail.userId }}</p>
        <p><strong>创建时间：</strong>{{ formatDate(currentDetail.createTime) }}</p>
        <p><strong>置顶：</strong>{{ currentDetail.isPinned ? '是' : '否' }}</p>
        <p><strong>状态：</strong>{{ currentDetail.status || '-' }}</p>
      </div>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import admin from '@/api/admin'

const loading = ref(false)
const tableData = ref([])
const categoryList = ref([])
const searchForm = reactive({ keyword: '', categoryId: '', userId: '' })
const pagination = reactive({ page: 1, size: 20, total: 0 })
const detailVisible = ref(false)
const currentDetail = ref(null)

const formatDate = (val) => {
  if (!val) return '-'
  return new Date(val).toLocaleString('zh-CN')
}

const truncate = (str, len) => {
  if (!str) return '-'
  return str.length > len ? str.substring(0, len) + '...' : str
}

const fetchCategories = async () => {
  try {
    const { data } = await admin.get('/admin/categories')
    categoryList.value = data || []
  } catch (e) { /* ignore */ }
}

const fetchData = async () => {
  loading.value = true
  try {
    const { data } = await admin.get('/admin/dreams', {
      params: {
        page: pagination.page,
        size: pagination.size,
        keyword: searchForm.keyword,
        categoryId: searchForm.categoryId !== '' ? searchForm.categoryId : undefined,
        userId: searchForm.userId || undefined
      }
    })
    tableData.value = data.records || data.content || data.list || []
    pagination.total = data.total || data.totalElements || 0
  } catch (err) {
    ElMessage.error('获取梦境列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pagination.page = 1; fetchData() }
const handleReset = () => { searchForm.keyword = ''; searchForm.categoryId = ''; searchForm.userId = ''; pagination.page = 1; fetchData() }

const handleDetail = (row) => {
  currentDetail.value = row
  detailVisible.value = true
}

const handlePinUnpin = async (row) => {
  const isPin = !row.isPinned
  try {
    await ElMessageBox.confirm(`确定要${isPin ? '置顶' : '取消置顶'}该梦境吗？`, '确认', { type: 'info' })
    const url = isPin ? `/admin/dreams/${row.id}/pin` : `/admin/dreams/${row.id}/unpin`
    await admin.post(url)
    ElMessage.success(`${isPin ? '置顶' : '取消置顶'}成功`)
    fetchData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该梦境吗？此操作不可恢复！', '警告', { type: 'warning' })
    await admin.post(`/admin/dreams/${row.id}/delete`)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

onMounted(() => { fetchCategories(); fetchData() })
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
.search-select { width: 140px; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 20px; }
.detail-content p { margin-bottom: 12px; line-height: 1.8; }
</style>

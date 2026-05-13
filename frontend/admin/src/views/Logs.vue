<template>
  <el-card shadow="hover" class="page-card">
    <template #header>
      <div class="card-header">
        <span>操作日志</span>
      </div>
    </template>

    <!-- 筛选栏 -->
    <div class="search-bar">
      <el-input v-model="searchForm.adminId" placeholder="管理员ID" clearable class="search-input-small" @keyup.enter="handleSearch" />
      <el-select v-model="searchForm.moduleName" placeholder="全部模块" clearable class="search-select">
        <el-option label="全部模块" value="" />
        <el-option label="用户" value="user" />
        <el-option label="梦境" value="dream" />
        <el-option label="分类" value="category" />
        <el-option label="讨论组" value="discussion" />
        <el-option label="评论" value="comment" />
        <el-option label="审核" value="audit" />
        <el-option label="通知" value="notification" />
        <el-option label="关键词" value="keyword" />
      </el-select>
      <el-select v-model="searchForm.operation" placeholder="全部操作" clearable class="search-select">
        <el-option label="全部操作" value="" />
        <el-option label="创建" value="CREATE" />
        <el-option label="更新" value="UPDATE" />
        <el-option label="删除" value="DELETE" />
        <el-option label="封禁" value="BAN" />
        <el-option label="解封" value="UNBAN" />
        <el-option label="置顶" value="PIN" />
        <el-option label="取消置顶" value="UNPIN" />
        <el-option label="通过" value="APPROVE" />
        <el-option label="驳回" value="REJECT" />
        <el-option label="隐藏" value="HIDE" />
        <el-option label="取消隐藏" value="UNHIDE" />
        <el-option label="发送" value="SEND" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="tableData" border stripe highlight-current-row v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="adminId" label="管理员ID" width="110" />
      <el-table-column prop="adminName" label="管理员名" width="120" />
      <el-table-column prop="moduleName" label="模块" width="120">
        <template #default="{ row }">{{ moduleLabel(row.moduleName) }}</template>
      </el-table-column>
      <el-table-column prop="operation" label="操作类型" width="120">
        <template #default="{ row }">{{ opLabel(row.operation) }}</template>
      </el-table-column>
      <el-table-column prop="targetId" label="目标ID" width="100">
        <template #default="{ row }">{{ row.targetId || '-' }}</template>
      </el-table-column>
      <el-table-column prop="targetName" label="目标名" min-width="140">
        <template #default="{ row }">{{ row.targetName || '-' }}</template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="160">
        <template #default="{ row }">{{ row.remark || '-' }}</template>
      </el-table-column>
      <el-table-column prop="createTime" label="时间" width="180">
        <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
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
import { ElMessage } from 'element-plus'
import admin from '@/api/admin'

const loading = ref(false)
const tableData = ref([])
const searchForm = reactive({ adminId: '', moduleName: '', operation: '' })
const pagination = reactive({ page: 1, size: 20, total: 0 })

const formatDate = (val) => {
  if (!val) return '-'
  return new Date(val).toLocaleString('zh-CN')
}

const moduleLabel = (m) => {
  const map = { user: '用户', dream: '梦境', category: '分类', discussion: '讨论组', comment: '评论', audit: '审核', notification: '通知', keyword: '关键词' }
  return map[m] || m || '-'
}

const opLabel = (op) => {
  const map = { CREATE: '创建', UPDATE: '更新', DELETE: '删除', BAN: '封禁', UNBAN: '解封', PIN: '置顶', UNPIN: '取消置顶', APPROVE: '通过', REJECT: '驳回', HIDE: '隐藏', UNHIDE: '取消隐藏', SEND: '发送' }
  return map[op] || op || '-'
}

const fetchData = async () => {
  loading.value = true
  try {
    const { data } = await admin.get('/admin/logs', {
      params: {
        page: pagination.page,
        size: pagination.size,
        adminId: searchForm.adminId || undefined,
        moduleName: searchForm.moduleName !== '' ? searchForm.moduleName : undefined,
        operation: searchForm.operation !== '' ? searchForm.operation : undefined
      }
    })
    tableData.value = data.records || data.content || data.list || []
    pagination.total = data.total || data.totalElements || 0
  } catch (err) {
    ElMessage.error('获取操作日志失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pagination.page = 1; fetchData() }
const handleReset = () => { searchForm.adminId = ''; searchForm.moduleName = ''; searchForm.operation = ''; pagination.page = 1; fetchData() }

onMounted(fetchData)
</script>

<style scoped>
.page-card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1) !important;
  padding: 20px;
}
.card-header { font-size: 18px; font-weight: 600; }
.search-bar { display: flex; gap: 12px; margin-bottom: 20px; flex-wrap: wrap; align-items: center; }
.search-input-small { width: 140px; }
.search-select { width: 140px; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 20px; }
</style>

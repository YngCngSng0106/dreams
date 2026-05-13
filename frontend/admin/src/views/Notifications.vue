<template>
  <el-card shadow="hover" class="page-card">
    <template #header>
      <div class="card-header">
        <span>通知管理</span>
        <el-button type="primary" @click="openSendDialog">发送系统公告</el-button>
      </div>
    </template>

    <!-- 筛选栏 -->
    <div class="search-bar">
      <el-input v-model="searchForm.userId" placeholder="用户ID" clearable class="search-input-small" @keyup.enter="handleSearch" />
      <el-select v-model="searchForm.type" placeholder="全部类型" clearable class="search-select">
        <el-option label="全部类型" value="" />
        <el-option label="系统公告" value="SYSTEM" />
        <el-option label="审核通知" value="AUDIT" />
        <el-option label="互动通知" value="INTERACTION" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="tableData" border stripe highlight-current-row v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="userId" label="用户ID" width="120">
        <template #default="{ row }">{{ row.userId || '全部' }}</template>
      </el-table-column>
      <el-table-column prop="type" label="类型" width="120">
        <template #default="{ row }">{{ typeLabel(row.type) }}</template>
      </el-table-column>
      <el-table-column label="内容" min-width="200">
        <template #default="{ row }">{{ truncate(row.content, 50) }}</template>
      </el-table-column>
      <el-table-column label="是否已读" width="100">
        <template #default="{ row }">
          <el-tag :type="row.isRead ? 'info' : 'warning'">{{ row.isRead ? '已读' : '未读' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="时间" width="180">
        <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrap">
      <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size" :page-sizes="[10, 20, 50, 100]" :total="pagination.total" layout="total, sizes, prev, pager, next, jumper" @size-change="fetchData" @current-change="fetchData" />
    </div>

    <!-- 发送公告弹窗 -->
    <el-dialog v-model="sendDialogVisible" title="发送系统公告" width="500px" @close="resetSendForm">
      <el-form :model="sendForm" label-width="120px">
        <el-form-item label="目标用户ID">
          <el-input v-model="sendForm.targetUserId" placeholder="留空则发送给全部用户" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="sendForm.type" placeholder="请选择类型" style="width: 100%">
            <el-option label="系统公告" value="SYSTEM" />
            <el-option label="审核通知" value="AUDIT" />
            <el-option label="互动通知" value="INTERACTION" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="sendForm.content" type="textarea" :rows="4" placeholder="请输入通知内容" />
        </el-form-item>
        <el-form-item label="关联ID">
          <el-input v-model="sendForm.relatedId" placeholder="可选，关联的资源ID" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="sendDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="sendLoading" @click="handleSend">发送</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import admin from '@/api/admin'

const loading = ref(false)
const tableData = ref([])
const searchForm = reactive({ userId: '', type: '' })
const pagination = reactive({ page: 1, size: 20, total: 0 })
const sendDialogVisible = ref(false)
const sendLoading = ref(false)
const sendForm = reactive({ targetUserId: '', type: 'SYSTEM', content: '', relatedId: '' })

const formatDate = (val) => {
  if (!val) return '-'
  return new Date(val).toLocaleString('zh-CN')
}

const truncate = (str, len) => {
  if (!str) return '-'
  return str.length > len ? str.substring(0, len) + '...' : str
}

const typeLabel = (type) => {
  const map = { SYSTEM: '系统公告', AUDIT: '审核通知', INTERACTION: '互动通知' }
  return map[type] || type || '-'
}

const fetchData = async () => {
  loading.value = true
  try {
    const { data } = await admin.get('/admin/notifications', {
      params: {
        page: pagination.page,
        size: pagination.size,
        userId: searchForm.userId || undefined,
        type: searchForm.type !== '' ? searchForm.type : undefined
      }
    })
    tableData.value = data.content || data.list || data || []
    pagination.total = data.totalElements || data.total || 0
  } catch (err) {
    ElMessage.error('获取通知列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pagination.page = 1; fetchData() }
const handleReset = () => { searchForm.userId = ''; searchForm.type = ''; pagination.page = 1; fetchData() }

const openSendDialog = () => { sendDialogVisible.value = true }
const resetSendForm = () => { sendForm.targetUserId = ''; sendForm.type = 'SYSTEM'; sendForm.content = ''; sendForm.relatedId = '' }

const handleSend = async () => {
  if (!sendForm.content.trim()) { ElMessage.warning('请输入通知内容'); return }
  sendLoading.value = true
  try {
    const body = {
      targetUserId: sendForm.targetUserId || undefined,
      content: sendForm.content,
      type: sendForm.type,
      relatedId: sendForm.relatedId || undefined
    }
    await admin.post('/admin/notifications/send', body)
    ElMessage.success('发送成功')
    sendDialogVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error('发送失败')
  } finally {
    sendLoading.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该通知吗？', '警告', { type: 'warning' })
    await admin.post(`/admin/notifications/${row.id}/delete`)
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
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: 600;
}
.search-bar { display: flex; gap: 12px; margin-bottom: 20px; flex-wrap: wrap; align-items: center; }
.search-input-small { width: 140px; }
.search-select { width: 140px; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 20px; }
</style>

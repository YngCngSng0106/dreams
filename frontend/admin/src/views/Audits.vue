<template>
  <el-card shadow="hover" class="page-card">
    <template #header>
      <div class="card-header">
        <span>内容审核</span>
      </div>
    </template>

    <!-- 筛选栏 -->
    <div class="search-bar">
      <el-select v-model="searchForm.status" placeholder="全部状态" clearable class="search-select">
        <el-option label="全部状态" value="" />
        <el-option label="待审(PENDING)" value="PENDING" />
        <el-option label="通过(PASSED)" value="PASSED" />
        <el-option label="驳回(REJECTED)" value="REJECTED" />
      </el-select>
      <el-select v-model="searchForm.targetType" placeholder="全部类型" clearable class="search-select">
        <el-option label="全部类型" value="" />
        <el-option label="梦境(DREAM)" value="DREAM" />
        <el-option label="评论(COMMENT)" value="COMMENT" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="tableData" border stripe highlight-current-row v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="targetType" label="目标类型" width="120">
        <template #default="{ row }">{{ row.targetType === 'DREAM' ? '梦境' : row.targetType === 'COMMENT' ? '评论' : row.targetType }}</template>
      </el-table-column>
      <el-table-column prop="targetId" label="目标ID" width="100" />
      <el-table-column label="内容" min-width="200">
        <template #default="{ row }">{{ truncate(row.content, 50) }}</template>
      </el-table-column>
      <el-table-column prop="status" label="审核状态" width="120">
        <template #default="{ row }">
          <el-tag v-if="row.status === 'PENDING'" type="warning">待审</el-tag>
          <el-tag v-else-if="row.status === 'PASSED'" type="success">通过</el-tag>
          <el-tag v-else-if="row.status === 'REJECTED'" type="danger">驳回</el-tag>
          <el-tag v-else>未知</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="rejectReason" label="驳回原因" min-width="150">
        <template #default="{ row }">{{ row.rejectReason || '-' }}</template>
      </el-table-column>
      <el-table-column prop="auditedAt" label="审核时间" width="180">
        <template #default="{ row }">{{ formatDate(row.auditedAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 'PENDING'" type="success" size="small" @click="handleApprove(row)">通过</el-button>
          <el-button v-if="row.status === 'PENDING'" type="danger" size="small" @click="handleReject(row)">驳回</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrap">
      <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size" :page-sizes="[10, 20, 50, 100]" :total="pagination.total" layout="total, sizes, prev, pager, next, jumper" @size-change="fetchData" @current-change="fetchData" />
    </div>

    <!-- 驳回原因弹窗 -->
    <el-dialog v-model="rejectDialogVisible" title="驳回原因" width="450px">
      <el-input v-model="rejectReason" type="textarea" :rows="4" placeholder="请输入驳回原因" />
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="submitLoading" @click="confirmReject">确定驳回</el-button>
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
const searchForm = reactive({ status: '', targetType: '' })
const pagination = reactive({ page: 1, size: 20, total: 0 })
const rejectDialogVisible = ref(false)
const rejectReason = ref('')
const submitLoading = ref(false)
const currentRejectId = ref(null)

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
    const { data } = await admin.get('/admin/audits', {
      params: {
        page: pagination.page,
        size: pagination.size,
        status: searchForm.status !== '' ? searchForm.status : undefined,
        targetType: searchForm.targetType !== '' ? searchForm.targetType : undefined
      }
    })
    tableData.value = data.records || data.content || data.list || []
    pagination.total = data.total || data.totalElements || 0
  } catch (err) {
    ElMessage.error('获取审核列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pagination.page = 1; fetchData() }
const handleReset = () => { searchForm.status = ''; searchForm.targetType = ''; pagination.page = 1; fetchData() }

const handleApprove = async (row) => {
  try {
    await ElMessageBox.confirm('确定要通过该审核吗？', '确认', { type: 'info' })
    await admin.post(`/admin/audits/${row.id}/approve`)
    ElMessage.success('审核通过')
    fetchData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

const handleReject = (row) => {
  currentRejectId.value = row.id
  rejectReason.value = ''
  rejectDialogVisible.value = true
}

const confirmReject = async () => {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请输入驳回原因')
    return
  }
  submitLoading.value = true
  try {
    await admin.post(`/admin/audits/${currentRejectId.value}/reject`, { reason: rejectReason.value })
    ElMessage.success('已驳回')
    rejectDialogVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error('驳回失败')
  } finally {
    submitLoading.value = false
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
.search-select { width: 160px; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 20px; }
</style>

<template>
  <el-card shadow="hover" class="page-card">
    <template #header>
      <div class="card-header">
        <span>审核关键词</span>
        <el-button type="primary" @click="openDialog()">新增关键词</el-button>
      </div>
    </template>

    <!-- 表格 -->
    <el-table :data="tableData" border stripe highlight-current-row v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="keyword" label="关键词" min-width="180" />
      <el-table-column prop="keywordType" label="类型" width="160">
        <template #default="{ row }">{{ typeLabel(row.keywordType) }}</template>
      </el-table-column>
      <el-table-column prop="severity" label="严重程度" width="120">
        <template #default="{ row }">
          <el-tag :type="severityType(row.severity)">{{ severityLabel(row.severity) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="openDialog(row)">编辑</el-button>
          <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑关键词' : '新增关键词'" width="480px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="关键词" prop="keyword">
          <el-input v-model="form.keyword" placeholder="请输入关键词" />
        </el-form-item>
        <el-form-item label="类型" prop="keywordType">
          <el-select v-model="form.keywordType" placeholder="请选择类型" style="width: 100%">
            <el-option label="垃圾信息(SPAM)" value="SPAM" />
            <el-option label="骚扰(HARASSMENT)" value="HARASSMENT" />
            <el-option label="不适当(INAPPROPRIATE)" value="INAPPROPRIATE" />
          </el-select>
        </el-form-item>
        <el-form-item label="严重程度" prop="severity">
          <el-select v-model="form.severity" placeholder="请选择严重程度" style="width: 100%">
            <el-option label="低(LOW)" value="LOW" />
            <el-option label="中(MEDIUM)" value="MEDIUM" />
            <el-option label="高(HIGH)" value="HIGH" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
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
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)

const form = reactive({ id: null, keyword: '', keywordType: '', severity: '' })
const rules = {
  keyword: [{ required: true, message: '请输入关键词', trigger: 'blur' }],
  keywordType: [{ required: true, message: '请选择类型', trigger: 'change' }],
  severity: [{ required: true, message: '请选择严重程度', trigger: 'change' }]
}

const typeLabel = (type) => {
  const map = { SPAM: '垃圾信息', HARASSMENT: '骚扰', INAPPROPRIATE: '不适当' }
  return map[type] || type || '-'
}

const severityLabel = (sev) => {
  const map = { LOW: '低', MEDIUM: '中', HIGH: '高' }
  return map[sev] || sev || '-'
}

const severityType = (sev) => {
  const map = { LOW: 'success', MEDIUM: 'warning', HIGH: 'danger' }
  return map[sev] || 'info'
}

const resetForm = () => {
  form.id = null; form.keyword = ''; form.keywordType = ''; form.severity = ''
  isEdit.value = false
}

const fetchData = async () => {
  loading.value = true
  try {
    const kwData = await admin.get('/admin/keywords')
    tableData.value = kwData || []
  } catch (err) {
    ElMessage.error('获取关键词列表失败')
  } finally {
    loading.value = false
  }
}

const openDialog = (row) => {
  if (row) {
    isEdit.value = true
    Object.assign(form, { id: row.id, keyword: row.keyword, keywordType: row.keywordType, severity: row.severity })
  } else {
    isEdit.value = false
    resetForm()
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitLoading.value = true
    if (isEdit.value) {
      await admin.put(`/admin/keywords/${form.id}`, form)
      ElMessage.success('编辑成功')
    } else {
      await admin.post('/admin/keywords', form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    if (e !== true) ElMessage.error(isEdit.value ? '编辑失败' : '新增失败')
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除关键词"${row.keyword}"吗？`, '警告', { type: 'warning' })
    await admin.post(`/admin/keywords/${row.id}/delete`, {})
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
</style>

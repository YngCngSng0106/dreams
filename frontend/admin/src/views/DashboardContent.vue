<template>
  <div class="page-card">
    <h2 class="page-title">控制台统计</h2>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6">
        <div class="stat-card stat-users">
          <div class="stat-icon">👥</div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalUsers || 0 }}</div>
            <div class="stat-label">总用户数</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-dreams">
          <div class="stat-icon">💭</div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalDreams || 0 }}</div>
            <div class="stat-label">总梦境数</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-pending">
          <div class="stat-icon">⏳</div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.pendingAudits || 0 }}</div>
            <div class="stat-label">待审核数</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-banned">
          <div class="stat-icon">🚫</div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.bannedUsers || 0 }}</div>
            <div class="stat-label">封禁用户数</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <span>用户增长（近30天）</span>
          </template>
          <div ref="userChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <span>梦境增长（近30天）</span>
          </template>
          <div ref="dreamChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近操作日志 -->
    <el-card shadow="hover" class="chart-card log-card">
      <template #header>
        <span>最近操作日志</span>
      </template>
      <el-table :data="recentLogs" border stripe highlight-current-row v-loading="logLoading" max-height="300">
        <el-table-column prop="adminName" label="管理员" width="120" />
        <el-table-column prop="moduleName" label="模块" width="100">
          <template #default="{ row }">{{ moduleLabel(row.moduleName) }}</template>
        </el-table-column>
        <el-table-column prop="operation" label="操作" width="100">
          <template #default="{ row }">{{ opLabel(row.operation) }}</template>
        </el-table-column>
        <el-table-column prop="targetName" label="目标" min-width="140" />
        <el-table-column prop="remark" label="备注" min-width="140" />
        <el-table-column prop="createTime" label="时间" width="180">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import admin from '@/api/admin'

const stats = ref({ totalUsers: 0, totalDreams: 0, pendingAudits: 0, bannedUsers: 0 })
const userChartRef = ref(null)
const dreamChartRef = ref(null)
const recentLogs = ref([])
const logLoading = ref(false)
let userChart = null
let dreamChart = null

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

const fetchStats = async () => {
  try {
    const statsData = await admin.get('/admin/stats')
    stats.value = statsData || {}
  } catch (err) {
    ElMessage.error('获取统计数据失败')
  }
}

const fetchUserGrowth = async () => {
  try {
    const userData = await admin.get('/admin/stats/user-growth', { params: { days: 30 } })
    renderChart(userChartRef.value, '用户增长', userData, '#409EFF')
  } catch (err) { /* ignore */ }
}

const fetchDreamGrowth = async () => {
  try {
    const dreamData = await admin.get('/admin/stats/dream-growth', { params: { days: 30 } } )
    renderChart(dreamChartRef.value, '梦境增长', dreamData, '#67C23A')
  } catch (err) { /* ignore */ }
}

const renderChart = (el, name, data, color) => {
  if (!el || !data) return
  const chart = echarts.init(el)
  const items = Array.isArray(data) ? data : (data.list || data.items || [])
  const dates = items.map(i => i.date || i.day || '')
  const values = items.map(i => i.count || i.total || i.value || 0)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: dates, axisLabel: { interval: 0, rotate: 30, fontSize: 10 } },
    yAxis: { type: 'value' },
    series: [{ name, type: 'bar', data: values, itemStyle: { color } }]
  })
  return chart
}

const fetchRecentLogs = async () => {
  logLoading.value = true
  try {
    const logData = await admin.get('/admin/logs', { params: { page: 1, size: 10 } })
    recentLogs.value = logData.content || logData.list || logData || []
  } catch (err) { /* ignore */ } finally {
    logLoading.value = false
  }
}

onMounted(async () => {
  await Promise.all([fetchStats(), fetchUserGrowth(), fetchDreamGrowth(), fetchRecentLogs()])
  userChart = echarts.getInstanceByDom(userChartRef.value)
  dreamChart = echarts.getInstanceByDom(dreamChartRef.value)
})

onBeforeUnmount(() => {
  if (userChart) userChart.dispose()
  if (dreamChart) dreamChart.dispose()
})
</script>

<style scoped>
.page-card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 20px;
  background: #fff;
}
.page-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 20px;
}
.stat-row { margin-bottom: 20px; }
.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  border-radius: 8px;
  color: #fff;
  min-height: 100px;
}
.stat-users { background: linear-gradient(135deg, #409EFF, #66b1ff); }
.stat-dreams { background: linear-gradient(135deg, #67C23A, #85ce61); }
.stat-pending { background: linear-gradient(135deg, #E6A23C, #ebb563); }
.stat-banned { background: linear-gradient(135deg, #F56C6C, #f78989); }
.stat-icon { font-size: 36px; margin-right: 16px; }
.stat-value { font-size: 28px; font-weight: 700; }
.stat-label { font-size: 14px; opacity: 0.85; margin-top: 4px; }
.chart-row { margin-bottom: 20px; }
.chart-card { }
.chart-container { height: 320px; width: 100%; }
.log-card { }
</style>

<template>
  <div class="console-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>控制台</span>
        </div>
      </template>
      <div class="welcome">
        <h2>欢迎使用 Dreams 后台管理系统</h2>
        <p>请选择左侧菜单开始管理</p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
</script>

<style scoped lang="scss">
.console-page {
  .card-header {
    font-size: 16px;
    font-weight: 600;
  }

  .welcome {
    text-align: center;
    padding: 40px 20px;

    h2 {
      color: #6C5CE7;
      font-size: 22px;
      margin-bottom: 12px;
    }

    p {
      color: #999;
      font-size: 14px;
    }
  }
}
</style>

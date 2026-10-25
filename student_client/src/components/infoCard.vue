<template>
  <el-card>
    <el-descriptions title="用户信息">
      <el-descriptions-item label="用户 id">{{ id }}</el-descriptions-item>
      <el-descriptions-item label="用户名">{{ name }}</el-descriptions-item>
      <el-descriptions-item label="用户类型">
        <el-tag size="small">{{ type }}</el-tag>
      </el-descriptions-item>
    </el-descriptions>
  </el-card>
</template>

<script>
export default {
  name: "infoCard",
  data() {
    return {
      id: sessionStorage.getItem('type') === 'student' ? sessionStorage.getItem('sid') : sessionStorage.getItem('tid'),
      name: sessionStorage.getItem('name'),
      type: sessionStorage.getItem('type')
    }
  }
}
</script>

<style scoped>

</style>

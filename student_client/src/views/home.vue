<template>
  <el-main>
    <h1>学生技能达标预警跟踪系统</h1>
    <info-card></info-card>
    <el-card style="margin-top: 16px">
      <p v-if="role === 'admin'">维护技能项目与达标线，查看考核和预警记录。</p>
      <p v-else-if="role === 'teacher'">开设技能项目，录入考核结果并跟进未达标学生。</p>
      <p v-else>选择技能项目，查看最新达标状态和历次考核。</p>
      <el-button type="primary" @click="$router.push(primaryPath)">{{ primaryLabel }}</el-button>
    </el-card>
  </el-main>
</template>

<script>
import InfoCard from '@/components/infoCard'

export default {
  components: { InfoCard },
  computed: {
    role() { return sessionStorage.getItem('type') },
    primaryPath() {
      return this.role === 'admin' ? '/adminWarningList' : this.role === 'teacher' ? '/teacherWarningList' : '/queryCourseGrade'
    },
    primaryLabel() {
      return this.role === 'student' ? '查看达标情况' : '查看预警跟踪'
    }
  }
}
</script>

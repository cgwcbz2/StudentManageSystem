<template>
  <div>
    <el-form >
      <el-form-item label="选择学期">
        <el-select v-model="term" placeholder="请选择学期">
          <el-option v-for="(item, index) in termList" :key="index" :label="item" :value="item"></el-option>
        </el-select>
      </el-form-item>
    </el-form>
    <el-card>
      <el-table
          :data="tableData"
          border
          style="width: 100%">
        <el-table-column
            fixed
            prop="cid"
            label="技能编号"
            width="150">
        </el-table-column>
        <el-table-column
            prop="cname"
            label="技能项目"
            width="150">
        </el-table-column>
        <el-table-column
            prop="tid"
            label="教师号"
            width="150">
        </el-table-column>
        <el-table-column
            prop="tname"
            label="教师名称"
            width="150">
        </el-table-column>
        <el-table-column
            prop="passScore"
            label="达标线"
            width="150">
        </el-table-column>
        <el-table-column
            prop="grade"
            label="最新考核"
            width="150">
        </el-table-column>
        <el-table-column
            label="达标状态"
            width="150">
          <template slot-scope="scope">
            <el-tag :type="achievementStatus(scope.row.grade, scope.row.passScore).type">
              {{ achievementStatus(scope.row.grade, scope.row.passScore).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="110">
          <template slot-scope="scope"><el-button type="text" @click="openHistory(scope.row)">考核历史</el-button></template>
        </el-table-column>
      </el-table>
      <el-pagination
          background
          layout="prev, pager, next"
          :total="total"
          :page-size="pageSize"
          @current-change="changePage"
      >
      </el-pagination>
    </el-card>
    <el-dialog title="历次考核" :visible.sync="historyVisible" width="650px">
      <el-table :data="history" border>
        <el-table-column prop="grade" label="分数"></el-table-column>
        <el-table-column prop="passScoreSnapshot" label="当时达标线"></el-table-column>
        <el-table-column label="当时状态"><template slot-scope="scope">{{ achievementStatus(scope.row.grade, scope.row.passScoreSnapshot).label }}</template></el-table-column>
        <el-table-column prop="recordedAt" label="记录时间"></el-table-column>
        <el-table-column label="来源"><template slot-scope="scope">{{ scope.row.source === 'initial_import' ? '初始导入' : '考核录入' }}</template></el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script>
export default {
  methods: {
    achievementStatus(grade, passScore) {
      if (grade === null || grade === undefined || grade === '') {
        return { label: '待考核', type: 'info' }
      }
      return Number(grade) < Number(passScore || 60)
        ? { label: '未达标预警', type: 'danger' }
        : { label: '已达标', type: 'success' }
    },
    async openHistory(row) {
      const response = await this.$axios.get('http://localhost:10086/SCT/' + row.sctid + '/assessments')
      this.history = response.data
      this.historyVisible = true
    },
    changePage(page) {
      page = page - 1
      const that = this
      let start = page * that.pageSize, end = that.pageSize * (page + 1)
      let length = that.tmpList.length
      let ans = (end < length) ? end : length
      that.tableData = that.tmpList.slice(start, ans)
    },
  },
  data() {
    return {
      tableData: null,
      pageSize: 10,
      total: null,
      tmpList: null,
      historyVisible: false,
      history: [],
      term: sessionStorage.getItem('currentTerm'),
      termList: null
    }
  },
  created() {
    const that = this
    axios.get('http://localhost:10086/SCT/findAllTerm').then(function (resp) {
      that.termList = resp.data
    })
  },
  watch: {
    term: {
      handler(newTerm, oldTerm) {
        const sid = sessionStorage.getItem('sid')
        const that = this
        axios.get('http://localhost:10086/SCT/findBySid/' + sid + '/' + newTerm).then(function (resp) {
          that.tmpList = resp.data
          that.total = resp.data.length
          let start = 0, end = that.pageSize
          let length = that.tmpList.length
          let ans = (end < length) ? end : length
          that.tableData = that.tmpList.slice(start, end)
        })
      },
      immediate: true
    }
  }
}
</script>

<!--
  TODO：
  1. 管理员：
    1. 学生选课管理
    2. 成绩管理（只能当前学期）
  2. 学生：成绩排名
  3. 教师：成绩管理（？使用弹框）（只能当前学期）

-->

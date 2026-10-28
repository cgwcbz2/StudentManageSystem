<template>
  <div>
    <el-container>
      <el-main>
        <el-card>
          <el-form :inline="true" :model="filters" label-width="100px">
            <el-form-item label="选择学期">
              <el-select v-model="filters.term" clearable placeholder="全部学期">
                <el-option v-for="term in termList" :key="term" :label="term" :value="term"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="技能项目">
              <el-select v-model="filters.cid" clearable filterable placeholder="全部技能">
                <el-option v-for="course in courseList" :key="course.cid" :label="course.cname" :value="course.cid"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="学号">
              <el-input v-model.number="filters.sid" clearable placeholder="按学号查询"></el-input>
            </el-form-item>
            <el-form-item label="学生姓名">
              <el-input v-model.trim="filters.sname" clearable placeholder="按姓名查询"></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadWarnings">查询</el-button>
              <el-button @click="resetFilters">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card style="margin-top: 10px">
          <el-radio-group v-model="view" @change="loadWarnings" style="margin-bottom: 16px">
            <el-radio-button label="active">当前预警</el-radio-button>
            <el-radio-button label="all">全部记录</el-radio-button>
          </el-radio-group>
          <el-table :data="pageRows" border stripe v-loading="loading" style="width: 100%">
            <el-table-column prop="sid" label="学号" width="100"></el-table-column>
            <el-table-column prop="sname" label="学生名" width="120"></el-table-column>
            <el-table-column prop="cname" label="技能项目" min-width="150"></el-table-column>
            <el-table-column v-if="!isTeacher" prop="tname" label="教师名" width="120"></el-table-column>
            <el-table-column prop="grade" label="最新考核" width="90"></el-table-column>
            <el-table-column prop="passScore" label="达标线" width="85"></el-table-column>
            <el-table-column label="状态" width="110">
              <template slot-scope="scope">
                <el-tag :type="scope.row.status === 'warning' ? 'danger' : scope.row.status === 'passed' ? 'success' : 'info'">{{ scope.row.status === 'warning' ? '未达标预警' : scope.row.status === 'passed' ? '已达标' : '待考核' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="term" label="学期" width="130"></el-table-column>
            <el-table-column label="最近跟进" width="170">
              <template slot-scope="scope">{{ formatTime(scope.row.lastFollowupAt) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="260">
              <template slot-scope="scope">
                <el-button type="text" size="small" @click="openFollowups(scope.row)">跟进记录</el-button>
                <el-button type="text" size="small" @click="openHistory(scope.row)">考核历史</el-button>
                <el-button type="text" size="small" @click="editGrade(scope.row)">录入考核</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            background
            layout="prev, pager, next"
            :total="warnings.length"
            :page-size="pageSize"
            :current-page="currentPage"
            @current-change="currentPage = $event">
          </el-pagination>
        </el-card>
      </el-main>
    </el-container>

    <el-dialog :title="selected ? selected.sname + ' - 跟进记录' : '跟进记录'" :visible.sync="dialogVisible" width="600px">
      <el-table :data="followups" border stripe v-loading="followupLoading" empty-text="暂无跟进记录" style="width: 100%">
        <el-table-column prop="content" label="跟进内容" min-width="250"></el-table-column>
        <el-table-column label="记录时间" width="170">
          <template slot-scope="scope">{{ formatTime(scope.row.createdAt) }}</template>
        </el-table-column>
        <el-table-column prop="operatorTid" label="操作人编号" width="100"></el-table-column>
      </el-table>
      <el-form v-if="canAddFollowup" style="margin-top: 20px">
        <el-form-item label="添加跟进备注">
          <el-input v-model="newContent" type="textarea" :rows="3" maxlength="1000" placeholder="填写本次跟进情况"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">关闭</el-button>
        <el-button v-if="canAddFollowup" type="primary" :loading="saving" @click="saveFollowup">保存备注</el-button>
      </span>
    </el-dialog>
    <el-dialog title="历次考核" :visible.sync="historyVisible" width="680px">
      <el-table :data="history" border>
        <el-table-column prop="grade" label="分数"></el-table-column>
        <el-table-column prop="passScoreSnapshot" label="当时达标线"></el-table-column>
        <el-table-column label="当时状态"><template slot-scope="scope">{{ scope.row.grade < scope.row.passScoreSnapshot ? '未达标预警' : '已达标' }}</template></el-table-column>
        <el-table-column prop="recordedAt" label="记录时间（UTC）"></el-table-column>
        <el-table-column label="来源"><template slot-scope="scope">{{ scope.row.source === 'initial_import' ? '初始导入' : '考核录入' }}</template></el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'WarningList',
  props: {
    scope: {
      type: String,
      default: 'admin'
    }
  },
  data() {
    return {
      filters: {
        term: sessionStorage.getItem('currentTerm') || '',
        cid: null,
        sid: null,
        sname: ''
      },
      termList: [],
      courseList: [],
      warnings: [],
      view: this.$route.query.view === 'all' ? 'all' : 'active',
      historyVisible: false,
      history: [],
      currentPage: 1,
      pageSize: 10,
      loading: false,
      dialogVisible: false,
      selected: null,
      followups: [],
      followupLoading: false,
      newContent: '',
      saving: false
    }
  },
  computed: {
    isTeacher() {
      return this.scope === 'teacher'
    },
    canAddFollowup() {
      return this.isTeacher && this.selected && this.selected.status === 'warning'
    },
    pageRows() {
      const start = (this.currentPage - 1) * this.pageSize
      return this.warnings.slice(start, start + this.pageSize)
    }
  },
  created() {
    this.loadOptions()
    this.loadWarnings()
  },
  methods: {
    async loadOptions() {
      try {
        const [terms, courses] = await Promise.all([
          this.$axios.get('http://localhost:10086/SCT/findAllTerm'),
          this.$axios.post('http://localhost:10086/course/findBySearch', {})
        ])
        this.termList = terms.data
        this.courseList = courses.data
      } catch (error) {
        this.$message.error('筛选项加载失败')
      }
    },
    async loadWarnings() {
      const params = {}
      if (this.filters.term) params.term = this.filters.term
      if (this.filters.cid) params.cid = this.filters.cid
      if (this.filters.sid) params.sid = this.filters.sid
      if (this.filters.sname) params.sname = this.filters.sname
      params.view = this.view
      if (this.isTeacher) {
        const tid = Number(sessionStorage.getItem('tid'))
        if (!tid) {
          this.$message.error('缺少教师信息，请重新登录')
          return
        }
        params.tid = tid
      }
      this.loading = true
      try {
        const response = await this.$axios.get('http://localhost:10086/warning', { params })
        this.warnings = response.data
        this.currentPage = 1
      } catch (error) {
        this.$message.error('预警名单加载失败')
      } finally {
        this.loading = false
      }
    },
    async openHistory(row) {
      try {
        const response = await this.$axios.get('http://localhost:10086/SCT/' + row.sctid + '/assessments')
        this.history = response.data
        this.historyVisible = true
      } catch (error) {
        this.$message.error('考核历史加载失败')
      }
    },
    resetFilters() {
      this.filters.term = sessionStorage.getItem('currentTerm') || ''
      this.filters.cid = null
      this.filters.sid = null
      this.filters.sname = ''
      this.loadWarnings()
    },
    formatTime(value) {
      if (!value) return '-'
      const date = new Date(value)
      return Number.isNaN(date.getTime()) ? '-' : date.toLocaleString('zh-CN', { hour12: false })
    },
    async openFollowups(row) {
      this.selected = row
      this.newContent = ''
      this.followups = []
      this.dialogVisible = true
      await this.loadFollowups()
    },
    async loadFollowups() {
      if (!this.selected) return
      this.followupLoading = true
      try {
        const response = await this.$axios.get('http://localhost:10086/warning/' + this.selected.sctid + '/followups')
        this.followups = response.data
      } catch (error) {
        this.$message.error('跟进记录加载失败')
      } finally {
        this.followupLoading = false
      }
    },
    async saveFollowup() {
      const content = this.newContent.trim()
      if (!content) {
        this.$message.error('请填写跟进备注')
        return
      }
      if (!this.selected) return
      this.saving = true
      try {
        await this.$axios.post('http://localhost:10086/warning/' + this.selected.sctid + '/followups', {
          content,
          operatorTid: Number(sessionStorage.getItem('tid'))
        })
        this.newContent = ''
        this.$message.success('跟进备注已保存')
        await Promise.all([this.loadFollowups(), this.loadWarnings()])
      } catch (error) {
        this.$message.error('跟进备注保存失败')
      } finally {
        this.saving = false
      }
    },
    editGrade(row) {
      this.$router.push({
        path: this.isTeacher ? '/teacherEditorGradeCourse' : '/editorGradeCourse',
        query: { sid: row.sid, cid: row.cid, tid: row.tid, term: row.term }
      })
    }
  }
}
</script>

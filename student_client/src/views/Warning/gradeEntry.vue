<template>
  <div>
    <el-card>
      <el-form style="width: 60%" :model="ruleForm" :rules="rules" ref="ruleForm" label-width="100px" class="demo-ruleForm">
        <el-form-item label="技能项目" prop="cid">
          <el-select v-model="ruleForm.cid" filterable placeholder="请选择技能项目" style="width: 100%" :loading="loading" @change="selectCourse">
            <el-option v-for="item in courseOptions" :key="item.cid" :label="item.cname" :value="item.cid"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="学生" prop="sid">
          <el-select v-model="ruleForm.sid" filterable placeholder="请选择学生" style="width: 100%" :disabled="!ruleForm.cid" @change="selectStudent">
            <el-option v-for="item in studentOptions" :key="item.sid" :label="item.sname + '（学号 ' + item.sid + '）'" :value="item.sid"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="考核分数" prop="grade">
          <el-input v-model.number="ruleForm.grade" placeholder="请输入 0 至 100 的整数"></el-input>
        </el-form-item>
        <el-form-item label="达标线">{{ ruleForm.passScore == null ? '请选择学生' : ruleForm.passScore + ' 分' }}</el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitForm('ruleForm')">提交</el-button>
          <el-button @click="resetForm('ruleForm')">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="history" border style="margin-top: 20px">
        <el-table-column prop="grade" label="历次分数"></el-table-column>
        <el-table-column prop="passScoreSnapshot" label="当时达标线"></el-table-column>
        <el-table-column label="当时状态"><template slot-scope="scope">{{ scope.row.grade < scope.row.passScoreSnapshot ? '未达标预警' : '已达标' }}</template></el-table-column>
        <el-table-column prop="recordedAt" label="记录时间（UTC）"></el-table-column>
        <el-table-column label="来源"><template slot-scope="scope">{{ scope.row.source === 'initial_import' ? '初始导入' : '考核录入' }}</template></el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
export default {
  props: {
    scope: {
      type: String,
      required: true
    },
    returnPath: {
      type: String,
      required: true
    }
  },
  data() {
    const checkGrade = (rule, value, callback) => {
      if (value === null || value === undefined || value === '') {
        return callback(new Error('考核分数不能为空'))
      }
      if (!Number.isInteger(value)) {
        return callback(new Error('请输入整数'))
      }
      if (value < 0 || value > 100) {
        return callback(new Error('成绩范围为 0 至 100'))
      }
      callback()
    }
    return {
      loading: false,
      records: [],
      courses: [],
      students: [],
      history: [],
      ruleForm: {
        cid: null,
        sctid: null,
        sid: null,
        tid: null,
        cname: null,
        sname: null,
        grade: null,
        term: null,
        passScore: null
      },
      rules: {
        cid: [{ required: true, message: '请选择技能项目', trigger: 'change' }],
        sid: [{ required: true, message: '请选择学生', trigger: 'change' }],
        grade: [
          { required: true, message: '请输入考核分数', trigger: 'change' },
          { type: 'number', message: '请输入数字', trigger: 'change' },
          { validator: checkGrade, trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    courseOptions() {
      const options = []
      this.courses.forEach(course => {
        if (!options.some(item => item.cid === course.cid)) {
          options.push(course)
        }
      })
      return options
    },
    studentOptions() {
      return this.students
    }
  },
  created() {
    this.loadRecords()
  },
  methods: {
    async loadRecords() {
      this.loading = true
      const term = this.$route.query.term || sessionStorage.getItem('currentTerm')
      const filters = {term: term}
      const currentTid = Number(sessionStorage.getItem('tid'))
      if (this.scope === 'teacher') {
        filters.tid = String(currentTid)
      }
      try {
        const courseRequest = this.scope === 'teacher'
          ? this.$axios.get('http://localhost:10086/courseTeacher/findMyCourse/' + currentTid + '/' + term)
          : this.$axios.post('http://localhost:10086/courseTeacher/findCourseTeacherInfo', {})
        const [recordResponse, courseResponse, studentResponse] = await Promise.all([
          this.$axios.post('http://localhost:10086/SCT/findBySearch', filters),
          courseRequest,
          this.$axios.post('http://localhost:10086/student/findBySearch', {})
        ])
        this.records = recordResponse.data
        this.courses = courseResponse.data.map(course => Object.assign({}, course, {
          tid: course.tid || currentTid
        }))
        this.students = studentResponse.data
        const query = this.$route.query
        const selected = this.records.find(record =>
          (query.sctid && record.sctid === Number(query.sctid)) ||
          (query.sid && record.sid === Number(query.sid) &&
            record.cid === Number(query.cid) &&
            record.tid === Number(query.tid) &&
            record.term === query.term)
        )
        if (selected) {
          this.applyRecord(selected)
        }
      } catch (error) {
        this.$message.error('技能和学生名单加载失败')
      } finally {
        this.loading = false
      }
    },
    selectCourse(cid) {
      const course = this.courseOptions.find(item => item.cid === cid)
      this.ruleForm = {
        cid: cid,
        sctid: null,
        sid: null,
        tid: course ? course.tid : null,
        cname: course ? course.cname : null,
        sname: null,
        grade: null,
        term: this.$route.query.term || sessionStorage.getItem('currentTerm'),
        passScore: course ? course.passScore : null
      }
      this.history = []
    },
    selectStudent(sid) {
      const selected = this.records.find(record =>
        record.sid === sid &&
        record.cid === this.ruleForm.cid &&
        record.tid === this.ruleForm.tid &&
        record.term === this.ruleForm.term
      )
      if (selected) {
        this.applyRecord(selected)
        return
      }
      const student = this.students.find(item => item.sid === sid)
      this.ruleForm.sctid = null
      this.ruleForm.sid = sid
      this.ruleForm.sname = student ? student.sname : null
      this.ruleForm.grade = null
      this.history = []
      this.$nextTick(() => this.$refs.ruleForm && this.$refs.ruleForm.clearValidate())
    },
    applyRecord(record) {
      this.ruleForm = Object.assign({}, record, {grade: null})
      this.loadHistory()
      this.$nextTick(() => this.$refs.ruleForm && this.$refs.ruleForm.clearValidate())
    },
    async loadHistory() {
      if (!this.ruleForm.sctid) {
        this.history = []
        return
      }
      const response = await this.$axios.get('http://localhost:10086/SCT/' + this.ruleForm.sctid + '/assessments')
      this.history = response.data
    },
    async ensureStudentSkill() {
      if (this.ruleForm.sctid) return this.ruleForm.sctid
      const relation = {
        sid: this.ruleForm.sid,
        cid: this.ruleForm.cid,
        tid: this.ruleForm.tid,
        term: this.ruleForm.term
      }
      await this.$axios.post('http://localhost:10086/SCT/save', relation)
      const response = await this.$axios.post('http://localhost:10086/SCT/findBySearch', {
        sid: String(relation.sid),
        cid: String(relation.cid),
        tid: String(relation.tid),
        term: relation.term
      })
      const record = response.data.find(item =>
        item.sid === relation.sid && item.cid === relation.cid &&
        item.tid === relation.tid && item.term === relation.term
      )
      if (!record) throw new Error('学生技能关联创建失败')
      this.ruleForm.sctid = record.sctid
      this.records.push(record)
      return record.sctid
    },
    submitForm(formName) {
      this.$refs[formName].validate(async valid => {
        if (!valid) return
        try {
          const sctid = await this.ensureStudentSkill()
          const response = await this.$axios.post('http://localhost:10086/SCT/' + sctid + '/assessments', {
            grade: this.ruleForm.grade,
            operatorTid: Number(sessionStorage.getItem('tid'))
          })
          if (response.data && response.data.id) {
            this.$message({showClose: true, message: '成绩录入成功', type: 'success'})
            this.$router.push({path: this.returnPath, query: {view: 'all'}})
          } else {
            this.$message.error('成绩录入失败，请检查数据库')
          }
        } catch (error) {
          this.$message.error('成绩录入失败，请稍后重试')
        }
      })
    },
    resetForm(formName) {
      this.ruleForm.grade = null
      this.$refs[formName].clearValidate()
    }
  }
}
</script>

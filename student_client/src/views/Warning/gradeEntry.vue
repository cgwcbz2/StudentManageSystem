<template>
  <div>
    <el-card>
      <el-form style="width: 60%" :model="ruleForm" :rules="rules" ref="ruleForm" label-width="100px" class="demo-ruleForm">
        <el-form-item label="技能项目" prop="cid">
          <el-select v-model="ruleForm.cid" filterable placeholder="请选择技能项目" style="width: 100%" :loading="loading" @change="selectCourse">
            <el-option v-for="item in courseOptions" :key="item.cid" :label="item.cname" :value="item.cid"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="学生" prop="sctid">
          <el-select v-model="ruleForm.sctid" filterable placeholder="请选择学生" style="width: 100%" :disabled="!ruleForm.cid" @change="selectStudent">
            <el-option v-for="item in studentOptions" :key="item.sctid" :label="item.sname + '（学号 ' + item.sid + '）'" :value="item.sctid"></el-option>
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
        sctid: [{ required: true, message: '请选择学生', trigger: 'change' }],
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
      this.records.forEach(record => {
        if (!options.some(item => item.cid === record.cid)) {
          options.push({cid: record.cid, cname: record.cname})
        }
      })
      return options
    },
    studentOptions() {
      return this.records.filter(record => record.cid === this.ruleForm.cid)
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
      if (this.scope === 'teacher') {
        filters.tid = sessionStorage.getItem('tid')
      }
      try {
        const response = await this.$axios.post('http://localhost:10086/SCT/findBySearch', filters)
        this.records = response.data
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
      this.ruleForm = {
        cid: cid,
        sctid: null,
        sid: null,
        tid: null,
        cname: null,
        sname: null,
        grade: null,
        term: this.$route.query.term || sessionStorage.getItem('currentTerm'),
        passScore: null
      }
      this.history = []
    },
    selectStudent(sctid) {
      const selected = this.records.find(record => record.sctid === sctid)
      if (selected) {
        this.applyRecord(selected)
      }
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
    submitForm(formName) {
      this.$refs[formName].validate(async valid => {
        if (!valid) return
        try {
          const response = await this.$axios.post('http://localhost:10086/SCT/' + this.ruleForm.sctid + '/assessments', {
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

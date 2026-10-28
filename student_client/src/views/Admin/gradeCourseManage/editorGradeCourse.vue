<template>
  <div>
    <el-card>
      <el-form style="width: 60%" :model="ruleForm" :rules="rules" ref="ruleForm" label-width="100px" class="demo-ruleForm">
        <el-form-item label="技能项目" prop="cname">
          <el-input v-model="ruleForm.cname" :value="ruleForm.cname" :disabled="true"></el-input>
        </el-form-item>
        <el-form-item label="教师名" prop="tname">
          <el-input v-model="ruleForm.tname" :value="ruleForm.tname" :disabled="true"></el-input>
        </el-form-item>
        <el-form-item label="学生名" prop="sname">
          <el-input v-model="ruleForm.sname" :value="ruleForm.sname" :disabled="true"></el-input>
        </el-form-item>
        <el-form-item label="考核分数" prop="grade">
          <el-input v-model.number="ruleForm.grade" :value="ruleForm.grade"></el-input>
        </el-form-item>
        <el-form-item label="达标线">{{ ruleForm.passScore }} 分</el-form-item>
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
  data() {
    var checkGrade = (rule, value, callback) => {
      if (value === null || value === undefined || value === '') {
        return callback(new Error('考核分数不能为空'));
      }
      if (!Number.isInteger(value)) {
        callback(new Error('请输入数字值'));
      } else {
        if (value > 100 || value < 0) {
          callback(new Error('成绩范围 [0 - 100]'));
        } else {
          callback();
        }
      }
    };
    return {
      history: [],
      ruleForm: {
        cid: null,
        cname: null,
        grade: null,
        sid: null,
        sname: null,
        tid: null,
        tname: null,
      },
      rules: {
        grade: [
          { required: true, message: '请输入考核分数', trigger: 'change'},
          { type: 'number', message: '请输入数字', trigger: 'change'},
          { validator: checkGrade, trigger: 'blur'}
        ],
      }
    };
  },
  created() {
    const that = this
    this.ruleForm.cid = this.$route.query.cid
    this.ruleForm.tid = this.$route.query.tid
    this.ruleForm.sid = this.$route.query.sid
    this.ruleForm.term = this.$route.query.term
    axios.get('http://localhost:10086/SCT/findById/' +
        this.ruleForm.sid + '/' +
        this.ruleForm.cid + '/' +
        this.ruleForm.tid + '/' +
        this.ruleForm.term).then(function (resp) {
      that.ruleForm = resp.data
      that.loadHistory()
    })
  },
  methods: {
    async loadHistory() {
      if (!this.ruleForm.sctid) return
      const response = await this.$axios.get('http://localhost:10086/SCT/' + this.ruleForm.sctid + '/assessments')
      this.history = response.data
    },
    submitForm(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          // 通过前端校验
          const that = this
          const grade = that.ruleForm.grade
          axios.post('http://localhost:10086/SCT/' + that.ruleForm.sctid + '/assessments', {
            grade, operatorTid: Number(sessionStorage.getItem('tid'))
          }).then(function (resp) {
            if (resp.data && resp.data.id) {
              that.$message({
                showClose: true,
                message: '编辑成功',
                type: 'success'
              });
            }
            else {
              that.$message.error('编辑失败，请检查数据库');
            }
            that.$router.push({path: '/adminWarningList', query: {view: 'all'}})
          })
        } else {
          return false;
        }
      });
    },
    resetForm(formName) {
      this.$refs[formName].resetFields();
    }
  }
}
</script>

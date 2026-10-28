<template>
  <div>
    <el-form style="width: 60%" :model="ruleForm" :rules="rules" ref="ruleForm" label-width="100px" class="demo-ruleForm">
      <el-form-item label="技能项目" prop="cname">
        <el-input v-model="ruleForm.cname" :value="ruleForm.cname"></el-input>
      </el-form-item>
      <el-form-item label="达标线" prop="passScore">
        <el-input-number v-model="ruleForm.passScore" :min="1" :max="100" :step="1"></el-input-number>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="submitForm('ruleForm')">提交</el-button>
        <el-button @click="resetForm('ruleForm')">重置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>
<script>
export default {
  data() {
    return {
      ruleForm: {
        cid: null,
        cname: null,
        ccredit: null,
        passScore: 60
      },
      rules: {
        cname: [
          { required: true, message: '请输入名称', trigger: 'blur' },
        ],
        passScore: [
          { required: true, type: 'integer', min: 1, max: 100, message: '达标线需为 1 至 100 分整数', trigger: 'change' },
        ],
      }
    };
  },
  created() {
    const that = this
    if (this.$route.query.cid === undefined) {
      this.ruleForm.cid = 1
    }
    else {
      this.ruleForm.cid = this.$route.query.cid
    }
    axios.get('http://localhost:10086/course/findById/' + this.ruleForm.cid).then(function (resp) {
      that.ruleForm = resp.data[0]
      console.log(that.ruleForm)
    })
  },
  methods: {
    submitForm(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          // 通过前端校验
          const that = this
          console.log(this.ruleForm)
          axios.post("http://localhost:10086/course/updateCourse", this.ruleForm).then(function (resp) {
            if (resp.data === true) {
              that.$message({
                showClose: true,
                message: '编辑成功',
                type: 'success'
              });
            }
            else {
              that.$message.error('编辑失败，请检查数据库');
            }
            that.$router.push("/queryCourse")
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

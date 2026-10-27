import Vue from 'vue';
import VueRouter from 'vue-router';
import login from '../views/login/index';
import admin from '../views/Admin/index';
import home from '../views/home';
import studentManage from '../views/Admin/studentManage/index'
import addStudent from "@/views/Admin/studentManage/addStudent";
import editorStudent from "@/views/Admin/studentManage/editorStudent";
import teacherManage from "@/views/Admin/teacherManage/index"
import addTeacher from "@/views/Admin/teacherManage/addTeacher";
import editorTeacher from "@/views/Admin/teacherManage/editorTeacher";
import courseManage from "@/views/Admin/courseManage/index";
import addCourse from "@/views/Admin/courseManage/addCourse";
import teacher from "@/views/Teacher/index";
import queryStudent from "@/views/Admin/studentManage/queryStudent";
import queryTeacher from "@/views/Admin/teacherManage/queryTeacher";
import student from "@/views/Student/index";
import editorCourse from "@/views/Admin/courseManage/editorCourse";
import queryCourse from "@/views/Admin/courseManage/queryCourse";
import offerCourse from "@/views/Teacher/offerCourse";
import setCourse from "@/views/Teacher/setCourse";
import myOfferCourse from "@/views/Teacher/myOfferCourse";
import studentSelectCourseManage from "@/views/Student/selectCourse/index";
import selectCourse from "@/views/Student/selectCourse/selectCourse";
import querySelectedCourse from "@/views/Student/selectCourse/querySelectedCourse";
import studentCourseGrade from "@/views/Student/courseGrade/index";
import queryCourseGrade from "@/views/Student/courseGrade/queryCourseGrade";
import gradeEntry from "@/views/Warning/gradeEntry";
import updateInfo from "@/components/updateInfo";
import warningManage from "@/views/Warning/index";
import warningList from "@/views/Warning/warningList";

Vue.use(VueRouter)

const routes = [
  {
    // 随便定义的首页
    path: '/',
    name: 'index',
    component: login,
    redirect: '/login'
  },
  {
    // 登陆页
    path: '/login',
    name: 'login',
    component: login
  },
  { path: '/queryGradeCourse', redirect: {path: '/adminWarningList', query: {view: 'all'}} },
  { path: '/teacherQueryGradeCourseManage', redirect: {path: '/teacherWarningList', query: {view: 'all'}} },
  { path: '/queryCourseTeacher', redirect: '/offerCourse' },
  {
    // admin 的路由
    path: '/admin',
    name: 'admin',
    redirect: '/adminHome',
    component: admin,
    meta: {requireAuth: true},
    children: [
      {
        path: '/adminHome',
        name: '首页',
        component: home,
        meta: {requireAuth: true}
      },
      {
        path: '/studentManage',
        name: '学生管理',
        component: studentManage,
        meta: {requireAuth: true},
        children: [
          {
            path: '/addStudent',
            name: '添加学生',
            component: addStudent,
            meta: {requireAuth: true}
          },
          {
            path: '/studentList',
            name: '学生列表',
            component: queryStudent,
            meta: {requireAuth: true},
          },
          {
            path: '/editorStudent',
            name: '编辑学生',
            component: editorStudent,
            meta: {requireAuth: true, hideInMenu: true}
          },
          {
            path: '/queryStudent',
            redirect: '/studentList',
            meta: {requireAuth: true, hideInMenu: true}
          }
        ]
      },
      {
        path: '/teacherManage',
        name: '教师管理',
        component: teacherManage,
        meta: {requireAuth: true},
        children: [
          {
            path: '/addTeacher',
            name: '添加教师',
            component: addTeacher,
            meta: {requireAuth: true}
          },
          {
            path: '/queryTeacher',
            name: '教师列表',
            component: queryTeacher,
            meta: {requireAuth: true},
            children: [
            ]
          },
          {
            path: '/editorTeacher',
            name: '编辑教师',
            component: editorTeacher,
            meta: {requireAuth: true, hideInMenu: true}
          },
        ]
      },
      {
        path: '/courseManage',
        name: '技能项目管理',
        component: courseManage,
        meta: {requireAuth: true},
        children: [
          {
            path: '/addCourse',
            name: '添加技能项目',
            component: addCourse,
            meta: {requireAuth: true}
          },
          {
            path: '/queryCourse',
            name: '查询技能项目',
            component: queryCourse,
            meta: {requireAuth: true}
          },
          {
            path: '/editorCourse',
            name: '编辑技能项目',
            component: editorCourse,
            meta: {requireAuth: true, hideInMenu: true}
          },
        ]
      },
      {
        path: '/adminWarningManage',
        name: '达标预警',
        component: warningManage,
        meta: {requireAuth: true},
        children: [
          {
            path: '/adminWarningList',
            name: '预警跟踪',
            component: warningList,
            props: {scope: 'admin'},
            meta: {requireAuth: true}
          },
          {
            path: '/editorGradeCourse',
            name: '录入成绩',
            component: gradeEntry,
            props: {scope: 'admin', returnPath: '/adminWarningList'},
            meta: {requireAuth: true}
          }
        ]
      }
    ]
  },
  {
    path: '/teacher',
    name: 'teacher',
    component: teacher,
    redirect: '/teacherHome',
    meta: {requireAuth: true},
    children: [
      {
        path: '/teacherHome',
        name: '首页',
        meta: {requireAuth: true},
        component: home
      },
      {
        path: '/teacherUpdateInfo',
        name: '个人信息',
        component: updateInfo,
        meta: {requireAuth: true}
      },
      {
        path: '/teacherCourseManage',
        name: '技能项目设置',
        meta: {requireAuth: true},
        component: setCourse,
        children: [
          {
            path: '/myOfferCourse',
            name: '我开设的技能',
            component: myOfferCourse,
            meta: {requireAuth: true}
          },
          {
            path: '/offerCourse',
            name: '开设技能',
            component: offerCourse,
            meta: {requireAuth: true}
          },
        ]
      },
      {
        path: '/teacherWarningManage',
        name: '达标预警',
        component: warningManage,
        meta: {requireAuth: true},
        children: [
          {
            path: '/teacherWarningList',
            name: '预警跟踪',
            component: warningList,
            props: {scope: 'teacher'},
            meta: {requireAuth: true}
          },
          {
            path: '/teacherEditorGradeCourse',
            name: '录入成绩',
            component: gradeEntry,
            props: {scope: 'teacher', returnPath: '/teacherWarningList'},
            meta: {requireAuth: true}
          }
        ]
      }
    ]
  },
  {
    path: '/student',
    name: 'student',
    component: student,
    redirect: '/studentHome',
    meta: {requireAuth: true},
    children: [
      {
        path: '/studentHome',
        name: '首页',
        component: home,
        meta: {requireAuth: true}
      },
      {
        path: '/studentUpdateInfo',
        name: '个人信息',
        component: updateInfo,
        meta: {requireAuth: true}
      },
      {
        path: '/studentSelectCourseManage',
        name: '技能选择',
        component: studentSelectCourseManage,
        meta: {requireAuth: true},
        children: [
          {
            path: '/studentSelectCourse',
            name: '选择技能',
            component: selectCourse,
            meta: {requireAuth: true}
          },
          {
            path: '/querySelectedCourse',
            name: '已选技能',
            component: querySelectedCourse,
            meta: {requireAuth: true}
          }
        ]
      },
      {
        path: '/courseGrade',
        name: '技能达标情况',
        component: studentCourseGrade,
        meta: {requireAuth: true},
        children: [
          {
            path: '/queryCourseGrade',
            name: '达标查询',
            component: queryCourseGrade,
            meta: {requireAuth: true}
          },
        ]
      }
    ]
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

export default router

/*
  session 设置：
    1. token
    2. name
    3. type
    4. tid
    5. sid
    5. 系统信息 info
 */
router.beforeEach((to, from, next) => {
  console.log(from.path + ' ====> ' + to.path)
  if (to.meta.requireAuth) { // 判断该路由是否需要登录权限
    if (sessionStorage.getItem("token") === 'true') { // 判断本地是否存在token
      next()
    } else {
      // 未登录,跳转到登陆页面
      next({
        path: '/login',
        query: {redirect: to.fullPath}
      })
    }
  } else {
    // 不需要登陆权限的页面，如果已经登陆，则跳转主页面
    if(sessionStorage.getItem("token") === 'true'){
      console.log('检查拦截器配置，大概率出现漏网之鱼')
      const t = sessionStorage.getItem("type")
      next('/' + t);
    }else{
      next();
    }
  }
});

<template>
  <div>
    <el-aside width="200px" style="background-color: rgb(238, 241, 246); height: 810px; border: 1px solid #eee">
      <el-menu router :default-active="this.$route.path">
        <template v-for="item in menuRoutes">
          <el-submenu v-if="visibleChildren(item).length" :index="item.path" :key="item.path">
            <template slot="title"><i class="el-icon-menu"></i>{{ item.name }}</template>
            <el-menu-item v-for="child in visibleChildren(item)" :index="child.path" :key="child.path"><i class="el-icon-s-promotion"></i>{{ child.name }}</el-menu-item>
          </el-submenu>
          <el-menu-item v-else :index="item.path" :key="item.path"><i class="el-icon-menu"></i>{{ item.name }}</el-menu-item>
        </template>
      </el-menu>
    </el-aside>
  </div>
</template>

<script>
export default {
  name: "r-aside",
  data() {
    return {
      type: null
    }
  },
  computed: {
    menuRoutes() {
      const roleRoute = this.$router.options.routes.find(route => route.name === this.type)
      return roleRoute ? roleRoute.children.filter(route => !route.meta || !route.meta.hideInMenu) : []
    }
  },
  methods: {
    visibleChildren(route) {
      return (route.children || []).filter(child => !child.meta || !child.meta.hideInMenu)
    }
  },
  created() {
    this.type = sessionStorage.getItem("type")
    console.log('aside: ' + this.type)
    console.log(this.$router.options)
  }
}
</script>

<style scoped>

</style>

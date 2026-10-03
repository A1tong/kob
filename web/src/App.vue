<template>
  <NavBar />
  <router-view/>
</template>

<script>
import NavBar from './components/NavBar.vue'
import "bootstrap/dist/css/bootstrap.min.css"
import "bootstrap/dist/js/bootstrap"

export default {
  components: {
    NavBar
  },
}
</script>

<style>
body {
  background-image: url("@/assets/images/background.jpg");
  background-size: cover;          /* contain 会留灰边；cover 让图片铺满整个屏幕 */
  background-repeat: no-repeat;
  background-position: center;
  background-attachment: fixed;
  background-color: #3a3a3a;       /* 图片还没加载出来时的兜底底色 */
  min-height: 100vh;
  margin: 0;
}
</style>

<template>
  <div id="app">
    <div id="nav">
      <router-link to="/">Home</router-link>|
      <router-link to="/about">About</router-link>
    </div>
    <router-view />
  </div>
</template>

<style lang="scss">
body {
  font-family: PingFangSC-Regular, sans-serif;
  section {
    margin: 0 auto;
    max-width: 1200px;
    @media (max-width: 600px) {
    }
  }
  margin: 0;
  .el-progress__text {
    display: none;
  }
}
#nav {
  display: none;
}
</style>

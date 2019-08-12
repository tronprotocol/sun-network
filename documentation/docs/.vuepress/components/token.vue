<template>
  <div class="token">
    <a href="http://testnet.tronex.io/join/getJoinPage" class="nav-link action-button">{{i18n.title}}</a>
  </div>
</template>

<script>
export default {
  name: 'token',
  data() {
    return {
      i18n: {
        title: 'Apply for test coins'
      }
    };
  },
  mounted() {
    if (this.$route.path.indexOf('/zh/') == 0) {
      this.i18n = {
        title: '测试币申请'
      };
    }
  }
};
</script>

<style lang="stylus" scoped>
.token {
  width: 100%;
  text-align: center;
  padding: 20px 0;

  .action-button {
    display: inline-block;
    font-size: 1.2rem;
    color: #fff;
    background-color: #3eaf7c;
    padding: 0.8rem 1.6rem;
    border-radius: 4px;
    transition: background-color 0.1s ease;
    box-sizing: border-box;
    border-bottom: 1px solid #389d70;
  }
}
</style>

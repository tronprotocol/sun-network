<template>
  <div class="footerBar">
    <section>
      <div class="container">
        <el-row :gutter="100">
          <el-col :span="8" :xs="24">
            <div class="content">
              <div class="title">Developer Resources</div>
              <div class="detail">
                <ul>
                  <li>
                    <a :href="$store.state.docUrl">Developer Docs</a>
                  </li>
                  <li>
                    <div @click="mainNet">
                      <a>DAppChain main-net</a>
                    </div>
                  </li>
                  <li>
                    <a :href="$store.state.testNetsUrl">DAppChain test-net</a>
                  </li>
                </ul>
              </div>
            </div>
          </el-col>
          <el-col :span="8" :xs="24">
            <div class="content">
              <div class="title">TRON Foundation</div>
              <div class="detail">
                <ul>
                  <li>
                    <a href="#/">Since the TRON Foundation was founded, it has upheld its original faith of decentralizing the internet, working hard to change the world with technology.</a>
                  </li>
                  <li>
                    <a :href="$store.state.donateUrl" class="donate">Donate to Support Development</a>
                  </li>
                </ul>
              </div>
            </div>
          </el-col>
          <el-col :span="8" :xs="24">
            <div class="content">
              <div class="title">Contact US</div>
              <div class="detail">
                <ul>
                  <li>
                    <a :href="$store.state.faqUrl">Join Gitter</a>
                  </li>
                  <li>
                    <a :href="$store.state.discordUrl">Join Discord</a>
                  </li>
                  <li>
                    <a :href="$store.state.telegramUrl">Join Telegram</a>
                  </li>
                  <li>
                    <a :href="$store.state.githubUrl">Join github</a>
                  </li>
                </ul>
              </div>
            </div>
          </el-col>
        </el-row>
        <div class="copyright">COPYRIGHT© 2017-2019 TRON FOUNDATION</div>
      </div>
    </section>
  </div>
</template>

<script>
export default {
  name: 'FooterBar',
  methods: {
    mainNet: function() {
      this.$message('Coming soon');
    }
  }
};
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped lang="scss">
.footerBar {
  bottom: 0%;
  width: 100%;
  min-height: 280px;
  background: rgb(13, 13, 13);
  font-family: Arial;
  .container {
    padding: 50px 150px 0 150px;
    background: rgb(13, 13, 13);

    @media (max-width: 600px) {
      padding: 50px 50px 0 50px;
    }
  }
  .title {
    color: white;
    font-size: 20px;
  }
  .detail {
    font-size: 13px;
    color: #d3d3d3;
    margin: 20px 0;

    ul {
      list-style-type: none;
      margin: 0;
      padding: 0;
      overflow: hidden;
    }

    li a {
      color: #8f8f8f;
      display: block;
      padding: 5px;
      text-decoration: none;
      &.donate {
        display: inline-block;
        width: 90%;
        height: 32px;
        line-height: 32px;
        text-align: center;
        color: rgb(255, 255, 255);
        margin: 20px 0px 0px;
        background: rgb(97, 97, 97);
      }
    }
  }
  .copyright {
    margin-top: 20px;
    font-size: 12px;
    color: white;
    width: 100%;
    text-align: center;
    padding-bottom: 20px;
  }
}
</style>

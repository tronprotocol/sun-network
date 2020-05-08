<template>
  <div>
    <div class="container">
      <my-nav :languageGroup="languageGroup" />
      <!-- <Loading :isLoading="showLoading" /> -->
      <div class="main">
        <div
          class="cell"
          style="margin-top:0.2rem;"
        >
        </div>
        <div class="cell">
          <div class="rank-content">
            <div class="col">
              <Play />
            </div>
          </div>
        </div>
        <div class="cell">
          <Result :contractAddress="contractAddress" />
        </div>
      </div>
      <Footer />
    </div>
  </div>
</template>

<script>
import { mapState } from "vuex";
import MyNav from "~/components/MyNav.vue";
import Select from "~/components/Select.vue";
import Play from "~/components/Play.vue";
import Result from "~/components/Result.vue";
import Loading from "~/components/loading.vue";

import { getBalance, getWeeklyRank, getaccount } from "~/assets/js/common";
import Mining from "~/components/Mining.vue";
import bus from "~/assets/js/bus";
import interfaceData from "@/api/config";

import SunWeb2 from 'sunweb';
let contractAddress = interfaceData.contractAddress;

export default {
  components: {
    MyNav,
    Loading,
    Select,
    Play,
    Result,
    Mining,
  },

  data() {
    return {
      sunWeb: {},
      contractAddress,
      rollBtnDisabled: false,
      lastRollNum: null,
      contractInstance: null,
      languageGroup: [
        { lng: "en", txt: "English" },
        { lng: "ch", txt: "Chinese" }
      ],
      eventServer: "",
      from: ""
    };
  },
  created() {
    this.initTronLink();
    const sunWeb2 = new SunWeb2(interfaceData.mainOptions, interfaceData.sideOptions, interfaceData.mainGatewayAddress, interfaceData.sideGatewayAddress, interfaceData.chainId);
    sunWeb2.mainchain.setAddress(interfaceData.ownAddr);
    sunWeb2.sidechain.setAddress(interfaceData.ownAddr);
    this.$store.commit('SET_SUNWEB2', sunWeb2);
  },
  watch: {
    address: {
      deep: true,
      handler(val) {}
    },
    // globalSunWeb: {
    //   deep: true,
    //   handler(val) {
    //     this.sunWeb = val;
    //     // window.sunWeb = this.sunWeb;
    //   }
    // },
    // "globalSunWeb.mainchain.defaultAddress": {
    //   deep: true,
    //   handler(newVal, oldVal) {
    //     if (
    //       newVal &&
    //       oldVal &&
    //       newVal.base58 &&
    //       oldVal.base58 &&
    //       newVal.base58 !== oldVal.base58
    //     ) {
    //       window.location.reload(true);
    //     }
    //   }
    // }
  },
  computed: {
    ...mapState(["dapp", "globalSunWeb", "globalSunWeb2", "address"])
  },
  methods: {
    initTronLink(){
      let sunWeb = {};
      let tmpTimer1 = setInterval(()=>{
        if (window.sunWeb) {
          clearInterval(tmpTimer1);
          sunWeb = window.sunWeb;
          if(tmpTimer2) clearInterval(tmpTimer2);
          //1s检测钱包是否登录
          let tmpTimer2 = setInterval(async ()=>{
            if (sunWeb.mainchain.defaultAddress.base58 != false) {
              clearInterval(tmpTimer2);
              this.$store.commit('SET_SUNWEB', sunWeb);
              const contractInstance = await sunWeb.sidechain
                .contract()
                .at(contractAddress);
              this.contractInstance = contractInstance;
              this.$store.commit("SET_CONTRACT_INSTANCE", contractInstance);
            }
          }, 1000);

        }
      }, 1000);
    },
  },
  async mounted() {
      /**
       * 游戏合约初始化
       */
      // const contractInstance = await this.globalSunWeb.sidechain
      //   .contract()
      //   .at(contractAddress);
      // this.contractInstance = contractInstance;
      // this.$store.commit("SET_CONTRACT_INSTANCE", contractInstance);
      // setTimeout(() => {
      //   this.$message({
      //     type: "warn",
      //     message: this.$t("noLogin"),
      //     showClose: true
      //   });
      // }, 1000)
  }
};
</script>

<style lang="scss">
.container {
  min-height: 120vh;
  display: flex;
  flex-direction: column;
  background: #16013a url("../../../assets/images/bg.png") no-repeat;
  background-size: 100% auto;
  background-position: top;
  color: #fff;
  padding-bottom: 0.2rem;
  a {
    color: #b3a6ff;
  }
  .banner {
    margin-top: 0.3rem;
    width: 12rem;
    margin: 0 auto;
  }
  .main {
    width: 12rem;
    flex: 1;
    margin: auto;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    & > .cell {
      &:nth-child(2) {
        display: flex;
        flex-direction: row;
        justify-content: space-between;

        .rank-content {
          flex: 1;
          width: 50%;
          height: 5.96rem;
          background: url("../../../assets/images/left-border.png") no-repeat;
          background-size: 100% 100%;
          .col {
            width: 100%;
            height: 5.74rem;
            margin: 0 auto;
            padding: 0.16rem;
          }
        }

        .rank-content:last-child {
          background: url("../../../assets/images/right-border.png") no-repeat;
          background-size: 100% 100%;
        }
      }
      &:nth-child(3) {
        padding-top: 0.1rem;
        padding-bottom: 0.2rem;
        // background-color: rgb(67, 39, 149);
      }
    }
  }
}

@media screen and (max-width: 1024px) {
  .el-icon-question {
    font-size: 14px;
  }
  .container .main > .cell:nth-child(2) .rank-content .col {
    height: 100%;
  }
  .container {
    background: #16013a url("../../../assets/images/mobile_bg.png") no-repeat;
    background-size: 100% auto;
    background-position: top;
    .main {
      padding: 0.7rem 0.2rem 0;
      width: 100%;

      .cell {
        &:nth-child(2) {
          flex-direction: column;
          .rank-content {
            width: 100%;
            height: 6.8rem;
          }
          .rank-content:nth-child(2) {
            height: 8.5rem;
          }
        }
      }
    }
  }
}
</style>

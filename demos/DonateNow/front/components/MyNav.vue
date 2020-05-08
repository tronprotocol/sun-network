
<template>
  <div>
    <div class="my-nav  pc_none">
      <div class="inner">
        <a
          class="logo"
          href="javascript:window.location.reload();"
        >
          <img :src="require('../assets/images/tron_banner.png')" style="height: 43px;">
        </a>
        <span class="sun-network">SUN Network</span>
        <div class="nav">
           <!-- <el-button
            class="test nav-menu-item"
            type="text"
            @click="openDoc()"
          >Apply for test coin</el-button> -->

          <!-- <el-button
            class="deposit nav-menu-item"
            type="text"
            @click="depositTrx()"
          >{{$t('DepositText')}}</el-button> -->

          <!-- <el-button
            class="withdraw nav-menu-item"
            type="text"
            @click="withdrawTrx()"
          >{{$t('WithdrawText')}}</el-button> -->

          <!--  账户-->
          <div
            v-if="address && address.base58"
            class="account"
          >
            <el-tooltip
              class="item account-tooltip nav-menu-item"
              effect="dark"
              placement="bottom"
            >
              <div slot="content">
                <div class="account">
                  <p class="wallet">Main chain TRX: {{this.mBalance | toLocaleString}} TRX</p>
                  <p class="wallet">Side chain TRX: {{this.balance | toLocaleString}} TRX</p>
                </div>
              </div>
              <el-button
                class="how"
                type="text"
              >
                {{address.base58 | hiddenAddress}}
                <i class="el-icon-arrow-down el-icon--right"></i>
              </el-button>
            </el-tooltip>
          </div>
          <el-button
            v-else
            class="nav-menu-item"
            type="text"
            @click="login()"
          >{{$t('Login')}}</el-button>
          <!-- <login-dg
            :params="loginDgParams"
            v-if="loginDgParams.show"
          ></login-dg> -->

          <!-- 国际化 -->
          <div class="language">
            <span style="display: flex;">
              English
              <!-- <img :src="require('../assets/images/'+locale+'.png')"> -->
            </span>
            <div class="group">
              <div
                v-for="(item,index) of languageGroup"
                class="item"
                @click="location(item.lng)"
                :key="index"
              >
                <span style="display: flex;">
                  English
                  <!-- <img :src="require('../assets/images/'+index+'.png')"> -->
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <el-dialog
      title=""
      :visible.sync="dialogVisible"
      width="40%">
      <p style="font-size: 16px; font-faily: 'roboto';color:#000;" v-html="loginWallet"></p>
  <!-- <span slot="footer" class="dialog-footer">
    <el-button @click="dialogVisible = false">取 消</el-button>
    <el-button type="primary" @click="dialogVisible = false">确 定</el-button>
  </span> -->
    </el-dialog>
    <deposit-trx-dg
      :params="depositTrxDgParams"
      v-if="depositTrxDgParams.show"
    ></deposit-trx-dg>
    <withdraw-trx-dg
      :params="withdrawTrxDgParams"
      v-if="withdrawTrxDgParams.show"
    ></withdraw-trx-dg>
  </div>
</template>

<script>
import LoginDg from './dialog/login';
import DepositTrxDg from './dialog/depositTrx';
import WithdrawTrxDg from './dialog/depositTrx';
import { getBalance, getaccount } from "~/assets/js/common";
import interfaceData from '../api/config';

import { mapState } from "vuex";

export default {
  name: "MyNav",
  components: {
    LoginDg,
    DepositTrxDg,
    WithdrawTrxDg,
  },
  data() {
    return {
      dialogVisible: false,
      loginInfo: '',
      loginWallet: '',
      loginDgParams: {
        show: false
      },
      depositTrxDgParams: {
        show: false
      },
      withdrawTrxDgParams: {
        show: false
      },
      intervalBalance: null,
      txt: "",
      icon: "",
      dialogFairness: false,
      luckyList: [
        { area: "0-100", prize: 0.2 },
        { area: "101-999", prize: 2 },
        { area: "1000-4999", prize: 20 },
        { area: "5000-9999", prize: 200 },
        { area: "10000", prize: 1000 }
      ],
      drawDialog: false,
      showMenu: false,
      languageGroup: {
        en: { lng: "en", txt: "English", img: "../assets/images/en.png" },
        // ch: { lng: "ch", txt: "简体中文", img: "../assets/images/ch.png" }
      }
    };
  },
  created() {
   
  },
  watch: {
    address: {
      deep: true,
      handler(val) {
        this.getData();
      }
    }
  },
  mounted() {
    this.intervalBalance = setInterval(() => {
      this.getBalance();
    }, 3000);
  },
  computed: {
    ...mapState([
      "globalSunWeb",
      "address",
      "locale",
      "dialogLogin",
      "balance",
      "mBalance",
      "loginState"
    ])
  },
  methods: {
    openDoc() {
      window.open('https://tron.network/sunnetwork/doc');
    },
    async login() {
      this.loginWallet = this.$t('loginWallet');

      this.dialogVisible = true;
      // this.$alert(this.$t('noLogin'), 'Login', {
      //     // confirmButtonText: '',
      //     callback: action => {
            
      //     }
      //   });
    },
    async getBalance() {
      if (!this.address.base58) {
        return;
      }
      const balance = await this.globalSunWeb.sidechain.trx.getBalance();
      this.$store.commit("SET_BALANCE", this.globalSunWeb.sidechain.fromSun(balance));
      const mBalance = await this.globalSunWeb.mainchain.trx.getBalance();
      this.$store.commit('SET_MBALANCE', this.globalSunWeb.mainchain.fromSun(mBalance));
    },
    withdrawTrx() {
      if (!this.address.base58 || !this.loginState) {
        this.$message({
          type: "warn",
          message: this.$t("noLogin"),
          showClose: true
        });
        return;
      }
      const self = this;
      this.withdrawTrxDgParams = {
        show: true,
        title: 'Withdraw TRX',
        confirm: (p) => {
          const num = self.globalSunWeb.mainchain.toSun(p.num);
          const feeLimit = self.globalSunWeb.mainchain.toSun(p.feeLimit);
          self.globalSunWeb.withdrawTrx(num, interfaceData.withdrawFee, feeLimit).then(txId => {
             this.$message({
              type: "success",
              message: self.$t("operationWithdraw"),
              showClose: true
            });
            return;
          }).catch(ex => {
            this.$message({
              type: "error",
              message: ex.message || 'error',
              showClose: true
            });
          })
        }
      }
    },
    depositTrx() {
      if (!this.address.base58 || !this.loginState) {
        this.$message({
          type: "warn",
          message: this.$t("noLogin"),
          showClose: true
        });
        return;
      }
      const self = this;
      this.depositTrxDgParams = {
        show: true,
        title: 'Deposit TRX',
        confirm: (p) => {
          const num = self.globalSunWeb.mainchain.toSun(p.num);
          const feeLimit = self.globalSunWeb.mainchain.toSun(p.feeLimit);
          self.globalSunWeb.depositTrx(num, interfaceData.depositFee, feeLimit).then(txId => {
            this.$message({
              type: "success",
              message: self.$t("operationDeposit"),
              showClose: true
            });
            return;
          }).catch(ex => {
            console.log(ex)
            this.$message({
              type: "error",
              message: ex.message || 'error',
              showClose: true
            });
          })
        }
      }
    },
    location(lng) {
      this.showMenu = false;
      let url = "";
      window.location.reload();
    },
    /**
     * 账号地址更新，重新获取数据
     */
    async getData() {
      if (!this.globalSunWeb.mainchain.defaultPrivateKey) {
        return;
      }
      let balance = await getBalance(this.address.hex);
      let account = await getaccount(this.address.hex);
      this.$store.commit("SET_BALANCE", this.globalSunWeb.mainchain.fromSun(balance));
      this.$store.commit("SET_ACCOUNT", account);
    }
  }
};
</script>

<style scoped lang="scss">
.clearfix:after {
  display: block;
  content: "";
  clear: both;
}

.clearfix {
  zoom: 1;
}

.nav-menu-item {
  padding-right: 0.3rem;
  cursor: pointer;
  font-weight: 500;
  font-size: 0.16rem;
  &:hover {
    color: #409EFF;
  }
}

.dividend-block {
  background-image: url("../static/images/hot-icon.png");
  background-repeat: no-repeat;
  background-size: auto 100%;
  padding-left: 0.2rem;
}
.dividend-mobile-block {
  text-align: center;
}
.dividend-mobile {
  img {
    width: 0.2rem;
    height: auto;
    vertical-align: bottom;
  }
}
.my-nav {
  font-family: Baskerville;
  height: 0.7rem;
  // background: rgba(0, 0, 0, 0.5);
  border-bottom: solid 1px #c4c4c4;
  .img-nav {
    .iconfont {
      font-size: 0.3rem;
    }
  }
  .logo {
    display: flex;
  }
  .sun-network {
    font-size: 0.3rem;
    margin-left: 15px;
    font-family: Baskerville;
    color: #24292c;
  }
  .inner {
    max-width: 12rem;
    height: 100%;
    margin: auto;
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: center;
    .nav {
      height: 100%;
      flex: 1;
      display: flex;
      flex-direction: row;
      justify-content: flex-end;
      align-items: center;
      .account {
        letter-spacing: 0.01rem;
        display: flex;
        align-items: center;
      }
      .el-button--text {
        color: #8f8f8f;
      }
      .language {
        cursor: pointer;
        position: relative;
        z-index: 10;
        height: 100%;
        padding-right: 20px;
        display: flex;
        align-items: center;
        transition: opacity 0.2s ease-in-out;
        font-size: 0.18rem;
        color: #8f8f8f;
        &:hover {
          & > span {
            opacity: 0.8;
          }
          & > .group {
            display: block;
          }
        }
        &:after {
          content: "";
          position: absolute;
          top: 0.34rem;
          right: 0.05rem;
          width: 0;
          height: 0;
          border-top: 0.04rem solid #b3a6ff;
          border-bottom: 0.04rem solid transparent;
          border-left: 0.04rem solid transparent;
          border-right: 0.04rem solid transparent;
        }
        .group {
          display: none;
          position: absolute;
          right: 0;
          top: 80%;
          width: 80px;

          &:before {
            content: "";
            position: absolute;
            top: -0.12rem;
            left: 0;
            right: 0;
            width: 0;
            height: 0;
            margin: auto;
            border-bottom: 0.06rem solid #6b4bad;
            border-top: 0.06rem solid transparent;
            border-left: 0.06rem solid transparent;
            border-right: 0.06rem solid transparent;
          }
          .item {
            height: 0.4rem;
            margin-bottom: 0.01rem;
            // border-top: 0.01rem solid #4e4e4e;
            padding: 0 0.16rem;
            display: flex;
            align-items: center;
            background: #6b4bad;
            cursor: pointer;
            &:first-child {
              border: none;
            }
            &:hover {
              background: #d6caff;
            }
          }
        }
      }
    }
  }
}

@media screen and (max-width: 1024px) {
  .flex1 {
    flex: 1;
  }
  .mobile_show {
    display: block !important;
  }
  .pc_none {
    display: none;
  }
  .my-nav {
    position: fixed;
    z-index: 100;
    height: 1rem;
    width: 100%;
    .inner {
      padding: 0 0.2rem;
      width: 100%;
      overflow: hidden;
      justify-content: space-between;
      .menu {
        cursor: pointer;
        display: block;
        font-size: 0.6rem;
      }
      .account {
        margin-left: 0;
        text-align: right;
        font-size: 14px;
      }

      .login {
        font-size: 14px;
        // background:red;
        padding: 0.05rem 0;
      }

      .nav {
        transition: all 0.3s ease-in-out;
        overflow: hidden;
        position: fixed;
        z-index: 1;
        width: 100%;
        padding: 0 0.4rem;
        left: 0;
        top: 1rem;
        height: 2rem;
        background-color: #131258;
        box-shadow: -0.2px 11px 46px 0px rgba(14, 13, 62, 0.52);
        flex-direction: column;
        align-items: flex-start;
        justify-content: flex-start;
        a {
          margin-left: 0;
          height: 1rem;
          width: 100%;
          display: flex;
          font-size: 0.28rem;
          align-items: center;
          cursor: pointer !important;
          &:nth-child(1) {
            order: 2;
          }
          &:nth-child(2) {
            order: 3;
          }
          &:nth-child(3) {
            order: 4;
          }
          &:nth-child(4) {
            order: 5;
            border-bottom: 0.01rem solid #39387b;
          }
          &:nth-child(5) {
            margin-top: 0.2rem;
            order: 1;
            width: auto;
            height: 0.6rem;
            font-size: 0.3rem;
            line-height: 0.6rem;
            padding-left: 0.6rem;
            padding-right: 0.2rem;
            background-image: linear-gradient(
                142deg,
                #2babf5 0%,
                #4786f9 50%,
                #6260fd 100%
              ),
              linear-gradient(#de5cff, #de5cff);
            background-blend-mode: normal, normal;
            border-radius: 0.1rem;
            &:before {
              width: 0.4rem;
              height: 0.4rem;
              background-size: auto 100%;
              top: 0.1rem;
            }
          }
        }
        .language-mobile {
          order: 6;
          height: 1rem;
          width: 100%;
          top: 5.9rem;
          display: flex;
          align-items: center;
          justify-content: space-between;
          .cell {
            display: flex;
            align-items: center;
            padding: 0.12rem;
            border: 0.01rem solid #39387b;
            border-radius: 0.28rem;
            img {
              width: 0.32rem;
            }
            span {
              margin-left: 0.04rem;
            }
          }
          .cell.focus {
            border-color: #64e1f5;
          }
        }
      }
      .language {
        display: none;
      }
    }
  }

  .my-nav .inner .logo img {
    width: 1.6 * 1.35rem;
    height: 0.31 * 1.35rem;
    margin: 0 auto;
  }
  .invite-prize-block .mobileBtn {
    padding: 0;
  }
  .invite-prize-mobile {
    padding: 10px 0;
    margin: 0 auto;
    width: 2.6rem;
    cursor: pointer;
    &:hover {
      color: #ffd200;
    }
  }
  .invite-prize-mobile2 {
    padding: 10px 0;
    margin: 0 auto;
    width: 2rem;
    cursor: pointer;
    &:hover {
      color: #ffd200;
    }
  }
}
</style>

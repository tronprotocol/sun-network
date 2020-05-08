
<template>
  <div>
    <div class="my-nav  pc_none">
      <div class="inner">
        <a
          class="logo"
          href="javascript:window.location.reload();"
        >
          <img :src="require('../assets/images/logo.png')">
        </a>
        <div class="nav">
          <!-- <a
            class="test nav-menu-item"
            type="text"
            href="https://tron.network/sunnetwork/doc"
            target="_blank"
            style="text-decoration: none;"
          >{{$t('ApplyTestCoin')}}</a> -->

          <!-- <el-button
            class="deposit nav-menu-item"
            type="text"
            @click="depositTrx()"
          >{{$t('DepositText')}}</el-button>

          <el-button
            class="withdraw nav-menu-item"
            type="text"
            @click="withdrawTrx()"
          >{{$t('WithdrawText')}}</el-button> -->

          <!-- 游戏介绍 -->
          <el-dropdown
            @command="handleCommand"
            class="nav-menu-item"
          >
            <span>
              {{$t('indruction')}}
              <i class="el-icon-arrow-down el-icon--right"></i>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="dialogHow">{{$t('HowToPlay')}}</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
          <!-- 怎么玩 -->
          <how-to-play
            :dialogHow="dialogHow"
            @dialogHandler="dialogHandler"
            v-if="dialogHow"
          ></how-to-play>
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
            class="how nav-menu-item"
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
              <img :src="require('../assets/images/'+locale+'.png')">
            </span>
            <div class="group">
              <div
                v-for="(item,index) of languageGroup"
                class="item"
                @click="location(item.lng)"
                :key="index"
              >
                <span style="display: flex;">
                  <img :src="require('../assets/images/'+index+'.png')">
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
      width="5.8rem"
      custom-class="how-dialog play-dialog">
      <p style="padding-bottom:20px;" v-html="loginWallet"></p>
    </el-dialog>
    <!-- <deposit-trx-dg
      :params="depositTrxDgParams"
      v-if="depositTrxDgParams.show"
    ></deposit-trx-dg>
    <withdraw-trx-dg
      :params="withdrawTrxDgParams"
      v-if="withdrawTrxDgParams.show"
    ></withdraw-trx-dg> -->
   <!-- <deposit-trc10-dg
      :params="depositTrc10DgParams"
      v-if="depositTrc10DgParams.show"
    ></deposit-trc10-dg>
    <deposit-trc20-dg
      :params="depositTrc20DgParams"
      v-if="depositTrc20DgParams.show"
    ></deposit-trc20-dg>
    <deposit-trc721-dg
      :params="depositTrc721DgParams"
      v-if="depositTrc721DgParams.show"
    ></deposit-trc721-dg> -->

    
  </div>
</template>

<script>
import LoginDg from './dialog/login';
import DepositTrxDg from './dialog/depositTrx';
import WithdrawTrxDg from './dialog/depositTrx';
import DepositTrc10Dg from './dialog/depositTrc10';
import DepositTrc20Dg from './dialog/depositTrc';
import DepositTrc721Dg from './dialog/depositTrc';
import { getBalance, getaccount } from "~/assets/js/common";
import interfaceData from '../api/config';

import { mapState } from "vuex";
import Invite from "./Invite";
/* 幸运抽奖 */
import LuckyDraw from "./LuckDraw.vue";
import HowToPlay from "./howToPlay";
import DiceToken from "./DiceToken";
import RoadMap from "./RoadMap";
import Login from "./Login";
import InvitePrize from "./InvitePrize";
import Account from "./Account";
import CountDown from "./CountDown.vue";
import Dividend from "./Dividend";
import bus from "~/assets/js/bus";
import FairPlay from "./FairPlay";

export default {
  name: "MyNav",
  components: {
    LoginDg,
    DepositTrxDg,
    WithdrawTrxDg,
    DepositTrc10Dg,
    DepositTrc20Dg,
    DepositTrc721Dg,
    LuckyDraw,
    Invite,
    HowToPlay,
    DiceToken,
    RoadMap,
    Login,
    InvitePrize,
    Account,
    CountDown,
    Dividend,
    FairPlay
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
      depositTrc10DgParams: {
        show: false
      },
      depositTrc20DgParams: {
        show: false,
        title: 'Deposit TRC20'
      }, 
      depositTrc721DgParams: {
        show: false,
        title: 'Deposit TRC721'
      },
      intervalBalance: null,
      txt: "",
      icon: "",
      dialogHow: false,
      dialogFairness: false,
      luckyList: [
        { area: "0-100", prize: 0.2 },
        { area: "101-999", prize: 2 },
        { area: "1000-4999", prize: 20 },
        { area: "5000-9999", prize: 200 },
        { area: "10000", prize: 1000 }
      ],
      inviteDialog: {
        visible: false,
        data: {
          prize: "",
          table: []
        }
      },
      drawDialog: false,
      showMenu: false,
      languageGroup: {
        en: { lng: "en", txt: "English", img: "../assets/images/en.png" },
        ch: { lng: "ch", txt: "简体中文", img: "../assets/images/ch.png" }
      },
      diceTokenVisible: false,
      roadMapVisible: false,
      InviteVisible: false,
      bandWidthRate: 0,
      energeRate: 0,
      bandColor: "",
      eneryColor: "",
      countDownVisible: false,
      isAndroid: false,
      dividingPoolVisible: false
    };
  },
  created() {
   
  },
  watch: {
    // address: {
    //   deep: true,
    //   handler(val) {
    //     this.getData();
    //   }
    // }
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
      "platForm",
      "balance",
      "account",
      "trx20Account",
      "mBalance",
      "loginState"
    ]),
    dice() {
      let asset = this.account ? this.account.asset : [];
      let data = 0;
      if (asset && asset.length && asset.length > 0) {
        data = asset.filter((item, index) => {
          return item.key === "dice";
        });
      }

      return data[0];
    }
  },
  methods: {
    async login() {
      this.loginWallet = this.$t('loginWallet');

      this.dialogVisible = true;
      // let self = this;
      // this.loginDgParams = {
      //   show: true,
      //   confirm: async (privateKey) => {
      //     self.globalSunWeb.mainchain.setPrivateKey(privateKey.privateKey);
      //     self.globalSunWeb.sidechain.setPrivateKey(privateKey.privateKey);
      //     // window.sunWeb = self.globalSunWeb; /////
      //     self.$store.commit('SET_SUNWEB', self.globalSunWeb);
      //     self.$store.commit('SET_LOGINSTATE', true);
      //     self.getBalance();
      //   }
      // }
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
      if (!this.loginState) {
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
     if (!this.loginState) {
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
            this.$message({
              type: "error",
              message: ex.message || 'error',
              showClose: true
            });
          })
        }
      }
    },
    depositTrc10() {
      const self = this;
      this.depositTrc10DgParams = {
        show: true,
        confirm: (p) => {
          const num = self.globalSunWeb.mainchain.toSun(p.num);
          const feeLimit = self.globalSunWeb.mainchain.toSun(p.feeLimit);
          self.globalSunWeb.depositTrc10(p.tokenId, num, feeLimit).then(txId => {
            console.log(txId)
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
    depositTrc20() {
      const self = this;
      this.depositTrc20DgParams = {
        show: true,
        title: 'Deposit TRC20',
        confirm: (p) => {
          console.log(p);
          const num = self.globalSunWeb.mainchain.toSun(p.num);
          const feeLimit = self.globalSunWeb.mainchain.toSun(p.feeLimit);
          self.globalSunWeb.depositTrc20(num, feeLimit, p.contractAddress).then(txId => {
            console.log(txId)
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
    depositTrc721() {
      const self = this;
      this.depositTrc721DgParams = {
        show: true,
        title: 'Deposit TRC721',
        confirm: (p) => {
          console.log(p);
          const num = self.globalSunWeb.mainchain.toSun(p.num);
          const feeLimit = self.globalSunWeb.mainchain.toSun(p.feeLimit);
          self.globalSunWeb.depositTrc721(num, feeLimit, p.contractAddress).then(txId => {
            console.log(txId)
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
    deposit(type) {
      if (!this.address.base58) {
        this.$message({
          type: "success",
          message: this.$t("noLogin"),
          showClose: true
        });
        return;
      }
      const self = this;
      switch(type) {
        case 'TRX':
          self.depositTrx();
          return;
        case 'TRC10': 
          self.depositTrc10();
          return;
        case 'TRC20':
          self.depositTrc20();
          return;
        case 'TRC721': 
          self.depositTrc721();
          return;
        default:
          return;
      }
    },
    location(lng) {
      this.showMenu = false;
      let from = sessionStorage.getItem("fromAddress");
      let url = "";

      if (lng === "en") {
        window.location = "/static/" + url;
      } else {
        window.location = "/static/" + lng + url;
      }
    },
    showMenus() {
      this.showMenu = !this.showMenu;
    },
    dialogHandler(val) {
      if (val.key === "dialogLogin") {
        this.$store.commit("SET_DIALOG_LOGIN", val.value);
        return;
      }
      this.$data[val.key] = val.value;
    },
    /**
     * 账号地址更新，重新获取数据
     */
    async getData() {
      if (!this.address.base58) {
        return;
      }
      let balance = await getBalance(this.address.hex);
      // let account = await getaccount(this.address.hex);
      this.$store.commit("SET_BALANCE", this.globalSunWeb.mainchain.fromSun(balance));
      this.$store.commit("SET_ACCOUNT", account);
    },
    setColor(value) {
      let color = "";
      if (value > 0 && value <= 50) {
        color = "#59d321";
      } else if (value > 50 && value <= 80) {
        color = "#f69f3d";
      } else {
        color = "#e52e2e";
      }

      return color;
    },
    /**
     * dropdown下拉，点击菜单项触发的事件回调
     */
    handleCommand(command) {
      if (command == "whitePaper") {
        let url = this.isAndroid
          ? "/doc/trondice_" + this.locale + ".jpg"
          : "/doc/trondice_" + this.locale + ".pdf";
        window.open(url, "_blank");
        return;
      }
      this.$data[command] = true;
    },
    // checkMobile() {
    //   let u = navigator.userAgent;
    //   let isAndroid = u.indexOf("Android") > -1 || u.indexOf("Adr") > -1; //android终端
    //   this.isAndroid = isAndroid;
    // }
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
  color: #fff;
  padding-right: 0.3rem;
  cursor: pointer;
  font-weight: 500;
  font-size: 0.16rem;
  &:hover {
    color: #ffbf58;
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
  height: 0.7rem;
  background: rgba(0, 0, 0, 0.5);

  .img-nav {
    .iconfont {
      font-size: 0.3rem;
    }
  }
  .logo {
    display: flex;
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
      .language {
        cursor: pointer;
        position: relative;
        z-index: 10;
        height: 100%;
        padding-right: 20px;
        display: flex;
        align-items: center;
        transition: opacity 0.2s ease-in-out;
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
          width: 0.56rem;

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

  .mobileNav {
    // text-align: center;
    position: absolute;
    transition: height 1s;
    top: 1rem;
    background: rgba(0, 0, 0, 0.8);
    width: 100%;
    z-index: 10000;
    .icons {
      text-align: center;
      height: 0.8rem;
      line-height: 0.8rem;
      a {
        color: #fff;
        &:first-child {
          margin-right: 0.2rem;
        }
      }
    }

    .el-button--text {
      margin: 0 auto;
    }
    .m-language {
      text-align: center;
      width: 100%;
      font-size: 14px;
      padding: 10px;
    }
    .active {
      color: #e7b01a;
    }
    .mobileBtn {
      display: block;
      font-size: 14px;
      color: #fff;
      padding: 10px 20px;
    }
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
      // .logo{
      //   img{
      //     width:1.5rem;

      //   }
      // }
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

<template>
  <!-- 推荐有奖 -->
  <el-dialog
    :title="$t('InviteLucky')"
    :visible.sync="dialog"
    width="5.88rem"
    custom-class="how-dialog invite-dialog"
    @close="dialogHandler"
  >
    <p
      v-html="$t('Recommended.title')"
      class="title"
    ></p>
    <div class="desc">
      <p v-html="$t('Recommended.desc')"></p>
      <a
        :href="url"
        style="margin-top:0.1rem;display:inline-block;color:#fff16b;word-break: break-all;"
      >{{url}}</a>
      <el-button
        class="copyButton copyContent"
        @click="copy"
        :data-clipboard-text="`${$t('Recommended.desc')}\n${url}`"
      >{{$t('Recommended.copyBtn')}}</el-button>
    </div>
    <div class="prize">
      {{$t('Recommended.prize')}}
      <el-tooltip
        class="item"
        effect="dark"
        placement="right"
      >
        <div slot="content">
          <div class="recommended">
            <p
              v-html="item"
              class="recommend"
              v-for="(item,index) in $t('Recommended.rule')"
              :key="index"
              style="line-height:0.2rem;"
            ></p>
          </div>
        </div>
        <i class="el-icon-question"></i>
      </el-tooltip>
    </div>
    <!-- 不可邀请文字说明 -->
    <div class="withDraw">
      <div class="num bg">
        <i class="iconfont icon-renshu"></i>
        {{inviteNum}}
      </div>
      <div class="left leftDice bg">
        {{(diceAmount | toLocaleString)}} dice
      </div>
      <!-- 不可点击状态 -->
      <el-button
        class="withDrawBtn"
        @click="widthDraw(1)"
        :loading="widthDrawLoading"
      >{{$t('Recommended.widthDraw')}}</el-button>
    </div>
    <div
      class="withDraw"
      v-if="amount > 0"
    >
      <div class="left bg">
        <i class="iconfont icon-logo"></i>
        <!-- <img src="../assets/images/tron.png" class="iconfont icon-logo"> -->
        {{(amount/Math.pow(10,6) | toLocaleString)}} TRX
      </div>
      <!-- 不可点击状态 -->
      <el-button
        class="withDrawBtn"
        @click="widthDraw(0)"
        :loading="widthDrawLoading"
      >{{$t('Recommended.widthDraw')}}</el-button>
    </div>
    <span
      slot="footer"
      class="dialog-footer"
      style="text-align:center"
    >
      <el-button
        type="primary"
        @click="dialogHandler"
      >{{$t('Confirm')}}</el-button>
      <p style="text-align:center;font-size:12px;margin-top:0.2rem;">{{$t('Recommended.explanation')}}</p>
    </span>
  </el-dialog>
</template>
<script>
import { mapState } from "vuex";
import Clipboard from "clipboard";
import {
  getInviteAmount,
  extractInviteReturn,
  getInviteReturnList
} from "~/api/user";
import bus from "~/assets/js/bus";
export default {
  props: {
    InviteVisible: {
      default: Boolean
    }
  },
  data() {
    return {
      dialog: false,
      url: "",
      amount: 0,
      widthDrawLoading: false,
      inviteNum: 0,
      diceAmount: ""
    };
  },
  mounted() {
    this.dialog = this.InviteVisible;
    this.url = location.origin;

    if (this.address && this.address.base58) {
      let url = `${this.url}?from=${this.address.base58}`;
      this.url = url;
    }

    this.getData();
    this.getList();
  },
  computed: {
    ...mapState(["address", "dapp", "contractAddress"])
  },
  watch: {
    address: {
      deep: true,
      handler(val) {
        if (val && val.base58) {
          let address = val || {};
          let url = `${this.url}?from=${val.base58}`;
          this.url = url;
        }
      }
    }
  },
  methods: {
    dialogHandler() {
      this.dialog = false;
      let obj = {
        key: "InviteVisible",
        value: this.dialog
      };
      this.$emit("dialogHandler", obj);
    },
    copy() {
      let clipboard = new Clipboard(".copyContent");
      clipboard.on("success", e => {
        this.$message({
          showClose: true,
          message: "Copy success",
          type: "success"
        });

        clipboard.destroy();
      });

      clipboard.on("error", e => {
        this.$message.success("该浏览器不支持自动复制");
        // 释放内存
        clipboard.destroy();
      });
    },
    /**
     * 获取邀请获得的返现，包括trx和dice
     * 0 trx  1 dice
     */
    getData() {
      let trx = {
        dapp_id: this.dapp,
        user_address: this.address.base58,
        cur_type: 0
      };

      getInviteAmount(trx)
        .then(response => {
          this.amount = response;
        })
        .catch(err => {
          console.log(err);
        });

      let dice = {
        dapp_id: this.dapp,
        user_address: this.address.base58,
        cur_type: 1
      };
      getInviteAmount(dice)
        .then(response => {
          this.diceAmount = response;
        })
        .catch(err => {
          console.log(err);
        });
    },
    /**
     * 提取邀请奖励中的dice/trx
     */
    widthDraw(curType) {
      let amount = curType === 0 ? this.amount : this.diceAmount;

      if (amount <= 0) {
        this.$message({
          type: "success",
          message: this.$t("Recommended.noDice"),
          showClose: true
        });
        return;
      }

      let obj = {
        dapp_id: this.dapp,
        user_address: this.address.base58,
        cur_type: curType
      };
      extractInviteReturn(obj)
        .then(response => {
          this.$message({
            type: "success",
            message: this.$t("Recommended.success"),
            showClose: true
          });

          if (curType == 0) {
            bus.$emit("changeBalance");
            this.amount = 0;
          } else {
            bus.$emit("widthDrawDice");
            this.diceAmount = 0;
          }
        })
        .catch(err => {
          this.widthDrawLoading = false;
        });
    },
    /**
     * 获取邀请的总人数(邀请好友列表)
     */
    getList() {
      let obj = {
        dapp_id: this.dapp,
        user_address: this.address.base58,
        start: 1,
        limit: 20
      };
      getInviteReturnList(obj)
        .then(response => {
          this.inviteNum = response.total;
        })
        .catch(err => {
          this.inviteNum = 0;
        });
    }
  }
};
</script>
<style lang="scss">
.invite-dialog {
  .recommended {
    width: 4rem;
  }
  .number {
    color: #ffd200 !important;
  }
  .title {
    margin-bottom: 0.1rem;
  }
  .desc {
    width: 100%;
    margin: 0 auto;
    background: #542099;
    border: 0.02rem solid #af47d2;
    box-shadow: inset 0 3px 14px 0 rgba(0, 0, 0, 0.6);
    padding: 0.15rem;
  }
  .url {
    a {
      color: #ffd200;
      text-decoration: none;
    }
  }
  .copyButton {
    background-image: linear-gradient(
      -180deg,
      #fd8acb 0%,
      rgba(174, 86, 245, 0.8) 100%
    );
    border: 0.01rem solid #900eb0;
    box-shadow: 2px 2px 5px 3px rgba(54, 35, 108, 0.32);
    border-radius: 0.03rem;
    width: 1.78rem;
    height: 0.44rem;
    display: block;
    padding: 0;
    margin: 0.1rem auto 0;
    font-size: 20px;
    color: #fff;
  }
  .el-icon-question {
    color: rgba(217, 214, 255, 0.5);
  }
  .prize {
    padding: 0.2rem 0;
    font-size: 20px;
    color: #ffffff;
  }
  .withDraw {
    display: flex;
    &:last-child {
      margin-top: 0.1rem;
    }
    .left {
      flex: 1;
      position: relative;
    }
    .leftDice:before {
      content: "";
      width: 0.27rem;
      height: 0.27rem;
      // height: 100%;
      position: absolute;
      left: 0.1rem;
      top: 0;
      bottom: 0;
      margin: auto;
      background: url("../assets/images/tron.png") no-repeat;
      background-size: 100%;
    }
    .bg {
      border-radius: 0.03rem;
      height: 0.54rem;
      background-position: 0.1rem;
      line-height: 0.54rem;
      font-size: 0.2rem;
      margin-right: 0.1rem;
      background-color: rgba(7, 8, 78, 0.42);
      text-align: right;
      padding: 0 0.1rem;
      i {
        font-size: 0.2rem;
        margin-right: 0.1rem;
        float: left;
      }
    }
    .num {
      width: 1rem;
    }
    .withDrawBtn {
      background-image: linear-gradient(
        -180deg,
        #ff53b4 0%,
        rgba(138, 19, 233, 0.42) 100%
      );
      border: 1px solid #900eb0;
      box-shadow: 2px 2px 5px 3px rgba(54, 35, 108, 0.32);
      border-radius: 0.03rem;
      // width: 0.98rem;
      height: 0.54rem;
      font-size: 20px;
      color: #ffffff;
    }
  }

  @media screen and (max-width: 1024px) {
    .withDraw {
      height: 0.8rem;
      line-height: 0.8rem;

      .bg {
        height: 100%;
        line-height: 0.8rem;
        font-size: 14px;
        .iconfont {
          font-size: 14px;
        }
      }

      .left {
        background-size: 0.3rem 0.3rem;
      }
      .withDrawBtn {
        font-size: 12px;
        padding: 0 0.1rem;
        height: 0.8rem;
        line-height: 0.8rem;
        width: 1rem;
      }
    }
    .copyButton {
      font-size: 12px;
      line-height: 0.6rem;
      height: 0.6rem;
    }
    .el-button--primary {
      font-size: 12px;
    }
    .prize {
      font-size: 14px;
    }
  }
}
</style>

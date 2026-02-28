<!--
 * @Description: pledge 质押
 * @Author: weiqinl
 * @Date: 2019-01-17 15:23:14
 * @LastEditors: weiqinl
 * @LastEditTime: 2019-02-15 19:44:00
 -->
<template>
  <div class="pledge-block">
    <div class="pledge-info">
      <!-- dice质押总量 -->
      <!-- <div class="dice-pledge-num">
          <span>dice质押总量：</span>
          <span>100,000,000</span>
        </div> -->
      <!-- <div class="dice-pledge-num">
          <span>解除质押：我的dice质押总额：</span>
          <span>{{freezePledgeNum}}</span>
        </div> -->
    </div>
    <div class="pledge-opration">
      <!-- 质押 -->
      <div class="pledge-opration-item">
        <div class="pledge-num">
          <span>{{$t('DividingPool.title')}}：</span> {{$t('DividingPool.pledge')}}<span>{{unFreezePledgeNum}} dice</span>
        </div>
        <div class="pledge-line">
          <div class="pledge-input">
            <input
              v-model="pledgeNum"
              type="text"
              class="pledge-input-block"
            >
            <div
              class="pledge-max"
              @click="pledgeMax"
            >MAX</div>
            <el-button
              class="pledge-button"
              @click="pledge"
            >{{$t('DividingPool.title')}}</el-button>
          </div>

        </div>
      </div>
      <!-- 解押 -->
      <div class="pledge-opration-item">
        <div class="pledge-num">
          <span>{{$t('DividingPool.unfreeze')}}：</span> {{$t('DividingPool.unfreezeInfo')}}<span>{{freezePledgeNum}} dice</span>
        </div>
        <div class="pledge-line">
          <div class="pledge-input">
            <input
              readonly
              v-model="unfreezeNum"
              type="text"
              class="pledge-input-block"
            >
            <div
              class="pledge-max"
              @click="unFreezeMax"
            >MAX</div>
            <el-button
              class="pledge-button"
              @click="affirmMethods"
            >{{$t('DividingPool.unfreezeBtn')}}</el-button>
          </div>
        </div>
        <p class="spaceLine">{{$t('DividingPool.note')}}：{{$t('DividingPool.unfreezeTip')}}</p>
      </div>
      <!-- 提现 -->
      <div class="pledge-opration-item">
        <div class="pledge-num">
          <span>{{$t('DividingPool.withdraw')}}：</span> {{$t('DividingPool.withdrawInfo')}}<span>{{lockBalance}} dice</span>
        </div>
        <div class="pledge-line">
          <div class="pledge-input">
            <input
              readonly
              v-model="withdrawNum"
              type="text"
              class="pledge-input-block"
            >
            <div
              class="pledge-max"
              @click="withdrawMax"
            >MAX</div>
            <el-button
              v-if="withdrawStatus"
              class="pledge-button"
              @click="withdraw"
            >{{$t('DividingPool.withdraw')}}</el-button>
            <!-- 不可提取 -->
            <div
              v-else
              class="pledge-button-gray"
            >{{hr}}:{{min}}</div>
          </div>
        </div>
        <p class="spaceLine">{{$t('DividingPool.note')}}: {{$t('DividingPool.withdrawTip')}}</p>
      </div>
    </div>

    <msgbox
      v-if="showMsg"
      @click="unFreeze"
      @cancel="showMsgBlock"
    />
  </div>
</template>
<script>
import msgbox from "@/components//msgbox";
import { mapState } from "vuex";
export default {
  components: {
    msgbox
  },
  data() {
    return {
      unFreezePledgeNum: 0,
      freezePledgeNum: 0,
      lockBalance: 0,
      pledgeNum: 0,
      unfreezeNum: 0,
      withdrawNum: 0,
      withdrawStatus: true,
      showMsg: false
    };
  },
  computed: {
    ...mapState(["trx20AccountInfo", "address"])
  },
  mounted() {
    if (this.trx20AccountInfo.balanceOf) {
      this.setDiceBanlance(); // 获取质押数量
    }
  },
  methods: {
    showMsgBlock() {
      this.showMsg = false;
    },
    affirmMethods() {
      this.showMsg = true;
    },
    withdrawMax() {
      this.withdrawNum = this.lockBalance;
    },
    /**
     * 获取质押数量
     */
    async setDiceBanlance() {
      //
      let unFreezePledgeNum = await this.trx20AccountInfo
        .balanceOf(this.address.base58)
        .call();
      let freezePledgeNum = await this.trx20AccountInfo
        .freezeBalance(this.address.base58)
        .call();
      let lockBalance = await this.trx20AccountInfo
        .lockBalance(this.address.base58)
        .call();
      this.freezePledgeNum = freezePledgeNum.toString() / Math.pow(10, 6);
      this.unFreezePledgeNum =
        unFreezePledgeNum.balance.toString() / Math.pow(10, 6);
      this.lockBalance = lockBalance.toString() / Math.pow(10, 6);
      this.pledgeNum = this.unFreezePledgeNum;
      this.unfreezeNum = this.freezePledgeNum;
      this.withdrawNum = this.lockBalance;
    },
    /**
     * 合约的质押事件
     * 1: 调用合约的freeze方法，生成hash值，即交易ID
     * 2: 根据交易ID获取交易事件，来判断是否成功
     */
    async pledge() {
      if (this.pledgeNum > 0 && this.pledgeNum <= this.unFreezePledgeNum) {
        let hash = await this.trx20AccountInfo
          .freeze(this.pledgeNum * Math.pow(10, 6))
          .send();
        this.$message({
          type: "success",
          message: this.$t("DividingPool.sendInfo"),
          showClose: true
        });

        let _this = this;
        let timer = setInterval(async () => {
          let tractionInfo = await tronWeb.getEventByTransactionID(hash);
          //
          if (tractionInfo.length != 0) {
            _this.setDiceBanlance();
            _this.pledgeNum = this.unFreezePledgeNum;
            clearInterval(timer);
          }
        }, 3000);
      } else if (this.pledgeNum > this.unFreezePledgeNum) {
        this.$message({
          type: "warning",
          message: this.$t("DividingPool.minInfo"),
          showClose: true
        });
      } else {
        this.$message({
          type: "warning",
          message: this.$t("DividingPool.numInfo"),
          showClose: true
        });
      }
    },
    pledgeMax() {
      this.pledgeNum = this.unFreezePledgeNum;
    },

    /**
     * 合约的解押事件
     * 1: 调用合约的unfreeze方法，生成hash值，即交易ID
     * 2: 根据交易ID获取交易事件，来判断是否成功
     */
    async unFreeze() {
      if (this.unfreezeNum > 0 && this.unfreezeNum <= this.freezePledgeNum) {
        this.showMsgBlock();
        let hash = await this.trx20AccountInfo
          .unfreeze(this.unfreezeNum * Math.pow(10, 6))
          .send();
        this.$message({
          type: "success",
          message: this.$t("DividingPool.sendInfo"),
          showClose: true
        });

        let _this = this;
        let timer = setInterval(async () => {
          let tractionInfo = await tronWeb.getEventByTransactionID(hash);
          if (tractionInfo.length != 0) {
            _this.setDiceBanlance();
            _this.unfreezeNum = this.freezePledgeNum;
            clearInterval(timer);
            _this.getWithdrawStatus();
          }
        }, 3000);
      } else if (this.unfreezeNum > this.freezePledgeNum) {
        this.$message({
          type: "warning",
          message: this.$t("DividingPool.minInfo"),
          showClose: true
        });
      } else {
        this.$message({
          type: "warning",
          message: this.$t("DividingPool.numInfo"),
          showClose: true
        });
      }
    },
    unFreezeMax() {
      this.unfreezeNum = this.freezePledgeNum;
    },
    /**
     * 提取事件
     * 1： 通过unlock事件，调用合约提取事件
     * 2:  根据交易ID获取交易事件，来判断是否成功
     */
    async withdraw() {
      if (this.withdrawNum > 0 && this.withdrawNum <= this.lockBalance) {
        let hash = await this.trx20AccountInfo
          .unlock(this.withdrawNum * Math.pow(10, 6))
          .send();
        this.$message({
          type: "success",
          message: this.$t("DividingPool.sendInfo"),
          showClose: true
        });

        let _this = this;
        let timer = setInterval(async () => {
          let tractionInfo = await tronWeb.getEventByTransactionID(hash);

          if (tractionInfo.length != 0) {
            _this.setDiceBanlance();
            _this.withdrawNum = this.lockBalance;
            clearInterval(timer);
          }
        }, 3000);
      } else if (this.withdrawNum > this.lockBalance) {
        this.$message({
          type: "warning",
          message: this.$t("DividingPool.minInfo"),
          showClose: true
        });
      } else {
        this.$message({
          type: "warning",
          message: this.$t("DividingPool.numInfo"),
          showClose: true
        });
      }
    }
  }
};
</script>


<style lang="scss">
.pledge-block {
  .pledge-info {
    padding-top: 0.32rem;
    span {
      display: inline-block;
      font-size: 0.18rem;
    }
    .dice-pledge-num {
      margin-bottom: 0.14rem;
      span:nth-child(2) {
        font-size: 0.22rem;
        font-weight: bold;
      }
    }
  }
  .pledge-opration {
    background: rgba(29, 7, 78, 0.22);
    border-radius: 3px;
    width: 100%;
    height: auto;
    padding: 0.26rem 0.2rem 0.01rem 0.2rem;
    .pledge-num {
      text-align: left;
      font-size: 0.18rem;
      span {
        margin-left: 0.1rem;
        font-size: 0.18rem;
        color: #fff16b;
        font-weight: bold;
      }
    }
    .pledge-line {
      width: 100%;
      height: 0.56rem;
      margin-top: 0.2rem;
      .el-button {
        padding: 0;
      }
    }
    .pledge-input {
      width: 100%;
      height: 100%;

      position: relative;
      float: left;
    }
    .pledge-input::before {
      content: "";
      width: 0.6rem;
      height: 0.56rem;
      position: absolute;
      background-image: url("/images/dividend-logo.png");
      background-repeat: no-repeat;
      background-position: 0.12rem center;
      background-size: auto 60%;
      left: 0;
    }
    .pledge-input-block {
      width: 3.6rem;
      height: 100%;
      background: rgba(7, 8, 78, 0.32);
      // background-color: transparent;
      border: none;
      outline: none;
      color: #ffffff;
      font-size: 0.2rem;
      text-align: right;
      padding-right: 0.5rem;
      float: left;
    }
    .pledge-input::after {
      content: "dice";
      position: absolute;
      width: 0.5rem;
      height: 100%;
      right: 1.9rem;
      top: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 0.18rem;
    }
    .pledge-button {
      background-image: linear-gradient(-180deg, #f64eb5 0%, #7427ca 100%);
      border: 1px solid #900eb0;
      border-radius: 3px;
      width: 1.1rem;
      color: #ffffff;
      float: right;
      height: 0.56rem;
      font-size: 0.18rem;
    }
    .pledge-button-gray {
      background-image: linear-gradient(-180deg, #dcc4d3 0%, #897f93 100%);
      border-radius: 3px;
      width: 1rem;
      color: #ffffff;
      float: right;
      height: 0.56rem;
      font-size: 0.18rem;
      text-align: center;
      line-height: 0.56rem;
    }
    .pledge-max {
      width: 0.64rem;
      height: 100%;
      background: rgba(7, 8, 78, 0.32);
      border-radius: 3px;
      font-size: 0.18rem;
      text-align: center;
      line-height: 0.56rem;
      float: left;
      margin: 0 0.06rem;
      cursor: pointer;
      color: rgba(255, 255, 255, 0.8);
    }
    .pledge-max:hover {
      color: #ffffff;
    }
  }
}
</style>

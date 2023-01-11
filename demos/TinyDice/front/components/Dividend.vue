<template>
  <!-- 倒计时 -->
  <el-dialog
    :visible.sync="dialog"
    width="6.5rem"
    custom-class="how-dialog dividend-dialog"
    @close="dialogHandler"
  >
    <!-- :title="$t('DividingPool.name')" -->
    <div
      slot="title"
      class="dividend-title"
    >
      <div
        class="dividend-title-item"
        @click="selectTab(1)"
        :class="{ dividendTitleItemActive: dividendStatus }"
      >
        {{$t('DividingPool.name')}}
        <span
          v-if="dividendStatus"
          class="dividend-title-item-line"
        ></span>
      </div>
      <div
        class="dividend-title-item"
        @click="selectTab(2)"
        :class="{ dividendTitleItemActive: pledgeStatus }"
      >
        {{$t('DividingPool.title')}}
        <span
          v-if="pledgeStatus"
          class="dividend-title-item-line"
        ></span>
      </div>
      <div
        class="dividend-title-item"
        @click="selectTab(3)"
        :class="{ dividendTitleItemActive: bonusStatus }"
      >
        {{$t('DividingPool.bonus')}}
        <span
          v-if="bonusStatus"
          class="dividend-title-item-line"
        ></span>
      </div>
    </div>
    <!-- 分红池 -->

    <dividend-pool v-if="dividendStatus" />
    <!-- 质押 -->
    <pledge v-if="pledgeStatus" />
    <!-- 分红记录 -->
    <bonus v-if="bonusStatus" />
    <!-- footer -->
    <span
      slot="footer"
      class="dialog-footer"
    >
      <el-button
        class="dialog-button"
        type="primary"
        @click="dialogHandler"
      >{{$t('Confirm')}}</el-button>
    </span>
  </el-dialog>
</template>
<script>
import DividendPool from "@/components/dividend/DividendPool.vue";
import Bonus from "@/components/dividend/Bonus";
import Pledge from "@/components/dividend/Pledge";

export default {
  props: {
    dividingPoolVisible: {
      default: Boolean
    }
  },
  components: {
    DividendPool,
    Bonus,
    Pledge
  },
  data() {
    return {
      dialog: false,
      dividendStatus: true,
      pledgeStatus: false,
      bonusStatus: false,
      animateId: null
    };
  },
  mounted() {
    this.dialog = this.dividingPoolVisible;
  },
  methods: {
    /**
     * 关闭弹窗的回调函数
     */
    dialogHandler() {
      this.dialog = false;
      let obj = {
        key: "dividingPoolVisible",
        value: this.dialog
      };
      this.cancalAm(this.animateId);
      this.$emit("dialogHandler", obj);
    },
    /**
     * 取消动画
     */
    cancalAm(animateId) {
      window.cancelAnimationFrame(animateId);
    },
    /**
     * Tab标签的选择
     */
    selectTab(index) {
      if (index === 1) {
        this.dividendStatus = true;
        this.pledgeStatus = false;
        this.bonusStatus = false;
      } else if (index === 2) {
        this.dividendStatus = false;
        this.pledgeStatus = true;
        this.bonusStatus = false;
      } else if (index === 3) {
        this.bonusStatus = true;
        this.dividendStatus = false;
        this.pledgeStatus = false;
      }
    }
  }
};
</script>
<style lang="scss">
.clearfix:after {
  display: block;
  content: "";
  clear: both;
}

.clearfix {
  zoom: 1;
}
.dividend-dialog {
  .spaceLine {
    margin-bottom: 0.1rem;
    text-align: left;
  }
  .spacePledge {
    margin-top: 0.2rem;
  }
  .pledge-opration-item {
    margin-bottom: 0.5rem;
  }
  .dividend-title {
    width: 100%;
    border-bottom: 1px solid #db89f7;
    height: 0.55rem;
    padding-top: 0.1rem;
  }
  .el-dialog__header {
    padding: 0;
    text-align: left;
  }
  .dividend-title-item {
    display: inline-block;
    padding: 0 0.3rem;
    border-right: 1px solid #db89f7;
    text-align: center;
    font-size: 0.18rem;
    cursor: pointer;
    position: relative;
  }
  .dividendTitleItemActive {
    color: #fff16b;
  }
  .dividend-title-item-line {
    position: absolute;
    background: #fff16b;
    width: 0.38rem;
    height: 3px;
    left: 31%;
    bottom: -0.14rem;
  }
  .block-title {
    font-size: 0.26rem;
    color: #fff16b;
    text-align: center;
    font-weight: 500;
    margin: 0.2rem 0 0.15rem 0;
  }
  .dividend-info {
    width: 100%;
    min-height: 2rem;
    // margin-bottom: 0.3rem;
  }
  .dividen-info-show {
    float: left;
    text-align: left;
    max-width: 4rem;
    // margin-left: 0.5rem;
    padding-left: 0.5rem;
    padding-top: 0.1rem;
    span {
      display: block;
      line-height: 0.24rem;
    }
    span:nth-child(1) {
      font-size: 0.18rem;
    }
    .specWord {
      font-size: 0.22rem;
      color: #fff16b;
      font-weight: bold;
      margin: 0.1rem 0;
    }
  }
  .content {
    width: 180px;
    height: 180px;
    position: relative;
    // 左侧展示信息
    margin-left: 0.4rem;
    float: left;
    margin: 0.1rem auto 0.2rem;
    margin-top: 0.2rem;
    margin-left: 0.2rem;
    .bg {
      // width: 100%;
      // height: 100%;
      // background: url("../assets/images/circle.png") no-repeat 100% 100%;
      position: absolute;
      top: 0;
      left: 0;
      text-align: center;
      background: rgba(255, 255, 255, 0.1);
      box-shadow: 0 1px 6px 2px rgba(0, 0, 0, 0.2),
        inset 0 1px 8px 0 rgba(255, 255, 255, 0.5);
      width: 100%;
      height: 100%;
      border-radius: 50%;

      .num {
        // margin: 0.5rem 0;
        font-size: 0.22rem;
        text-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
        color: #fff;
        font-family: Arial-Black;
        line-height: 0.3rem;
        height: 0.6rem;
        width: 100%;
        position: absolute;
        top: 0;
        bottom: 0;
        margin: auto;
        text-align: center;
      }
    }
  }
  .diceNum {
    text-align: center;
    font-size: 0.18rem;
    color: #ffffff;
    line-height: 0.5rem;
    span {
      font-size: 0.22rem;
      font-family: AvenirNext-Bold;
    }
  }
  .desc {
    font-size: 0.14rem;
    line-height: 0.2rem;
    span {
      color: #fff16b;
    }
  }
  .dialog-button {
    min-width: 1rem;
    height: 0.46rem;
    padding: 0;
  }
  @media screen and (max-width: 1024px) {
    .font {
      font-size: 30px;
    }
    .el-dialog__title {
      font-size: 20px !important;
    }
    .el-dialog__body {
      font-size: 12px;
      line-height: 30px;
      .lineHei {
        line-height: 20px;
      }
    }
    .content .bg .num {
      font-size: 18px;
      height: 1rem;
    }
    .diceNum {
      font-size: 14px;
      span {
        font-size: 16px;
      }
    }
    .desc {
      font-size: 12px;
      span {
        color: #fff16b;
      }
    }
    .pledge-button {
      width: 1.4rem !important;
    }
    .pledge-input-block {
      width: 3.3rem !important;
    }
    .pledge-input::after {
      right: 2.16rem !important;
    }
    .content {
      margin-left: auto !important;
      float: none !important;
    }
    .dividen-info-show {
      float: none !important;
      text-align: center;
      max-width: 100%;
    }
    .bonus-num {
      padding-left: 0.5rem !important;
      height: 0.5rem !important;
      height: 0.4rem;
      top: 0;
    }
    .bonus-num::before {
      width: 0.35rem !important;
      background-size: auto 60% !important;
    }
    .dividend-title-item {
      padding: 0 0.1rem !important;
    }
  }
}
</style>

<template>
  <!-- 倒计时 -->
  <el-dialog
    :visible.sync="dialog"
    :title="$t('Dividend.notice')"
    width="6.5rem"
    custom-class="how-dialog count-dialog"
    @close="dialogHandler"
  >
    <h4 v-html="$t('Dividend.rules.title')"></h4>
    <p
      v-for="(item,index) in $t('Dividend.rules.content')"
      :key="index"
      class="lineHei"
    >{{item}}</p>
    <p class="font">
      <span v-if="this.day > 0">{{this.day}} {{$t('day')}}</span>
      {{this.hr > 0 ? this.hr : '00'}}:{{this.min > 0 ? this.min : '00'}}:{{this.sec > 0 ? this.sec : '00'}}
    </p>
    <span
      slot="footer"
      class="dialog-footer"
    >
      <el-button
        type="primary"
        @click="dialogHandler"
      >{{$t('Confirm')}}</el-button>
    </span>
  </el-dialog>
</template>
<script>
import moment from "moment";
export default {
  props: {
    countDownVisible: {
      default: Boolean
    }
  },
  data() {
    return {
      dialog: false,
      day: 0,
      hr: 0,
      min: 0,
      sec: 0,
      date: "2018-12-01T10:00:00.000Z"
    };
  },
  mounted() {
    this.dialog = this.countDownVisible;
    this.countDown();
  },
  methods: {
    dialogHandler() {
      this.dialog = false;
      let obj = {
        key: "countDownVisible",
        value: this.dialog
      };
      this.$emit("dialogHandler", obj);
    },
    countDown() {
      // 目标日期时间戳
      const end = Date.parse(new Date(this.date));
      // 当前时间戳
      const now = Date.parse(new Date());
      // 相差的毫秒数
      const msec = end - now;

      // 计算时分秒数
      let day = parseInt(msec / 1000 / 60 / 60 / 24);
      let hr = parseInt((msec / 1000 / 60 / 60) % 24);
      let min = parseInt((msec / 1000 / 60) % 60);
      let sec = parseInt((msec / 1000) % 60);

      // 个位数前补零
      hr = hr > 9 ? hr : "0" + hr;
      min = min > 9 ? min : "0" + min;
      sec = sec > 9 ? sec : "0" + sec;

      this.day = day;
      this.hr = hr;
      this.min = min;
      this.sec = sec;
      // 一秒后递归
      setTimeout(() => {
        this.countDown();
      }, 1000);
    }
  }
};
</script>
<style lang="scss">
.count-dialog {
  .title {
    text-align: center;
    font-family: PingFang-SC-Medium;
    font-size: 20px;
    color: #fff16b;
    margin-bottom: 0.2rem;
  }
  .font {
    font-family: AvenirNext;
    font-size: 50px;
    color: #fff16b;
    text-align: center;
    line-height: 80px;
  }
  .lineHei {
    line-height: 0.3rem;
  }
  @media screen and (max-width: 1024px) {
    .el-dialog__header {
      padding-top: 0.5rem;
    }
    .font {
      font-size: 30px;
      line-height: 50px;
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
  }
}
</style>

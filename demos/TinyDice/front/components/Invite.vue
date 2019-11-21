<template>

  <el-dialog
    :title="$t('invite.title')"
    :visible.sync="inviteDialog.visible"
    center
    class="inviteDialog"
    top="10vh"
  >
    <p class="center">{{$t('invite.desc')}}</p>
    <el-row class="invite-url">
      <p class="url">{{inviteUrl}}</p>
      <el-button
        @click="copy"
        class="copyContent"
        :data-clipboard-text="inviteUrl"
      >
        {{$t('invite.copyButton')}}
      </el-button>
    </el-row>
    <el-row>
      {{$t('vip.copyRight')}}
    </el-row>
  </el-dialog>
</template>
<script>
import Clipboard from "clipboard";
import moment from "moment";

export default {
  props: {
    inviteDialog: {
      type: Object,
      default: {}
    }
  },
  data() {
    return {
      currentGrade: 1,
      emaining: 15000,
      url: null,
      extractLoading: false,
      activityAddress: "", //合约地址
      contractInstance: {}, //合约对象
      prize: 0,
      tableLoading: true
    };
  },

  async mounted() {
    this.activityAddress = this.$store.state.activityAddress;
    let tronWeb = window.tronWeb;
    this.contractInstance = await tronWeb.contract().at(this.activityAddress);
    await this.getBalance();

    this.url = window.location.origin;

    // this.contractInstance.setInvitationBalance('TN7KpFteYkkGUPM4wQ8uKRLCzq2M3ngkmc',200).send();
  },
  computed: {
    inviteUrl() {
      let address = this.$store.state.address || {};
      let finalUrl = `${this.url}?from=${address.base58}`;
      return finalUrl;
    }
  },
  methods: {
    /**
     * 获取邀请的余额
     */
    async getBalance() {
      let banlance = await this.contractInstance.getInvitationBalance().call();
      banlance = banlance.toString();
      this.prize = banlance;
    },
    /**
     * 复制按钮
     */
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
    }
  }
};
</script>
<style lang="scss">
.inviteDialog {
  .el-dialog {
    background: #fff;
    color: #0a0a30;
    width: 5.9rem;
    border-radius: 0.1rem;
  }
  .el-dialog__title {
    font-family: PingFangSC-Medium;
    font-size: 0.2755rem;
    font-weight: normal;
    font-stretch: normal;
    color: #0a0a30;
  }
  .center {
    text-align: center;
    font-family: PingFangSC-Regular;
    font-size: 0.18rem;
    font-weight: normal;
    font-stretch: normal;
    line-height: 0.48rem;
    color: #0a0a30;
  }
  .invite-url {
    height: 1.61rem;
    background-color: #eaebed;
    border-radius: 0.1rem;
    padding: 0.23rem;
    box-sizing: border-box;
    text-align: center;
    position: relative;

    .url {
      height: 36px;
      font-family: PingFangSC-Regular;
      font-size: 0.17rem;
      font-weight: normal;
      font-stretch: normal;
      height: 0.595rem;
      letter-spacing: -0.4px;
      color: #7f8084;
      word-break: break-all;
      text-align: left;
    }
    .copyContent {
      margin-top: 0.2rem;
      position: absolute;
      bottom: 0.15rem;
      left: 0;
      right: 0;
      margin: auto;
      color: #fff;
      padding: 0.1rem 0.15rem;
      width: 2.23rem;
      height: 0.41rem;
      background-image: linear-gradient(
          -25deg,
          #7341ca 0%,
          #7854e5 50%,
          #7c67ff 100%
        ),
        linear-gradient(#4648bf, #4648bf);
      background-blend-mode: normal, normal;
      border-radius: 0.1rem;
    }
  }

  .message {
    height: 60px;
    line-height: 60px;
  }

  .textRight {
    text-align: right;
  }
  .table {
    margin-top: 40px;
    .el-table th,
    .el-table tr {
      background: #000;
      color: #fff;
    }
    .el-table--enable-row-hover .el-table__body tr:hover > td {
      background-color: #212e3e !important;
    }
  }

  .el-input-group__append {
    background-color: #409eff;
    color: #fff;
  }

  .title {
    margin-bottom: 10px;
  }

  .textCenter {
    p {
      text-align: center;
      font-size: 20px;
      color: #5bd4d7;
      font-weight: bold;
    }

    .el-button {
      margin-top: 10px;
      background: #5bd4d7;
      border-color: #fff;
    }

    text-align: center;
  }

  .el-row {
    margin-top: 20px;
  }

  .el-table__empty-block {
    background-color: #000;
  }
  .el-table__empty-text {
    color: #fff;
  }

  .el-pagination__total,
  .el-pagination__jump {
    color: #fff;
  }

  .record {
    height: 0.87rem;
    margin: 0 auto;
    background-image: linear-gradient(
        -25deg,
        #7341ca 0%,
        #7854e5 50%,
        #7c67ff 100%
      ),
      linear-gradient(#eaebed, #eaebed);
    background-blend-mode: normal, normal;
    border-radius: 0.1rem;
    padding: 0.2rem;
    box-sizing: border-box;
    color: #fff;
    line-height: 0.25rem;
    .el-button {
      float: right;
      width: 0.65rem;
      height: 0.28rem;
      background-color: #ffffff;
      border-radius: 0.08rem;
      padding: 0;
    }
  }
}
</style>

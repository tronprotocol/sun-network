<template>
  <!-- 怎么玩 -->
  <el-dialog
    :title="$t('HowToPlay')"
    :visible.sync="dialog"
    width="5.8rem"
    custom-class="how-dialog play-dialog"
    @close="dialogHandler"
  >
    <p v-html="$t('Rule.p1')"></p>
    <p v-html="$t('Rule.p2')"></p>
    <p v-html="$t('Rule.p3')"></p>
    <p v-html="$t('Rule.p4')"></p>
    <!-- <p v-html="$t('Rule.p5')"></p> -->
    <!-- <p v-html="$t('Rule.p6')"></p>
    <p v-html="$t('Rule.p7')"></p>-->
    <!-- <p v-if="locale == 'ch'">{{$t('notFindGoogle')}}</p>
    <p v-if="locale == 'ch'"><a href="https://s3.amazonaws.com/trondice/Chromewallet/TRONlink.zip" targe="_blank">https://s3.amazonaws.com/trondice/Chromewallet/TRONlink.zip</a></p>-->
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
export default {
  props: {
    dialogHow: {
      default: Boolean
    }
  },
  data() {
    return {
      dialog: false
    };
  },
  mounted() {
    this.dialog = this.dialogHow;
  },
  methods: {
    dialogHandler() {
      this.dialog = false;
      let obj = {
        key: "dialogHow",
        value: this.dialog
      };
      this.$emit("dialogHandler", obj);
    }
  }
};
</script>
<style lang="scss">
.play-dialog {
  a {
    color: #ffd200 !important;
  }
  .el-dialog__body {
    word-break: break-all;
  }
}
</style>

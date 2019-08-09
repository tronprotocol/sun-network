<template>

  <el-dialog
    :title="$t('LoginTipTitle')"
    :visible.sync="dialog"
    width="6rem"
    custom-class="how-dialog login-dialog"
    @close="dialogHandler"
  >
    <p v-html="$t('LoginTipContent')"></p>
    <p style="padding:.2rem 0">{{$t('loginTip')}}</p>
    <p>{{$t('noLogin')}}</p>
    <p v-if="locale == 'ch' ">{{$t('notFindGoogle')}}</p>
    <p v-if="locale == 'ch'"><a
        href="https://s3.amazonaws.com/trondice/Chromewallet/TRONlink.zip"
        targe="_blank"
      >https://s3.amazonaws.com/trondice/Chromewallet/TRONlink.zip</a></p>
    <span
      slot="footer"
      class="dialog-footer"
    >
      <el-button
        type="primary"
        @click="$store.commit('SET_DIALOG_LOGIN',false)"
      >{{$t('Confirm')}}</el-button>
    </span>
  </el-dialog>
</template>
<script>
import { mapState } from "vuex";
export default {
  props: {
    dialogLogin: {
      default: Boolean
    }
  },
  data() {
    return {
      dialog: false
    };
  },
  mounted() {
    // this.dialog = this.dialogLogin;
    this.dialog = false;
  },
  computed: {
    ...mapState(["locale"])
  },
  methods: {
    dialogHandler() {
      this.dialog = false;
      let obj = {
        key: "dialogLogin",
        value: this.dialog
      };
      this.$emit("dialogHandler", obj);
    }
  }
};
</script>
<style lang="scss">
.login-dialog {
  word-break: break-all;
}
</style>


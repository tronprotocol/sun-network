<template>
  <!-- dice token -->
  <el-dialog
    :title="$t('Token.button')"
    :visible.sync="dialog"
    :show-close="true"
    width="5.8rem"
    custom-class="how-dialog dice-dialog"
    @close="dialogHandler"
  >
    <h4>{{$t('Token.Dice.head')}}</h4>
    <p
      v-html="item"
      v-for="(item,index) in $t('Token.Dice.content')"
      :key="'p1_'+index"
    ></p>
    <h4 style="margin-top:0.2rem;">{{$t('Token.Distribution.head')}}</h4>
    <p
      v-html="$t('Token.Distribution.title')"
      class="title"
    ></p>
    <p
      v-html="item"
      v-for="(item,index) in $t('Token.Distribution.content')"
      :key="'p2_'+index"
    ></p>
    <h4 style="margin-top:0.2rem;">{{$t('Token.Address.head')}}</h4>
    <p
      v-html="item"
      v-for="(item,index) in $t('Token.Address.content')"
      :key="'p3_'+index"
    ></p>
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
    diceTokenVisible: {
      default: Boolean
    }
  },
  data() {
    return {
      dialog: false
    };
  },
  mounted() {
    this.dialog = this.diceTokenVisible;
  },
  methods: {
    dialogHandler() {
      this.dialog = false;
      let obj = {
        key: "diceTokenVisible",
        value: this.dialog
      };
      this.$emit("dialogHandler", obj);
    }
  }
};
</script>
<style lang="scss">
.dice-dialog .el-dialog__body {
  h4,
  .title,
  .number {
    color: #ffd200 !important;
  }
  .number {
    font-weight: bold;
  }
  p {
    line-height: 0.25rem;
  }
}
</style>

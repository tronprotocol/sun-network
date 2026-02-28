<template>
  <!-- 路线图 -->
  <el-dialog
    :title="$t('Roadmap.button')"
    :visible.sync="dialog"
    width="5.8rem"
    custom-class="how-dialog road-dialog"
    @close="dialogHandler"
  >
    <p
      class="list-item"
      v-html="item"
      v-for="(item,index) in $t('Roadmap.content')"
      :key="index"
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
    roadMapVisible: {
      default: Boolean
    }
  },
  data() {
    return {
      dialog: false
    };
  },
  mounted() {
    this.dialog = this.roadMapVisible;
  },
  methods: {
    dialogHandler() {
      this.dialog = false;
      let obj = {
        key: "roadMapVisible",
        value: this.dialog
      };
      this.$emit("dialogHandler", obj);
    }
  }
};
</script>
<style lang="scss">
.road-dialog {
  .page {
    color: #ffd200 !important;
    font-weight: bold;
  }
}
@media screen and (max-width: 1024px) {
  p {
    line-height: 0.4rem;
  }
}
</style>

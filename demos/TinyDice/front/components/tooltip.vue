<template>
  <el-dialog
    :visible.sync="dialog"
    width="6.5rem"
    custom-class="how-dialog auto-dialog"
    @close="dialogHandler"
  >
    <div v-if="type=='autoBet'">
      <div class="autoBet">
        <h4 style="color: #fff16b;">{{$t('autoBet.title')}}</h4>
        <p
          v-html="item"
          v-for="(item,index) in $t('autoBet.content')"
          :key="index"
        ></p>
      </div>
    </div>
    <div v-if="type=='dice'">
      <p style="margin-bottom:0.1rem;">{{$t('mineNum')}}</p>
      <p>{{$t('diceTip')}}</p>
    </div>
    <div v-if="type=='mining'">
      <div class="mining">
        <table style="margin-top:0.1rem;">
          <thead>
            <tr>
              <td>{{$t('Mining.table.stage')}}</td>
              <td>{{$t('Mining.table.proportion')}}</td>
              <td>{{$t('Mining.table.diceNum')}}</td>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="(item,index) in miningTable"
              :key="index"
            >
              <td>{{item.stage}}</td>
              <td>{{item.prop}}</td>
              <td>{{item.diceNum}}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
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
    toolTipVisible: {
      default: Boolean
    },
    type: {
      default: String
    }
  },
  data() {
    return {
      dialog: false,
      miningTable: [
        { stage: 1, prop: "1", diceNum: "600,000,000" },
        { stage: 2, prop: "0.5", diceNum: "600,000,000" },
        { stage: 3, prop: "0.25", diceNum: "600,000,000" },
        { stage: 4, prop: "0.2", diceNum: "600,000,000" },
        { stage: 5, prop: "0.15", diceNum: "600,000,000" },
        { stage: 6, prop: "0.12", diceNum: "600,000,000" },
        { stage: 7, prop: "0.1", diceNum: "600,000,000" },
        { stage: 8, prop: "0.08", diceNum: "600,000,000" },
        { stage: 9, prop: "0.06", diceNum: "600,000,000" },
        { stage: 10, prop: "0.05", diceNum: "600,000,000" }
      ]
    };
  },
  mounted() {
    this.dialog = this.toolTipVisible;
  },
  methods: {
    dialogHandler() {
      this.dialog = false;
      let obj = {
        key: "toolTipVisible",
        value: this.dialog
      };
      this.$emit("dialogHandler", obj);
    }
  }
};
</script>
<style lang="scss">
.auto-dialog {
  @media screen and (max-width: 1024px) {
    .font {
      font-size: 30px;
    }
    .el-dialog__title {
      font-size: 20px !important;
    }
    .el-dialog__body {
      font-size: 12px;
      line-height: 20px;
      .lineHei {
        line-height: 20px;
      }
    }
    .mining {
      line-height: 0.2rem;
      table {
        width: 100%;
        tr {
          line-height: 0.4rem;
          font-size: 12px;
        }
      }
    }

    .mining table td {
      text-align: center;
      border: 1px solid rgba(255, 255, 255, 0.3);
      margin-bottom: 0.02rem;
    }
    .mining table tr td:first-child {
      width: 20%;
    }
    .mining table tr td:nth-child(2) {
      width: 40%;
    }

    .mining table tr td:last-child {
      width: 70%;
    }
  }
}
</style>

<template>
  <!--奖励规则-->
  <el-dialog
    :title="$t('PrizeRule.title')"
    :visible.sync="dialog"
    width="5.8rem"
    custom-class="how-dialog rank-dialog"
    @close="dialogHandler"
  >
    <div class="ruleDialog">
      <table
        class="table"
        cellpadding="0"
        cellspacing="0"
      >
        <thead>
          <tr>
            <th>{{$t('PrizeRule.content.rank')}}</th>
            <th>{{$t('PrizeRule.content.prize')}}</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="(item,index) in tableData"
            :key="index"
          >
            <td>{{$t('PrizeRule.content.few',{no:item.rank})}}</td>
            <td>{{item.prize}} TRX</td>
          </tr>
        </tbody>
      </table>
      <p class="rule">{{$t('PrizeRule.footer')}}</p>
      <p class="rule">{{$t('PrizeRule.footer2')}}</p>
      <span
        slot="footer"
        class="dialog-footer"
      >
        <el-button
          type="primary"
          @click="dialogHandler"
        >{{$t('Confirm')}}</el-button>
      </span>
      <p style="text-align:center;font-size:0.14rem;">{{$t('PrizeRule.copyright')}}</p>
    </div>
  </el-dialog>
</template>
<script>
export default {
  props: {
    holidayDialogVisible: {
      default: Boolean
    },
    ruleDialogVisible: {
      default: Boolean
    }
  },
  data() {
    return {
      dialog: false,
      tableData: [
        { rank: "1", prize: "10,000" },
        { rank: "2", prize: "5,000" },
        { rank: "3", prize: "2,000" },
        { rank: "4-10", prize: "800" },
        { rank: "11-20", prize: "500" }
      ]
    };
  },
  mounted() {
    // this.dialog = this.holidayDialogVisible;
    this.dialog = this.ruleDialogVisible;
  },
  methods: {
    dialogHandler() {
      this.dialog = false;
      let obj = {
        key: "ruleDialogVisible",
        value: this.dialog
      };
      this.$emit("dialogHandler", obj);
    }
  }
};
</script>
<style lang="scss">
.rank-dialog {
  .table {
    width: 100%;
    background: rgba(29, 7, 78, 0.32);
    tr {
      td,
      th {
        border-bottom: 1px solid rgba(255, 255, 255, 0.2);
        height: 0.46rem;
      }
    }
    tr:last-child {
      td {
        border-bottom: none;
      }
    }
    tbody {
      width: 100%;
      text-align: center;
    }
  }
  .dialog-footer {
    display: block;
    text-align: center;
    margin: 0.2rem 0;
  }
  .rule {
    font-size: 0.14rem;
    line-height: 0.2rem;
    margin-top: 0.1rem !important;
  }
  @media screen and (max-width: 1024px) {
    .rule {
      line-height: 18px;
      font-size: 12px;
    }
    table {
      th,
      td {
        height: 30px !important;
      }
    }
  }
}
</style>
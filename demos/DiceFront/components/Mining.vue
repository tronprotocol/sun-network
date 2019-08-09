<template>
  <div>
    <div class="msg mobile">
      <span class="br">
        <span v-html="$t('Mining.desc1',{date:miniStatus.UpdateTime.substr(-8),round:miniStatus.Stage,})"></span>
        <i
          class="el-icon-question"
          style="color:rgba(253,253,255,0.71);margin-right:0.1rem;"
          v-if="platForm == 'mobile'"
          @click="messageBox('mining')"
        ></i>
        <el-tooltip
          class="item"
          effect="dark"
          placement="right"
          v-if="platForm == 'pc'"
        >
          <div slot="content">
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
          <i
            class="el-icon-question"
            style="color:rgba(253,253,255,0.71);margin-right:0.1rem;"
          ></i>
        </el-tooltip>
      </span>

      <span v-html="$t('Mining.desc2',
      {rate:` 1 TRX : ${miniStatus.StageMinRate} dice `,
      currentPool:parseFloat((miniStatus.StageTarget*miniStatus.StageProgress/100).toFixed(1)).toLocaleString(),
      currentPoolRate:parseFloat((miniStatus.StageProgress)).toFixed(2),
      totalPool:parseFloat((miniStatus.MineTotal*miniStatus.MineProgress/100).toFixed(1)).toLocaleString(),
      totalPoolRate:parseFloat((miniStatus.MineProgress)).toFixed(2)})"></span>
      <tool-tip
        :toolTipVisible="toolTipVisible"
        :type="toolTipType"
        @dialogHandler="dialogHandler"
        v-if="toolTipVisible"
      ></tool-tip>
    </div>
  </div>
</template>
<script>
import moment from "moment";

import { mapState } from "vuex";
import { getProgress } from "~/api/mining";
import bus from "~/assets/js/bus";
import toolTip from "./tooltip";
export default {
  name: "wakuang",
  data() {
    return {
      date: moment().format("YYYY-MM-DD HH:mm:ss"),
      miniStatus: {
        UpdateTime: moment().format("YYYY-MM-DD HH:mm:ss"),
        StageMineRate: 0,
        Stage: 1,
        StageTarget: 0,
        StageProgress: 0,
        MineTotal: 0,
        MineProgress: 0
      },
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
      ],
      toolTipVisible: false,
      toolTipType: ""
    };
  },
  computed: {
    ...mapState(["address", "contractAddress", "platForm"])
  },
  components: {
    toolTip
  },
  mounted() {
    bus.$on("getContractAddress", val => {
      this.getData(val);

      setInterval(() => {
        this.getData(val);
      }, 60000);
    });
  },
  methods: {
    getData(contractAddress) {
      getProgress(contractAddress)
        .then(response => {
          this.miniStatus = response;
          this.$store.commit("SET_DIVIEND", this.miniStatus);
        })
        .catch(err => {
          console.log("getProgress", err);
        });
    },
    messageBox(type) {
      this.toolTipVisible = true;
      this.toolTipType = type;
    },
    dialogHandler(val) {
      this.$data[val.key] = val.value;
    }
  }
};
</script>
<style lang="scss">
.msg {
  text-align: center;
  width: 13rem;
  position: relative;
  left: -0.4rem;
  .yellow {
    color: #ffc559;
    font-family: AvenirNext;
  }
}

@media screen and (max-width: 1024px) {
  .mobile {
    width: 100%;
    left: 0;
    font-size: 12px;
    line-height: 0.4rem;
    .br {
      display: block;
      text-align: left;
      text-indent: 0.15rem;
    }
  }
}
</style>

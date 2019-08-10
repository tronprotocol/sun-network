<template>
  <div class="rank">
    <table
      border="0"
      cellspacing="0"
      cellpadding="0"
      class="rank-table"
    >
      <caption class="title">
        <el-row :gutter="20">
          {{$t('RankTitle')}}
          <!-- <i class="el-icon-question" @click="ruleDialogVisible=true"></i> -->
          <div
            class="rank-box"
            @click="ruleDialogVisible=true"
          ></div>
          <!-- 圣诞活动规则 -->
          <!-- <div class="christmas-box" @click="holidayDialogVisible=true"></div> -->
        </el-row>
        <el-row>
          <el-col :span="24">
            <div class="dateTime">
              <i
                class="iconfont icon-jiantou-zuo-cuxiantiao"
                @click="changeTime('-')"
                :class="{grey:(!isPreClick || isLoading)}"
              ></i>
              <span style="width:1.5rem;display:inline-block;font-size:0.18rem;text-align:center">{{date}}(UTC)</span>
              <i
                class="iconfont icon-jiantou-zuo-cuxiantiao right"
                :class="{grey:(!isClick||isLoading)}"
                @click="changeTime('+')"
              ></i>
            </div>
          </el-col>
        </el-row>
      </caption>
      <thead>
        <tr>
          <th>{{$t('Order')}}</th>
          <th>{{$t('Player')}}</th>
          <th>{{$t('TotalMount')}}</th>
          <th>{{$t('Prize')}}</th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="(item,index) of ranks"
          :key="index"
        >
          <td>
            <img
              v-if="index < 3"
              :src="require('../assets/images/rank'+(index+1)+'.png')"
            >
            <span v-else>{{index+1}}</span>
          </td>
          <td>{{item.player|hiddenAddress}}</td>
          <td style="font-family:Arial;">{{item.total | toLocaleString}} TRX</td>
          <td style="color:#e7b01a">{{item.prize}} TRX</td>
        </tr>
        <tr v-if="ranks.length === 0">
          <td
            colspan="5"
            class="span"
            v-if="isLoading"
          >
            <div class="cell"></div>
            <div class="cell"></div>
            <div class="cell"></div>
            <div class="cell"></div>
            <div class="cell"></div>
            <div class="cell"></div>
            <div class="cell"></div>
            <div class="cell"></div>
            <div class="cell"></div>
            <div class="cell"></div>
          </td>
          <td v-else>No Data</td>
        </tr>
      </tbody>
    </table>

    <!-- websocket 实时排行 -->
    <websocket
      :wsOption="wsOption"
      @wsData="wsData"
      v-if="isWs"
    ></websocket>

    <table
      class="rank-table"
      v-if="ownerData"
    >
      <tbody>
        <tr>
          <td>
            <span>{{ownerData.ranking === 0 ? '-' :ownerData.ranking}}</span>
          </td>
          <td>{{ ownerData.user_address | hiddenAddress }}</td>
          <td style="font-family:Arial;">{{ ownerData.total_bet| toLocaleString }} TRX</td>
          <td style="color:#e7b01a">{{ownerData.prize}} TRX</td>
        </tr>
      </tbody>
    </table>
    <!--奖励规则-->
    <rank-prize
      :ruleDialogVisible="ruleDialogVisible"
      @dialogHandler="dialogHandler"
      v-if="ruleDialogVisible"
    ></rank-prize>
  </div>
</template>

<script>
import { mapState } from "vuex";
import { getRanks } from "@/api/rank";
import moment from "moment";
import websocket from "./websocket";
import rankPrize from "./RankPrize";
import { getCurrentRank } from "@/api/rank";
export default {
  name: "Rank",
  components: {
    websocket,
    rankPrize
  },
  data() {
    return {
      ranks: [],
      isLoading: true,
      ruleDialogVisible: false,
      isClick: false,
      isPreClick: true,
      ownerData: {},
      isWs: true,
      holidayDialogVisible: false,
      date: moment()
        .utc()
        .format("YYYY.MM.DD")
    };
  },
  computed: {
    ...mapState(["address", "globalTronWeb"]),
    wsOption() {
      let obj = {
        dapp_id: 1,
        user_address: this.address.base58 || ""
      };
      return getCurrentRank(obj);
    }
  },
  methods: {
    dialogHandler(val) {
      this.$data[val.key] = val.value;
    },
    /**
     * 切换日期，查询每日排行榜
     */
    changeTime(type) {
      this.isWs = false;
      //如果数据没回来，就不可点击
      if (this.isLoading) {
        return;
      }
      if (type === "-" && !this.isPreClick) {
        return;
      }

      if (type === "+" && !this.isClick) {
        return;
      }

      this.isClick = true;
      this.isPreClick = true;

      let multiple = 0;
      switch (type) {
        case "+":
          multiple++;
          break;
        case "-":
          multiple--;
          break;
      }

      this.date = moment(this.date, "YYYY.MM.DD")
        .add(multiple, "days")
        .format("YYYY.MM.DD");
      let dateTimeStemp = moment(this.date, "YYYY.MM.DD")
        .utc()
        .format("x");

      let currentDate = moment()
        .utc()
        .format("YYYY.MM.DD");

      let currentDateTimeStemp = moment(currentDate, "YYYY.MM.DD").format("x");

      // console.log("this.date:", this.date);
      // console.log("multiple: ", multiple);
      // /**
      //  * 当前时间的时间戳 Unix Millisecond Timestamp
      //  */
      // let currentDateTimeStemp = moment()
      //   .utc()
      //   .format("x");

      // console.log("currentDateTimeStemp: ", currentDateTimeStemp);
      // /**
      //  * 增/减 天数，生成历史时间。2019.01.23形式
      //  */
      // let nextDate = moment(this.date, "YYYY.MM.DD")
      //   .add(multiple, "days")
      //   .format("YYYY.MM.DD");

      // console.log("nextDate: ", nextDate);
      // // 用于显示的，赋值显示在表格上
      // this.date = nextDate;

      // /**
      //  * 用于比较，历史时间的时间戳
      //  */
      // let dateTimeStemp = moment(nextDate, "YYYY.MM.DD").format("x");
      // console.log("dateTimeStemp: ", dateTimeStemp);

      // let rightTime = moment(this.date, "YYYY.MM.DD")
      //   .add(1, "days")
      //   .format("YYYY.MM.DD");
      // console.log("rightTime: ", rightTime);

      // /**
      //  * 用于计算的历史时间戳
      //  */
      // let rigthTimeTemp = moment()
      //   .utc(rightTime)
      //   .format("x");
      // console.log("rigthTimeTemp: ", rigthTimeTemp);

      if (dateTimeStemp == currentDateTimeStemp) {
        this.isWs = true;
        this.isLoading = true;
      } else {
        this.getData();
      }

      if (dateTimeStemp >= currentDateTimeStemp && type === "+") {
        this.isClick = false;
        return;
      }
    },
    /**
     * 获取历史日排行榜数据
     */
    getData() {
      let ranks = [];
      this.isLoading = true;
      this.ranks = [];

      let time1 = moment(this.date)
        .subtract(-1, "days")
        .format("YYYY.MM.DD");

      let time = moment.utc(time1).format("x");

      let obj = {
        dapp_id: 1,
        date: time,
        user_address: this.address.base58 || ""
      };
      getRanks(obj)
        .then(data => {
          this.isLoading = false;
          const ownerDataDefault = {
            user_address: this.address.base58 || "",
            total_bet: 0,
            ranking: 0
          };
          const ownerData = data.owner;
          this.ownerData = ownerData.user_address
            ? ownerData
            : ownerDataDefault;
          ownerData.total_bet = this.globalTronWeb.fromSun(ownerData.total_bet);

          const rankList = data.list;
          if (rankList.length > 0) {
            let message = rankList.slice(0, 30);
            this.setData(message, ranks);
          }
        })
        .catch(err => {
          this.isLoading = false;
        });
    },
    /**
     * 获取实时排行数据
     * 通过websocket接口
     */
    wsData(message) {
      if (this.ranks.length == 0) {
        this.isLoading = true;
      }
      let ranks = [];
      let data = message.data;
      if (data) {
        this.isLoading = false;
        const ownerDataDefault = {
          user_address: this.address.base58 || "",
          total_bet: 0,
          ranking: 0
        };
        const ownerData = data.owner;
        this.ownerData = ownerData.user_address ? ownerData : ownerDataDefault;
        ownerData.total_bet = this.globalTronWeb.fromSun(ownerData.total_bet);

        const rankList = data.list;
        if (rankList) {
          let message = rankList.slice(0, 30);
          this.setData(message, ranks);
        }
      }
    },
    /**
     * 排行数据显示处理
     * 前三名，前4-10名，前10-20名
     */
    setData(message, ranks) {
      message.forEach((v, i) => {
        let prize = 0;
        switch (i + 1) {
          case 1:
            prize = 10000;
            break;
          case 2:
            prize = 5000;
            break;
          case 3:
            prize = 2000;
            break;
          default:
            if (i + 1 >= 4 && i + 1 <= 10) {
              prize = 800;
            } else if (i + 1 > 10 && i + 1 <= 20) {
              prize = 500;
            }
            break;
        }

        ranks.push({
          player: v.user_address,
          total: window.tronWeb
            ? window.tronWeb.fromSun(v.total_bet)
            : v.total_bet / 1000000,
          prize: prize
        });
      });

      if (this.ownerData.ranking > 0 && this.ownerData.ranking < 20) {
        this.$set(
          this.ownerData,
          "prize",
          ranks[this.ownerData.ranking - 1].prize
        );
      } else {
        this.$set(this.ownerData, "prize", 0);
      }

      this.ranks = ranks;
    }
  }
};
</script>
<style scoped lang="scss">
.ruleDialog {
  padding: 20px;
  h4 {
    margin-bottom: 20px;
    line-height: 20px;
  }
  p {
    line-height: 20px;
  }
  p:last-child {
    margin-bottom: 20px;
  }
}

.rank {
  width: 100%;
  height: 100%;
  padding: 0 0.11rem;
  border-radius: 0.2rem;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  align-items: center;

  .title {
    font-size: 0.22rem;
    position: relative;
    .rank-box {
      cursor: pointer;
      position: absolute;
      left: -0.15rem;
      top: -0.15rem;
      z-index: 20;
      width: 1.43rem;
      height: 1.37rem;
      background: url("../assets/images/box.png") no-repeat;
      background-position: 10px center;
      &:hover {
        background-position: -128px center;
      }
    }
    .christmas-box {
      position: absolute;
      right: 0.4rem;
      top: 0.2rem;
      z-index: 20;
      width: 0.6rem;
      height: 0.8rem;
      background: url("../static/images/christmas.png") no-repeat;
      background-position: 10px center;
      background-size: cover;
      cursor: pointer;
      &:hover {
        background-position: -45.5px center;
      }
    }
    .el-row:first-child {
      height: 0.6rem;
      line-height: 0.8rem;
    }
    .el-row:last-child {
      height: 0.4rem;
      line-height: 0.4rem;
      background: #662faf;
      font-size: 0.18rem;
      color: rgba(255, 255, 255, 0.8);
      .iconfont {
        font-size: 0.16rem;
      }
    }
    .dateTime {
      width: 100%;
    }
  }

  .rank-table {
    padding: 0 0.08rem;
    tr {
      display: table;
      width: 100%;
      table-layout: fixed;
      padding: 0 0.12rem;
      th,
      td {
        &:first-child {
          width: 0.8rem;
        }
        &:nth-child(2) {
          text-align: left;
        }
        &:nth-child(3),
        &:nth-child(4) {
          text-align: right;
        }
        &:nth-child(4) {
          padding-right: 0.3rem;
        }
      }
    }
    thead {
      th {
        height: 0.4rem;
        color: #fff;
        font-size: 0.16rem;
      }
    }
    tbody {
      tr {
        td.span {
          height: 2.4rem;
          text-align: center !important;
          .cell {
            display: inline-block;
            width: 0.04rem;
            height: 0.5rem;
            margin-right: 0rem;
            background-color: #b3a6ff;
            animation: animate 1s infinite;
            &:last-child {
              margin-right: 0px;
            }
            &:nth-child(10) {
              -webkit-animation-delay: 0.9s;
              animation-delay: 0.9s;
            }
            &:nth-child(9) {
              -webkit-animation-delay: 0.8s;
              animation-delay: 0.8s;
            }
            &:nth-child(8) {
              -webkit-animation-delay: 0.7s;
              animation-delay: 0.7s;
            }
            &:nth-child(7) {
              -webkit-animation-delay: 0.6s;
              animation-delay: 0.6s;
            }
            &:nth-child(6) {
              -webkit-animation-delay: 0.5s;
              animation-delay: 0.5s;
            }
            &:nth-child(5) {
              -webkit-animation-delay: 0.4s;
              animation-delay: 0.4s;
            }
            &:nth-child(4) {
              -webkit-animation-delay: 0.3s;
              animation-delay: 0.3s;
            }
            &:nth-child(3) {
              -webkit-animation-delay: 0.2s;
              animation-delay: 0.2s;
            }
            &:nth-child(2) {
              -webkit-animation-delay: 0.1s;
              animation-delay: 0.1s;
            }
          }
          @keyframes animate {
            50% {
              transform: scaleY(0);
            }
          }
        }
        td {
          height: 0.5rem;
          font-size: 0.15rem;
          // border-bottom: 0.02rem solid #262778;
          border-bottom: 1px solid #6f28a7;

          &:first-child {
            text-align: center;
            img {
              height: 0.28rem;
              width: 0.28rem;
            }
            span {
              border-radius: 100%;
              background-color: #474fab;
              display: inline-block;
              background: rgba(130, 71, 201, 0.5);
              width: 0.28rem;
              height: 0.28rem;
              border-radius: 100%;
              line-height: 0.28rem;
              text-align: center;
            }
          }
        }
        &:last-child {
          td {
            border: none;
          }
        }
      }
      display: block;
      max-height: 3.5rem;
      flex: 1;
      overflow: auto;
      margin-bottom: 0.05rem;
      background: #431e91;
      box-shadow: inset 0 1px 6px 0 rgba(0, 0, 0, 0.3);
      &::-webkit-scrollbar {
        width: 0.03rem;
        border-radius: 0.04rem;
      }
      &::-webkit-scrollbar-thumb {
        background-color: rgba(255, 255, 255, 0.3);
        border-radius: 0.04rem;
      }
    }
  }
  .last {
    width: calc(100% - 0.16rem);
    height: 0.5rem;
    background-color: #431e91;
    display: flex;
    flex-direction: row;
    align-items: center;
    line-height: 0.5rem;
    padding: 0 0.1rem;
    box-shadow: inset 0 1px 6px 0 rgba(0, 0, 0, 0.3);
    .cell {
      &:first-child {
        width: 0.8rem;
        display: flex;
        justify-content: center;
        text-align: center;
        span {
          width: 0.28rem;
          height: 0.28rem;
          border-radius: 100%;
          background-color: rgba(130, 71, 201, 0.5);
          display: inline-block;
          line-height: 0.28rem;
          color: #fff;
          text-align: center;
        }
      }
      &:nth-child(2) {
        text-align: center;
        width: 2.1rem;
      }
      &:nth-child(3) {
        flex: 1;
      }
      &:nth-child(3),
      &:nth-child(4) {
        text-align: right;
      }

      &:nth-child(4) {
        padding-right: 0.3rem;
      }
      &:nth-child(2),
      &:nth-child(3),
      &:nth-child(4) {
        // width: 1.73rem;
        display: table-cell;
      }
    }
  }
}

.grey {
  color: grey;
}

.dateTime {
  .right {
    transform: rotate(180deg);
    display: inline-block;
  }
}

// 暂时去掉prize
.rank table tr th:nth-child(2),
.rank table tr td:nth-child(2) {
  text-align: center !important;
}

@media screen and (max-width: 1024px) {
  .title {
    font-size: 16px !important;
  }
  .dateTime {
    .iconfont {
      font-size: 14px;
    }
    span {
      width: 1.5rem !important;
      font-size: 14px !important;
    }
  }
  .rank {
    padding-bottom: 0.15rem;
  }
  .rank table thead th {
    font-size: 12px;
  }
  .rank table tbody tr td {
    font-size: 12px;
  }
  .rank table tbody tr td:first-child span {
    width: 0.4rem;
    height: 0.4rem;
    line-height: 0.4rem;
  }
  .rank .last .cell:first-child span {
    width: 0.4rem;
    height: 0.4rem;
    line-height: 0.4rem;
  }
  .rank table tbody {
    max-height: 3.8rem;
  }
  .last {
    font-size: 12px !important;
    display: table;
    .cell {
      height: 0.5rem;
      &:first-child {
        margin-top: 0.1rem;
      }
    }
  }
  .rank .last {
    display: table;
    height: 0.75rem;
    line-height: 0.75rem;
  }
  .rank .last .cell:nth-child(2) {
    text-align: left;
    width: auto;
  }
  .rank table tbody tr td {
    height: 0.75rem;
  }
  .rank .title .el-row:first-child {
    height: 0.8rem;
    line-height: 1rem;
  }
  .rank .title .el-row:last-child {
    width: 98%;
  }
  .rank table tbody tr td:first-child img {
    width: 0.4rem;
    height: 0.4rem;
  }
  .rank .title .rank-box {
    position: absolute;
    left: 0.15rem;
    top: 0;
    z-index: 20;
    width: 1rem;
    height: 1rem;
    background: url(/_nuxt/assets/images/box.png) no-repeat;
    background-position: 1px center;
    background-size: auto 100%;
    &:hover {
      background-position: -49px center;
    }
  }
  .rank .title .christmas-box {
    position: absolute;
    right: 0.5rem;
    top: 0.25rem;
    z-index: 20;
    width: 0.4rem;
    height: 0.55rem;
    background: url("../static/images/christmas.png") no-repeat;
    background-position: 1px center;
    background-size: auto 100%;
    &:hover {
      background-position: 1px center;
    }
  }
}
</style>

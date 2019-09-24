<template>
  <div class="result">
    <div
      class="result-title"
      v-if="platForm == 'pc'"
    >{{$t('Result.BettingRecord')}}</div>
    <div class="result-table">
      <div
        class="tab"
        ref="tab"
      >
        <a
          href="javascript:;"
          class="focus"
          @click="tab(0)"
        >{{$t('Result.AllBets')}}</a>
        <a
          href="javascript:;"
          @click="tab(1)"
        >{{$t('Result.MyBets')}}</a>

        <!--<a href="javascript:;" @click="tab(2)">{{$t('LuckyPrize')}}</a>-->
      </div>
      <div
        class="output"
        ref="output"
      >
        <table
          style="display: table"
          cellspacing="0"
        >
          <thead>
            <tr>
              <th>{{$t('Result.Time')}}</th>
              <th>{{$t('Result.Player')}}</th>
              <th>{{$t('Result.Input')}}</th>
              <th>{{$t('Result.Result')}}</th>
              <th>{{$t('Result.Output')}}</th>
            </tr>
          </thead>
          <tbody v-if="all.length!=0">
            <tr
              v-for="(item,index) of all"
              :class="item.output > 0 ? 'win':'lose'"
              :key="'all_'+index"
            >
              <td>{{item.time}}</td>
              <td>{{item.player | hiddenAddress(10)}}</td>
              <td>{{item.input | toLocaleString}}</td>
              <td>
                <div>
                  {{$t('Result.LessThan')}} {{item.select}} &nbsp;&nbsp;
                  <span class="progress">
                    <el-progress
                      :text-inside="false"
                      :stroke-width="7"
                      :percentage="item.select"
                      :show-text="false"
                    ></el-progress>
                    <!-- <div class="block" :style="'left:'+item.result+'%'">{{item.result}}</div> -->
                    <div
                      class="block"
                      :style="'left:calc( '+item.result+'% - '+'.115rem'"
                    >{{item.result}}</div>
                  </span>
                </div>
              </td>
              <td class="prize">{{item.output | toLocaleString}}</td>
            </tr>
          </tbody>
          <tbody v-else>
            <tr></tr>
            <tr>
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

              <td
                v-else
                colspan="5"
                style="text-align:center"
              >
              <span>No Data</span>
              </td>
            </tr>
          </tbody>
        </table>
        <table cellspacing="0">
          <thead>
            <tr>
              <th>{{$t('Result.Time')}}</th>
              <th>{{$t('Result.Player')}}</th>
              <th>{{$t('Result.Input')}}</th>
              <th>{{$t('Result.Result')}}</th>
              <th>{{$t('Result.Output')}}</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="(item,index) of my"
              :class="item.output > 0 ? 'win':'lose'"
              :key="'my_'+index"
            >
              <td :class="{'auto':item.auto}">
                {{item.time}}
              </td>
              <td>{{item.player | hiddenAddress(10)}}</td>
              <td>{{item.input | toLocaleString }}</td>
              <td>
                <div>
                  {{$t('Result.LessThan')}} {{item.select}} &nbsp;&nbsp;
                  <span class="progress">
                    <el-progress
                      :text-inside="false"
                      :stroke-width="7"
                      :percentage="item.select"
                      :show-text="false"
                    ></el-progress>
                    <div
                      class="block"
                      :style="'left:calc( '+item.result+'% - '+'.115rem'"
                    >{{item.result}}</div>
                  </span>
                </div>
              </td>
              <td class="prize">{{item.output | toLocaleString}}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState } from "vuex";
import { getPoint, formatTime, getOdds } from "@/static/js/Util";
import bus from "~/assets/js/bus";
import moment from "moment";
import interfaceData from "@/api/config";
import { doBet } from "@/api/play.js";

export default {
  name: "Result",
  data() {
    return {
      all: [], // 所有的投注
      my: [], // 我的下注
      lucky: [],
      isLoading: true, // 加载状态
      isRoll: false, //
      localMy: [], // 本地存储我的下注
      maxNum: 30, // 存储的最大条数
      timers: null,
      i: 0,
      limitAmount: 500,
      promiseData: [] // 每次获取到的数组数据
    };
  },
  components: {
    // websocket
  },
  computed: {
    ...mapState([
      "contractAddress",
      "contractInstance",
      "address",
      "autoBet",
      "platForm",
      "globalSunWeb",
      "globalSunWeb2",
      "loginState"
    ])
  },
  watch: {
    contractAddress() {
      //this.getAllBets();
    },
    my(n, o) {
      if (n.length > 0 && this.isRoll) {
        this.$store.commit("SET_MY_BETS_LENGTH", n.length);
        this.$store.commit("SET_RANDOM", this.my[0].result);
        this.isRoll = false;
      }
    },
    address: {
      deep: true,
      handler(val) {
        // let localMy = localStorage.getItem(val.base58);
        // if (localMy) {
        //   let myObj = JSON.parse(localMy);
        //   this.my = myObj;
        //   this.localMy = myObj;
        // }
      }
    }
  },
  async mounted() {
    let timer = setInterval(() => {
      // if (!this.address.base58) {
      //   return;
      // }
      this.getAllData();
    }, 3000);
  },
  methods: {
    /**
     * 切换不同的tab标签页
     */
    tab(index) {
      if (index == 1) {
        if (!this.address.base58) {
          this.$message({
            type: "warn",
            message: 'Please Login',
            showClose: true
          });
          return;
        }
      }
      const tables = this.$refs.output.getElementsByTagName("table");
      const tabItem = this.$refs.tab.getElementsByTagName("a");
      for (let i = 0; i < tabItem.length; i++) {
        if (i === index) {
          tabItem[i].classList.add("focus");
          tables[i].style.display = "table";
        } else {
          tabItem[i].classList.remove("focus");
          tables[i].style.display = "none";
        }
      }
    },
    getOutput(d, input) {
      if ( Number(d["_random"]) > Number(d["_point"])) {
        return 0;
      }
      const odds = getOdds(Number(d["_point"]));
      const odds1 = Math.floor(odds * 10000) / 10000;
      const expectBonuses = Math.floor(input * odds * 1000) / 1000;
      return expectBonuses;
    },
    /**
     * 数据转换，
     * 将获取的数据，转换成想要的数据
     * 1. 将所有的投注信息合并
     * 2. 提取我的投注信息，并保存在本地
     * 3. 恭喜的信息
     */
    transData() {
      if (this.promiseData.length === 0) return;
      this.isLoading = false;
      // 1. 去重复,然后赋值
      let removeDupArr = this.deworming(this.promiseData, "transactionId");
      // 2. 倒序
      let sortArr = this.descSort(removeDupArr, 'timeStamp');
      // 3. 取前20个数据
      let needArr = removeDupArr.slice(0, 20);

      let needAllTable = [];
      let myBetsTable = [];

      needArr.forEach(item => {
        let d = item.dataMap;
        const event = Number(d["_random"]) < Number(d["_point"]) ? 'UserWin' : 'UserLose'; // 输赢的事件状态 UserWin UserLose
        const player = d["_addr"]; // 用户地址
        const select = Number(d["_point"]); // 下注的数
        const result = d["_random"]; // 获得的随机数
        const input = this.globalSunWeb2.sidechain.fromSun(d["_amount"]); // 下注的金额
        // const output = item["_W"] // 赢取的金额
        //   ? this.globalSunWeb.fromSun(item["_W"])
        //   : "0";
        const output = this.getOutput(d, input);
        const time = formatTime(item.timeStamp); // 格式化下注的时间 HH:mm:ss
        const timestamp = item.timeStamp; // 下注的时间戳
        const txId = item.transactionId; // 交易id

        if (this.address.base58 === player) {
          myBetsTable.push({
            select,
            result,
            player,
            input,
            output,
            timestamp,
            time,
            txId,
            event
          });
        }

        needAllTable.push({
          select,
          result,
          player,
          input,
          output,
          time,
          txId,
          event
        });
      });
      this.all = needAllTable;
      this.my = myBetsTable;
      // // 筛选出成功的，且投注数大于500，猜小于80的
      // this.filterSuccess(needAllTable);
      // this.all = needAllTable;
      // // 将我的投注信息提取，并赋值和保存在本地
      // this.saveMyBetsListToLocal(myBetsTable);
      // //  * 1. 将所有的投注信息合并,去重复,然后赋值
      // //  * 2. 提取我的投注信息，并保存在本地
      // //  * 3. 恭喜的信息
    },
    /**
     * 将我的投注信息提取，并赋值和保存在本地
     * 本地存储
     */
    saveMyBetsListToLocal(myBetsTable) {
      let myBetsList = [];
      this.localMy = JSON.parse(localStorage.getItem(this.address.base58));
      if (this.localMy && this.localMy.length != 0) {
        let arr = myBetsTable.concat(this.localMy);

        arr = this.deworming(arr, "txId");

        arr = arr.length > this.maxNum ? arr.slice(0, this.maxNum) : arr;

        myBetsList = arr;
      } else {
        myBetsList = myBetsTable;
      }

      localStorage.setItem(this.address.base58, JSON.stringify(myBetsList));

      this.$store.commit("SET_MY_BETS", myBetsList);

      this.my = myBetsList;
    },
    /**
     * 根据某个属性去除重复的数据
     */
    deworming(arr, attr) {
      let transactionIds = [];
      arr = arr.filter((item, index) => {
        if (transactionIds.indexOf(item[attr]) === -1) {
          transactionIds.push(item[attr]);
          return item;
        }
      });
      return arr;
    },
    /**
     * 倒序排序
     */
    descSort(arr, attr) {
      arr = arr.sort((o1, o2) => {
        if (o1[attr] > o2[attr]) {
          return -1;
        } else {
          return 1;
        }
      });
      return arr;
    },
    filterSuccess(arr) {
      let data = arr.filter((item, index) => {
        if (
          item.output > 0 &&
          parseInt(item.select) <= 80 &&
          item.input >= this.limitAmount
        ) {
          return item;
        }
      });

      this.$store.commit("SET_SUCCESS_RECORD", data);
    },
    getAllData() {
      const p1 = {
        contractAddress: interfaceData.contractAddress,
        limit: 30,
        eventName: 'UserLose'
      };
      doBet(p1).then(res => {
        this.promiseData = this.promiseData.concat(res);
        const p2 = {
          contractAddress: interfaceData.contractAddress,
          limit: 30,
          eventName: 'UserWin'
        };
        doBet(p2).then(res => {
          this.promiseData = this.promiseData.concat(res);
           this.transData();
        });
       
      }).catch(err => {
        console.log(err)
      })
      
    }
  }
};
</script>

<style scoped lang="scss">
.result {
  // padding-bottom: 0.2rem;
  height: 8.29rem;
  border-radius: 0.1rem;
  .result-title {
    width: 1.8rem;
    height: 0.83rem;
    background: url("../assets/images/record-btn.png") no-repeat;
    margin: 0.3rem auto;
    text-align: center;
    line-height: 0.83rem;
    font-family: Arial-Black;
    font-size: 0.2rem;
    color: #ffffff;
    background-size: 100% 100%;
  }
  .result-talbe {
    background-color: #432795;
    .tab {
      height: 0.28rem;
      line-height: 0.28rem;
    }
  }
  .tab {
    position: relative;
    z-index: 1;
    height: 0.42rem;
    line-height: 0.42rem;
    display: flex;
    flex-wrap: wrap;
    flex-direction: row;
    align-items: center;
    font-size: 0.16rem;
    a {
      padding: 0 0.3rem;
      font-size: 0.18rem;
      margin-left: 0.15rem;
      // color: #9fa5ff;
      color: #9fa2db;
      //background: rgba(48,41,95,0.9);
      //border: .01rem solid #7064B3;
      border-bottom: none;
      border-radius: 0.1rem 0.1rem 0 0;
      transition: all 0.2s ease-in-out;
      opacity: 0.9;
      background: #2e1962;
      border: 2px solid #7b2aae;
      border-radius: 10px 10px 1px 1px;
      &:first-child {
        margin: 0;
        margin-left: 0.11rem;
      }
    }
    a.focus {
      color: #fff;
      border: 2px solid #da50e2;
      background: #4c2b9f;
    }
  }
  .output {
    flex: 1;
    overflow: hidden;
    border: 0.02rem solid #cc4ad3;
    border-radius: 0.1rem;
    position: relative;
    z-index: 2;
    background: rgb(67, 39, 149);
    table {
      display: none;
      width: 100%;
      thead {
        display: table;
        width: 100%;
        table-layout: fixed;
      }
      tbody {
        display: block;
        height: 5.96rem;
        background: #241557;
        overflow-y: scroll;
        &::-webkit-scrollbar {
          width: 0.03rem;
          border-radius: 0.1rem;
          background: rgba(10, 10, 10, 0.2);
        }
        &::-webkit-scrollbar-thumb {
          background-color: rgba(255, 255, 255, 0.3);
          border-radius: 0.6rem;
        }
      }
      tr {
        display: table;
        width: 100%;
        table-layout: fixed;

        .auto {
          position: relative;
          // padding-left: 0 !important;
          .autoSan {
            width: 0.37rem;
            height: 0.37rem;
            display: inline-block;
            background-image: url("../assets/images/auto.png");
            background-repeat: no-repeat;
            position: absolute;
            left: 0;
            top: 0;
          }
          .translate {
            transform: rotate(-45deg);
            display: inline-block;
            font-size: 12px;
            position: absolute;
            top: -0.09rem;
            left: -0.05rem;
          }
        }
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
        th,
        td {
          font-size: 0.16rem;
          // color: rgba(255, 255, 255, 0.6);
          color: #fff;
          font-weight: normal;
          &:first-child {
            text-align: left;
            width: 2rem;
            padding-left: 0.5rem;
          }
          &:nth-child(2) {
            text-align: left;
          }
          &:nth-child(4) {
            width: 3rem;
          }
          &:last-child,
          &:nth-child(3) {
            //width:.7rem;
          }
          &:last-child {
            padding-right: 0.1rem;
          }
        }
        th {
          // background-color: #262c7f;
          // background-color: #191c61;

          height: 0.56rem;
          line-height: 0.56rem;
        }
        td {
          text-align: center;
          height: 0.5rem;
          line-height: 0.5rem;
          border-bottom: 0.02rem solid rgb(67, 39, 149);
          background: rgb(36, 21, 87);
        }
        td.win {
          color: #69c265;
        }
        td.lose {
          color: #d54e4e;
        }
        &:last-child {
          td {
            &:first-child {
              border-radius: 0 0 0 0.1rem;
            }
            &:last-child {
              border-radius: 0 0 0.1rem 0;
            }
          }
        }
      }
      .even {
        td {
          //background-color: #3A3366;
        }
      }
    }

    .win {
      .prize {
        color: #59d321;
        font-weight: bold;
      }
    }
    .lose {
      .prize {
        color: #e52e2e;
        font-weight: bold;
      }
    }
  }

  .progress {
    width: 1.7rem;
    display: inline-block;
    .block {
      transition: left 1s;
    }
  }
  .win {
    .progress {
      position: relative;
      .block {
        background: #0f4d11;
        border: 0.01rem solid #a2a2a2;
        box-shadow: inset 0 2px 4px 1px rgba(0, 0, 0, 0.3);
        border-radius: 0.05rem;
        width: 0.23rem;
        height: 0.16rem;
        font-size: 0.12rem;
        position: absolute;
        left: 0;
        top: -0.05rem;
        text-align: center;
        line-height: 0.14rem;
      }
    }
  }
  .lose {
    .progress {
      position: relative;
      .block {
        background: #970e0e;
        border: 1px solid #a2a2a2;
        box-shadow: inset 0 1px 3px 0 rgba(0, 0, 0, 0.3);
        border-radius: 0.05rem;
        width: 0.23rem;
        height: 0.16rem;
        font-size: 0.12rem;
        position: absolute;
        left: 0;
        top: -0.04rem;
        text-align: center;
        line-height: 0.14rem;
      }
    }
  }
}

@media screen and (max-width: 1024px) {
  .result .tab {
    height: 0.47rem;
  }
  .result .output {
    overflow-y: hidden;
    overflow-x: scroll;
  }
  .result .output table tr {
    td,
    th {
      &:first-child {
        padding-left: 0.2rem;
        width: 0.8rem !important;
      }
      &:nth-child(2) {
        width: 0.8rem !important;
      }
      &:nth-child(3) {
        display: none;
      }
      &:nth-child(4) {
        width: 0.8rem;
      }
    }
  }
  th,
  td {
    &:first-child {
      width: 1.5rem !important;
    }
    &:nth-child(2) {
      width: 2.5rem !important;
    }
    &:nth-child(3) {
      width: 1rem !important;
    }
    &:last-child {
      width: 1rem !important;
    }
  }
  .result .tab a {
    font-size: 14px;
  }
  .result .output table tr th,
  .result .output table tr td {
    font-size: 11px;
  }

  .result {
    overflow: hidden;
    height: 7.29rem;
  }
  tbody {
    padding-bottom: 0.4rem;
  }
  .result .win .progress .block {
    width: 0.4rem;
    height: 0.3rem;
    line-height: 0.3rem;
    top: -0.1rem;
  }
  .result .lose .progress .block {
    width: 0.4rem;
    height: 0.3rem;
    line-height: 0.3rem;
    top: -0.1rem;
  }
  .result .win .progress .block {
    font-size: 12px;
  }
  .result .lose .progress .block {
    font-size: 12px;
  }
  .result .output table tr th {
    font-weight: bold;
  }
}
</style>

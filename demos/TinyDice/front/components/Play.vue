<template>
  <div class="play">
    <div class="totalWin">
      <el-col
        :lg="12"
        :md="24"
        class="textLeft"
        :sm="24"
      >
        <div class="desc">
          <span style="position:relative">
            <span class="bet-left">{{$t('Play.Bet.Left')}}:</span>
            <i
              ref="effect"
              class="effect"
              :style="'color:'+ (gap>0? '#59d321':'#e52e2e')"
            >{{gap>0?'+':''}}{{gap}}</i>
          </span>&nbsp;
          <strong ref="balance">
            <span v-if="!(address && address.base58)">--</span>
          </strong>&nbsp;TRX
        </div>
      </el-col>
      <!-- <el-col
        :lg="12"
        :md="24"
        class="textLeft"
        :sm="24"
      >
        <div class="desc">
          <span style="position:relative">
            <span class="bet-left">{{$t('Play.Bet.Right')}}:</span>
            <i
              ref="effect"
              class="effect"
              :style="'color:'+ (gap>0? '#59d321':'#e52e2e')"
            >{{gap>0?'+':''}}{{gap}}</i>
          </span>&nbsp;
          <strong ref="balance">
            <span v-if="!(address && address.base58)">--</span>
          </strong>&nbsp;TRX
        </div>
      </el-col> -->
    </div>
    <div class="bet">
      <el-row :gutter="20">
        <el-col :span="12">
          <div class="tit green">{{$t('Play.Bet.Title')}}</div>
          <div class="input-group trx-input">
            <div class="input">
              <input
                type="text"
                :value="stake"
                @input="handleInput"
                @blur="handleBlur"
                name
                id
              >
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
    <div class="win">
      <div
        class="percentage"
        ref="percentage"
      >
        <span @click="handlePercentage(0.5,0)">1/2</span>
        <span @click="handlePercentage(2,1)">2X</span>
        <span @click="handlePercentage('min',2)">MIN</span>
        <span @click="handlePercentage('max',3)">MAX</span>
      </div>
    </div>
    <div class="show">
      <el-row>
        <el-col
          :span="8"
          :offset="4"
          class="roll-num"
        >
          <div class="light">
            <div class="show-number">
              <p class="title">{{$t('Play.Less')}}</p>
              <div class="lucky-num">{{number}}</div>
            </div>
          </div>
        </el-col>
        <el-col
          :span="8"
          style="position:relative;"
          class="roll-num"
        >
          <div
            class="light"
            :class="{'light1':isLight,'light2':!isLight}"
          >
            <div class="show-number">
              <p class="title">{{$t('Play.LuckNum')}}</p>
              <div
                class="lucky-num"
                :class="{'redColor': result<0 ,'greenColor': result>0}"
              >{{r ? r : '0'}}</div>
            </div>
          </div>
        </el-col>
      </el-row>
      <el-row class="slide row-1">
        <el-col :span="2" style="font-size: 30px;">1</el-col>
        <el-col :span="20">
          <el-slider
            :show-tooltip="false"
            v-model="number"
          ></el-slider>
        </el-col>
        <el-col :span="2" style="font-size: 30px;">100</el-col>
      </el-row>
      <el-row class="row-2">
        <el-col :span="8" style="margin-top: -30px;">
          <span class="title">{{$t('Play.WinTitle')}}</span>
          <span class="num">{{ expectBonuses }}</span>
        </el-col>
        <el-col :span="8" style="margin-top: -30px;">
          <span class="title">{{$t('Play.WinRate')}}</span>
          <span class="num">{{number-1}}%</span>
        </el-col>
        <el-col :span="8" style="margin-top: -30px;">
          <span class="title">{{$t('Play.PayOut')}}</span>
          <span class="num">{{odds}}X</span>
        </el-col>
      </el-row>
      <div class="row-3">
        <el-row style="width:100%">
          <el-col
            :xs="6"
            :sm="8"
            :span="8"
            class="center mobile_box"
          >
            <span class="img-box">
              <!-- 挖矿工具展示区 -->
              <el-tooltip
                effect="dark"
                placement="top-end"
              >
                <div
                  slot="content"
                  class="mining-tool-tip"
                >
                  {{ $t('Play.tronminer')}}
                </div>
              </el-tooltip>
            </span>
          </el-col>
          <el-col
            :xs="8"
            :sm="8"
            :lg="8"
            :md="8"
            class="center topSpace"
            style="margin-top: -45px;"
          >
            <el-button
              class="roll"
              @click="roll"
              :disabled="disabled"
              :class="{'disabled':disabled}"
            >{{$t('Play.Roll')}} {{ isRoll ? '...' : ''}}</el-button>
          </el-col>
          <el-col
            :xs="10"
            :sm="8"
            :lg="8"
            :md="8"
            class="center topSpace"
          >
          </el-col>
        </el-row>
      </div>
    </div>
    <tool-tip
      :toolTipVisible="toolTipVisible"
      :type="toolTipType"
      @dialogHandler="dialogHandler"
      v-if="toolTipVisible"
    ></tool-tip>
  </div>
</template>

<script>
import { mapState } from "vuex";
import { getOdds, formatTime } from "@/static/js/Util";
import { getBalance, getTransactionInfoById } from "@/assets/js/common";
import CountTo from "vue-count-to";
import bus from "~/assets/js/bus";
import { doBet } from "@/api/play.js";
import moment from "moment";
import toolTip from "./tooltip";
import api from "@/api/config";

let tronminer = api.tronminer;
export default {
  name: "Play",
  components: {
    CountTo,
    toolTip
  },
  data() {
    return {
      number: 50, // 下注骰子数 betnumber 投注数字
      disabled: false,
      stake: 10, // 下注额，初始时候绑定的最小下注额value值
      diceCount: 0, // dice(TRX20)的数量
      odds: 2, // 赔率
      expectBonuses: 20, //预计赢取
      limit: {
        min: 10,
        max: 10000
      },
      r: "",
      rolling: null,
      timer: null,
      transactionId: "",
      baseTime: new Date("2018-10-31").getTime(),
      startTime: 0,
      showTrx: parseInt(new Date().getTime() / 10000) - 12348901,
      i: 0,
      isRoll: false, // 摇骰子中...
      gap: "",
      isLight: true,
      isOpen: false, //是否自动投注
      autoBetTimer: null,
      lightTimer: null,
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
      timeoutNum: 0, // 超时计数器
      minBet: 1, //最小下注额
      toolTipVisible: false,
      toolTipType: "",
      result: 0 // 输赢结果的标识 1 赢 , -1 输。用来判断显示进度条的颜色
    };
  },
  watch: {
    number(newVal, oldVal) {
      const tip = document.querySelector(".el-slider__button-wrapper");
      if (newVal <= 2) {
        oldVal = 3;
        this.number = 2;
      } else if (newVal >= 96) {
        this.number = 96;
        oldVal = 96;
      } else {
        this.number = newVal;
      }
      tip.setAttribute("data-before", this.number);
      this.changeState(this.number);
    },
    /**
     * 账户余额
     */
    balance(n, o) {
      this.animate("balance", n, o);
      if (o != 0) {
        const effect = this.$refs["effect"];
        effect.classList.add("animate");
        effect.addEventListener("animationend", _ => {
          setTimeout(() => {
            effect.classList.remove("animate");
          }, 1000);
        });
        this.gap = (n - o).toFixed(5);
      }
    },
    myBets: {
      deep: true,
      handler(newVal, oldVal) {
        if (this.isRoll) {
          this.timeoutNum++;
          if (this.timeoutNum >= 20) {
            clearInterval(this.rolling);
            clearInterval(this.lightTimer);
            this.r = 0;
            this.disabled = false;
            this.isRoll = false;
            this.timeoutNum = 0;
            this.isOpen = false;
            // 提示服务器卡顿
            this.$alert(this.$t("timeOut"), "", {
              confirmButtonText: this.$t("Confirm"),
              callback: action => {}
            });

            return;
          }

          if (oldVal.length === 0 && newVal.length === 1) {
            this.result = newVal[0].select - newVal[0].result > 0 ? 1 : -1;
            this.clearRoll(newVal[0].result);
            return;
          }

          if (
            newVal.length > 1 ||
            (oldVal.length === 1 && newVal.length === 1)
          ) {
            let newTimeTemp = newVal[0].timestamp;
            let oldTimeTemp = oldVal[0].timestamp;
            let amount = parseInt(newVal[0].input);
            let stake = this.stake;
            let select = parseInt(newVal[0].select);
            let number = parseInt(this.number);

            if (
              newTimeTemp > oldTimeTemp &&
              amount === stake &&
              select === number
            ) {
              this.result = newVal[0].select - newVal[0].result > 0 ? 1 : -1;

              this.clearRoll(newVal[0].result);
            }
          }
        }
      }
    },
    isOpen: {
      deep: true,
      handler(val) {
        if (val) {
          this.$message({
            message: this.$t("openAuto"),
            type: "success",
            showClose: true
          });

          let time = moment().format("x");
          let obj = {
            switch: true,
            time: time
          };
          this.$store.commit("SET_AUTO_BET_TIME", obj);

          this.autoBetFun();
        } else {
          this.$message({
            message: this.$t("closeAuto"),
            type: "success",
            showClose: true
          });

          clearInterval(this.autoBetTimer);
          let time = moment().format("x");
          let obj = {
            switch: true,
            time: time
          };
          this.$store.commit("SET_AUTO_BET_TIME", obj);
        }
      }
    }
  },
  computed: {
    ...mapState([
      "address",
      "balance",
      "contractInstance",
      "random",
      "myBetsLength",
      "contractAddress",
      "dapp",
      "myBets",
      "diviend",
      "platForm",
      "trx20Account",
      "globalSunWeb",
      "globalSunWeb2",
      "loginState"
    ]),
    /**
     * 挖矿数量
     * = 挖矿效率 * 下注数量
     */
    miningDice() {
      if (this.diviend) {
        return this.diviend.StageMinRate * this.stake;
      }
    }
  },
  mounted() {
    window.roll = this.roll;
    this.my = localStorage.my ? JSON.parse(localStorage.my) : [];
    const tip = document.querySelector(".el-slider__button-wrapper");
    tip.setAttribute("data-before", this.number);

    // bus.$on("changeBalance", () => {
    //   this.watchBalance();
    // });
  },
  methods: {
    // 挖矿链接
    openMining() {
      window.open(tronminer, "_blank");
    },
    /**
     * 停止投掷骰子
     */
    clearRoll(random) {
      clearInterval(this.rolling);
      clearInterval(this.lightTimer);
      this.r = random;
      this.timeoutNum = 0;

      setTimeout(() => {
        this.gap = 0;
        this.result = 0;
        clearInterval(this.rolling);
        clearInterval(this.lightTimer);
        this.disabled = false;
      }, 5000);
      this.watchBalance();
      // bus.$emit("watchDiceBanlance");
      this.isRoll = false;

      if (this.isOpen) {
        setTimeout(() => {
          this.roll();
        }, 6000);
      }
    },
    /**
     * 计算赔率 和 预计赢取
     * 赔率 = 返现率 / 中奖率
     * 预计赢取 = 投注额度 * （ 返现率 / 中奖率 ）
     */
    changeState(num) {
      const odds = getOdds(num);
      this.odds = Math.floor(odds * 10000) / 10000;
      this.expectBonuses = Math.floor(this.stake * odds * 1000) / 1000;
    },
    /**
     * input 输入框的处理事件
     */
    handleInput(e) {
      let v = e.target.value;
      v = v.replace(/\D/g, "");
      v = Math.min(v, Math.floor(this.balance), this.limit.max);
      v = v ? v : "";
      e.target.value = v;
      this.stake = v;
      this.changeState(this.number);
    },
    /**
     * input输入框blur事件的处理
     * 保证 投注额为最小值
     */
    handleBlur(e) {
      if (e.target.value < this.minBet) {
        e.target.value = this.minBet;
        this.stake = this.minBet;
      }
      this.changeState(this.number);
    },
    /**
     * 投注数量的快捷控制
     */
    handlePercentage(p, index) {
      const cells = this.$refs["percentage"].getElementsByTagName("span");
      for (let i = 0; i < cells.length; i++) {
        cells[i].classList.remove("green");
      }
      cells[index].classList.add("green");

      let v = 0;
      switch (p) {
        case "min":
          v = this.minBet;
          break;
        case "max":
          v =
            this.balance > this.limit.max
              ? this.limit.max
              : Math.floor(this.balance);
          break;
        default:
          v = Math.floor(this.stake * p);
          break;
      }
      v = Math.min(v, Math.floor(this.balance), this.limit.max);

      if (v < this.minBet) {
        v = this.minBet;
      }

      this.stake = v;
      this.changeState(this.number);
    },
    /**
     * ROLL 摇色子事件
     */
    async roll() {
      if (!this.address.base58) {
        this.$message({
          type: "warn",
          message: 'Please Login',
          showClose: true
        });
        return;
      }
      if (this.stake == 0) {
        this.dialogPleaseInput = true;
        return false;
      }

      if (this.balance < this.minBet) {
        this.dialogNotEnough = true;
        this.$message({
          showClose: true,
          message: this.$t("NoWallet"),
          type: "warning"
        });

        return false;
      }
      if (this.disabled) return false;
      this.disabled = true;
      this.isRoll = true;
      this.animateLight(); // 跑马灯
      let transactionId = await this.contractInstance
        .bet(this.number)
        .send({
          callValue: this.globalSunWeb.sidechain.toSun(this.stake), // 投注金额,以最小单位(sun)传递
          shouldPollResponse: false //是否等待响应
        })
        .catch(err => {
          console.log(err)
          this.isOpen = false;
          this.$alert(this.$t("Play.lackOfMoneyMistakes"), "", {
            confirmButtonText: this.$t("Confirm"),
            callback: action => {}
          });
          this.isOpen = false;
          this.clearRoll(0);
          this.disabled = false;
        });

      if (!transactionId) return;

      /**
       * 掷骰子的过程中，幸运数的变动
       */
      this.rolling = setInterval(_ => {
        this.r = Math.ceil(Math.random() * 100);
      }, 50);

      this.doBetFunc(transactionId);

      this.transactionId = transactionId;
    },
    // doBet(params) {
    //   this.contractInstance["UserWin"]().watch(function(err, res) {
    //     console.log("error " + err);
    //     console.log('eventResult:',res);
    //   });
    // },
    /**
     * 绑定 交易id和下注的序号 bet index
     */
    async doBetFunc(transactionId) {
      let tInfo;
      let num = 10;
      let oTime = setInterval(async () => {
        if (--num <= 0) {
          clearInterval(oTime);
          return;
        } else {
          // 根据transactionId获取交易信息
          tInfo = await getTransactionInfoById(transactionId);

          let bet_index = 0;
          if (tInfo) {
            console.log("tInfo: ", tInfo);
            if (
              tInfo.receipt.hasOwnProperty("result") &&
              tInfo.receipt.result === "SUCCESS"
            ) {
              bet_index = parseInt(tInfo.contractResult[0] || 0, 16); // bet index 下注的序号
              let doBetParams = {
                address: this.address.base58 || "",
                // dapp_id: this.dapp,
                tx_id: transactionId, //交易id
                bet_index // number类型 下注的序号
              };
              const p1 = {
                contractAddress: api.contractAddress,
                limit: 10,
                eventName: 'UserLose'
              };
              doBet(p1)
                .then(res => {
                  let filter = res.filter(item => {
                    return item.dataMap['0'] == bet_index;
                  });
                  if (filter.length > 0) {
                    this.result = -1;
                    this.clearRoll(filter[0].dataMap._random);
                    clearInterval(oTime);
                    this.saveMyBets(filter[0]);
                  } else {
                    const p2 = {
                      contractAddress: api.contractAddress,
                      limit: 10,
                      eventName: 'UserWin'
                    };
                    doBet(p2)
                      .then(res => {
                        let filter = res.filter(item => {
                          return item.dataMap['0'] == bet_index;
                        });
                        if (filter.length > 0) {
                          this.result = 1;
                          this.clearRoll(filter[0].dataMap._random);
                          clearInterval(oTime);
                          this.saveMyBets(filter[0]);
                        }
                      });
                  } 
                  console.log("愿您猪运亨通!I hope you will win every gamble.");
                })
                .catch(err => console.log(err));
            } else if (tInfo.result === "FAILED") {
              this.isOpen = false;
              // 钱不够的提示
              this.$alert(this.$t("Play.lackOfMoneyMistakes"), "", {
                confirmButtonText: this.$t("Confirm"),
                callback: action => {}
              });
              this.clearRoll(0);
              clearInterval(oTime);
              return;
            }
          }
        }
      }, 1000);
    },
    saveMyBets(data) {
      const d = data.dataMap;
      const bet = {
        time: data.timeStamp,
        player: d[1],
        select: parseInt(d._point),
        result: this.result,
        input: d._point,
        output: d._random
      };
      this.my = localStorage.my ? JSON.parse(localStorage.my) : [];
      this.my.unshift(bet)
      localStorage.setItem(this.address.base58, JSON.stringify(this.my));
      // this.$store.commit("SET_MY_BETS", this.my);
    },
    /**
     * 更新store中balance的值
     */
    async watchBalance() {
      if (!this.address.base58) {
        return;
      }
      // const balance = await getBalance(this.address.hex);
      const balance = await this.globalSunWeb.sidechain.trx.getBalance();

      this.$store.commit("SET_BALANCE", this.globalSunWeb.sidechain.fromSun(balance));
    },
    /**
     * 新旧值变化，动画效果
     */
    animate(ref, newVal, oldVal) {
      const dom = this.$refs[ref];
      if (!dom) return;
      if (ref == "luckyPoint") {
        const item = dom.getElementsByClassName("leaf");
        for (let i = 0; i < item.length; i++) {
          if (i < newVal) {
            item[i].classList.add("active");
          } else {
            item[i].classList.remove("active");
          }
        }
      } else {
        newVal = parseFloat(newVal);
        oldVal = parseFloat(oldVal);
        const t = setInterval(() => {
          oldVal = oldVal + (newVal - oldVal) / 5;
          oldVal = Math.floor(oldVal * 100) / 100;
          this.$refs[ref].innerHTML = oldVal;
          if (Math.abs(oldVal - newVal) < 0.4) {
            clearInterval(t);
            this.$refs[ref].innerHTML = newVal;
          }
        }, 50);
      }
    },
    getTime() {
      this.i++;
      this.startTime = this.showTrx;

      let time = new Date().getTime();

      let times = (time - this.baseTime) / 6000;
      let diff = Math.floor(Math.random() * 1001 + 1000);
      if (this.i == 1) {
        this.showTrx += Math.ceil(2000 * times);
      } else {
        this.showTrx += diff;
      }

      this.showTrx += diff;
    },
    /**
     * 幸运数周围的跑马灯
     */
    animateLight() {
      this.lightTimer = setInterval(() => {
        this.isLight = !this.isLight;
      }, 300);
    },
    /**
     * 自动投注
     */
    async autoBetFun() {
      // this.autoBetTimer = setInterval(() => {
      this.roll(this.roll);
      // }, 2000);
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

<style scoped lang="scss">
.totalWin {
  font-weight: bold;
  font-size: 0.18rem;
  height: 0.5rem;
  line-height: 0.6rem;
  & > div {
    &:last-child {
      padding-left: 0.1rem;
    }
  }
  .textLeft {
    text-align: left;
    font-size: 0.15rem;
  }
  .bet-left {
    color: #d9d9d9;
    font-size: 16px;
  }
}
.topSpace {
  padding-top: 0.05rem;
}
.card-panel-num {
  min-width: 1.2rem;
  display: inline-block;
}
.green {
  // color: #e7b01a;
  color: #ffd200;
  font-family: AvenirNext;
  font-weight: bold;
}
.pink {
  // color: #ff7878;
  color: #ffd200;
  font-family: AvenirNext;
  font-weight: bold;
  font-size: 0.16rem;
}
.play {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 0.2rem 0.7rem 0;
  .tit {
    height: 0.3rem;
    line-height: 0.3rem;
    font-size: 0.16rem;
    padding-left: 0.04rem;
  }
  .input-group {
    height: 0.4rem;
    .input {
      position: relative;
      padding: 0 0.68rem 0 0.76rem;
      font-size: 0.18rem;
      input {
        width: 100%;
        height: 100%;
        background-color: transparent;
        border: none;
        outline: none;
        color: #ffd200;
        font-size: 0.2rem;
        text-align: right;
      }
    }
  }
  .trx-input {
    .input {
      &:after {
        content: "TRX";
        position: absolute;
        width: 0.68rem;
        height: 100%;
        right: 0;
        top: 0;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 18px;
      }
    }
  }
  .dice-input {
    .input {
      &:after {
        content: "dice";
        position: absolute;
        width: 0.68rem;
        height: 100%;
        right: 0;
        top: 0;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 18px;
      }
    }
  }
  .bet {
    display: flex;
    flex-direction: column;
    position: relative;
    .input-group {
      display: flex;
      border-width: 0.01rem;
      .input {
        width: 3.74rem;
        background-color: rgba(104, 37, 159, 0.8);
        background-image: url("../assets/images/logo-input.png");
        background-repeat: no-repeat;
        background-position: 0.12rem center;
        background-size: auto 70%;
        &:after {
          // @extend .green;
          color: #fff;
        }
      }
    }
    .dice-input {
      .input {
        background-color: rgba(104, 37, 159, 0.8);
        background-image: url("../assets/images/tron.png");
        background-repeat: no-repeat;
      }
    }
  }
  .win {
    margin-top: 0.1rem;
    .percentage {
      flex: 1;
      display: flex;
      // padding: 0 0.14rem;
      align-items: center;
      justify-content: space-between;
      font-size: 16px;
      color: #d9d6ff;
      font-size: 0.16rem;
      height: 0.3rem;
      // background: rgba(79, 35, 147, 0.8);
      line-height: 0.3rem;
      padding-top: 0.1rem;

      span {
        cursor: pointer;
        border: 2px solid rgba(79, 35, 147, 0.5);
        width: 100%;
        display: inline-block;
        text-align: center;
        margin-right: 0.2rem;
        color: #d9d6ff;
        font-weight: bold;
      }
      span:last-child {
        margin-right: 0rem;
      }
      span:hover {
        background: rgba(79, 35, 147, 0.5);
        color: #fff;
      }
    }
  }
  .show {
    flex: 1;
    display: flex;
    flex-direction: column;
    margin-top: 0.2rem;

    .show-number {
      background: #542099;
      border: 2px solid #9949dd;
      box-shadow: inset 0 3px 12px 0 rgba(0, 0, 0, 0.6);
      width: 1.46rem;
      height: 0.86rem;
      text-align: center;
      padding-top: 0.1rem;
      box-sizing: border-box;
      font-size: 0.12rem;
      margin: 0 auto;
      position: relative;
      top: 0.05rem;
      .lucky-num {
        font-size: 0.42rem;
        font-weight: bold;
        font-family: AvenirNext;
      }
      .greenColor {
        color: #59d321;
      }
      .redColor {
        color: red;
      }
    }

    .light {
      width: 1.74rem;
      height: 1.14rem;
      position: relative;
      position: relative;
      z-index: 2;

      .show-number {
        position: absolute;
        left: 0;
        top: 0;
        right: 0;
        bottom: 0;
        margin: auto;
      }
    }

    .light1 {
      background: url("../assets/images/light1.png");
      background-size: 100% 100%;
    }

    .light2 {
      background: url("../assets/images/light2.png");
      background-size: 100% 100%;
    }

    .border {
      width: 1.6rem;
      height: 1rem;
      position: absolute;
      left: 0;
      top: 0;
      right: 0;
      bottom: 0;
      margin: auto;
      border: 2px solid #652e8e;
      opacity: 0.8;
      z-index: 1;
    }

    .slide {
      margin-top: 0.3rem;
      line-height: 0.5rem;
      .el-col:first-child {
        text-align: right;
        padding-right: 0.05rem;
      }
      .el-col:last-child {
        padding-left: 0.05rem;
      }
    }
    .row-2 {
      display: flex;
      position: relative;
      padding: 0 0.25rem;
      align-items: center;
      background: #45229f;
      height: 0.56rem;
      line-height: 0.25rem;
      margin: 0.1rem 0 0;
      .el-col {
        text-align: center;
      }
      .title {
        color: rgba(255, 255, 255, 0.8);
        font-size: 0.16rem;
      }
      .num {
        color: #ffc559;
        font-size: 0.17rem;
        font-weight: bold;
        display: block;
      }
    }
    .row-3 {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      .el-button {
        font-family: Arial-Black;
        width: 1.56rem;
        height: 0.72rem;
        margin: 0 auto;
        font-size: 0.26rem;
        background-image: url("../assets/images/roll-button.png");

        background-color: transparent;
        background-size: 100% 100%;
        border: none;
        color: #ffffff;
        font-weight: bold;
        cursor: pointer;
        box-shadow: 2px 2px 5px 3px rgba(58, 66, 156, 0.5);
        padding: 0;
        outline: 0 none !important;
        border-radius: none;
        text-shadow: 0 1px 2px #0f5211;
      }
      .el-button.disabled {
        background-image: url("../assets/images/btn_disabled.png");

        background-color: transparent;
        background-size: 100% 100%;
      }
    }
  }
}

.effect {
  position: absolute;
  top: -100%;
  right: -100%;
  font-weight: bold;
  opacity: 0;
  font-size: 16px;
}
.effect.lose {
  color: #e52e2e;
}
.effect.win {
  color: #59d321;
}
.effect.animate {
  animation: animate 2s ease;
}
@keyframes animate {
  0% {
    top: -150%;
    opacity: 1;
  }
  99% {
    top: -270%;
    opacity: 0.5;
  }
  100% {
    top: -280%;
    opacity: 0;
  }
}

button {
  outline: none;
}

.center {
  text-align: center;
}

.img-box {
  width: 100%;
  display: inline-block;
  height: 0.72rem;
  line-height: 0.72rem;
}
.img-box.animate1 {
  animation: animate1 2s ease;
}
.mining-bot-img {
  width: 60%;
  height: 100%;
  cursor: pointer;
}
.mining-tool-tip {
  font-size: 20px;
}
@keyframes animate1 {
  0% {
    opacity: 0;
  }
  100% {
    opacity: 1;
  }
}

@media screen and (max-width: 1024px) {
  .play {
    padding: 0.2rem 0.4rem 0;
    .show {
      & > div {
        .title {
          font-size: 12px;
        }
        &:first-child {
          .show-number {
            width: 100%;
            height: 100%;
          }
          & > div {
            &:last-child {
              .light {
                display: flex;
                align-items: center;
                justify-content: center;
                background-image: none;
                /*.show-number{*/
                /*width: 80%;*/
                /*height: 80%;*/
                /*}*/
              }
            }
          }
        }
      }
    }
  }
  .totalWin {
    font-size: 12px;
    & > div {
      &:last-child {
        padding: 0;
      }
      span {
        font-size: 14px !important;
      }
    }
    .balance {
      font-size: 14px !important;
    }
  }
  .play .tit {
    font-size: 12px;
    margin-bottom: 0.1rem;
  }
  .play .bet .desc {
    font-size: 12px;
  }
  .play .show .row-1 .cell .t {
    font-size: 12px;
  }
  .play .show .row-2 .line .cell {
    font-size: 12px;
  }
  .play .show .row-3 button {
    font-size: 18px;
  }

  button {
    outline: 0;
  }
  .play .bet .input-group .input {
    width: 100%;
  }

  .totalWin {
    height: auto;
    line-height: 0.7rem;
    margin-bottom: 0.1rem;
    .textLeft {
      font-size: 12px !important;
    }
  }
  .play .input-group {
    height: 0.5rem;
  }
  .play .input-group .input input {
    font-size: 14px;
  }
  .play .show .border {
    left: -0.26rem;
  }
  .play .show .slide {
    margin-top: 0.3rem 0 0.1rem;
  }
  .play .show .row-3 {
    align-items: baseline;
  }
  .play .show .row-2 {
    margin: 0rem;
    padding: 0;
    margin-bottom: 0.15rem;
    margin-top: 0.2rem;
    .title {
      font-size: 12px;
    }
    .num {
      font-size: 12px;
    }
  }

  .play .show .light {
    width: 2.4rem;
    height: 1.24rem;
  }

  .play .show {
    .el-col-offset-4 {
      margin-left: 8.33333%;
    }
    .roll-num {
      width: calc((100% / 24) * 10);
    }
    .show-number .lucky-num {
      font-size: 0.5rem;
    }
  }
  .play .show .row-2 {
    height: 0.8rem;
    line-height: 0.3rem;
  }
  .play .win .percentage span {
    font-size: 12px;
  }
  .play .show .slide.row-1 {
    font-size: 12px;
    line-height: 0.8rem;
  }
  .img-box {
    font-size: 12px;
    .el-icon-question {
      font-size: 14px;
    }
  }
  .play .win .percentage {
    height: 0.4rem;
    line-height: 0.4rem;
    margin: 0.1rem 0;
  }
  .play .show .row-3 .el-button {
    width: 1.8rem;
    height: 0.8rem;
  }
  .dice-input {
    .input {
      &:after {
        font-size: 12px !important;
      }
    }
  }
  .bet {
    .input {
      &:after {
        font-size: 12px !important;
      }
    }
  }
  .topSpace {
    padding-top: 0 !important;
  }
}
</style>

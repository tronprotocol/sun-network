<template>
  <div>
    <div class="container">
      <my-nav :languageGroup="languageGroup" />
      <div class="main">
        <div
          class="cell"
          style="margin-top:0.2rem;">
          <div class="intro">
            <div class="intro-l">
              <p style="font-size:0.26rem;font-weight:600;color:#afafaf;">DApp Demo#1</p>
              <p style="font-size:0.38rem;font-weight:600;color:#484848;margin-top:30px;">Donate — Pets Need U</p>
              <p style="font-family: STSongti-SC;font-size:0.18rem;color:#c3bdbd;">{{totalDonate | toLocaleString}}  TRX Donated </p>
             
              <p class="p-text" style="margin-top: 50px;">Look at these stray animals, they need your help.</p>
              <p class="p-text">Your donation will help give animals a voice and make a greater impact where it is needed most.</p>
              <p class="p-text">We invite you to leave a story about your help to these stray animals and donate a little love.</p>
            </div>
            <div class="intro-r">
               <img :src="require('../../../assets/images/pet.jpg')" alt="Pet Png">
            </div>
          </div>
          <div class="input-div">
            <el-input placeholder="Share your story" v-model.trim="story" style="width: 80%;"></el-input>
            <el-input placeholder="Donate with love" v-model.trim="amount" style="width: calc(80% - 0.5rem);margin-top: 20px;"></el-input> <span style="font-size:0.2rem; color: #777777;">TRX</span>
            <span @click="donate()" class="donate-btn">Let’s Do it</span>
          </div>
          <div class="balance-div">
            <span>My Balance:   <span>main-chain <strong>{{mBalance | toLocaleString}}</strong> TRX</span> <span style="margin-left: 40px;"> side-chain <strong>{{balance | toLocaleString}}</strong> TRX</span></span>
          </div>
        </div>
        <div class="all">
          <div class="title">
            <span class="divider"></span>
            <span class="text">All Donations</span>
            <span class="divider"></span>
          </div>
          <div v-if="allDonations.length > 0">
            <div v-for="(item, index) in allDonations" :key="'all' + index">
              <div style="box-shadow: 0 2px 4px 0 rgba(0, 0, 0, 0.15);padding: 15px 20px;margin-top: 15px;">
                <div class="addr-amont">
                  <span class="addr">{{item.address}}</span>
                  <span class="amont">{{item.amount}} TRX</span>
                </div>
                <div class="story"><span>"</span>{{item.story}}<span>"</span></div>
              </div>
            </div>
          </div>
          <div v-else class="empty-text">No Donation.</div>
        </div>
      </div>
      <Footer />
    </div>
  </div>
</template>

<script>
import { mapState } from "vuex";
import MyNav from "~/components/MyNav.vue";
import { getTransactionInfoById } from "@/assets/js/common";

import { getBalance, getaccount } from "~/assets/js/common";
import bus from "~/assets/js/bus";
import interfaceData from "@/api/config";
import SunWeb from 'sunweb';
import { clearInterval, setInterval } from 'timers';

let contractAddress = interfaceData.contractAddress;

export default {
  components: {
    MyNav
  },

  data() {
    return {
      contractAddress,
      rollBtnDisabled: false,
      lastRollNum: null,
      contractInstance: null,
      languageGroup: [
        { lng: "en", txt: "English" },
        // { lng: "ch", txt: "Chinese" }
      ],
      amount: '',
      totalDonate: '--',
      story: '',
      allDonations: [],
      intervalAll: null,
      intervalTotal: null
    };
  },
  created() {
    const sunWeb = new SunWeb(interfaceData.mainOptions, interfaceData.sideOptions, interfaceData.mainGatewayAddress, interfaceData.sideGatewayAddress, interfaceData.chainId);
    this.$store.commit('SET_SUNWEB', sunWeb);
  
  },
  watch: {
    "globalSunWeb.mainchain.defaultAddress": {
      deep: true,
      handler(newVal, oldVal) {
        if (
          newVal &&
          oldVal &&
          newVal.base58 &&
          oldVal.base58 &&
          newVal.base58 !== oldVal.base58
        ) {
          window.location.reload(true);
        }
      }
    }
  },
  computed: {
    ...mapState(["globalSunWeb", "address", "balance", "mBalance", "donateIndex"])
  },
  async mounted() {
      /**
       * 合约初始化
       */
      const contractInstance = await this.globalSunWeb.sidechain
        .contract()
        .at(contractAddress);
      this.contractInstance = contractInstance;
      this.$store.commit("SET_CONTRACT_INSTANCE", contractInstance);
      setTimeout(()=>{this.getAllDonations()}, 0);
      this.interTotal = setInterval(async () => {
        this.getAllDonationAmount();
      }, 1000);

  },
  methods: {
    async getAllDonationAmount() {
      if (!this.address.base58) {
        return;
      }

      const self = this;
      let result = await this.contractInstance
        .totalDonation()
        .call()
        .catch(err => {
          console.log(err)
          self.$message.error(err);
          return;
        });
        let total = parseInt(result._hex.replace('/^0x/', '') || 0, 16);
        this.totalDonate = this.globalSunWeb.sidechain.fromSun(total);
    },
    async getAllDonations() {
      if (!this.address.base58) {
        setTimeout(() => { this.getAllDonations() }, 1000);
        return;
      }
      const self = this;
      let result = await this.contractInstance
        .index()
        .call()
        .catch(err => {
          console.log(err)
          self.$message.error(err);
          return;
        });

      if (!result) return;
      let donateIndex = parseInt(result._hex.replace('/^0x/', '') || 0, 16);
      let num = 1;
      let arr = [];
      while(donateIndex > 0) {
        num++;
        if (num > 20) break;
        let record = await this.contractInstance
          .check(donateIndex)
          .call()
          .catch(err => {
            console.log(err)
            self.$message.error(err);
            return;
          });
          if (record) {
            const temp = {
              address: self.globalSunWeb.sidechain.address.fromHex(record[0]),
              amount: parseInt(record[1]._hex.replace('/^0x/', '') || 0, 16),
              story: record[2],
              index: donateIndex
            }
            temp.amount = this.globalSunWeb.sidechain.fromSun(temp.amount);
            const filter = self.allDonations.filter(item => {
              return item.index == donateIndex;
            });
            if (filter.length === 0) {
              self.allDonations.push(temp);
            }
          }
          --donateIndex;
      }
      this.allDonations = this.descSort(this.allDonations, 'index');
      setTimeout(() => {
        this.getAllDonations()
      }, 1000);
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
    async donate() {
      if (!this.address.base58) {
        this.$message({
          type: "success",
          message: this.$t("noLogin"),
          showClose: true
        });
        return;
      }

      const amount = parseInt(this.amount);
      if (isNaN(amount) && amount <= 0) {
        this.$message({
          type: "success",
          message: "Please input valid donate TRX",
          showClose: true
        });
        return;
      }
      const story = this.story;
      const self = this;
      let transactionId = await this.contractInstance
        .donate(this.story)
        .send({
          callValue: this.globalSunWeb.sidechain.toSun(amount), // 投注金额,以最小单位(sun)传递
          shouldPollResponse: false //是否等待响应
        })
        .catch(err => {
          console.log(err)
          self.$message.error(err);
          return;
        });

      if (!transactionId) return;
      this.checkResult(transactionId);

    },
    checkResult(transactionId) {
      const self = this;
      let tInfo;
      let num = 10;
      let oTime = setInterval(async () => {
        if (--num <= 0) {
          clearInterval(oTime);
          return;
        } else {
          tInfo = await getTransactionInfoById(transactionId);
          let donateIndex = 0;
          if (tInfo) {
            console.log("tInfo: ", tInfo);
            if (
              tInfo.receipt.hasOwnProperty("result") &&
              tInfo.receipt.result === "SUCCESS"
            ) {
              self.getAllDonations()
              this.$message({
                type: "success",
                message: 'Donate Successfully. Thanks for your kindness.',
                showClose: true
              });
              donateIndex = parseInt(tInfo.contractResult[0] || 0, 16);
              self.getBalance();
              if (oTime) clearInterval(oTime);
            }
          }
      }}, 1000);
    },
    async getBalance() {
       if (!this.address.base58) {
        return;
      }
      const balance = await this.globalSunWeb.sidechain.trx.getBalance();
      this.$store.commit("SET_BALANCE", this.globalSunWeb.sidechain.fromSun(balance));
      const mBalance = await this.globalSunWeb.mainchain.trx.getBalance();
      this.$store.commit('SET_MBALANCE', this.globalSunWeb.mainchain.fromSun(mBalance));
    }
  }
};
</script>

<style lang="scss">
.container {
  font-family: Baskerville;
  // min-height: 120vh;
  display: flex;
  flex-direction: column;
  padding-bottom: 0.2rem;
  a {
    color: #b3a6ff;
  }
  .banner {
    margin-top: 0.3rem;
    width: 12rem;
    margin: 0 auto;
  }
  .main {
    width: 12rem;
    flex: 1;
    margin: auto;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
  }
  .intro {
    display: flex;
    justify-content: space-between;
  }
  .p-text {
     font-family: STSongti-SC;
     font-size: 0.2rem;
     color: #8d8d8d;
     margin-top: 20px;
  }
  .input-div {
    font-size: 0.2rem;
    font-family: STSongti-SC;
    position: relative;
    .el-input__inner {
      border-top: none;
      border-left: none;
      border-right: none;
      border-radius: 0;
      font-size: 0.2rem;
      font-family: STSongti-SC;
      color: #cacaca;
    }
    .donate-btn {
      position: absolute;
      right: 0;
      width: calc(20% - 30px);
      background: #bb0012;
      display: inline-block;
      height: 0.6rem;
      line-height: 0.6rem;
      text-align: center;
      color: white;
      font-size: 0.2rem;
      cursor: pointer;
      &:hover {
        color: blue;

      }
    }
  }
  .balance-div {
    font-family: STSongti-SC;
    font-size: 0.14rem;
    color: #cacaca;
    margin-top: 15px;
  }
  .all {
    text-align: center;
    .divider {
      display: inline-block;
      width: 0.8rem;
      border-bottom: 1px solid #e8e8e8;
      vertical-align: middle;
    }
    .text {
      font-size: 0.22rem;
      color: #8d8d8d;
      vertical-align: middle;
    }
    .addr-amont {
      display: flex;
      justify-content: space-between;
      margin-top: 10px;
    }
    .addr {
      font-size: 0.16rem;
      color: #8d8d8d;
    }
    .amont {
      font-size: 0.18rem;
      color: #a0a0a0;
    }
    .story {
      font-size: 0.18rem;
      color: #222222;
      text-align: left;
      margin-top: 10px;
    }
  }
  .empty-text {
    font-size: 0.18rem;
    color: #bababa;
  }
}

@media screen and (max-width: 1024px) {
  .el-icon-question {
    font-size: 14px;
  }
  .container .main > .cell:nth-child(2) .rank-content .col {
    height: 100%;
  }
  .container {
    .main {
      padding: 0.7rem 0.2rem 0;
      width: 100%;
    }
  }
}
</style>

<!--
 * @Description: Bonus 分红记录
 * @Author: weiqinl
 * @Date: 2019-01-17 15:24:12
 * @LastEditors: weiqinl
 * @LastEditTime: 2019-02-15 19:45:29
 -->
<template>
  <div class="bonus-block">
    <div class="bonus-title">{{$t('DividingPool.bonusTitle')}}</div>
    <div class="bonus-num">{{dividendTotal | fixed(3)}} TRX</div>
    <div class="bonus-list">
      <div class="bonus-list-item">
        <div class="bonus-list-item-left">{{$t('DividingPool.bonusTime')}}</div>
        <div class="bonus-list-item-right">{{$t('DividingPool.bonusNum')}}</div>
      </div>
      <div
        class="bonus-list-item"
        v-for="(item, index) in list"
        :key="index"
      >
        <div class="bonus-list-item-left">{{item.newDate}}(UTC)</div>
        <div class="bonus-list-item-right">{{item.bonusAmount/Math.pow(10,6) | toLocaleString}} TRX</div>
      </div>
    </div>
  </div>
</template>
<script>
import { mapState } from "vuex";
import { queryBonusRecord } from "~/api/dividend";
import moment from "moment";
export default {
  data() {
    return {
      dividendTotal: 0, // 我的分红累计
      list: []
    };
  },
  computed: {
    ...mapState(["address", "trx20AccountInfo", "globalTronWeb"])
  },
  mounted() {
    if (this.trx20AccountInfo.balanceOf) {
      this.queryBonusRecord(); // 分红记录
    }
  },
  methods: {
    /**
     * 分红记录
     */
    queryBonusRecord() {
      queryBonusRecord({
        address: this.address.base58
      })
        .then(response => {
          this.list = response.data;
          for (let i in this.list) {
            let item = this.list[i];
            let date = moment(item.date, "YYYY.MM.DD HH:mm:ss")
              .set("hour", 8)
              .utc()
              .format("YYYY-MM-DD HH:mm:ss");
            this.$set(item, "newDate", date);
          }
          this.dividendTotal = this.globalTronWeb.fromSun(
            response.totalBonusAmount
          );
        })
        .catch(err => {
          console.log("queryBonusRecord", err);
        });
    }
  }
};
</script>
<style lang="scss">
.bonus-block {
  .bonus-title {
    font-size: 0.18rem;
    margin: 0.29rem 0 0.11rem;
    text-align: left;
  }
  .bonus-num {
    font-size: 0.22rem;
    color: #fff16b;
    font-weight: bold;
    margin-bottom: 0.19rem;
    position: relative;
    padding-left: 0.3rem;
    height: 0.5rem;
    text-align: left;
    line-height: 0.5rem;
  }
  .bonus-num::before {
    content: "";
    width: 0.22rem;
    height: 0.5rem;
    position: absolute;
    background-image: url("/images/bonus-icon.png");
    background-repeat: no-repeat;
    background-position: center;
    background-size: auto;
    left: 0;
    top: -0.02rem;
  }
  .bonus-list {
    width: 100%;
    height: 4.4rem;
    margin-bottom: 0.3rem;
    background: rgba(29, 7, 78, 0.32);
    border-radius: 3px;
    overflow: auto;
  }
  .bonus-list-item {
    height: 0.43rem;
    border-bottom: 1px solid rgba(255, 255, 255, 0.2);
    line-height: 0.43rem;
  }
  .bonus-list-item-left {
    float: left;
    text-align: center;
    width: 50%;
  }
  .bonus-list-item-right {
    float: left;
    text-align: center;
    width: 50%;
  }
}
</style>
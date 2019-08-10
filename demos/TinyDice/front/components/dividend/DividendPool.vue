<!--
 * @Description: everyday dividend pool 每日分红池
 * @Author: weiqinl
 * @Date: 2019-01-17 15:18:06
 * @LastEditors: weiqinl
 * @LastEditTime: 2019-02-18 10:55:05
 -->
<template>
  <div class="dividend-block">
    <div class="block-title">{{$t('DividingPool.name')}}</div>
    <div class="dividend-info clearfix">
      <div class="content">
        <wave-canvas
          :width="canvasWidth"
          :height="canvasHeight"
          :range="range"
        />
        <div class="bg">
          <div class="num">
            <p>{{ dailyDividendPool}}</p>
            <p>TRX</p>
          </div>
          <!-- <p class="count">{{$t('countDown.name')}}00:00:00</p> -->
        </div>
      </div>
      <!-- 右侧显示dice质押总量与预期收益 -->
      <div class="dividen-info-show">
        <div class="dice-totalNum">
          <span>{{$t('DividingPool.ledgeNum')}}：</span>
          <div>{{$t('DividingPool.ledgeUpdate')}}</div>
          <span class="specWord">{{totalFreeze | toLocaleString}}</span>
        </div>
        <div class="dice-earnings">
          <span>{{$t('DividingPool.standardNum')}}：</span>
          <span class="specWord">{{expected | toLocaleString}} TRX</span>
        </div>
        <div class="dice-earnings">
          <span>{{$t('DividingPool.earnings')}}：</span>
          <span class="specWord">{{expectedBonus | toLocaleString}} TRX</span>
        </div>
      </div>
    </div>
    <div class="desc">
      <p
        v-for="(item,index) in $t('DividingPool.content')"
        :key="index"
        v-html="item"
      ></p>
    </div>
  </div>
</template>
<script>
import { mapState } from "vuex";
import moment from "moment";
import WaveCanvas from "@/components/canvas/WaveCanvas";
export default {
  components: {
    WaveCanvas
  },
  computed: {
    ...mapState(["diviend", "trx20AccountInfo", "address", "globalTronWeb"]),
    range() {
      let range = 0;
      if (this.diviend.PoolSize) {
        let trx = this.globalTronWeb.fromSun(this.diviend.PoolSize);
        // 当前分红池数量/总量
        range = (trx / this.totalTrx) * 100;
      }
      // 固定一个数，1.好看。2. 实际比例太小
      return 50;
    }
  },
  data() {
    return {
      dailyDividendPool: 0, // 每日分红池数据
      totalFreeze: 0, // dice质押总量
      expectedBonus: 0, // 我的预期收益
      expected: 0, // 每十万份dice预期trx收益
      totalTrx: 7 * Math.pow(10, 6),
      list: [],
      canvasHeight: "180px",
      canvasWidth: "180px",
      hr: "00",
      min: "00"
    };
  },
  mounted() {
    // balanceOf 余额 && dividend,为了获取到值
    if (this.trx20AccountInfo.balanceOf && this.diviend) {
      this.getData();
    }
  },
  methods: {
    /**
     * totalFreeze查询dice质押总量，从挖矿的接口获取
     * expected每十万份dice预期trx收益 =  当天分红池数量 * (10万dice / dice质押总量 )
     * expectedBonus我的预期收益 = 我质押的dice/ dic质押总量 * 当天分红池数量
     */
    async getData() {
      const dividendData = this.diviend;
      const hash = await this.trx20AccountInfo
        .freezeBalance(this.address.base58)
        .call();
      this.dailyDividendPool =
        dividendData.PoolSize === 0
          ? 0
          : Number(this.globalTronWeb.fromSun(dividendData.PoolSize)).toFixed(
              3
            );
      this.totalFreeze = Number(
        this.globalTronWeb.fromSun(dividendData.totalFreeze)
      ).toFixed(0);
      if (dividendData.totalFreeze === 0 || dividendData.PoolSize === 0) {
        return;
      } else {
        this.expected = this.globalTronWeb.fromSun(
          dividendData.PoolSize *
            ((Math.pow(10, 5) * Math.pow(10, 6)) / dividendData.totalFreeze)
        );
        this.expectedBonus = this.globalTronWeb.fromSun(
          (hash.toString() / dividendData.totalFreeze) * dividendData.PoolSize
        );
      }
    }
  }
};
</script>
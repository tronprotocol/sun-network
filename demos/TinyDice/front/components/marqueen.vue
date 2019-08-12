<template>
  <div>
    <marquee class="carousel" style="vertical-align:center;">
      <span v-for="(item,index) in successRecord" :key="index" class="imgSpan">
        <span
          v-html="$t('marquee',{player:hiddenAddress(item.player),trx:parseFloat(item.output).toLocaleString(),level:getOdds(item.select).toFixed(4)})"
        ></span>
      </span>
    </marquee>
  </div>
</template>
<script>
import { mapState } from "vuex";
import { getOdds, hiddenAddress } from "~/static/js/Util";

export default {
  data() {
    return {
        
    };
  },
 
  computed: {
    ...mapState(["successRecord"])
    
  },
  watch: {},
  methods: {
    getOdds(val) {
      return getOdds(val);
    },
    hiddenAddress(val) {
      return hiddenAddress(val);
    }
  },
  mounted(){

  }
};
</script>
<style lang="scss">

.carousel {
  line-height: 0.6rem;
  background: url("../assets/images/marqueen_bg.png") no-repeat;
  display: block;
  margin-top: 0.38rem;
  font-size: 18px;
  color: #ffd200;
  // font-weight: bold;

  .num {
    color: #59d321;
    font-family: AvenirNext;
    margin-right: 0;
  }
  .imgSpan {
    display: inline-block;
    position: relative;
    margin-right: 0.5rem;
    height: 100%;
    &::before {
      background: url("../assets/images/amount.png") no-repeat 50% 50%;
      content: " ";
      left: -0.3rem;
      top: 0rem;
      bottom: 0;
      margin: auto;
      width: 0.25rem;
      height: 0.25rem;
      display: inline-block;
      position: absolute;
      border-radius: 100%;
    }
  }
}
@media screen and (max-width: 1024px) {
  .carousel {
    position: relative;
    top: 0.8rem;
    font-size: 14px;
  }
}
</style>

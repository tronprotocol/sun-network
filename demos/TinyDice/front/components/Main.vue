<template>
  <div class="main">
    <Logo />
    <Play
      :balance="balance"
      :odds="odds"
      :win="win"
      :stake="stake"
      :myRewardPool="myRewardPool"
      :rollBtnDisabled="rollBtnDisabled"
      :lastRollNum="lastRollNum"
      :contractBalance="contractBalance"
      :endVal="endVal"
      :startVal="startVal"
      @changeMount="changeMount"
      @sendNumber="sendNumber"
      @withDraw="withDraw"
      @roll="roll"
      @percentage="percentage" />
    <Result :result="result" @rankTime="rankTime" :rankTableLoading="rankTableLoading"/>
  </div>
</template>

<script>
  import Logo from '~/components/Logo.vue'
  import Play from '~/components/Play.vue'
  import Result from '~/components/Result.vue'
  export default {
    props:['balance','odds','stake','win','myRewardPool','result','lastRollNum','rollBtnDisabled','contractBalance','rankTableLoading','endVal','startVal'],
    components: {
      Logo,Play,Result
    },
    name: "Main",
    methods:{
      changeMount(mount){
        this.$emit('changeMount',mount);
      },
      sendNumber(number,oldVal){
        this.$emit('sendNumber',number,oldVal);
      },
      withDraw(number){
        this.$emit('withDraw',number);
      },
      roll(number){
        this.$emit('roll',number)
      },
      percentage(per){
        this.$emit('percentage',per);
      },
      rankTime(time){
        this.$emit('rankTime',time)
      }
    }
  }
</script>

<style scoped lang="scss">
  .main{
    width:1043px;
    margin:0 auto;
  }
  .marquee{
    color: #5BD4D7;
    /*background: #fff;*/
  }
</style>

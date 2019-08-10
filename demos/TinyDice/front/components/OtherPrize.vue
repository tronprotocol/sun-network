<template>
    <div class="other-prize">
        <div class="desc">
            <div class="r1">
                {{$t('OtherPrize.Txt1')}}3.5454 DICE
            </div>
            <div class="r2">
                {{$t('OtherPrize.Txt2')}}
            </div>
        </div>
        <img :src="require('../assets/images/other-prize.png')" alt="" />
    </div>
</template>

<script>
    export default {
        name: "OtherPrize"
    }
</script>

<style scoped lang="scss">
    .other-prize{
        width: 6.2rem;
        height: .8rem;
        background-image: linear-gradient(91deg,
                #5b60d6 0%,
                #5b37b6 100%),
        linear-gradient(
                        #a8abe4,
                        #a8abe4);
        background-blend-mode: normal,
        normal;
        border-radius: .1rem;
        display: flex;
        flex-direction: row;
        padding:0 .3rem 0 .26rem;
        justify-content: space-between;
        align-items: center;
        .desc{
            flex:1;
            display: flex;
            flex-direction: column;
            .r1{
                font-size: .18rem;
            }
            .r2{
                font-size: .14rem;
                color: #64e1f6;
            }
        }
        img{
            width: .53rem;
            height: .55rem;
        }
    }
</style>
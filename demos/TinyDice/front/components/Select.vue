<template>
    <div class="select">
        <div class="tab focus">
            <img :src="require('../assets/images/logo-trx.png')" alt="">
            <span>TRX</span>
        </div>
        <div class="tab">
            <img :src="require('../assets/images/logo-dice.png')" alt="">
            <span>DICE</span>
        </div>
    </div>
</template>
<script>
    export default {
        name: "Select",
        data(){
            return {

            }
        },

        methods:{

        }
    }
</script>

<style scoped lang="scss">
    .select{
        height: 1.2rem;
        display: flex;
        flex-direction: row;
        align-items: center;
        justify-content: center;
        .tab{
            cursor: pointer;
            font-size: .15rem;
            width: 1.23rem;
            height: .38rem;
            background-color: #363995;
            border-radius: .19rem;
            display: flex;
            justify-content: center;
            align-items: center;
            img{
                width: .16rem;
            }
            span{
                margin-left: .06rem;
            }
            &:last-child{
                margin-left: .3rem;
            }
        }
        .tab.focus{
            background-image: linear-gradient(142deg,
                    #2babf5 0%,
                    #4786f9 50%,
                    #6260fd 100%),
            linear-gradient(
                            #de5cff,
                            #de5cff);
            background-blend-mode: normal,
            normal;
            border-radius: 19px;
        }
    }
</style>
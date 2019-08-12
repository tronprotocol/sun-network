<template>
    <div class="footer">
        <div class="link">
            <el-button class="how" @click="dialogService = true">{{$t('ServiceItem')}}</el-button>
            <el-dialog
                    :title="$t('ServiceItem')"
                    :visible.sync="dialogService"
                    width="5.8rem"
                    custom-class="how-dialog">
                <p v-html="$t('Service.P1')"></p>
                <p v-html="$t('Service.P2')"></p>
                <p v-html="$t('Service.P3')"></p>
                <p v-html="$t('Service.P4')"></p>
                <span slot="footer" class="dialog-footer">
            <el-button type="primary" @click="dialogService = false">{{$t('Confirm')}}</el-button>
          </span>
            </el-dialog>
            <a href="javascript:;"></a>
            <span>|</span>
            <a target="_blank" href="https://t.me/joinchat/JUyvA1KP9kU2qfwtESfvpg">{{$t('Telegram')}}</a>
        </div>
        <div class="copyright">
            Copyright © TRONGAME.TOP All Rights Reserved.
        </div>
    </div>
</template>

<script>
    export default {
        name: "Footer",
        data(){
            return {
                dialogService:false
            }
        }
    }
</script>

<style scoped lang="scss">
    .footer{
        height: 1.1rem;
        background-color: #332C66;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        font-size: .16rem;
        .link{
            display: flex;
            flex-direction: row;
            span{
                margin:0 .15rem;
            }
        }
        .copyright{
            font-size: .14rem;
            margin-top: .12rem;
        }
    }
</style>
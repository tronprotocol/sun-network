<template>
  <div class="token">
    <div class="row">
      <h3 class="tokenLabel">{{i18n.title}}</h3>
      <VueInput v-model="addr" :placeholder="i18n.placeholder" class="big addr" />
      <VueButton class="primary big" :label="i18n.submit" @click="token" />
      <div class="tokenRet" v-if="showRes">
        <VueIcon :icon="tokenRet ? 'done' : 'error'" />
        {{tokenRet ? i18n.submitDone : i18n.submitError}}
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'token',
  data() {
    return {
      addr: '',
      showRes: false,
      tokenRet: false,
      i18n: {
        title: 'Receive 10,000 Test (TRX) Coins',
        placeholder: 'Test wallet address',
        submit: 'SUBMIT',
        submitDone: 'Submitted successfully',
        submitError: 'Incorrect address or reach daily limit'
      }
    };
  },
  mounted() {
    if (this.$route.path.indexOf('/zh/') == 0) {
      this.i18n = {
        title: '获取10000测试币',
        placeholder: '测试网钱包地址',
        submit: '提交',
        submitDone: '提交成功',
        submitError: '地址错误或达到每日获取上限'
      };
    }
  },
  methods: {
    token: function() {
      let dataUrl = '';
      if (process.env.NODE_ENV == 'development') {
        dataUrl = 'http://localhost:3000';
      }
      if (process.env.GITHUB == 'github') {
        dataUrl = 'https://tron.network';
      }
      this.$axios
        .post(`${dataUrl}/sunnetwork/token`, {
          addr: this.addr
        })
        .then(_ => {
          this.tokenRet = _.data.ok;
          this.showRes = true;
        })
        .catch(error => {
          this.tokenRet = false;
          this.showRes = true;
        });
    }
  }
};
</script>

<style lang="stylus" scoped>
.token {
  width: 100%;
  text-align: center;
  padding: 10px 0;

  .tokenLabel {
    color: lighten($textColor, 10%);
  }

  .addr {
    min-width: 25rem;
  }

  .tokenRet {
    padding-top: 10px;
  }
}
</style>

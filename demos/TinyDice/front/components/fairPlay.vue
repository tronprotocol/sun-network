<!--
 * @Description: 玩法与公平 (Rules and Fair Play )
 * @Author: weiqinl
 * @Date: 2019-01-11 19:35:32
 * @LastEditors: weiqinl
 * @LastEditTime: 2019-01-28 19:58:27
 -->
<template>
  <el-dialog
    :title="$t('FairPlay.button')"
    :visible.sync="showDialog"
    width="6.5rem"
    custom-class="how-dialog fair-play"
    @close="dialogHandler"
  >
    <el-form
      label-position="top"
      :model="formSeeds"
      class="fair-play-form"
    >
      <el-form-item :label="$t('FairPlay.description.default')">
        <el-input
          v-model="formSeeds.default"
          disabled
        >
        </el-input>
      </el-form-item>
      <el-form-item :label="$t('FairPlay.description.custom')">
        <el-input
          v-model="formSeeds.custom"
          @input="changeSHA"
          class="fair-play-input"
        ></el-input>
      </el-form-item>
      <el-form-item :label="$t('FairPlay.description.new')">
        <el-input
          v-model="formSeeds.new"
          disabled
        ></el-input>
      </el-form-item>
      <el-form-item
        size="medium"
        class="footer"
      >
        <el-button
          type="primary"
          @click="updateSHA"
        >{{$t('FairPlay.description.button')}}
        </el-button>
      </el-form-item>
    </el-form>
    <div class="fair-play-description">
      <p>{{ $t('FairPlay.description.p1')}}</p>
      <p class="practice">{{ $t('FairPlay.description.p2')}}</p>
      <p>{{ $t('FairPlay.description.p3')}}</p>
      <p>{{ $t('FairPlay.description.p4')}}</p>
    </div>
  </el-dialog>
</template>
<script>
import { mapState } from "vuex";
export default {
  name: "FairPlay",
  props: {
    dialogFairness: {
      default: Boolean
    }
  },
  data() {
    return {
      showDialog: false,
      formSeeds: {
        default: "",
        custom: "",
        new: ""
      }
    };
  },
  created() {
    this.getDefaultSHA();
  },
  computed: {
    ...mapState(["address", "contractInstance", "globalTronWeb"])
  },
  mounted() {
    this.showDialog = this.dialogFairness;
  },
  methods: {
    /**
     * 获取游戏合约中的默认种子
     */
    async getDefaultSHA() {
      if (this.address.base58) {
        let salt = await this.contractInstance
          .getUserSalt(this.address.base58)
          .call();
        this.formSeeds.default = salt.slice(-64);
      }
    },
    /**
     * 根据默认种子+ 固定值 + 输入的种子 + 随机数 ，生成新的种子
     */
    changeSHA() {
      if (this.formSeeds.default) {
        // 调用tronWeb的sha3方法 https://developers.tron.network/v3.0/reference#sha3
        let newSeeds = this.globalTronWeb
          .sha3(
            `${this.formSeeds.default}'weiqinl'${
              this.formSeeds.custom
            }${Math.random()}`,
            false
          )
          .toString();
        this.formSeeds.new = newSeeds;
      }
    },
    /**
     * 将新生成的种子发送给合约
     * 更新页面信息
     */
    async updateSHA() {
      if (this.address.base58 && this.formSeeds.new) {
        let update = await this.contractInstance
          .setUserSalt(this.address.base58, "0x" + this.formSeeds.new)
          .send();
        if (!!update) {
          this.formSeeds = {
            default: this.formSeeds.new,
            custom: "",
            new: ""
          };
        }
      }
    },
    /**
     * 关闭弹窗的处理事件
     */
    dialogHandler() {
      this.showDialog = false;
      let obj = {
        key: "dialogFairness",
        value: this.showDialog
      };
      this.$emit("dialogHandler", obj);
    }
  }
};
</script>
<style lang="scss">
$common-value: 0.25rem;
.fair-play {
  .fair-play-form {
    .el-input {
      .el-input__inner {
        text-align: center;
        background-color: rgba(7, 8, 78, 0.42);
        color: #fff;
        border: none;
      }
    }
    .el-form-item__label {
      padding: 0;
      letter-spacing: 0.5px;
      font-size: 0.22rem;
      color: #fff;
    }
    .footer {
      text-align: center;
      margin-top: $common-value;
      .generate {
        font-size: 0.15rem;
      }
    }
  }
  &-description {
    margin: $common-value 0;
    color: rgba(255, 255, 255, 0.8);
    letter-spacing: 0.4px;
    .practice {
      padding: $common-value 0;
    }
  }
}
</style>

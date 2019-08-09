<template></template>

<script>
import { initWebSocket } from "~/assets/js/websocket";
import { parallelLoadScripts } from "~/assets/js/common";

export default {
  name: "WebSocket",
  props: ["wsOption"],
  data() {
    const wsUrl = this.wsOption;
    return {
      wsStatus: {
        websocket: null,
        wsUrl: wsUrl,
        count: 0,
        speed: 0,
        status: false,
        errMsg: "",
        display: true
      },
      wsUrl: wsUrl,
      option: null
    };
  },
  methods: {
    goUrl(routeName, params) {
      this.$router.push({ name: routeName, params: {} });
    }
  },
  mounted() {
    const self = this;
    this.option = {
      url: this.wsStatus.wsUrl,
      connentCallback: function(ws) {
        setTimeout(() => {}, 1000);
      },
      //接收到事件回调
      callback: evtData => {
        const message = JSON.parse(evtData);
        this.$emit("wsData", message);
      }
    };

    parallelLoadScripts(["/js/reconnecting-websocket.min.js"], () => {
      if (this.wsOption) {
        this.wsStatus.websocket = initWebSocket(this.wsStatus, this.option);
      }
    });
  },
  beforeDestroy() {
    if (this.wsStatus.websocket) {
      this.wsStatus.websocket.close(1000, "关闭当前页面");
    }
  },
  watch: {
    wsOption: {
      deep: true,
      handler(val) {
        if (this.wsStatus.websocket) {
          this.wsStatus.websocket.close();
        }

        this.wsStatus.wsUrl = val;
        this.option.url = this.wsStatus.wsUrl;
        if (this.wsOption) {
          // this.wsStatus.websocket = initWebSocket(this.wsStatus, this.option);
        }
      }
    }
  }
};
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>

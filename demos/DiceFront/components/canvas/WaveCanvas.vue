<!--
 * @Description: 半圆形波浪
 * @Author: weiqinl
 * @Date: 2019-01-17 19:07:27
 * @LastEditors: weiqinl
 * @LastEditTime: 2019-01-17 19:58:04
 -->
<template>
  <canvas
    ref="wavecanvas"
    :height="height"
    :width="width"
  />
</template>
<script>
import Retina from "@/assets/js/retina";
import Wave from "@/assets/js/wave";
export default {
  props: ["height", "width", "range"],
  data() {
    return {
      isDrawContainer: false,
      wave1: null,
      wave2: null,
      animateId: null
    };
  },
  mounted() {
    this.ready();
  },
  methods: {
    ready() {
      const canvas = this.$refs.wavecanvas;
      this.canvas = canvas;
      this.canvasWidth = canvas.width;
      this.canvasHeight = canvas.height;
      this.radius = this.canvasWidth / 2;

      // 高清适配
      Retina.run(canvas);
      this.nowRange = 0;
      this.rangeValue = this.range;
      this.wave1 = new Wave({
        canvasWidth: this.canvasWidth, // 轴长
        canvasHeight: this.canvasHeight, // 轴高
        waveWidth: 0.055, // 波浪宽度,数越小越宽
        waveHeight: 4, // 波浪高度,数越大越高
        colors: ["#ffd200", "#a754e4"], // 波浪颜色
        xOffset: 0, // 初始偏移
        speed: 0.08 // 速度
      });
      this.wave2 = new Wave({
        canvasWidth: this.canvasWidth, // 轴长
        canvasHeight: this.canvasHeight, // 轴高
        waveWidth: 0.04, // 波浪宽度,数越小越宽
        waveHeight: 3, // 波浪高度,数越大越高
        colors: ["rgba(243, 156, 107, 0.48)", "rgba(160, 86, 59, 0.48)"], // 波浪颜色
        xOffset: 2, // 初始偏移
        speed: 0.06 // 速度
      });
      this.drawWave();
    },
    /**
     * 绘制波浪
     */
    drawWave() {
      const ctx = this.canvas.getContext("2d");
      ctx.clearRect(0, 0, this.canvasWidth, this.canvasHeight);
      if (!this.isDrawContainer) {
        this.drawContainer(ctx);
      }
      if (this.nowRange <= this.rangeValue) {
        this.nowRange += 1;
      }
      if (this.nowRange > this.rangeValue) {
        this.nowRange -= 1;
      }
      this.wave2.update({
        nowRange: this.nowRange
      });
      this.wave2.draw(ctx);
      this.wave1.update({
        nowRange: this.nowRange
      });
      this.wave1.draw(ctx);
      this.animateId = window.requestAnimationFrame(this.drawWave);
    },
    /**
     * 画圆
     */
    drawCircle(ctx) {
      const r = this.canvasWidth / 2;
      const lineWidth = 0;
      const d = 2 * r;
      const cR = r - lineWidth;
      ctx.lineWidth = lineWidth;
      ctx.beginPath();
      ctx.arc(r, r, cR, 0, 2 * Math.PI);
      // ctx.shadowOffsetX = 0; // 设置水平位移
      // ctx.shadowOffsetY = 0; // 设置垂直位移
      // ctx.shadowBlur = 10; // 设置模糊度
      // ctx.shadowColor = "rgba(255,255,255,0.5)"; // 设置阴影颜色

      // var img = new Image();
      // img.src = require("../assets/images/circle.png");
      // img.onload = function() {

      //   ctx.drawImage(img, 0, 0, d, d);

      // };
      // ctx.strokeStyle = "rgba(186, 165, 130, 0.3)";
      // ctx.stroke();
      // ctx.fillStyle = "rgba(255,255,255,0.10)";
      // ctx.fill();
      ctx.clip();
      this.isDrawContainer = true;
    },
    drawContainer(ctx) {
      this.drawCircle(ctx);
    },
    /**
     * 画圆
     */
    drawCircle(ctx) {
      const r = this.canvasWidth / 2;
      const lineWidth = 0;
      const d = 2 * r;
      const cR = r - lineWidth;
      ctx.lineWidth = lineWidth;
      ctx.beginPath();
      ctx.arc(r, r, cR, 0, 2 * Math.PI);
      // ctx.shadowOffsetX = 0; // 设置水平位移
      // ctx.shadowOffsetY = 0; // 设置垂直位移
      // ctx.shadowBlur = 10; // 设置模糊度
      // ctx.shadowColor = "rgba(255,255,255,0.5)"; // 设置阴影颜色

      // var img = new Image();
      // img.src = require("../assets/images/circle.png");
      // img.onload = function() {

      //   ctx.drawImage(img, 0, 0, d, d);

      // };
      // ctx.strokeStyle = "rgba(186, 165, 130, 0.3)";
      // ctx.stroke();
      // ctx.fillStyle = "rgba(255,255,255,0.10)";
      // ctx.fill();
      ctx.clip();
      this.isDrawContainer = true;
    }
  }
};
</script>


 <template>
 <div class="content">
   <transition name="msgbox-fade">

     <div class="Popup" v-if="value">
       <div class="popupcon">
           <div class="close clearfix">
             <i class="el-icon-close  close" @click="close()"></i>
           </div>
           <div class="con_clos">
             <div class="createContract_contents">
               qweq
               <h3>{{ head }}</h3>
               <p v-for="(item, index) in msgList" :key="index">{{item}}</p>
             </div>
           <div class="btn">
           <!-- @click="generateContract(allDate,2)" -->
             <bm-button class="affirmBtn" type="primary" @click="btnFn" :text="btnName || '确定'"></bm-button>
             <bm-button class="affirmBtn" type="primary" @click="closeFn" v-if="showClose" text="取消"></bm-button>
             <i class="popup__tip">{{ tip }}</i>
           </div>
         </div>
       </div>
     </div>

   </transition>
   </div>
 </template>

<script>
export default {
  name: 'BmMsgbox',
  data () {
    return {
      value: false
    }
  },

  methods: {
    close () {
      this.value = false
    },
    closeFn () {
      this.errorfn()
      this.close()
    },
    btnFn () {
      this.value = false
      this.callback()

      // this.$emit('click', event)
    }
    // this.$msgbox(
    //   {
    //     head: '请先选择上传文件类型', showClose: false
    //   },
    //   function () {},
    //   function () {}
    // )
  }
}
</script>

 <style lang="scss" scoped>
 h3{
    //  color:#fff;
    color:#8F8F8F;
     line-height:24px;
     text-align:center;
     margin-top:30px;
     margin-bottom: 20px;
     font-weight: normal;
   }
   p{
      font-size: 14px;
      color: #636770;
      // color:#8F8F8F;
      line-height: 22px;
      text-align: left;
      text-indent: 30px;
      width: 276px;
      padding-left: 24px;
   }
 .Popup {
   width: 100%;
   height: 100%;
   background: rgba(36,40,81,0.5);
    position: fixed;
    left: 0;
     top: 0;
     z-index: 1000;
   }
   .popupcon {
     border-radius: 4px;
     position: absolute;
     left: 50%;
     top: 50%;
     width: 300px;
    //  background: #404899;
     margin-left: -150px;
     margin-top: -120px;
     // box-shadow: 0px 0px 4px 1px #666666;
     overflow: hidden;
     padding-bottom: 30px;
     opacity: 0.9;
      background: #FFFFFF;
      // border-radius: 8px;

   }
 [class*=" el-icon-"], [class^=el-icon-] {
   float: right;
   cursor: pointer;
 }
 .close {
   margin-right: 6px;
   margin-top: 6px;
   font-size:14px;

 }
 .con_clos {

 }
 .btn {
   padding: 20px 10% 0;
 }
 .affirmBtn{
   color:#ffffff;
   &:nth-child(1){
     margin-bottom: 20px;
   }
   &:nth-child(2){
     background-image: linear-gradient(-180deg, #EAF5FF 0%, #C5E2FF 100%);
   }
 }

 .popup__tip{
  text-align: center;
  line-height: 50px;
  // height: 50px;
 }

.msgbox-fade-enter-active {
    animation: msgbox-fade-in .3s
}

.msgbox-fade-leave-active {
    animation: msgbox-fade-out .3s
}

@keyframes msgbox-fade-in {
    0% {
        transform: translate3d(0, -20px, 0);
        opacity: 0
    }
    100% {
        transform: translate3d(0, 0, 0);
        opacity: 1
    }
}

@keyframes msgbox-fade-out {
    0% {
        transform: translate3d(0, 0, 0);
        opacity: 1
    }
    100% {
        transform: translate3d(0, -20px, 0);
        opacity: 0
    }
}
 </style>

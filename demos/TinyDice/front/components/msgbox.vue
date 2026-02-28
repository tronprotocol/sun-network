 <template>
 <div class="content">
   <transition name="msgbox-fade">
     <!-- v-if="value" -->
     <div class="Popup">
       <div class="popupcon">
           <!-- <div class="close clearfix">
             <i class="el-icon-close  close" @click="close()"></i>
           </div> -->
           <div class="con_clos">
             <div class="createContract_contents">
               {{$t('DividingPool.unfreezeMsg')}}
             </div>
           <div class="btn">
              <el-button class="affirmBtn affirm-pledge" @click="btnFn">{{$t('DividingPool.unfreeze')}}</el-button>
             <el-button class="affirmBtn cancel-pledge" @click="close">{{$t('DividingPool.cancel')}}</el-button>
           </div>
         </div>
       </div>
     </div>

   </transition>
   </div>
 </template>

<script>
export default {
  name: 'msgbox',
  data () {
    return {
      value: true
    }
  },

  methods: {
    close () {
      this.$emit('cancel', event)
    },
    btnFn () {
      // this.value = false
      // this.callback()

      this.$emit('click', event)
    }
  }
}
</script>

 <style lang="scss" scoped>
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
     background-image:linear-gradient(-180deg, #bc56df 3%, #4c31ad 100%);
      border:1px solid #ff68fc;
      border-radius:3px;
      width:4.06rem;
      min-height:1.62rem;
     position: absolute;
     left: 50%;
     top: 50%;
     margin-left: -2.03rem;
     margin-top: -0.81rem;
     overflow: hidden;
     padding: 0.3rem 0.2rem 0.2rem 0.2rem;

   }
 [class*=" el-icon-"], [class^=el-icon-] {
   float: right;
   cursor: pointer;
 }
 .affirm-pledge {
    background-image:linear-gradient(-180deg, #f199d7 0%, #9e5db5 99%);
    border-radius:0.08rem;
  }
 .cancel-pledge {
    background-image:linear-gradient(-180deg, #dd54b5 0%, #bc56df 99%);
    border:1px solid #542099;
    box-shadow:0 1px 2px 0 rgba(0,0,0,0.30);
    border-radius:0.08rem;
  }
  .affirmBtn {
    min-width: 1rem;
    height:0.46rem;
    font-size: 0.16rem;
    outline: none;
    border: none;
    color: #ffffff;
  }
 .close {
   margin-right: 6px;
   margin-top: 6px;
   font-size:14px;

 }
 .createContract_contents {
   font-size: 0.14rem;
   margin-bottom: 0.2rem;
 }
 .btn {
   text-align: center;
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

<template>
  <div class="wrap">
    <div class="logo">
      <div class="light1" ref="light1"></div>
      <div class="light2" ref="light2"></div>
    </div>
  </div>
</template>

<script>
  export default {
    name: "Logo",
    mounted(){
      const light1 = this.$refs.light1,light2 = this.$refs.light2;
      setInterval(()=>{
        if(light1.className === 'light1' && light2.className === 'light2'){
          light1.className = 'light4';
          light2.className = 'light3';
        }else{
          light1.className = 'light1';
          light2.className = 'light2';
        }
      },200)
    }
  }
</script>


<style scoped lang="scss">
  .wrap{
    //height: 183px;
    display: flex;
    justify-content: center;
    align-items: center;
    .logo{
      position: relative;
      height:183px;
      width:230px;
      background-image: url('../static/images/logo-big.png');
      background-size:auto 100%;
      background-repeat: no-repeat;
      background-position:center;
      .light1{
        width: 100%;
        height: 100%;
        position: absolute;
        background-image: url('../static/images/white1.png');
        background-repeat: no-repeat;
        background-position:center center;
        background-size:78%;
      }
      .light2{
        width: 100%;
        height: 100%;
        position: absolute;
        background-image: url('../static/images/yellow1.png');
        background-repeat: no-repeat;
        background-position:center center;
        background-size:78%;
      }
      .light3{
        width: 100%;
        height: 100%;
        position: absolute;
        background-image: url('../static/images/white2.png');
        background-repeat: no-repeat;
        background-position:center center;
        background-size:78%;
      }
      .light4{
        width: 100%;
        height: 100%;
        position: absolute;
        background-image: url('../static/images/yellow2.png');
        background-repeat: no-repeat;
        background-position:center center;
        background-size:78%;
      }
    }
  }
</style>

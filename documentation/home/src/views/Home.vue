<template>
  <div class="home">
    <NavBar />
    <Main />
    <FooterBar />
  </div>
</template>

<script>
// @ is an alias to /src
import NavBar from '@/components/NavBar.vue';
import Main from '@/components/Main.vue';
import FooterBar from '@/components/FooterBar.vue';

export default {
  name: 'Home',
  components: {
    NavBar,
    Main,
    FooterBar
  }
};
</script>

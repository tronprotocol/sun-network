import Vue from 'vue';
import Vuex from 'vuex';

Vue.use(Vuex);

export default new Vuex.Store({
  state: {
    homeUrl: '/sunnetwork',
    mainNetsUrl: '/sunnetwork',
    testNetsUrl: '/sunnetwork/doc/',
    docUrl: '/sunnetwork/doc/guide/',
    resourcesUrl: 'https://github.com/tronprotocol/sun-network',
    githubUrl: 'https://github.com/tronprotocol/sun-network',
    telegramUrl: 'https://t.me/sun_network',
    demo1Url: '/sunnetwork/demo/donate',
    demo2Url: '/sunnetwork/demo/dice',
    donateUrl: 'https://tron.network/donation?lng=en',
    faqUrl: 'https://gitter.im/tronprotocol/sun-network',
    discordUrl: 'https://discordapp.com/invite/Anun6b7'
  },
  mutations: {},
  actions: {}
});

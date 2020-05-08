export const strict = false;
export const state = () => ({
  locales: ["en", "ch"],
  locale: "en",
  address: {},
  balance: '--',
  mBalance: '--',
  limit: 10000,
  contractAddress: "",
  contractInstance: null,
  loginState: false,
  dialogLogin: false,
  myBetsLength: 0,
  showLoading: true,
  random: 0,
  activityAddress: "",
  token: "",
  dapp: 1,
  myBets: [],
  platForm: "pc",
  account: {},
  trx20Account: {},
  trx20AccountInfo: {},
  autoBet: { switch: false, time: "" },
  successRecord: [],
  globalSunWeb: {},
  diviend: {}, //分红
  globalSunWeb2: {}

});

export const mutations = {
  SET_LANG(state, locale) {
    if (state.locales.indexOf(locale) !== -1) {
      state.locale = locale;
    }
  },
  SET_LOGINSTATE(state, loginState) {
    state.loginState = loginState;
  },
  SET_CONTRACT_ADDRESS(state, address) {
    state.contractAddress = address;
  },
  SET_BALANCE(state, balance) {
    state.balance = balance;
  },
  SET_MBALANCE(state, balance) {
    state.mBalance = balance;
  },
  SET_CONTRACT_INSTANCE(state, obj) {
    state.contractInstance = { ...state.contractInstance, ...obj };
  },
  SET_DIALOG_LOGIN(state, dialogLogin) {
    state.dialogLogin = dialogLogin;
  },
  SET_MY_BETS_LENGTH(state, myBetsLength) {
    state.myBetsLength = myBetsLength;
  },
  SET_RANDOM(state, random) {
    state.random = random;
  },
  SET_SHOW_LOADING(state, showLoading) {
    state.showLoading = showLoading;
  },
  SET_ACTIVITYADDRESS(state, activityAddress) {
    state.activityAddress = activityAddress;
  },
  SET_TOKEN(state, token) {
    state.token = token;
  },
  SET_MY_BETS(state, myBets) {
    state.myBets = myBets;
  },
  SET_PLATFORM(state, platForm) {
    state.platForm = platForm;
  },
  SET_ACCOUNT(state, data) {
    state.account = data;
  },
  SET_TRX20ACCOUNT(state, data) {
    state.trx20Account = data
  },
  SET_AUTO_BET_TIME(state, obj) {
    state.autoBet = obj;
  },
  SET_SUCCESS_RECORD(state, arr) {
    state.successRecord = arr;
  },
  SET_SUNWEB(state, obj) {
    state.globalSunWeb = obj;
    state.address = obj.sidechain.defaultAddress;
    // window.sunWeb = obj;
  },
  SET_SUNWEB2(state, obj) {
    state.globalSunWeb2 = obj;
    // window.sunWeb2 = obj;
  },
  SET_DIVIEND(state, obj) {
    state.diviend = obj;
  },
  SET_TRX20ACCOUNT_INFO(state, obj) {
    state.trx20AccountInfo = obj
  }
};

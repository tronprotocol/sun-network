import Vue from "vue";

export default () => {
  Vue.filter("hiddenAddress", (value, num = 6) => {
    return value ? value.substr(0, num) + "..." + value.substr(-num) : "";
  });
  Vue.filter("fixed", (value, number) => {
    return parseFloat(value).toFixed(number);
  });
  Vue.filter("toLocaleString", (value, number) => {
    if (isNaN(value)) return '--';
    return parseFloat(value).toLocaleString();
  });
};

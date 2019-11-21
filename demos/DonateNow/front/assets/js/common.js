import xhr from "axios";
import interfaceData from "@/api/config"
const eventServer = interfaceData.sideOptions.fullNode;

/**
 * 获取账户余额
 * @param {*} address 
 */
export async function getBalance(address) {

  let { data } = await xhr.post(`${eventServer}/wallet/getaccount`, {
    address: address
  });

  return data.balance;
}
/**
 * 获取账户信息
 * @param {*} address 
 */
export async function getaccount(address) {

  let { data } = await xhr.post(`${eventServer}/wallet/getaccount`, {
    address: address
  });

  return data;
}

/**
 * 根据id获取交易信息
 */
export async function getTransactionInfoById(transactionId) {
  let { data } = await xhr.get(`${eventServer}/wallet/gettransactioninfobyid?value=${transactionId}`)
  return data
}

export function parallelLoadScripts(scripts, callback) {
  if (typeof (scripts) != "object") var scripts = [scripts];
  var HEAD = document.getElementsByTagName("head").item(0) || document.documentElement, s = new Array(), loaded = 0;
  for (var i = 0; i < scripts.length; i++) {
    s[i] = document.createElement("script");
    s[i].setAttribute("type", "text/javascript");
    s[i].onload = s[i].onreadystatechange = function() { //Attach handlers for all browsers
      if (!/*@cc_on!@*/0 || this.readyState == "loaded" || this.readyState == "complete") {
        loaded++;
        this.onload = this.onreadystatechange = null;
        this.parentNode.removeChild(this);
        if (loaded == scripts.length && typeof (callback) == "function") callback();
      }
    };
    s[i].setAttribute("src", scripts[i]);
    HEAD.appendChild(s[i]);
  }
}

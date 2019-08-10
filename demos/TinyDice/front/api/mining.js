import { Message } from "element-ui";
import request from "~/plugins/request";

import interfaceData from "./config"
const cli_url = interfaceData.CLI_API

/**
 * 挖矿信息,
 * 挖矿阶段，挖矿效率，本阶段矿池挖出率，总矿池挖出率，总的分红池
 * @param {*} params 游戏合约地址
 */
export function getProgress(params) {
  return new Promise(function(resolve, reject) {
    request({
      url: `${cli_url}/api/mine/${params}/progress`,
      method: "get"
    })
      .then(response => {
        if (response) {
          resolve(response)
        } else {
          Message({ type: "warning", message: response, showClose: true });
          reject(response)
        }
      })
      .catch(err => {
        Message({ type: "error", message: err.message, showClose: true });
        reject(err);
      });
  });
}
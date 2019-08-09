import { Message } from 'element-ui'

import request from "~/plugins/request";
import interfaceData from "./config"
const server_url = interfaceData.SERVER_API

/**
 * 分红记录
 * 最近100条数据
 * @param {*} params 
 */
export function queryBonusRecord(params) {
  return new Promise(function(resolve, reject) {
    request({
      url: `${server_url}/api/bonuses`,
      method: "get",
      params: params
    })
      .then(response => {
        if (response.code === 0) {
          resolve(response.data)
        } else {
          Message({ type: "warning", message: response.errMsg, showClose: true });
          reject(response)
        }
      })
      .catch(err => {
        Message({ type: "error", message: err.message, showClose: true });
        reject(err);
      });
  });
}

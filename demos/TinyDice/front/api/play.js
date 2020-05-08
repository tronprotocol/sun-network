import request from "~/plugins/request";
import { Message } from 'element-ui'
import interfaceData from "./config"
const SERVER_API = interfaceData.sideOptions.eventServer

/**
 *  摇塞子，绑定contractResult和transactionId
 * @param {Object} params
 */
export function doBet(params) {
  return new Promise(function(resolve, reject) {
    request({
      url: `${SERVER_API}/event/contract/${params.contractAddress}/${params.eventName}`,
      method: "get",
      data: {limit: params.limit}
    })
      .then(response => {
        if (response) {
          resolve(response)
        } else {
          Message({ type: "warning", message: response.message, showClose: true });
          reject(response)
        }
      })
      .catch(err => {
        Message({ type: "error", message: err.message, showClose: true });
        reject(err);
      });
  });
}
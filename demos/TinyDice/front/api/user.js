import request from "~/plugins/request";

import interfaceData from "./config"
const SERVER_API = interfaceData.SERVER_API
const CLI_API = interfaceData.CLI_API

import { Message } from "element-ui";


/**
 * 注册登陆用户，增加用户到后台数据库
 * 进入到dice系统的，才统计分红
 * 如果有inviter_address,则同时绑定邀请用户
 * @param {*} params 
 */
export function addDiceUser(params) {
  return new Promise(function(resolve, reject) {
    request({
      url: `${SERVER_API}/api/user`,
      method: "post",
      data: params
    })
      .then(response => {
        if (response.code === 0) {
          resolve(response.data)
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

/**
 * 查询邀请好友列表
 * @param {Object} params 请求参数
 */
export function getInviteReturnList(params) {
  return new Promise(function(resolve, reject) {
    request({
      url: `${SERVER_API}/api/invite/list`,
      method: "get",
      params: params
    })
      .then(response => {
        if (response.code === 0) {
          resolve(response.data)
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
/**
 * 邀请的返现金额
 * @param {*} params 
 */
export function getInviteAmount(params) {
  return new Promise(function(resolve, reject) {
    request({
      url: `${SERVER_API}/api/invite/dice`,
      method: "get",
      params: params
    })
      .then(response => {
        if (response.code === 0) {
          resolve(response.data)
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
/**
 * 邀请返现的提取
 * @param {*} params 
 */
export function extractInviteReturn(params) {
  return new Promise(function(resolve, reject) {
    request({
      url: `${CLI_API}/api/invite/withdraw`,
      method: "post",
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

//初始化ws
export function initWebSocket(wsStatus, option) {

  //服务器地址
  let locate = window.location
  let url = ""
  if (option.url) {
    url = option.url
  }


  //回调函数
  let callback = option.callback
  if (typeof callback !== "function") {

    return false
  }
  //回调函数
  let connentCallback = option.connentCallback
  if (typeof connentCallback !== "function") {

    return false
  }
  // 一些对浏览器的兼容已经在插件里面完成
  let websocket = new ReconnectingWebSocket(url, null, {
    debug: false,
    reconnectDecay: 1,
    reconnectInterval: 1000
  });
  // let websocket = new WebSocket(url)

  //连接发生错误的回调方法
  websocket.onerror = function(err) {
    wsStatus.status = false
    wsStatus.errMsg = err

  };

  //连接成功建立的回调方法
  websocket.onopen = function(event) {

    connentCallback()
  }

  //接收到消息的回调方法
  websocket.onmessage = function(event) {
    callback(event.data)
  }

  //连接关闭的回调方法
  websocket.onclose = function(err) {

    wsStatus.status = false
    wsStatus.errMsg = err
    //websocket.close()

  }

  websocket.addEventListener("error", function(event) {
    // handle error event


  });


  //监听窗口关闭事件，当窗口关闭时，主动去关闭websocket连接，防止连接还没断开就关闭窗口，server端会抛异常。
  window.onbeforeunload = function() {
    websocket.close()
  }

  return websocket

}

export function careCanWS(webSocket, canIds, vehicleGuid) {



  if (!webSocket) {
    return false
  }

  try {
    let careCan = {
      "act": "care",
      "msg": canIds.join(',')
    }

    webSocket.send(JSON.stringify(careCan))

    let careVehicle = {
      "act": "vehicle",
      "msg": vehicleGuid || ''
    }

    webSocket.send(JSON.stringify(careVehicle))

  } catch (e) {
    return false
  }

  return true
}

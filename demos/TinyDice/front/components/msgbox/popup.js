import Vue from 'vue'
import msgboxVue from './index.vue'

const MessageBoxConstructor = Vue.extend(msgboxVue)

let instance

const initInstance = () => {
  instance = new MessageBoxConstructor({
    el: windows.document.createElement('div')
  })
}

const showNextMsg = (options, callback, errorfn) => {
  if (!instance) {
    initInstance()
  }
  if (!instance.value) {
    for (let prop in options) {
      instance[prop] = options[prop]
    }
    instance.callback = callback
    instance.errorfn = errorfn;
    ['head', 'btnName', 'tip', 'msgList', 'showClose'].forEach(prop => {
      if (instance[prop] === undefined) {
        instance[prop] = null
      }
    })
    windows.document.body.appendChild(instance.$el)

    Vue.nextTick(() => {
      instance.value = true
    })
  }
}

const MessageBox = function (options, callback, errorfn) {
  showNextMsg(options, callback, errorfn)
}

export default MessageBox

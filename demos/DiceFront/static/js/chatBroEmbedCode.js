/* Chatbro Widget Embed Code Start */
function ChatbroLoader(chats, async) {
  async = !1 !== async;
  var params = {
      embedChatsParameters: chats instanceof Array ? chats : [chats],
      lang: navigator.language || navigator.userLanguage,
      needLoadCode: "undefined" == typeof Chatbro,
      embedParamsVersion: localStorage.embedParamsVersion,
      chatbroScriptVersion: localStorage.chatbroScriptVersion
    },
    xhr = new XMLHttpRequest();
  (xhr.withCredentials = !0),
    (xhr.onload = function() {
      eval(xhr.responseText);
    }),
    (xhr.onerror = function() {
      console.error("Chatbro loading error");
    }),
    xhr.open(
      "GET",
      "//www.chatbro.com/embed.js?" +
        btoa(unescape(encodeURIComponent(JSON.stringify(params)))),
      async
    ),
    xhr.send();
}
ChatbroLoader({
  // chatPath: 'tg/677657606/TRONdice',
  encodedChatId: "82qT7",
  chatTitle: "TRONdice",
  // // chatAlias: 'TRONdice',
  chatHeaderBackgroundColor: "#9a70d7",
  chatHeaderTextColor: "#ffffff",
  chatBodyBackgroundColor: "#241557",
  chatBodyTextColor: "#ffffff",
  chatInputBackgroundColor: "#6555b4",
  chatInputTextColor: "#ffffff",
  chatBottom: "0px",
  chatLeft: "20px"
  // chatLanguage: lan
});
/* Chatbro Widget Embed Code End */

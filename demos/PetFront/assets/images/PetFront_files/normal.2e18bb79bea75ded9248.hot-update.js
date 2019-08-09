webpackHotUpdate("pages/static/_lang/normal",{

/***/ "./node_modules/vue-loader/lib/loaders/templateLoader.js?!./node_modules/vue-loader/lib/index.js?!./pages/static/_lang/normal.vue?vue&type=template&id=c61cd6fe&":
/*!****************************************************************************************************************************************************************************************************!*\
  !*** ./node_modules/vue-loader/lib/loaders/templateLoader.js??vue-loader-options!./node_modules/vue-loader/lib??vue-loader-options!./pages/static/_lang/normal.vue?vue&type=template&id=c61cd6fe& ***!
  \****************************************************************************************************************************************************************************************************/
/*! exports provided: render, staticRenderFns */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "render", function() { return render; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "staticRenderFns", function() { return staticRenderFns; });
var render = function() {
  var _vm = this
  var _h = _vm.$createElement
  var _c = _vm._self._c || _h
  return _c("div", [
    _c(
      "div",
      { staticClass: "container" },
      [
        _c("my-nav", { attrs: { languageGroup: _vm.languageGroup } }),
        _vm._v(" "),
        _c("div", { staticClass: "main" }, [
          _c(
            "div",
            { staticClass: "cell", staticStyle: { "margin-top": "0.2rem" } },
            [
              _c("div", { staticClass: "intro" }, [
                _c("div", { staticClass: "intro-l" }, [
                  _c(
                    "p",
                    {
                      staticStyle: {
                        "font-size": "0.26rem",
                        "font-weight": "600",
                        color: "#afafaf"
                      }
                    },
                    [_vm._v("DApp Demo#1")]
                  ),
                  _vm._v(" "),
                  _c(
                    "p",
                    {
                      staticStyle: {
                        "font-size": "0.38rem",
                        "font-weight": "600",
                        color: "#484848",
                        "margin-top": "30px"
                      }
                    },
                    [_vm._v("Donate — Pets Need U")]
                  ),
                  _vm._v(" "),
                  _c(
                    "p",
                    {
                      staticStyle: {
                        "font-family": "STSongti-SC",
                        "font-size": "0.18rem",
                        color: "#c3bdbd"
                      }
                    },
                    [_vm._v(_vm._s(_vm.totalDonate) + "  TRX Donated ")]
                  ),
                  _vm._v(" "),
                  _c(
                    "p",
                    {
                      staticClass: "p-text",
                      staticStyle: { "margin-top": "50px" }
                    },
                    [
                      _vm._v(
                        "Look at these stray animals, they need your help."
                      )
                    ]
                  ),
                  _vm._v(" "),
                  _c("p", { staticClass: "p-text" }, [
                    _vm._v(
                      "Your donation will help give animals a voice and make a greater impact where it is needed most."
                    )
                  ]),
                  _vm._v(" "),
                  _c("p", { staticClass: "p-text" }, [
                    _vm._v(
                      "We invite you to leave a story about your help to these stray animals and donate a little love."
                    )
                  ])
                ]),
                _vm._v(" "),
                _c("div", { staticClass: "intro-r" }, [
                  _c("img", {
                    attrs: {
                      src: __webpack_require__(/*! ../../../assets/images/pet.jpg */ "./assets/images/pet.jpg"),
                      alt: "Pet Png"
                    }
                  })
                ])
              ]),
              _vm._v(" "),
              _c(
                "div",
                { staticClass: "input-div" },
                [
                  _c("el-input", {
                    staticStyle: { width: "80%" },
                    attrs: { placeholder: "Share your story" },
                    model: {
                      value: _vm.story,
                      callback: function($$v) {
                        _vm.story = typeof $$v === "string" ? $$v.trim() : $$v
                      },
                      expression: "story"
                    }
                  }),
                  _vm._v(" "),
                  _c("el-input", {
                    staticStyle: { width: "80%", "margin-top": "20px" },
                    attrs: { placeholder: "Donate with love" },
                    model: {
                      value: _vm.amount,
                      callback: function($$v) {
                        _vm.amount = typeof $$v === "string" ? $$v.trim() : $$v
                      },
                      expression: "amount"
                    }
                  }),
                  _vm._v(" "),
                  _c(
                    "span",
                    {
                      staticStyle: { "font-size": "0.2rem", color: "#777777" }
                    },
                    [_vm._v("TRX")]
                  ),
                  _vm._v(" "),
                  _c(
                    "el-button",
                    {
                      staticStyle: { "margin-top": "-30px" },
                      on: {
                        click: function($event) {
                          return _vm.donate()
                        }
                      }
                    },
                    [_vm._v("Let’s Do it")]
                  )
                ],
                1
              ),
              _vm._v(" "),
              _c("div", { staticClass: "balance-div" }, [
                _c("span", [
                  _vm._v("My Balance:   "),
                  _c("span", [
                    _vm._v("main-chain " + _vm._s(_vm.mBalance) + " TRX")
                  ]),
                  _vm._v(" "),
                  _c("span", [_vm._v(" " + _vm._s(_vm.balance) + " TRX")])
                ])
              ])
            ]
          ),
          _vm._v(" "),
          _c("div", { staticClass: "all" }, [
            _c("div", { staticClass: "title" }, [_vm._v("All Donations")]),
            _vm._v(" "),
            _vm.allDonations.length > 0
              ? _c(
                  "div",
                  _vm._l(_vm.allDonations, function(item, index) {
                    return _c("div", { key: "all" + index }, [
                      _c("div", [
                        _c("span", [_vm._v(_vm._s(item.addr))]),
                        _vm._v(" "),
                        _c("span", [_vm._v(_vm._s(item.amount))])
                      ]),
                      _vm._v(" "),
                      _c("div", [_vm._v(_vm._s(item.story))])
                    ])
                  }),
                  0
                )
              : _c("div", [_vm._v("No Donation.")])
          ])
        ]),
        _vm._v(" "),
        _c("Footer")
      ],
      1
    )
  ])
}
var staticRenderFns = []
render._withStripped = true



/***/ })

})
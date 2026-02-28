(window["webpackJsonp"] = window["webpackJsonp"] || []).push([["app"],{

/***/ "./.nuxt/App.js":
/*!**********************!*\
  !*** ./.nuxt/App.js ***!
  \**********************/
/*! exports provided: default */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony import */ var core_js_modules_es6_object_to_string__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! core-js/modules/es6.object.to-string */ "./node_modules/core-js/modules/es6.object.to-string.js");
/* harmony import */ var core_js_modules_es6_object_to_string__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es6_object_to_string__WEBPACK_IMPORTED_MODULE_0__);
/* harmony import */ var vue__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! vue */ "./node_modules/vue/dist/vue.runtime.esm.js");
/* harmony import */ var _components_nuxt_build_indicator__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! ./components/nuxt-build-indicator */ "./.nuxt/components/nuxt-build-indicator.vue");
/* harmony import */ var _node_modules_element_ui_lib_theme_chalk_index_css__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! ../node_modules/element-ui/lib/theme-chalk/index.css */ "./node_modules/element-ui/lib/theme-chalk/index.css");
/* harmony import */ var _node_modules_element_ui_lib_theme_chalk_index_css__WEBPACK_IMPORTED_MODULE_3___default = /*#__PURE__*/__webpack_require__.n(_node_modules_element_ui_lib_theme_chalk_index_css__WEBPACK_IMPORTED_MODULE_3__);
/* harmony import */ var _static_css_reset_css__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! ../static/css/reset.css */ "./static/css/reset.css");
/* harmony import */ var _static_css_reset_css__WEBPACK_IMPORTED_MODULE_4___default = /*#__PURE__*/__webpack_require__.n(_static_css_reset_css__WEBPACK_IMPORTED_MODULE_4__);
/* harmony import */ var _layouts_default_vue__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! ../layouts/default.vue */ "./layouts/default.vue");






var layouts = {
  "_default": _layouts_default_vue__WEBPACK_IMPORTED_MODULE_5__["default"]
};
/* harmony default export */ __webpack_exports__["default"] = ({
  head: {
    "title": "PetFront",
    "meta": [{
      "charset": "utf-8"
    }, {
      "name": "viewport",
      "content": "width=device-width, initial-scale=1"
    }, {
      "hid": "description",
      "name": "description",
      "content": "Pet Demo for SUN Network"
    }],
    "link": [{
      "rel": "icon",
      "type": "image/x-icon",
      "href": "/favicon.ico"
    }, {
      "rel": "stylesheet",
      "type": "text/css",
      "href": "//at.alicdn.com/t/font_887145_pjmxwtmmaus.css"
    }],
    "script": [{
      "src": "/js/vconsole.min.js"
    }],
    "style": []
  },
  render: function render(h, props) {
    var layoutEl = h(this.layout || 'nuxt');
    var templateEl = h('div', {
      domProps: {
        id: '__layout'
      },
      key: this.layoutName
    }, [layoutEl]);
    var transitionEl = h('transition', {
      props: {
        name: 'layout',
        mode: 'out-in'
      },
      on: {
        beforeEnter: function beforeEnter(el) {
          // Ensure to trigger scroll event after calling scrollBehavior
          window.$nuxt.$nextTick(function () {
            window.$nuxt.$emit('triggerScroll');
          });
        }
      }
    }, [templateEl]);
    return h('div', {
      domProps: {
        id: '__nuxt'
      }
    }, [h(_components_nuxt_build_indicator__WEBPACK_IMPORTED_MODULE_2__["default"]), transitionEl]);
  },
  data: function data() {
    return {
      isOnline: true,
      layout: null,
      layoutName: ''
    };
  },
  beforeCreate: function beforeCreate() {
    vue__WEBPACK_IMPORTED_MODULE_1__["default"].util.defineReactive(this, 'nuxt', this.$options.nuxt);
  },
  created: function created() {
    // Add this.$nuxt in child instances
    vue__WEBPACK_IMPORTED_MODULE_1__["default"].prototype.$nuxt = this; // add to window so we can listen when ready

    if (true) {
      window.$nuxt = this;
      this.refreshOnlineStatus(); // Setup the listeners

      window.addEventListener('online', this.refreshOnlineStatus);
      window.addEventListener('offline', this.refreshOnlineStatus);
    } // Add $nuxt.error()


    this.error = this.nuxt.error;
  },
  computed: {
    isOffline: function isOffline() {
      return !this.isOnline;
    }
  },
  methods: {
    refreshOnlineStatus: function refreshOnlineStatus() {
      if (true) {
        if (typeof window.navigator.onLine === 'undefined') {
          // If the browser doesn't support connection status reports
          // assume that we are online because most apps' only react
          // when they now that the connection has been interrupted
          this.isOnline = true;
        } else {
          this.isOnline = window.navigator.onLine;
        }
      }
    },
    setLayout: function setLayout(layout) {
      if (layout && typeof layout !== 'string') throw new Error('[nuxt] Avoid using non-string value as layout property.');

      if (!layout || !layouts['_' + layout]) {
        layout = 'default';
      }

      this.layoutName = layout;
      this.layout = layouts['_' + layout];
      return this.layout;
    },
    loadLayout: function loadLayout(layout) {
      if (!layout || !layouts['_' + layout]) {
        layout = 'default';
      }

      return Promise.resolve(layouts['_' + layout]);
    }
  },
  components: {}
});

/***/ }),

/***/ "./.nuxt/client.js":
/*!*************************!*\
  !*** ./.nuxt/client.js ***!
  \*************************/
/*! no exports provided */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* WEBPACK VAR INJECTION */(function(global) {/* harmony import */ var core_js_modules_es7_symbol_async_iterator__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! core-js/modules/es7.symbol.async-iterator */ "./node_modules/core-js/modules/es7.symbol.async-iterator.js");
/* harmony import */ var core_js_modules_es7_symbol_async_iterator__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es7_symbol_async_iterator__WEBPACK_IMPORTED_MODULE_0__);
/* harmony import */ var core_js_modules_es6_symbol__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! core-js/modules/es6.symbol */ "./node_modules/core-js/modules/es6.symbol.js");
/* harmony import */ var core_js_modules_es6_symbol__WEBPACK_IMPORTED_MODULE_1___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es6_symbol__WEBPACK_IMPORTED_MODULE_1__);
/* harmony import */ var core_js_modules_es6_string_iterator__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! core-js/modules/es6.string.iterator */ "./node_modules/core-js/modules/es6.string.iterator.js");
/* harmony import */ var core_js_modules_es6_string_iterator__WEBPACK_IMPORTED_MODULE_2___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es6_string_iterator__WEBPACK_IMPORTED_MODULE_2__);
/* harmony import */ var _babel_runtime_helpers_esm_typeof__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! @babel/runtime/helpers/esm/typeof */ "./node_modules/@babel/runtime/helpers/esm/typeof.js");
/* harmony import */ var regenerator_runtime_runtime__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! regenerator-runtime/runtime */ "./node_modules/regenerator-runtime/runtime.js");
/* harmony import */ var regenerator_runtime_runtime__WEBPACK_IMPORTED_MODULE_4___default = /*#__PURE__*/__webpack_require__.n(regenerator_runtime_runtime__WEBPACK_IMPORTED_MODULE_4__);
/* harmony import */ var core_js_modules_es6_regexp_match__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! core-js/modules/es6.regexp.match */ "./node_modules/core-js/modules/es6.regexp.match.js");
/* harmony import */ var core_js_modules_es6_regexp_match__WEBPACK_IMPORTED_MODULE_5___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es6_regexp_match__WEBPACK_IMPORTED_MODULE_5__);
/* harmony import */ var _babel_runtime_helpers_esm_asyncToGenerator__WEBPACK_IMPORTED_MODULE_6__ = __webpack_require__(/*! @babel/runtime/helpers/esm/asyncToGenerator */ "./node_modules/@babel/runtime/helpers/esm/asyncToGenerator.js");
/* harmony import */ var core_js_modules_es7_array_includes__WEBPACK_IMPORTED_MODULE_7__ = __webpack_require__(/*! core-js/modules/es7.array.includes */ "./node_modules/core-js/modules/es7.array.includes.js");
/* harmony import */ var core_js_modules_es7_array_includes__WEBPACK_IMPORTED_MODULE_7___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es7_array_includes__WEBPACK_IMPORTED_MODULE_7__);
/* harmony import */ var core_js_modules_es6_string_includes__WEBPACK_IMPORTED_MODULE_8__ = __webpack_require__(/*! core-js/modules/es6.string.includes */ "./node_modules/core-js/modules/es6.string.includes.js");
/* harmony import */ var core_js_modules_es6_string_includes__WEBPACK_IMPORTED_MODULE_8___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es6_string_includes__WEBPACK_IMPORTED_MODULE_8__);
/* harmony import */ var core_js_modules_web_dom_iterable__WEBPACK_IMPORTED_MODULE_9__ = __webpack_require__(/*! core-js/modules/web.dom.iterable */ "./node_modules/core-js/modules/web.dom.iterable.js");
/* harmony import */ var core_js_modules_web_dom_iterable__WEBPACK_IMPORTED_MODULE_9___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_web_dom_iterable__WEBPACK_IMPORTED_MODULE_9__);
/* harmony import */ var core_js_modules_es6_object_to_string__WEBPACK_IMPORTED_MODULE_10__ = __webpack_require__(/*! core-js/modules/es6.object.to-string */ "./node_modules/core-js/modules/es6.object.to-string.js");
/* harmony import */ var core_js_modules_es6_object_to_string__WEBPACK_IMPORTED_MODULE_10___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es6_object_to_string__WEBPACK_IMPORTED_MODULE_10__);
/* harmony import */ var core_js_modules_es6_object_keys__WEBPACK_IMPORTED_MODULE_11__ = __webpack_require__(/*! core-js/modules/es6.object.keys */ "./node_modules/core-js/modules/es6.object.keys.js");
/* harmony import */ var core_js_modules_es6_object_keys__WEBPACK_IMPORTED_MODULE_11___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es6_object_keys__WEBPACK_IMPORTED_MODULE_11__);
/* harmony import */ var core_js_modules_es6_array_find__WEBPACK_IMPORTED_MODULE_12__ = __webpack_require__(/*! core-js/modules/es6.array.find */ "./node_modules/core-js/modules/es6.array.find.js");
/* harmony import */ var core_js_modules_es6_array_find__WEBPACK_IMPORTED_MODULE_12___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es6_array_find__WEBPACK_IMPORTED_MODULE_12__);
/* harmony import */ var core_js_modules_es6_function_name__WEBPACK_IMPORTED_MODULE_13__ = __webpack_require__(/*! core-js/modules/es6.function.name */ "./node_modules/core-js/modules/es6.function.name.js");
/* harmony import */ var core_js_modules_es6_function_name__WEBPACK_IMPORTED_MODULE_13___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es6_function_name__WEBPACK_IMPORTED_MODULE_13__);
/* harmony import */ var core_js_modules_es6_array_iterator__WEBPACK_IMPORTED_MODULE_14__ = __webpack_require__(/*! core-js/modules/es6.array.iterator */ "./node_modules/core-js/modules/es6.array.iterator.js");
/* harmony import */ var core_js_modules_es6_array_iterator__WEBPACK_IMPORTED_MODULE_14___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es6_array_iterator__WEBPACK_IMPORTED_MODULE_14__);
/* harmony import */ var core_js_modules_es6_promise__WEBPACK_IMPORTED_MODULE_15__ = __webpack_require__(/*! core-js/modules/es6.promise */ "./node_modules/core-js/modules/es6.promise.js");
/* harmony import */ var core_js_modules_es6_promise__WEBPACK_IMPORTED_MODULE_15___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es6_promise__WEBPACK_IMPORTED_MODULE_15__);
/* harmony import */ var core_js_modules_es6_object_assign__WEBPACK_IMPORTED_MODULE_16__ = __webpack_require__(/*! core-js/modules/es6.object.assign */ "./node_modules/core-js/modules/es6.object.assign.js");
/* harmony import */ var core_js_modules_es6_object_assign__WEBPACK_IMPORTED_MODULE_16___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es6_object_assign__WEBPACK_IMPORTED_MODULE_16__);
/* harmony import */ var core_js_modules_es7_promise_finally__WEBPACK_IMPORTED_MODULE_17__ = __webpack_require__(/*! core-js/modules/es7.promise.finally */ "./node_modules/core-js/modules/es7.promise.finally.js");
/* harmony import */ var core_js_modules_es7_promise_finally__WEBPACK_IMPORTED_MODULE_17___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es7_promise_finally__WEBPACK_IMPORTED_MODULE_17__);
/* harmony import */ var vue__WEBPACK_IMPORTED_MODULE_18__ = __webpack_require__(/*! vue */ "./node_modules/vue/dist/vue.runtime.esm.js");
/* harmony import */ var unfetch__WEBPACK_IMPORTED_MODULE_19__ = __webpack_require__(/*! unfetch */ "./node_modules/unfetch/dist/unfetch.mjs");
/* harmony import */ var _middleware_js__WEBPACK_IMPORTED_MODULE_20__ = __webpack_require__(/*! ./middleware.js */ "./.nuxt/middleware.js");
/* harmony import */ var _utils_js__WEBPACK_IMPORTED_MODULE_21__ = __webpack_require__(/*! ./utils.js */ "./.nuxt/utils.js");
/* harmony import */ var _index_js__WEBPACK_IMPORTED_MODULE_22__ = __webpack_require__(/*! ./index.js */ "./.nuxt/index.js");
/* harmony import */ var _components_nuxt_link_client_js__WEBPACK_IMPORTED_MODULE_23__ = __webpack_require__(/*! ./components/nuxt-link.client.js */ "./.nuxt/components/nuxt-link.client.js");
/* harmony import */ var consola__WEBPACK_IMPORTED_MODULE_24__ = __webpack_require__(/*! consola */ "./node_modules/consola/dist/consola.browser.js");
/* harmony import */ var consola__WEBPACK_IMPORTED_MODULE_24___default = /*#__PURE__*/__webpack_require__.n(consola__WEBPACK_IMPORTED_MODULE_24__);























 // should be included after ./index.js


consola__WEBPACK_IMPORTED_MODULE_24___default.a.wrapConsole();
console.log = console.__log; // Component: <NuxtLink>

vue__WEBPACK_IMPORTED_MODULE_18__["default"].component(_components_nuxt_link_client_js__WEBPACK_IMPORTED_MODULE_23__["default"].name, _components_nuxt_link_client_js__WEBPACK_IMPORTED_MODULE_23__["default"]);
vue__WEBPACK_IMPORTED_MODULE_18__["default"].component('NLink', _components_nuxt_link_client_js__WEBPACK_IMPORTED_MODULE_23__["default"]);

if (!global.fetch) {
  global.fetch = unfetch__WEBPACK_IMPORTED_MODULE_19__["default"];
} // Global shared references


var _lastPaths = [];
var app;
var router;
var store; // Try to rehydrate SSR data from window

var NUXT = window.__NUXT__ || {};
Object.assign(vue__WEBPACK_IMPORTED_MODULE_18__["default"].config, {
  "silent": false,
  "performance": true
});
var logs = NUXT.logs || [];

if (logs.length > 0) {
  console.group("%c🚀 Nuxt SSR Logs", 'font-size: 110%');
  logs.forEach(function (logObj) {
    return consola__WEBPACK_IMPORTED_MODULE_24___default.a[logObj.type](logObj);
  });
  delete NUXT.logs;
  console.groupEnd();
} // Setup global Vue error handler


if (!vue__WEBPACK_IMPORTED_MODULE_18__["default"].config.$nuxt) {
  var defaultErrorHandler = vue__WEBPACK_IMPORTED_MODULE_18__["default"].config.errorHandler;

  vue__WEBPACK_IMPORTED_MODULE_18__["default"].config.errorHandler = function (err, vm, info) {
    // Call other handler if exist
    var handled = null;

    if (typeof defaultErrorHandler === 'function') {
      for (var _len = arguments.length, rest = new Array(_len > 3 ? _len - 3 : 0), _key = 3; _key < _len; _key++) {
        rest[_key - 3] = arguments[_key];
      }

      handled = defaultErrorHandler.apply(void 0, [err, vm, info].concat(rest));
    }

    if (handled === true) {
      return handled;
    }

    if (vm && vm.$root) {
      var nuxtApp = Object.keys(vue__WEBPACK_IMPORTED_MODULE_18__["default"].config.$nuxt).find(function (nuxtInstance) {
        return vm.$root[nuxtInstance];
      }); // Show Nuxt Error Page

      if (nuxtApp && vm.$root[nuxtApp].error && info !== 'render function') {
        vm.$root[nuxtApp].error(err);
      }
    }

    if (typeof defaultErrorHandler === 'function') {
      return handled;
    } // Log to console


    if (true) {
      console.error(err);
    } else {}
  };

  vue__WEBPACK_IMPORTED_MODULE_18__["default"].config.$nuxt = {};
}

vue__WEBPACK_IMPORTED_MODULE_18__["default"].config.$nuxt.$nuxt = true;
var errorHandler = vue__WEBPACK_IMPORTED_MODULE_18__["default"].config.errorHandler || console.error; // Create and mount App

Object(_index_js__WEBPACK_IMPORTED_MODULE_22__["createApp"])().then(mountApp).catch(errorHandler);

function componentOption(component, key) {
  if (!component || !component.options || !component.options[key]) {
    return {};
  }

  var option = component.options[key];

  if (typeof option === 'function') {
    for (var _len2 = arguments.length, args = new Array(_len2 > 2 ? _len2 - 2 : 0), _key2 = 2; _key2 < _len2; _key2++) {
      args[_key2 - 2] = arguments[_key2];
    }

    return option.apply(void 0, args);
  }

  return option;
}

function mapTransitions(Components, to, from) {
  var componentTransitions = function componentTransitions(component) {
    var transition = componentOption(component, 'transition', to, from) || {};
    return typeof transition === 'string' ? {
      name: transition
    } : transition;
  };

  return Components.map(function (Component) {
    // Clone original object to prevent overrides
    var transitions = Object.assign({}, componentTransitions(Component)); // Combine transitions & prefer `leave` transitions of 'from' route

    if (from && from.matched.length && from.matched[0].components.default) {
      var fromTransitions = componentTransitions(from.matched[0].components.default);
      Object.keys(fromTransitions).filter(function (key) {
        return fromTransitions[key] && key.toLowerCase().includes('leave');
      }).forEach(function (key) {
        transitions[key] = fromTransitions[key];
      });
    }

    return transitions;
  });
}

function loadAsyncComponents(_x, _x2, _x3) {
  return _loadAsyncComponents.apply(this, arguments);
}

function _loadAsyncComponents() {
  _loadAsyncComponents = Object(_babel_runtime_helpers_esm_asyncToGenerator__WEBPACK_IMPORTED_MODULE_6__["default"])(
  /*#__PURE__*/
  regeneratorRuntime.mark(function _callee3(to, from, next) {
    var Components, err, statusCode, message;
    return regeneratorRuntime.wrap(function _callee3$(_context3) {
      while (1) {
        switch (_context3.prev = _context3.next) {
          case 0:
            // Check if route path changed (this._pathChanged), only if the page is not an error (for validate())
            this._pathChanged = Boolean(app.nuxt.err) || from.path !== to.path;
            this._queryChanged = JSON.stringify(to.query) !== JSON.stringify(from.query);
            this._diffQuery = this._queryChanged ? Object(_utils_js__WEBPACK_IMPORTED_MODULE_21__["getQueryDiff"])(to.query, from.query) : [];
            _context3.prev = 3;
            _context3.next = 6;
            return Object(_utils_js__WEBPACK_IMPORTED_MODULE_21__["resolveRouteComponents"])(to);

          case 6:
            Components = _context3.sent;
            // Call next()
            next();
            _context3.next = 21;
            break;

          case 10:
            _context3.prev = 10;
            _context3.t0 = _context3["catch"](3);
            err = _context3.t0 || {};
            statusCode = err.statusCode || err.status || err.response && err.response.status || 500;
            message = err.message || ''; // Handle chunk loading errors
            // This may be due to a new deployment or a network problem

            if (!/^Loading( CSS)? chunk (\d)+ failed\./.test(message)) {
              _context3.next = 18;
              break;
            }

            window.location.reload(true
            /* skip cache */
            );
            return _context3.abrupt("return");

          case 18:
            this.error({
              statusCode: statusCode,
              message: message
            });
            this.$nuxt.$emit('routeChanged', to, from, err);
            next();

          case 21:
          case "end":
            return _context3.stop();
        }
      }
    }, _callee3, this, [[3, 10]]);
  }));
  return _loadAsyncComponents.apply(this, arguments);
}

function applySSRData(Component, ssrData) {
  if (NUXT.serverRendered && ssrData) {
    Object(_utils_js__WEBPACK_IMPORTED_MODULE_21__["applyAsyncData"])(Component, ssrData);
  }

  Component._Ctor = Component;
  return Component;
} // Get matched components


function resolveComponents(router) {
  var path = Object(_utils_js__WEBPACK_IMPORTED_MODULE_21__["getLocation"])(router.options.base, router.options.mode);
  return Object(_utils_js__WEBPACK_IMPORTED_MODULE_21__["flatMapComponents"])(router.match(path),
  /*#__PURE__*/
  function () {
    var _ref = Object(_babel_runtime_helpers_esm_asyncToGenerator__WEBPACK_IMPORTED_MODULE_6__["default"])(
    /*#__PURE__*/
    regeneratorRuntime.mark(function _callee(Component, _, match, key, index) {
      var _Component;

      return regeneratorRuntime.wrap(function _callee$(_context) {
        while (1) {
          switch (_context.prev = _context.next) {
            case 0:
              if (!(typeof Component === 'function' && !Component.options)) {
                _context.next = 4;
                break;
              }

              _context.next = 3;
              return Component();

            case 3:
              Component = _context.sent;

            case 4:
              // Sanitize it and save it
              _Component = applySSRData(Object(_utils_js__WEBPACK_IMPORTED_MODULE_21__["sanitizeComponent"])(Component), NUXT.data ? NUXT.data[index] : null);
              match.components[key] = _Component;
              return _context.abrupt("return", _Component);

            case 7:
            case "end":
              return _context.stop();
          }
        }
      }, _callee);
    }));

    return function (_x4, _x5, _x6, _x7, _x8) {
      return _ref.apply(this, arguments);
    };
  }());
}

function callMiddleware(Components, context, layout) {
  var _this = this;

  var midd = ["i18n"];
  var unknownMiddleware = false; // If layout is undefined, only call global middleware

  if (typeof layout !== 'undefined') {
    midd = []; // Exclude global middleware if layout defined (already called before)

    layout = Object(_utils_js__WEBPACK_IMPORTED_MODULE_21__["sanitizeComponent"])(layout);

    if (layout.options.middleware) {
      midd = midd.concat(layout.options.middleware);
    }

    Components.forEach(function (Component) {
      if (Component.options.middleware) {
        midd = midd.concat(Component.options.middleware);
      }
    });
  }

  midd = midd.map(function (name) {
    if (typeof name === 'function') return name;

    if (typeof _middleware_js__WEBPACK_IMPORTED_MODULE_20__["default"][name] !== 'function') {
      unknownMiddleware = true;

      _this.error({
        statusCode: 500,
        message: 'Unknown middleware ' + name
      });
    }

    return _middleware_js__WEBPACK_IMPORTED_MODULE_20__["default"][name];
  });
  if (unknownMiddleware) return;
  return Object(_utils_js__WEBPACK_IMPORTED_MODULE_21__["middlewareSeries"])(midd, context);
}

function render(_x9, _x10, _x11) {
  return _render.apply(this, arguments);
} // Fix components format in matched, it's due to code-splitting of vue-router


function _render() {
  _render = Object(_babel_runtime_helpers_esm_asyncToGenerator__WEBPACK_IMPORTED_MODULE_6__["default"])(
  /*#__PURE__*/
  regeneratorRuntime.mark(function _callee4(to, from, next) {
    var _this4 = this;

    var fromMatches, nextCalled, _next, matches, Components, layout, _layout, isValid, _iteratorNormalCompletion, _didIteratorError, _iteratorError, _iterator, _step, Component, error, _layout2;

    return regeneratorRuntime.wrap(function _callee4$(_context4) {
      while (1) {
        switch (_context4.prev = _context4.next) {
          case 0:
            if (!(this._pathChanged === false && this._queryChanged === false)) {
              _context4.next = 2;
              break;
            }

            return _context4.abrupt("return", next());

          case 2:
            // Handle first render on SPA mode
            if (to === from) _lastPaths = [];else {
              fromMatches = [];
              _lastPaths = Object(_utils_js__WEBPACK_IMPORTED_MODULE_21__["getMatchedComponents"])(from, fromMatches).map(function (Component, i) {
                return Object(_utils_js__WEBPACK_IMPORTED_MODULE_21__["compile"])(from.matched[fromMatches[i]].path)(from.params);
              });
            } // nextCalled is true when redirected

            nextCalled = false;

            _next = function _next(path) {
              if (nextCalled) return;
              nextCalled = true;
              next(path);
            }; // Update context


            _context4.next = 7;
            return Object(_utils_js__WEBPACK_IMPORTED_MODULE_21__["setContext"])(app, {
              route: to,
              from: from,
              next: _next.bind(this)
            });

          case 7:
            this._dateLastError = app.nuxt.dateErr;
            this._hadError = Boolean(app.nuxt.err); // Get route's matched components

            matches = [];
            Components = Object(_utils_js__WEBPACK_IMPORTED_MODULE_21__["getMatchedComponents"])(to, matches); // If no Components matched, generate 404

            if (Components.length) {
              _context4.next = 25;
              break;
            }

            _context4.next = 14;
            return callMiddleware.call(this, Components, app.context);

          case 14:
            if (!nextCalled) {
              _context4.next = 16;
              break;
            }

            return _context4.abrupt("return");

          case 16:
            _context4.next = 18;
            return this.loadLayout(typeof _index_js__WEBPACK_IMPORTED_MODULE_22__["NuxtError"].layout === 'function' ? _index_js__WEBPACK_IMPORTED_MODULE_22__["NuxtError"].layout(app.context) : _index_js__WEBPACK_IMPORTED_MODULE_22__["NuxtError"].layout);

          case 18:
            layout = _context4.sent;
            _context4.next = 21;
            return callMiddleware.call(this, Components, app.context, layout);

          case 21:
            if (!nextCalled) {
              _context4.next = 23;
              break;
            }

            return _context4.abrupt("return");

          case 23:
            // Show error page
            app.context.error({
              statusCode: 404,
              message: "This page could not be found"
            });
            return _context4.abrupt("return", next());

          case 25:
            // Update ._data and other properties if hot reloaded
            Components.forEach(function (Component) {
              if (Component._Ctor && Component._Ctor.options) {
                Component.options.asyncData = Component._Ctor.options.asyncData;
                Component.options.fetch = Component._Ctor.options.fetch;
              }
            }); // Apply transitions

            this.setTransitions(mapTransitions(Components, to, from));
            _context4.prev = 27;
            _context4.next = 30;
            return callMiddleware.call(this, Components, app.context);

          case 30:
            if (!nextCalled) {
              _context4.next = 32;
              break;
            }

            return _context4.abrupt("return");

          case 32:
            if (!app.context._errored) {
              _context4.next = 34;
              break;
            }

            return _context4.abrupt("return", next());

          case 34:
            // Set layout
            _layout = Components[0].options.layout;

            if (typeof _layout === 'function') {
              _layout = _layout(app.context);
            }

            _context4.next = 38;
            return this.loadLayout(_layout);

          case 38:
            _layout = _context4.sent;
            _context4.next = 41;
            return callMiddleware.call(this, Components, app.context, _layout);

          case 41:
            if (!nextCalled) {
              _context4.next = 43;
              break;
            }

            return _context4.abrupt("return");

          case 43:
            if (!app.context._errored) {
              _context4.next = 45;
              break;
            }

            return _context4.abrupt("return", next());

          case 45:
            // Call .validate()
            isValid = true;
            _context4.prev = 46;
            _iteratorNormalCompletion = true;
            _didIteratorError = false;
            _iteratorError = undefined;
            _context4.prev = 50;
            _iterator = Components[Symbol.iterator]();

          case 52:
            if (_iteratorNormalCompletion = (_step = _iterator.next()).done) {
              _context4.next = 64;
              break;
            }

            Component = _step.value;

            if (!(typeof Component.options.validate !== 'function')) {
              _context4.next = 56;
              break;
            }

            return _context4.abrupt("continue", 61);

          case 56:
            _context4.next = 58;
            return Component.options.validate(app.context);

          case 58:
            isValid = _context4.sent;

            if (isValid) {
              _context4.next = 61;
              break;
            }

            return _context4.abrupt("break", 64);

          case 61:
            _iteratorNormalCompletion = true;
            _context4.next = 52;
            break;

          case 64:
            _context4.next = 70;
            break;

          case 66:
            _context4.prev = 66;
            _context4.t0 = _context4["catch"](50);
            _didIteratorError = true;
            _iteratorError = _context4.t0;

          case 70:
            _context4.prev = 70;
            _context4.prev = 71;

            if (!_iteratorNormalCompletion && _iterator.return != null) {
              _iterator.return();
            }

          case 73:
            _context4.prev = 73;

            if (!_didIteratorError) {
              _context4.next = 76;
              break;
            }

            throw _iteratorError;

          case 76:
            return _context4.finish(73);

          case 77:
            return _context4.finish(70);

          case 78:
            _context4.next = 84;
            break;

          case 80:
            _context4.prev = 80;
            _context4.t1 = _context4["catch"](46);
            // ...If .validate() threw an error
            this.error({
              statusCode: _context4.t1.statusCode || '500',
              message: _context4.t1.message
            });
            return _context4.abrupt("return", next());

          case 84:
            if (isValid) {
              _context4.next = 87;
              break;
            }

            this.error({
              statusCode: 404,
              message: "This page could not be found"
            });
            return _context4.abrupt("return", next());

          case 87:
            _context4.next = 89;
            return Promise.all(Components.map(function (Component, i) {
              // Check if only children route changed
              Component._path = Object(_utils_js__WEBPACK_IMPORTED_MODULE_21__["compile"])(to.matched[matches[i]].path)(to.params);
              Component._dataRefresh = false; // Check if Component need to be refreshed (call asyncData & fetch)
              // Only if its slug has changed or is watch query changes

              if (_this4._pathChanged && _this4._queryChanged || Component._path !== _lastPaths[i]) {
                Component._dataRefresh = true;
              } else if (!_this4._pathChanged && _this4._queryChanged) {
                var watchQuery = Component.options.watchQuery;

                if (watchQuery === true) {
                  Component._dataRefresh = true;
                } else if (Array.isArray(watchQuery)) {
                  Component._dataRefresh = watchQuery.some(function (key) {
                    return _this4._diffQuery[key];
                  });
                }
              }

              if (!_this4._hadError && _this4._isMounted && !Component._dataRefresh) {
                return Promise.resolve();
              }

              var promises = [];
              var hasAsyncData = Component.options.asyncData && typeof Component.options.asyncData === 'function';
              var hasFetch = Boolean(Component.options.fetch); // Call asyncData(context)

              if (hasAsyncData) {
                var promise = Object(_utils_js__WEBPACK_IMPORTED_MODULE_21__["promisify"])(Component.options.asyncData, app.context).then(function (asyncDataResult) {
                  Object(_utils_js__WEBPACK_IMPORTED_MODULE_21__["applyAsyncData"])(Component, asyncDataResult);
                });
                promises.push(promise);
              } // Check disabled page loading


              _this4.$loading.manual = Component.options.loading === false; // Call fetch(context)

              if (hasFetch) {
                var p = Component.options.fetch(app.context);

                if (!p || !(p instanceof Promise) && typeof p.then !== 'function') {
                  p = Promise.resolve(p);
                }

                p.then(function (fetchResult) {});
                promises.push(p);
              }

              return Promise.all(promises);
            }));

          case 89:
            // If not redirected
            if (!nextCalled) {
              next();
            }

            _context4.next = 106;
            break;

          case 92:
            _context4.prev = 92;
            _context4.t2 = _context4["catch"](27);
            error = _context4.t2 || {};

            if (!(error.message === 'ERR_REDIRECT')) {
              _context4.next = 97;
              break;
            }

            return _context4.abrupt("return", this.$nuxt.$emit('routeChanged', to, from, error));

          case 97:
            _lastPaths = [];
            Object(_utils_js__WEBPACK_IMPORTED_MODULE_21__["globalHandleError"])(error); // Load error layout

            _layout2 = _index_js__WEBPACK_IMPORTED_MODULE_22__["NuxtError"].layout;

            if (typeof _layout2 === 'function') {
              _layout2 = _layout2(app.context);
            }

            _context4.next = 103;
            return this.loadLayout(_layout2);

          case 103:
            this.error(error);
            this.$nuxt.$emit('routeChanged', to, from, error);
            next();

          case 106:
          case "end":
            return _context4.stop();
        }
      }
    }, _callee4, this, [[27, 92], [46, 80], [50, 66, 70, 78], [71,, 73, 77]]);
  }));
  return _render.apply(this, arguments);
}

function normalizeComponents(to, ___) {
  Object(_utils_js__WEBPACK_IMPORTED_MODULE_21__["flatMapComponents"])(to, function (Component, _, match, key) {
    if (Object(_babel_runtime_helpers_esm_typeof__WEBPACK_IMPORTED_MODULE_3__["default"])(Component) === 'object' && !Component.options) {
      // Updated via vue-router resolveAsyncComponents()
      Component = vue__WEBPACK_IMPORTED_MODULE_18__["default"].extend(Component);
      Component._Ctor = Component;
      match.components[key] = Component;
    }

    return Component;
  });
}

function showNextPage(to) {
  // Hide error component if no error
  if (this._hadError && this._dateLastError === this.$options.nuxt.dateErr) {
    this.error();
  } // Set layout


  var layout = this.$options.nuxt.err ? _index_js__WEBPACK_IMPORTED_MODULE_22__["NuxtError"].layout : to.matched[0].components.default.options.layout;

  if (typeof layout === 'function') {
    layout = layout(app.context);
  }

  this.setLayout(layout);
} // When navigating on a different route but the same component is used, Vue.js
// Will not update the instance data, so we have to update $data ourselves


function fixPrepatch(to, ___) {
  var _this2 = this;

  if (this._pathChanged === false && this._queryChanged === false) return;
  var matches = [];
  var instances = Object(_utils_js__WEBPACK_IMPORTED_MODULE_21__["getMatchedComponentsInstances"])(to, matches);
  var Components = Object(_utils_js__WEBPACK_IMPORTED_MODULE_21__["getMatchedComponents"])(to, matches);
  vue__WEBPACK_IMPORTED_MODULE_18__["default"].nextTick(function () {
    instances.forEach(function (instance, i) {
      if (!instance || instance._isDestroyed) return; // if (
      //   !this._queryChanged &&
      //   to.matched[matches[i]].path.indexOf(':') === -1 &&
      //   to.matched[matches[i]].path.indexOf('*') === -1
      // ) return // If not a dynamic route, skip

      if (instance.constructor._dataRefresh && Components[i] === instance.constructor && instance.$vnode.data.keepAlive !== true && typeof instance.constructor.options.data === 'function') {
        var newData = instance.constructor.options.data.call(instance);

        for (var key in newData) {
          vue__WEBPACK_IMPORTED_MODULE_18__["default"].set(instance.$data, key, newData[key]);
        } // Ensure to trigger scroll event after calling scrollBehavior


        window.$nuxt.$nextTick(function () {
          window.$nuxt.$emit('triggerScroll');
        });
      }
    });
    showNextPage.call(_this2, to); // Hot reloading

    setTimeout(function () {
      return hotReloadAPI(_this2);
    }, 100);
  });
}

function nuxtReady(_app) {
  window.onNuxtReadyCbs.forEach(function (cb) {
    if (typeof cb === 'function') {
      cb(_app);
    }
  }); // Special JSDOM

  if (typeof window._onNuxtLoaded === 'function') {
    window._onNuxtLoaded(_app);
  } // Add router hooks


  router.afterEach(function (to, from) {
    // Wait for fixPrepatch + $data updates
    vue__WEBPACK_IMPORTED_MODULE_18__["default"].nextTick(function () {
      return _app.$nuxt.$emit('routeChanged', to, from);
    });
  });
}

var noopData = function noopData() {
  return {};
};

var noopFetch = function noopFetch() {}; // Special hot reload with asyncData(context)


function getNuxtChildComponents($parent) {
  var $components = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : [];
  $parent.$children.forEach(function ($child) {
    if ($child.$vnode && $child.$vnode.data.nuxtChild && !$components.find(function (c) {
      return c.$options.__file === $child.$options.__file;
    })) {
      $components.push($child);
    }

    if ($child.$children && $child.$children.length) {
      getNuxtChildComponents($child, $components);
    }
  });
  return $components;
}

function hotReloadAPI(_app) {
  if (false) {}
  var $components = getNuxtChildComponents(_app.$nuxt, []);
  $components.forEach(addHotReload.bind(_app));
}

function addHotReload($component, depth) {
  var _this3 = this;

  if ($component.$vnode.data._hasHotReload) return;
  $component.$vnode.data._hasHotReload = true;

  var _forceUpdate = $component.$forceUpdate.bind($component.$parent);

  $component.$vnode.context.$forceUpdate =
  /*#__PURE__*/
  Object(_babel_runtime_helpers_esm_asyncToGenerator__WEBPACK_IMPORTED_MODULE_6__["default"])(
  /*#__PURE__*/
  regeneratorRuntime.mark(function _callee2() {
    var Components, Component, promises, next, context;
    return regeneratorRuntime.wrap(function _callee2$(_context2) {
      while (1) {
        switch (_context2.prev = _context2.next) {
          case 0:
            Components = Object(_utils_js__WEBPACK_IMPORTED_MODULE_21__["getMatchedComponents"])(router.currentRoute);
            Component = Components[depth];

            if (Component) {
              _context2.next = 4;
              break;
            }

            return _context2.abrupt("return", _forceUpdate());

          case 4:
            if (Object(_babel_runtime_helpers_esm_typeof__WEBPACK_IMPORTED_MODULE_3__["default"])(Component) === 'object' && !Component.options) {
              // Updated via vue-router resolveAsyncComponents()
              Component = vue__WEBPACK_IMPORTED_MODULE_18__["default"].extend(Component);
              Component._Ctor = Component;
            }

            _this3.error();

            promises = [];

            next = function next(path) {
              router.push(path);
            };

            _context2.next = 10;
            return Object(_utils_js__WEBPACK_IMPORTED_MODULE_21__["setContext"])(app, {
              route: router.currentRoute,
              isHMR: true,
              next: next.bind(_this3)
            });

          case 10:
            context = app.context;
            callMiddleware.call(_this3, Components, context).then(function () {
              // If layout changed
              if (depth !== 0) return Promise.resolve();
              var layout = Component.options.layout || 'default';

              if (typeof layout === 'function') {
                layout = layout(context);
              }

              if (_this3.layoutName === layout) return Promise.resolve();

              var promise = _this3.loadLayout(layout);

              promise.then(function () {
                _this3.setLayout(layout);

                vue__WEBPACK_IMPORTED_MODULE_18__["default"].nextTick(function () {
                  return hotReloadAPI(_this3);
                });
              });
              return promise;
            }).then(function () {
              return callMiddleware.call(_this3, Components, context, _this3.layout);
            }).then(function () {
              // Call asyncData(context)
              var pAsyncData = Object(_utils_js__WEBPACK_IMPORTED_MODULE_21__["promisify"])(Component.options.asyncData || noopData, context);
              pAsyncData.then(function (asyncDataResult) {
                Object(_utils_js__WEBPACK_IMPORTED_MODULE_21__["applyAsyncData"])(Component, asyncDataResult);
              });
              promises.push(pAsyncData); // Call fetch()

              Component.options.fetch = Component.options.fetch || noopFetch;
              var pFetch = Component.options.fetch(context);

              if (!pFetch || !(pFetch instanceof Promise) && typeof pFetch.then !== 'function') {
                pFetch = Promise.resolve(pFetch);
              }

              promises.push(pFetch);
              return Promise.all(promises);
            }).then(function () {
              _forceUpdate();

              setTimeout(function () {
                return hotReloadAPI(_this3);
              }, 100);
            });

          case 12:
          case "end":
            return _context2.stop();
        }
      }
    }, _callee2);
  }));
}

function mountApp(_x12) {
  return _mountApp.apply(this, arguments);
}

function _mountApp() {
  _mountApp = Object(_babel_runtime_helpers_esm_asyncToGenerator__WEBPACK_IMPORTED_MODULE_6__["default"])(
  /*#__PURE__*/
  regeneratorRuntime.mark(function _callee5(__app) {
    var Components, _app, mount, clientFirstMount;

    return regeneratorRuntime.wrap(function _callee5$(_context5) {
      while (1) {
        switch (_context5.prev = _context5.next) {
          case 0:
            // Set global variables
            app = __app.app;
            router = __app.router;
            store = __app.store; // Resolve route components

            _context5.next = 5;
            return Promise.all(resolveComponents(router));

          case 5:
            Components = _context5.sent;
            // Create Vue instance
            _app = new vue__WEBPACK_IMPORTED_MODULE_18__["default"](app); // Mounts Vue app to DOM element

            mount = function mount() {
              _app.$mount('#__nuxt'); // Add afterEach router hooks


              router.afterEach(normalizeComponents);
              router.afterEach(fixPrepatch.bind(_app)); // Listen for first Vue update

              vue__WEBPACK_IMPORTED_MODULE_18__["default"].nextTick(function () {
                // Call window.{{globals.readyCallback}} callbacks
                nuxtReady(_app); // Enable hot reloading

                hotReloadAPI(_app);
              });
            }; // Enable transitions


            _app.setTransitions = _app.$options.nuxt.setTransitions.bind(_app);

            if (Components.length) {
              _app.setTransitions(mapTransitions(Components, router.currentRoute));

              _lastPaths = router.currentRoute.matched.map(function (route) {
                return Object(_utils_js__WEBPACK_IMPORTED_MODULE_21__["compile"])(route.path)(router.currentRoute.params);
              });
            } // Initialize error handler


            _app.$loading = {}; // To avoid error while _app.$nuxt does not exist

            if (NUXT.error) _app.error(NUXT.error); // Add beforeEach router hooks

            router.beforeEach(loadAsyncComponents.bind(_app));
            router.beforeEach(render.bind(_app)); // If page already is server rendered

            if (!NUXT.serverRendered) {
              _context5.next = 17;
              break;
            }

            mount();
            return _context5.abrupt("return");

          case 17:
            // First render on client-side
            clientFirstMount = function clientFirstMount() {
              normalizeComponents(router.currentRoute, router.currentRoute);
              showNextPage.call(_app, router.currentRoute); // Don't call fixPrepatch.call(_app, router.currentRoute, router.currentRoute) since it's first render

              mount();
            };

            render.call(_app, router.currentRoute, router.currentRoute, function (path) {
              // If not redirected
              if (!path) {
                clientFirstMount();
                return;
              } // Add a one-time afterEach hook to
              // mount the app wait for redirect and route gets resolved


              var unregisterHook = router.afterEach(function (to, from) {
                unregisterHook();
                clientFirstMount();
              }); // Push the path and let route to be resolved

              router.push(path, undefined, function (err) {
                if (err) errorHandler(err);
              });
            });

          case 19:
          case "end":
            return _context5.stop();
        }
      }
    }, _callee5);
  }));
  return _mountApp.apply(this, arguments);
}
/* WEBPACK VAR INJECTION */}.call(this, __webpack_require__(/*! ./../node_modules/webpack/buildin/global.js */ "./node_modules/webpack/buildin/global.js")))

/***/ }),

/***/ "./.nuxt/components/no-ssr.js":
/*!************************************!*\
  !*** ./.nuxt/components/no-ssr.js ***!
  \************************************/
/*! exports provided: default */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony import */ var core_js_modules_es7_object_get_own_property_descriptors__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! core-js/modules/es7.object.get-own-property-descriptors */ "./node_modules/core-js/modules/es7.object.get-own-property-descriptors.js");
/* harmony import */ var core_js_modules_es7_object_get_own_property_descriptors__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es7_object_get_own_property_descriptors__WEBPACK_IMPORTED_MODULE_0__);
/* harmony import */ var core_js_modules_es6_symbol__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! core-js/modules/es6.symbol */ "./node_modules/core-js/modules/es6.symbol.js");
/* harmony import */ var core_js_modules_es6_symbol__WEBPACK_IMPORTED_MODULE_1___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es6_symbol__WEBPACK_IMPORTED_MODULE_1__);
/* harmony import */ var core_js_modules_web_dom_iterable__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! core-js/modules/web.dom.iterable */ "./node_modules/core-js/modules/web.dom.iterable.js");
/* harmony import */ var core_js_modules_web_dom_iterable__WEBPACK_IMPORTED_MODULE_2___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_web_dom_iterable__WEBPACK_IMPORTED_MODULE_2__);
/* harmony import */ var core_js_modules_es6_object_to_string__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! core-js/modules/es6.object.to-string */ "./node_modules/core-js/modules/es6.object.to-string.js");
/* harmony import */ var core_js_modules_es6_object_to_string__WEBPACK_IMPORTED_MODULE_3___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es6_object_to_string__WEBPACK_IMPORTED_MODULE_3__);
/* harmony import */ var core_js_modules_es6_object_keys__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! core-js/modules/es6.object.keys */ "./node_modules/core-js/modules/es6.object.keys.js");
/* harmony import */ var core_js_modules_es6_object_keys__WEBPACK_IMPORTED_MODULE_4___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es6_object_keys__WEBPACK_IMPORTED_MODULE_4__);
/* harmony import */ var _babel_runtime_helpers_esm_defineProperty__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! @babel/runtime/helpers/esm/defineProperty */ "./node_modules/@babel/runtime/helpers/esm/defineProperty.js");
/* harmony import */ var vue_no_ssr__WEBPACK_IMPORTED_MODULE_6__ = __webpack_require__(/*! vue-no-ssr */ "./node_modules/vue-no-ssr/dist/vue-no-ssr.common.js");
/* harmony import */ var vue_no_ssr__WEBPACK_IMPORTED_MODULE_6___default = /*#__PURE__*/__webpack_require__.n(vue_no_ssr__WEBPACK_IMPORTED_MODULE_6__);







function ownKeys(object, enumerableOnly) { var keys = Object.keys(object); if (Object.getOwnPropertySymbols) { var symbols = Object.getOwnPropertySymbols(object); if (enumerableOnly) symbols = symbols.filter(function (sym) { return Object.getOwnPropertyDescriptor(object, sym).enumerable; }); keys.push.apply(keys, symbols); } return keys; }

function _objectSpread(target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i] != null ? arguments[i] : {}; if (i % 2) { ownKeys(source, true).forEach(function (key) { Object(_babel_runtime_helpers_esm_defineProperty__WEBPACK_IMPORTED_MODULE_5__["default"])(target, key, source[key]); }); } else if (Object.getOwnPropertyDescriptors) { Object.defineProperties(target, Object.getOwnPropertyDescriptors(source)); } else { ownKeys(source).forEach(function (key) { Object.defineProperty(target, key, Object.getOwnPropertyDescriptor(source, key)); }); } } return target; }

/*
** From https://github.com/egoist/vue-no-ssr
** With the authorization of @egoist
*/

/* harmony default export */ __webpack_exports__["default"] = (_objectSpread({}, vue_no_ssr__WEBPACK_IMPORTED_MODULE_6___default.a, {
  name: 'NoSsr'
}));

/***/ }),

/***/ "./.nuxt/components/nuxt-build-indicator.vue":
/*!***************************************************!*\
  !*** ./.nuxt/components/nuxt-build-indicator.vue ***!
  \***************************************************/
/*! exports provided: default */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony import */ var _nuxt_build_indicator_vue_vue_type_template_id_71e9e103_scoped_true___WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./nuxt-build-indicator.vue?vue&type=template&id=71e9e103&scoped=true& */ "./.nuxt/components/nuxt-build-indicator.vue?vue&type=template&id=71e9e103&scoped=true&");
/* harmony import */ var _nuxt_build_indicator_vue_vue_type_script_lang_js___WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! ./nuxt-build-indicator.vue?vue&type=script&lang=js& */ "./.nuxt/components/nuxt-build-indicator.vue?vue&type=script&lang=js&");
/* empty/unused harmony star reexport *//* harmony import */ var _nuxt_build_indicator_vue_vue_type_style_index_0_id_71e9e103_scoped_true_lang_css___WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! ./nuxt-build-indicator.vue?vue&type=style&index=0&id=71e9e103&scoped=true&lang=css& */ "./.nuxt/components/nuxt-build-indicator.vue?vue&type=style&index=0&id=71e9e103&scoped=true&lang=css&");
/* harmony import */ var _node_modules_vue_loader_lib_runtime_componentNormalizer_js__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! ../../node_modules/vue-loader/lib/runtime/componentNormalizer.js */ "./node_modules/vue-loader/lib/runtime/componentNormalizer.js");






/* normalize component */

var component = Object(_node_modules_vue_loader_lib_runtime_componentNormalizer_js__WEBPACK_IMPORTED_MODULE_3__["default"])(
  _nuxt_build_indicator_vue_vue_type_script_lang_js___WEBPACK_IMPORTED_MODULE_1__["default"],
  _nuxt_build_indicator_vue_vue_type_template_id_71e9e103_scoped_true___WEBPACK_IMPORTED_MODULE_0__["render"],
  _nuxt_build_indicator_vue_vue_type_template_id_71e9e103_scoped_true___WEBPACK_IMPORTED_MODULE_0__["staticRenderFns"],
  false,
  null,
  "71e9e103",
  null
  
)

/* hot reload */
if (true) {
  var api = __webpack_require__(/*! ./node_modules/vue-hot-reload-api/dist/index.js */ "./node_modules/vue-hot-reload-api/dist/index.js")
  api.install(__webpack_require__(/*! vue */ "./node_modules/vue/dist/vue.runtime.esm.js"))
  if (api.compatible) {
    module.hot.accept()
    if (!api.isRecorded('71e9e103')) {
      api.createRecord('71e9e103', component.options)
    } else {
      api.reload('71e9e103', component.options)
    }
    module.hot.accept(/*! ./nuxt-build-indicator.vue?vue&type=template&id=71e9e103&scoped=true& */ "./.nuxt/components/nuxt-build-indicator.vue?vue&type=template&id=71e9e103&scoped=true&", function(__WEBPACK_OUTDATED_DEPENDENCIES__) { /* harmony import */ _nuxt_build_indicator_vue_vue_type_template_id_71e9e103_scoped_true___WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./nuxt-build-indicator.vue?vue&type=template&id=71e9e103&scoped=true& */ "./.nuxt/components/nuxt-build-indicator.vue?vue&type=template&id=71e9e103&scoped=true&");
(function () {
      api.rerender('71e9e103', {
        render: _nuxt_build_indicator_vue_vue_type_template_id_71e9e103_scoped_true___WEBPACK_IMPORTED_MODULE_0__["render"],
        staticRenderFns: _nuxt_build_indicator_vue_vue_type_template_id_71e9e103_scoped_true___WEBPACK_IMPORTED_MODULE_0__["staticRenderFns"]
      })
    })(__WEBPACK_OUTDATED_DEPENDENCIES__); }.bind(this))
  }
}
component.options.__file = ".nuxt/components/nuxt-build-indicator.vue"
/* harmony default export */ __webpack_exports__["default"] = (component.exports);

/***/ }),

/***/ "./.nuxt/components/nuxt-build-indicator.vue?vue&type=script&lang=js&":
/*!****************************************************************************!*\
  !*** ./.nuxt/components/nuxt-build-indicator.vue?vue&type=script&lang=js& ***!
  \****************************************************************************/
/*! exports provided: default */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony import */ var _node_modules_babel_loader_lib_index_js_ref_2_0_node_modules_vue_loader_lib_index_js_vue_loader_options_nuxt_build_indicator_vue_vue_type_script_lang_js___WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! -!../../node_modules/babel-loader/lib??ref--2-0!../../node_modules/vue-loader/lib??vue-loader-options!./nuxt-build-indicator.vue?vue&type=script&lang=js& */ "./node_modules/babel-loader/lib/index.js?!./node_modules/vue-loader/lib/index.js?!./.nuxt/components/nuxt-build-indicator.vue?vue&type=script&lang=js&");
/* empty/unused harmony star reexport */ /* harmony default export */ __webpack_exports__["default"] = (_node_modules_babel_loader_lib_index_js_ref_2_0_node_modules_vue_loader_lib_index_js_vue_loader_options_nuxt_build_indicator_vue_vue_type_script_lang_js___WEBPACK_IMPORTED_MODULE_0__["default"]); 

/***/ }),

/***/ "./.nuxt/components/nuxt-build-indicator.vue?vue&type=style&index=0&id=71e9e103&scoped=true&lang=css&":
/*!************************************************************************************************************!*\
  !*** ./.nuxt/components/nuxt-build-indicator.vue?vue&type=style&index=0&id=71e9e103&scoped=true&lang=css& ***!
  \************************************************************************************************************/
/*! no static exports found */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony import */ var _node_modules_vue_style_loader_index_js_ref_5_oneOf_1_0_node_modules_css_loader_dist_cjs_js_ref_5_oneOf_1_1_node_modules_vue_loader_lib_loaders_stylePostLoader_js_node_modules_postcss_loader_src_index_js_ref_5_oneOf_1_2_node_modules_vue_loader_lib_index_js_vue_loader_options_nuxt_build_indicator_vue_vue_type_style_index_0_id_71e9e103_scoped_true_lang_css___WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! -!../../node_modules/vue-style-loader??ref--5-oneOf-1-0!../../node_modules/css-loader/dist/cjs.js??ref--5-oneOf-1-1!../../node_modules/vue-loader/lib/loaders/stylePostLoader.js!../../node_modules/postcss-loader/src??ref--5-oneOf-1-2!../../node_modules/vue-loader/lib??vue-loader-options!./nuxt-build-indicator.vue?vue&type=style&index=0&id=71e9e103&scoped=true&lang=css& */ "./node_modules/vue-style-loader/index.js?!./node_modules/css-loader/dist/cjs.js?!./node_modules/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/postcss-loader/src/index.js?!./node_modules/vue-loader/lib/index.js?!./.nuxt/components/nuxt-build-indicator.vue?vue&type=style&index=0&id=71e9e103&scoped=true&lang=css&");
/* harmony import */ var _node_modules_vue_style_loader_index_js_ref_5_oneOf_1_0_node_modules_css_loader_dist_cjs_js_ref_5_oneOf_1_1_node_modules_vue_loader_lib_loaders_stylePostLoader_js_node_modules_postcss_loader_src_index_js_ref_5_oneOf_1_2_node_modules_vue_loader_lib_index_js_vue_loader_options_nuxt_build_indicator_vue_vue_type_style_index_0_id_71e9e103_scoped_true_lang_css___WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(_node_modules_vue_style_loader_index_js_ref_5_oneOf_1_0_node_modules_css_loader_dist_cjs_js_ref_5_oneOf_1_1_node_modules_vue_loader_lib_loaders_stylePostLoader_js_node_modules_postcss_loader_src_index_js_ref_5_oneOf_1_2_node_modules_vue_loader_lib_index_js_vue_loader_options_nuxt_build_indicator_vue_vue_type_style_index_0_id_71e9e103_scoped_true_lang_css___WEBPACK_IMPORTED_MODULE_0__);
/* harmony reexport (unknown) */ for(var __WEBPACK_IMPORT_KEY__ in _node_modules_vue_style_loader_index_js_ref_5_oneOf_1_0_node_modules_css_loader_dist_cjs_js_ref_5_oneOf_1_1_node_modules_vue_loader_lib_loaders_stylePostLoader_js_node_modules_postcss_loader_src_index_js_ref_5_oneOf_1_2_node_modules_vue_loader_lib_index_js_vue_loader_options_nuxt_build_indicator_vue_vue_type_style_index_0_id_71e9e103_scoped_true_lang_css___WEBPACK_IMPORTED_MODULE_0__) if(__WEBPACK_IMPORT_KEY__ !== 'default') (function(key) { __webpack_require__.d(__webpack_exports__, key, function() { return _node_modules_vue_style_loader_index_js_ref_5_oneOf_1_0_node_modules_css_loader_dist_cjs_js_ref_5_oneOf_1_1_node_modules_vue_loader_lib_loaders_stylePostLoader_js_node_modules_postcss_loader_src_index_js_ref_5_oneOf_1_2_node_modules_vue_loader_lib_index_js_vue_loader_options_nuxt_build_indicator_vue_vue_type_style_index_0_id_71e9e103_scoped_true_lang_css___WEBPACK_IMPORTED_MODULE_0__[key]; }) }(__WEBPACK_IMPORT_KEY__));
 /* harmony default export */ __webpack_exports__["default"] = (_node_modules_vue_style_loader_index_js_ref_5_oneOf_1_0_node_modules_css_loader_dist_cjs_js_ref_5_oneOf_1_1_node_modules_vue_loader_lib_loaders_stylePostLoader_js_node_modules_postcss_loader_src_index_js_ref_5_oneOf_1_2_node_modules_vue_loader_lib_index_js_vue_loader_options_nuxt_build_indicator_vue_vue_type_style_index_0_id_71e9e103_scoped_true_lang_css___WEBPACK_IMPORTED_MODULE_0___default.a); 

/***/ }),

/***/ "./.nuxt/components/nuxt-build-indicator.vue?vue&type=template&id=71e9e103&scoped=true&":
/*!**********************************************************************************************!*\
  !*** ./.nuxt/components/nuxt-build-indicator.vue?vue&type=template&id=71e9e103&scoped=true& ***!
  \**********************************************************************************************/
/*! exports provided: render, staticRenderFns */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony import */ var _node_modules_vue_loader_lib_loaders_templateLoader_js_vue_loader_options_node_modules_vue_loader_lib_index_js_vue_loader_options_nuxt_build_indicator_vue_vue_type_template_id_71e9e103_scoped_true___WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! -!../../node_modules/vue-loader/lib/loaders/templateLoader.js??vue-loader-options!../../node_modules/vue-loader/lib??vue-loader-options!./nuxt-build-indicator.vue?vue&type=template&id=71e9e103&scoped=true& */ "./node_modules/vue-loader/lib/loaders/templateLoader.js?!./node_modules/vue-loader/lib/index.js?!./.nuxt/components/nuxt-build-indicator.vue?vue&type=template&id=71e9e103&scoped=true&");
/* harmony reexport (safe) */ __webpack_require__.d(__webpack_exports__, "render", function() { return _node_modules_vue_loader_lib_loaders_templateLoader_js_vue_loader_options_node_modules_vue_loader_lib_index_js_vue_loader_options_nuxt_build_indicator_vue_vue_type_template_id_71e9e103_scoped_true___WEBPACK_IMPORTED_MODULE_0__["render"]; });

/* harmony reexport (safe) */ __webpack_require__.d(__webpack_exports__, "staticRenderFns", function() { return _node_modules_vue_loader_lib_loaders_templateLoader_js_vue_loader_options_node_modules_vue_loader_lib_index_js_vue_loader_options_nuxt_build_indicator_vue_vue_type_template_id_71e9e103_scoped_true___WEBPACK_IMPORTED_MODULE_0__["staticRenderFns"]; });



/***/ }),

/***/ "./.nuxt/components/nuxt-child.js":
/*!****************************************!*\
  !*** ./.nuxt/components/nuxt-child.js ***!
  \****************************************/
/*! exports provided: default */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony default export */ __webpack_exports__["default"] = ({
  name: 'NuxtChild',
  functional: true,
  props: {
    nuxtChildKey: {
      type: String,
      default: ''
    },
    keepAlive: Boolean,
    keepAliveProps: {
      type: Object,
      default: undefined
    }
  },
  render: function render(h, _ref) {
    var parent = _ref.parent,
        data = _ref.data,
        props = _ref.props;
    data.nuxtChild = true;
    var _parent = parent;
    var transitions = parent.$nuxt.nuxt.transitions;
    var defaultTransition = parent.$nuxt.nuxt.defaultTransition;
    var depth = 0;

    while (parent) {
      if (parent.$vnode && parent.$vnode.data.nuxtChild) {
        depth++;
      }

      parent = parent.$parent;
    }

    data.nuxtChildDepth = depth;
    var transition = transitions[depth] || defaultTransition;
    var transitionProps = {};
    transitionsKeys.forEach(function (key) {
      if (typeof transition[key] !== 'undefined') {
        transitionProps[key] = transition[key];
      }
    });
    var listeners = {};
    listenersKeys.forEach(function (key) {
      if (typeof transition[key] === 'function') {
        listeners[key] = transition[key].bind(_parent);
      }
    }); // Add triggerScroll event on beforeEnter (fix #1376)

    var beforeEnter = listeners.beforeEnter;

    listeners.beforeEnter = function (el) {
      // Ensure to trigger scroll event after calling scrollBehavior
      window.$nuxt.$nextTick(function () {
        window.$nuxt.$emit('triggerScroll');
      });
      if (beforeEnter) return beforeEnter.call(_parent, el);
    };

    var routerView = [h('router-view', data)];

    if (props.keepAlive) {
      routerView = [h('keep-alive', {
        props: props.keepAliveProps
      }, routerView)];
    }

    return h('transition', {
      props: transitionProps,
      on: listeners
    }, routerView);
  }
});
var transitionsKeys = ['name', 'mode', 'appear', 'css', 'type', 'duration', 'enterClass', 'leaveClass', 'appearClass', 'enterActiveClass', 'enterActiveClass', 'leaveActiveClass', 'appearActiveClass', 'enterToClass', 'leaveToClass', 'appearToClass'];
var listenersKeys = ['beforeEnter', 'enter', 'afterEnter', 'enterCancelled', 'beforeLeave', 'leave', 'afterLeave', 'leaveCancelled', 'beforeAppear', 'appear', 'afterAppear', 'appearCancelled'];

/***/ }),

/***/ "./.nuxt/components/nuxt-error.vue":
/*!*****************************************!*\
  !*** ./.nuxt/components/nuxt-error.vue ***!
  \*****************************************/
/*! exports provided: default */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony import */ var _nuxt_error_vue_vue_type_template_id_74e3df5b___WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./nuxt-error.vue?vue&type=template&id=74e3df5b& */ "./.nuxt/components/nuxt-error.vue?vue&type=template&id=74e3df5b&");
/* harmony import */ var _nuxt_error_vue_vue_type_script_lang_js___WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! ./nuxt-error.vue?vue&type=script&lang=js& */ "./.nuxt/components/nuxt-error.vue?vue&type=script&lang=js&");
/* empty/unused harmony star reexport *//* harmony import */ var _nuxt_error_vue_vue_type_style_index_0_lang_css___WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! ./nuxt-error.vue?vue&type=style&index=0&lang=css& */ "./.nuxt/components/nuxt-error.vue?vue&type=style&index=0&lang=css&");
/* harmony import */ var _node_modules_vue_loader_lib_runtime_componentNormalizer_js__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! ../../node_modules/vue-loader/lib/runtime/componentNormalizer.js */ "./node_modules/vue-loader/lib/runtime/componentNormalizer.js");






/* normalize component */

var component = Object(_node_modules_vue_loader_lib_runtime_componentNormalizer_js__WEBPACK_IMPORTED_MODULE_3__["default"])(
  _nuxt_error_vue_vue_type_script_lang_js___WEBPACK_IMPORTED_MODULE_1__["default"],
  _nuxt_error_vue_vue_type_template_id_74e3df5b___WEBPACK_IMPORTED_MODULE_0__["render"],
  _nuxt_error_vue_vue_type_template_id_74e3df5b___WEBPACK_IMPORTED_MODULE_0__["staticRenderFns"],
  false,
  null,
  null,
  null
  
)

/* hot reload */
if (true) {
  var api = __webpack_require__(/*! ./node_modules/vue-hot-reload-api/dist/index.js */ "./node_modules/vue-hot-reload-api/dist/index.js")
  api.install(__webpack_require__(/*! vue */ "./node_modules/vue/dist/vue.runtime.esm.js"))
  if (api.compatible) {
    module.hot.accept()
    if (!api.isRecorded('74e3df5b')) {
      api.createRecord('74e3df5b', component.options)
    } else {
      api.reload('74e3df5b', component.options)
    }
    module.hot.accept(/*! ./nuxt-error.vue?vue&type=template&id=74e3df5b& */ "./.nuxt/components/nuxt-error.vue?vue&type=template&id=74e3df5b&", function(__WEBPACK_OUTDATED_DEPENDENCIES__) { /* harmony import */ _nuxt_error_vue_vue_type_template_id_74e3df5b___WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./nuxt-error.vue?vue&type=template&id=74e3df5b& */ "./.nuxt/components/nuxt-error.vue?vue&type=template&id=74e3df5b&");
(function () {
      api.rerender('74e3df5b', {
        render: _nuxt_error_vue_vue_type_template_id_74e3df5b___WEBPACK_IMPORTED_MODULE_0__["render"],
        staticRenderFns: _nuxt_error_vue_vue_type_template_id_74e3df5b___WEBPACK_IMPORTED_MODULE_0__["staticRenderFns"]
      })
    })(__WEBPACK_OUTDATED_DEPENDENCIES__); }.bind(this))
  }
}
component.options.__file = ".nuxt/components/nuxt-error.vue"
/* harmony default export */ __webpack_exports__["default"] = (component.exports);

/***/ }),

/***/ "./.nuxt/components/nuxt-error.vue?vue&type=script&lang=js&":
/*!******************************************************************!*\
  !*** ./.nuxt/components/nuxt-error.vue?vue&type=script&lang=js& ***!
  \******************************************************************/
/*! exports provided: default */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony import */ var _node_modules_babel_loader_lib_index_js_ref_2_0_node_modules_vue_loader_lib_index_js_vue_loader_options_nuxt_error_vue_vue_type_script_lang_js___WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! -!../../node_modules/babel-loader/lib??ref--2-0!../../node_modules/vue-loader/lib??vue-loader-options!./nuxt-error.vue?vue&type=script&lang=js& */ "./node_modules/babel-loader/lib/index.js?!./node_modules/vue-loader/lib/index.js?!./.nuxt/components/nuxt-error.vue?vue&type=script&lang=js&");
/* empty/unused harmony star reexport */ /* harmony default export */ __webpack_exports__["default"] = (_node_modules_babel_loader_lib_index_js_ref_2_0_node_modules_vue_loader_lib_index_js_vue_loader_options_nuxt_error_vue_vue_type_script_lang_js___WEBPACK_IMPORTED_MODULE_0__["default"]); 

/***/ }),

/***/ "./.nuxt/components/nuxt-error.vue?vue&type=style&index=0&lang=css&":
/*!**************************************************************************!*\
  !*** ./.nuxt/components/nuxt-error.vue?vue&type=style&index=0&lang=css& ***!
  \**************************************************************************/
/*! no static exports found */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony import */ var _node_modules_vue_style_loader_index_js_ref_5_oneOf_1_0_node_modules_css_loader_dist_cjs_js_ref_5_oneOf_1_1_node_modules_vue_loader_lib_loaders_stylePostLoader_js_node_modules_postcss_loader_src_index_js_ref_5_oneOf_1_2_node_modules_vue_loader_lib_index_js_vue_loader_options_nuxt_error_vue_vue_type_style_index_0_lang_css___WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! -!../../node_modules/vue-style-loader??ref--5-oneOf-1-0!../../node_modules/css-loader/dist/cjs.js??ref--5-oneOf-1-1!../../node_modules/vue-loader/lib/loaders/stylePostLoader.js!../../node_modules/postcss-loader/src??ref--5-oneOf-1-2!../../node_modules/vue-loader/lib??vue-loader-options!./nuxt-error.vue?vue&type=style&index=0&lang=css& */ "./node_modules/vue-style-loader/index.js?!./node_modules/css-loader/dist/cjs.js?!./node_modules/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/postcss-loader/src/index.js?!./node_modules/vue-loader/lib/index.js?!./.nuxt/components/nuxt-error.vue?vue&type=style&index=0&lang=css&");
/* harmony import */ var _node_modules_vue_style_loader_index_js_ref_5_oneOf_1_0_node_modules_css_loader_dist_cjs_js_ref_5_oneOf_1_1_node_modules_vue_loader_lib_loaders_stylePostLoader_js_node_modules_postcss_loader_src_index_js_ref_5_oneOf_1_2_node_modules_vue_loader_lib_index_js_vue_loader_options_nuxt_error_vue_vue_type_style_index_0_lang_css___WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(_node_modules_vue_style_loader_index_js_ref_5_oneOf_1_0_node_modules_css_loader_dist_cjs_js_ref_5_oneOf_1_1_node_modules_vue_loader_lib_loaders_stylePostLoader_js_node_modules_postcss_loader_src_index_js_ref_5_oneOf_1_2_node_modules_vue_loader_lib_index_js_vue_loader_options_nuxt_error_vue_vue_type_style_index_0_lang_css___WEBPACK_IMPORTED_MODULE_0__);
/* harmony reexport (unknown) */ for(var __WEBPACK_IMPORT_KEY__ in _node_modules_vue_style_loader_index_js_ref_5_oneOf_1_0_node_modules_css_loader_dist_cjs_js_ref_5_oneOf_1_1_node_modules_vue_loader_lib_loaders_stylePostLoader_js_node_modules_postcss_loader_src_index_js_ref_5_oneOf_1_2_node_modules_vue_loader_lib_index_js_vue_loader_options_nuxt_error_vue_vue_type_style_index_0_lang_css___WEBPACK_IMPORTED_MODULE_0__) if(__WEBPACK_IMPORT_KEY__ !== 'default') (function(key) { __webpack_require__.d(__webpack_exports__, key, function() { return _node_modules_vue_style_loader_index_js_ref_5_oneOf_1_0_node_modules_css_loader_dist_cjs_js_ref_5_oneOf_1_1_node_modules_vue_loader_lib_loaders_stylePostLoader_js_node_modules_postcss_loader_src_index_js_ref_5_oneOf_1_2_node_modules_vue_loader_lib_index_js_vue_loader_options_nuxt_error_vue_vue_type_style_index_0_lang_css___WEBPACK_IMPORTED_MODULE_0__[key]; }) }(__WEBPACK_IMPORT_KEY__));
 /* harmony default export */ __webpack_exports__["default"] = (_node_modules_vue_style_loader_index_js_ref_5_oneOf_1_0_node_modules_css_loader_dist_cjs_js_ref_5_oneOf_1_1_node_modules_vue_loader_lib_loaders_stylePostLoader_js_node_modules_postcss_loader_src_index_js_ref_5_oneOf_1_2_node_modules_vue_loader_lib_index_js_vue_loader_options_nuxt_error_vue_vue_type_style_index_0_lang_css___WEBPACK_IMPORTED_MODULE_0___default.a); 

/***/ }),

/***/ "./.nuxt/components/nuxt-error.vue?vue&type=template&id=74e3df5b&":
/*!************************************************************************!*\
  !*** ./.nuxt/components/nuxt-error.vue?vue&type=template&id=74e3df5b& ***!
  \************************************************************************/
/*! exports provided: render, staticRenderFns */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony import */ var _node_modules_vue_loader_lib_loaders_templateLoader_js_vue_loader_options_node_modules_vue_loader_lib_index_js_vue_loader_options_nuxt_error_vue_vue_type_template_id_74e3df5b___WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! -!../../node_modules/vue-loader/lib/loaders/templateLoader.js??vue-loader-options!../../node_modules/vue-loader/lib??vue-loader-options!./nuxt-error.vue?vue&type=template&id=74e3df5b& */ "./node_modules/vue-loader/lib/loaders/templateLoader.js?!./node_modules/vue-loader/lib/index.js?!./.nuxt/components/nuxt-error.vue?vue&type=template&id=74e3df5b&");
/* harmony reexport (safe) */ __webpack_require__.d(__webpack_exports__, "render", function() { return _node_modules_vue_loader_lib_loaders_templateLoader_js_vue_loader_options_node_modules_vue_loader_lib_index_js_vue_loader_options_nuxt_error_vue_vue_type_template_id_74e3df5b___WEBPACK_IMPORTED_MODULE_0__["render"]; });

/* harmony reexport (safe) */ __webpack_require__.d(__webpack_exports__, "staticRenderFns", function() { return _node_modules_vue_loader_lib_loaders_templateLoader_js_vue_loader_options_node_modules_vue_loader_lib_index_js_vue_loader_options_nuxt_error_vue_vue_type_template_id_74e3df5b___WEBPACK_IMPORTED_MODULE_0__["staticRenderFns"]; });



/***/ }),

/***/ "./.nuxt/components/nuxt-link.client.js":
/*!**********************************************!*\
  !*** ./.nuxt/components/nuxt-link.client.js ***!
  \**********************************************/
/*! exports provided: default */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony import */ var core_js_modules_es6_object_to_string__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! core-js/modules/es6.object.to-string */ "./node_modules/core-js/modules/es6.object.to-string.js");
/* harmony import */ var core_js_modules_es6_object_to_string__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es6_object_to_string__WEBPACK_IMPORTED_MODULE_0__);
/* harmony import */ var core_js_modules_es7_symbol_async_iterator__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! core-js/modules/es7.symbol.async-iterator */ "./node_modules/core-js/modules/es7.symbol.async-iterator.js");
/* harmony import */ var core_js_modules_es7_symbol_async_iterator__WEBPACK_IMPORTED_MODULE_1___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es7_symbol_async_iterator__WEBPACK_IMPORTED_MODULE_1__);
/* harmony import */ var core_js_modules_es6_symbol__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! core-js/modules/es6.symbol */ "./node_modules/core-js/modules/es6.symbol.js");
/* harmony import */ var core_js_modules_es6_symbol__WEBPACK_IMPORTED_MODULE_2___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es6_symbol__WEBPACK_IMPORTED_MODULE_2__);
/* harmony import */ var core_js_modules_web_dom_iterable__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! core-js/modules/web.dom.iterable */ "./node_modules/core-js/modules/web.dom.iterable.js");
/* harmony import */ var core_js_modules_web_dom_iterable__WEBPACK_IMPORTED_MODULE_3___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_web_dom_iterable__WEBPACK_IMPORTED_MODULE_3__);
/* harmony import */ var core_js_modules_es7_array_includes__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! core-js/modules/es7.array.includes */ "./node_modules/core-js/modules/es7.array.includes.js");
/* harmony import */ var core_js_modules_es7_array_includes__WEBPACK_IMPORTED_MODULE_4___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es7_array_includes__WEBPACK_IMPORTED_MODULE_4__);
/* harmony import */ var core_js_modules_es6_string_includes__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! core-js/modules/es6.string.includes */ "./node_modules/core-js/modules/es6.string.includes.js");
/* harmony import */ var core_js_modules_es6_string_includes__WEBPACK_IMPORTED_MODULE_5___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es6_string_includes__WEBPACK_IMPORTED_MODULE_5__);
/* harmony import */ var vue__WEBPACK_IMPORTED_MODULE_6__ = __webpack_require__(/*! vue */ "./node_modules/vue/dist/vue.runtime.esm.js");








var requestIdleCallback = window.requestIdleCallback || function (cb) {
  var start = Date.now();
  return setTimeout(function () {
    cb({
      didTimeout: false,
      timeRemaining: function timeRemaining() {
        return Math.max(0, 50 - (Date.now() - start));
      }
    });
  }, 1);
};

var observer = window.IntersectionObserver && new window.IntersectionObserver(function (entries) {
  entries.forEach(function (_ref) {
    var intersectionRatio = _ref.intersectionRatio,
        link = _ref.target;

    if (intersectionRatio <= 0) {
      return;
    }

    link.__prefetch();
  });
});
/* harmony default export */ __webpack_exports__["default"] = ({
  name: 'NuxtLink',
  extends: vue__WEBPACK_IMPORTED_MODULE_6__["default"].component('RouterLink'),
  props: {
    noPrefetch: {
      type: Boolean,
      default: false
    }
  },
  mounted: function mounted() {
    if (!this.noPrefetch) {
      requestIdleCallback(this.observe, {
        timeout: 2e3
      });
    }
  },
  beforeDestroy: function beforeDestroy() {
    if (this.__observed) {
      observer.unobserve(this.$el);
      delete this.$el.__prefetch;
    }
  },
  methods: {
    observe: function observe() {
      // If no IntersectionObserver, avoid prefetching
      if (!observer) {
        return;
      } // Add to observer


      if (this.shouldPrefetch()) {
        this.$el.__prefetch = this.prefetch.bind(this);
        observer.observe(this.$el);
        this.__observed = true;
      }
    },
    shouldPrefetch: function shouldPrefetch() {
      return this.getPrefetchComponents().length > 0;
    },
    canPrefetch: function canPrefetch() {
      var conn = navigator.connection;
      var hasBadConnection = this.$nuxt.isOffline || conn && ((conn.effectiveType || '').includes('2g') || conn.saveData);
      return !hasBadConnection;
    },
    getPrefetchComponents: function getPrefetchComponents() {
      var ref = this.$router.resolve(this.to, this.$route, this.append);
      var Components = ref.resolved.matched.map(function (r) {
        return r.components.default;
      });
      return Components.filter(function (Component) {
        return typeof Component === 'function' && !Component.options && !Component.__prefetched;
      });
    },
    prefetch: function prefetch() {
      if (!this.canPrefetch()) {
        return;
      } // Stop obersing this link (in case of internet connection changes)


      observer.unobserve(this.$el);
      var Components = this.getPrefetchComponents();
      var _iteratorNormalCompletion = true;
      var _didIteratorError = false;
      var _iteratorError = undefined;

      try {
        for (var _iterator = Components[Symbol.iterator](), _step; !(_iteratorNormalCompletion = (_step = _iterator.next()).done); _iteratorNormalCompletion = true) {
          var Component = _step.value;
          var componentOrPromise = Component();

          if (componentOrPromise instanceof Promise) {
            componentOrPromise.catch(function () {});
          }

          Component.__prefetched = true;
        }
      } catch (err) {
        _didIteratorError = true;
        _iteratorError = err;
      } finally {
        try {
          if (!_iteratorNormalCompletion && _iterator.return != null) {
            _iterator.return();
          }
        } finally {
          if (_didIteratorError) {
            throw _iteratorError;
          }
        }
      }
    }
  }
});

/***/ }),

/***/ "./.nuxt/components/nuxt.js":
/*!**********************************!*\
  !*** ./.nuxt/components/nuxt.js ***!
  \**********************************/
/*! exports provided: default */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony import */ var core_js_modules_es6_regexp_replace__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! core-js/modules/es6.regexp.replace */ "./node_modules/core-js/modules/es6.regexp.replace.js");
/* harmony import */ var core_js_modules_es6_regexp_replace__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es6_regexp_replace__WEBPACK_IMPORTED_MODULE_0__);
/* harmony import */ var _babel_runtime_helpers_esm_slicedToArray__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @babel/runtime/helpers/esm/slicedToArray */ "./node_modules/@babel/runtime/helpers/esm/slicedToArray.js");
/* harmony import */ var vue__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! vue */ "./node_modules/vue/dist/vue.runtime.esm.js");
/* harmony import */ var _utils__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! ../utils */ "./.nuxt/utils.js");
/* harmony import */ var _nuxt_error_vue__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! ./nuxt-error.vue */ "./.nuxt/components/nuxt-error.vue");
/* harmony import */ var _nuxt_child__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! ./nuxt-child */ "./.nuxt/components/nuxt-child.js");






/* harmony default export */ __webpack_exports__["default"] = ({
  name: 'Nuxt',
  components: {
    NuxtChild: _nuxt_child__WEBPACK_IMPORTED_MODULE_5__["default"],
    NuxtError: _nuxt_error_vue__WEBPACK_IMPORTED_MODULE_4__["default"]
  },
  props: {
    nuxtChildKey: {
      type: String,
      default: undefined
    },
    keepAlive: Boolean,
    keepAliveProps: {
      type: Object,
      default: undefined
    },
    name: {
      type: String,
      default: 'default'
    }
  },
  computed: {
    routerViewKey: function routerViewKey() {
      // If nuxtChildKey prop is given or current route has children
      if (typeof this.nuxtChildKey !== 'undefined' || this.$route.matched.length > 1) {
        return this.nuxtChildKey || Object(_utils__WEBPACK_IMPORTED_MODULE_3__["compile"])(this.$route.matched[0].path)(this.$route.params);
      }

      var _this$$route$matched = Object(_babel_runtime_helpers_esm_slicedToArray__WEBPACK_IMPORTED_MODULE_1__["default"])(this.$route.matched, 1),
          matchedRoute = _this$$route$matched[0];

      if (!matchedRoute) {
        return this.$route.path;
      }

      var Component = matchedRoute.components.default;

      if (Component && Component.options) {
        var options = Component.options;

        if (options.key) {
          return typeof options.key === 'function' ? options.key(this.$route) : options.key;
        }
      }

      var strict = /\/$/.test(matchedRoute.path);
      return strict ? this.$route.path : this.$route.path.replace(/\/$/, '');
    }
  },
  beforeCreate: function beforeCreate() {
    vue__WEBPACK_IMPORTED_MODULE_2__["default"].util.defineReactive(this, 'nuxt', this.$root.$options.nuxt);
  },
  render: function render(h) {
    // If there is some error
    if (this.nuxt.err) {
      return h('NuxtError', {
        props: {
          error: this.nuxt.err
        }
      });
    } // Directly return nuxt child


    return h('NuxtChild', {
      key: this.routerViewKey,
      props: this.$props
    });
  }
});

/***/ }),

/***/ "./.nuxt/index.js":
/*!************************!*\
  !*** ./.nuxt/index.js ***!
  \************************/
/*! exports provided: createApp, NuxtError */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "createApp", function() { return createApp; });
/* harmony import */ var core_js_modules_es7_object_get_own_property_descriptors__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! core-js/modules/es7.object.get-own-property-descriptors */ "./node_modules/core-js/modules/es7.object.get-own-property-descriptors.js");
/* harmony import */ var core_js_modules_es7_object_get_own_property_descriptors__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es7_object_get_own_property_descriptors__WEBPACK_IMPORTED_MODULE_0__);
/* harmony import */ var core_js_modules_es6_symbol__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! core-js/modules/es6.symbol */ "./node_modules/core-js/modules/es6.symbol.js");
/* harmony import */ var core_js_modules_es6_symbol__WEBPACK_IMPORTED_MODULE_1___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es6_symbol__WEBPACK_IMPORTED_MODULE_1__);
/* harmony import */ var core_js_modules_web_dom_iterable__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! core-js/modules/web.dom.iterable */ "./node_modules/core-js/modules/web.dom.iterable.js");
/* harmony import */ var core_js_modules_web_dom_iterable__WEBPACK_IMPORTED_MODULE_2___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_web_dom_iterable__WEBPACK_IMPORTED_MODULE_2__);
/* harmony import */ var core_js_modules_es6_object_keys__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! core-js/modules/es6.object.keys */ "./node_modules/core-js/modules/es6.object.keys.js");
/* harmony import */ var core_js_modules_es6_object_keys__WEBPACK_IMPORTED_MODULE_3___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es6_object_keys__WEBPACK_IMPORTED_MODULE_3__);
/* harmony import */ var core_js_modules_es6_object_to_string__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! core-js/modules/es6.object.to-string */ "./node_modules/core-js/modules/es6.object.to-string.js");
/* harmony import */ var core_js_modules_es6_object_to_string__WEBPACK_IMPORTED_MODULE_4___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es6_object_to_string__WEBPACK_IMPORTED_MODULE_4__);
/* harmony import */ var _babel_runtime_helpers_esm_defineProperty__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! @babel/runtime/helpers/esm/defineProperty */ "./node_modules/@babel/runtime/helpers/esm/defineProperty.js");
/* harmony import */ var regenerator_runtime_runtime__WEBPACK_IMPORTED_MODULE_6__ = __webpack_require__(/*! regenerator-runtime/runtime */ "./node_modules/regenerator-runtime/runtime.js");
/* harmony import */ var regenerator_runtime_runtime__WEBPACK_IMPORTED_MODULE_6___default = /*#__PURE__*/__webpack_require__.n(regenerator_runtime_runtime__WEBPACK_IMPORTED_MODULE_6__);
/* harmony import */ var _babel_runtime_helpers_esm_asyncToGenerator__WEBPACK_IMPORTED_MODULE_7__ = __webpack_require__(/*! @babel/runtime/helpers/esm/asyncToGenerator */ "./node_modules/@babel/runtime/helpers/esm/asyncToGenerator.js");
/* harmony import */ var core_js_modules_es6_function_name__WEBPACK_IMPORTED_MODULE_8__ = __webpack_require__(/*! core-js/modules/es6.function.name */ "./node_modules/core-js/modules/es6.function.name.js");
/* harmony import */ var core_js_modules_es6_function_name__WEBPACK_IMPORTED_MODULE_8___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es6_function_name__WEBPACK_IMPORTED_MODULE_8__);
/* harmony import */ var vue__WEBPACK_IMPORTED_MODULE_9__ = __webpack_require__(/*! vue */ "./node_modules/vue/dist/vue.runtime.esm.js");
/* harmony import */ var vue_meta__WEBPACK_IMPORTED_MODULE_10__ = __webpack_require__(/*! vue-meta */ "./node_modules/vue-meta/lib/vue-meta.js");
/* harmony import */ var vue_meta__WEBPACK_IMPORTED_MODULE_10___default = /*#__PURE__*/__webpack_require__.n(vue_meta__WEBPACK_IMPORTED_MODULE_10__);
/* harmony import */ var _router_js__WEBPACK_IMPORTED_MODULE_11__ = __webpack_require__(/*! ./router.js */ "./.nuxt/router.js");
/* harmony import */ var _components_no_ssr_js__WEBPACK_IMPORTED_MODULE_12__ = __webpack_require__(/*! ./components/no-ssr.js */ "./.nuxt/components/no-ssr.js");
/* harmony import */ var _components_nuxt_child_js__WEBPACK_IMPORTED_MODULE_13__ = __webpack_require__(/*! ./components/nuxt-child.js */ "./.nuxt/components/nuxt-child.js");
/* harmony import */ var _components_nuxt_error_vue__WEBPACK_IMPORTED_MODULE_14__ = __webpack_require__(/*! ./components/nuxt-error.vue */ "./.nuxt/components/nuxt-error.vue");
/* harmony reexport (safe) */ __webpack_require__.d(__webpack_exports__, "NuxtError", function() { return _components_nuxt_error_vue__WEBPACK_IMPORTED_MODULE_14__["default"]; });

/* harmony import */ var _components_nuxt_js__WEBPACK_IMPORTED_MODULE_15__ = __webpack_require__(/*! ./components/nuxt.js */ "./.nuxt/components/nuxt.js");
/* harmony import */ var _App_js__WEBPACK_IMPORTED_MODULE_16__ = __webpack_require__(/*! ./App.js */ "./.nuxt/App.js");
/* harmony import */ var _utils__WEBPACK_IMPORTED_MODULE_17__ = __webpack_require__(/*! ./utils */ "./.nuxt/utils.js");
/* harmony import */ var _store_js__WEBPACK_IMPORTED_MODULE_18__ = __webpack_require__(/*! ./store.js */ "./.nuxt/store.js");
/* harmony import */ var nuxt_plugin_i18n_66ff12a5__WEBPACK_IMPORTED_MODULE_19__ = __webpack_require__(/*! nuxt_plugin_i18n_66ff12a5 */ "./plugins/i18n.js");
/* harmony import */ var nuxt_plugin_elementui_d905880e__WEBPACK_IMPORTED_MODULE_20__ = __webpack_require__(/*! nuxt_plugin_elementui_d905880e */ "./plugins/element-ui.js");
/* harmony import */ var nuxt_plugin_filter_6c04580b__WEBPACK_IMPORTED_MODULE_21__ = __webpack_require__(/*! nuxt_plugin_filter_6c04580b */ "./plugins/filter.js");










function ownKeys(object, enumerableOnly) { var keys = Object.keys(object); if (Object.getOwnPropertySymbols) { var symbols = Object.getOwnPropertySymbols(object); if (enumerableOnly) symbols = symbols.filter(function (sym) { return Object.getOwnPropertyDescriptor(object, sym).enumerable; }); keys.push.apply(keys, symbols); } return keys; }

function _objectSpread(target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i] != null ? arguments[i] : {}; if (i % 2) { ownKeys(source, true).forEach(function (key) { Object(_babel_runtime_helpers_esm_defineProperty__WEBPACK_IMPORTED_MODULE_5__["default"])(target, key, source[key]); }); } else if (Object.getOwnPropertyDescriptors) { Object.defineProperties(target, Object.getOwnPropertyDescriptors(source)); } else { ownKeys(source).forEach(function (key) { Object.defineProperty(target, key, Object.getOwnPropertyDescriptor(source, key)); }); } } return target; }











/* Plugins */

 // Source: ../plugins/i18n.js (mode: 'all')

 // Source: ../plugins/element-ui (mode: 'all')

 // Source: ../plugins/filter.js (mode: 'all')
// Component: <NoSsr>

vue__WEBPACK_IMPORTED_MODULE_9__["default"].component(_components_no_ssr_js__WEBPACK_IMPORTED_MODULE_12__["default"].name, _components_no_ssr_js__WEBPACK_IMPORTED_MODULE_12__["default"]); // Component: <NuxtChild>

vue__WEBPACK_IMPORTED_MODULE_9__["default"].component(_components_nuxt_child_js__WEBPACK_IMPORTED_MODULE_13__["default"].name, _components_nuxt_child_js__WEBPACK_IMPORTED_MODULE_13__["default"]);
vue__WEBPACK_IMPORTED_MODULE_9__["default"].component('NChild', _components_nuxt_child_js__WEBPACK_IMPORTED_MODULE_13__["default"]); // Component NuxtLink is imported in server.js or client.js
// Component: <Nuxt>`

vue__WEBPACK_IMPORTED_MODULE_9__["default"].component(_components_nuxt_js__WEBPACK_IMPORTED_MODULE_15__["default"].name, _components_nuxt_js__WEBPACK_IMPORTED_MODULE_15__["default"]); // vue-meta configuration

vue__WEBPACK_IMPORTED_MODULE_9__["default"].use(vue_meta__WEBPACK_IMPORTED_MODULE_10___default.a, {
  keyName: 'head',
  // the component option name that vue-meta looks for meta info on.
  attribute: 'data-n-head',
  // the attribute name vue-meta adds to the tags it observes
  ssrAttribute: 'data-n-head-ssr',
  // the attribute name that lets vue-meta know that meta info has already been server-rendered
  tagIDKeyName: 'hid' // the property name that vue-meta uses to determine whether to overwrite or append a tag

});
var defaultTransition = {
  "name": "page",
  "mode": "out-in",
  "appear": true,
  "appearClass": "appear",
  "appearActiveClass": "appear-active",
  "appearToClass": "appear-to"
};

function createApp(_x) {
  return _createApp.apply(this, arguments);
}

function _createApp() {
  _createApp = Object(_babel_runtime_helpers_esm_asyncToGenerator__WEBPACK_IMPORTED_MODULE_7__["default"])(
  /*#__PURE__*/
  regeneratorRuntime.mark(function _callee2(ssrContext) {
    var router, store, app, next, route, path, inject;
    return regeneratorRuntime.wrap(function _callee2$(_context2) {
      while (1) {
        switch (_context2.prev = _context2.next) {
          case 0:
            _context2.next = 2;
            return Object(_router_js__WEBPACK_IMPORTED_MODULE_11__["createRouter"])(ssrContext);

          case 2:
            router = _context2.sent;
            store = Object(_store_js__WEBPACK_IMPORTED_MODULE_18__["createStore"])(ssrContext); // Add this.$router into store actions/mutations

            store.$router = router; // Create Root instance
            // here we inject the router and store to all child components,
            // making them available everywhere as `this.$router` and `this.$store`.

            app = _objectSpread({
              router: router,
              store: store,
              nuxt: {
                defaultTransition: defaultTransition,
                transitions: [defaultTransition],
                setTransitions: function setTransitions(transitions) {
                  if (!Array.isArray(transitions)) {
                    transitions = [transitions];
                  }

                  transitions = transitions.map(function (transition) {
                    if (!transition) {
                      transition = defaultTransition;
                    } else if (typeof transition === 'string') {
                      transition = Object.assign({}, defaultTransition, {
                        name: transition
                      });
                    } else {
                      transition = Object.assign({}, defaultTransition, transition);
                    }

                    return transition;
                  });
                  this.$options.nuxt.transitions = transitions;
                  return transitions;
                },
                err: null,
                dateErr: null,
                error: function error(err) {
                  err = err || null;
                  app.context._errored = Boolean(err);
                  err = err ? Object(_utils__WEBPACK_IMPORTED_MODULE_17__["normalizeError"])(err) : null;
                  var nuxt = this.nuxt || this.$options.nuxt;
                  nuxt.dateErr = Date.now();
                  nuxt.err = err; // Used in src/server.js

                  if (ssrContext) ssrContext.nuxt.error = err;
                  return err;
                }
              }
            }, _App_js__WEBPACK_IMPORTED_MODULE_16__["default"]); // Make app available into store via this.app

            store.app = app;
            next = ssrContext ? ssrContext.next : function (location) {
              return app.router.push(location);
            }; // Resolve route

            if (ssrContext) {
              route = router.resolve(ssrContext.url).route;
            } else {
              path = Object(_utils__WEBPACK_IMPORTED_MODULE_17__["getLocation"])(router.options.base);
              route = router.resolve(path).route;
            } // Set context to app.context


            _context2.next = 11;
            return Object(_utils__WEBPACK_IMPORTED_MODULE_17__["setContext"])(app, {
              route: route,
              next: next,
              error: app.nuxt.error.bind(app),
              store: store,
              payload: ssrContext ? ssrContext.payload : undefined,
              req: ssrContext ? ssrContext.req : undefined,
              res: ssrContext ? ssrContext.res : undefined,
              beforeRenderFns: ssrContext ? ssrContext.beforeRenderFns : undefined,
              ssrContext: ssrContext
            });

          case 11:
            inject = function inject(key, value) {
              if (!key) throw new Error('inject(key, value) has no key provided');
              if (typeof value === 'undefined') throw new Error('inject(key, value) has no value provided');
              key = '$' + key; // Add into app

              app[key] = value; // Add into store

              store[key] = app[key]; // Check if plugin not already installed

              var installKey = '__nuxt_' + key + '_installed__';
              if (vue__WEBPACK_IMPORTED_MODULE_9__["default"][installKey]) return;
              vue__WEBPACK_IMPORTED_MODULE_9__["default"][installKey] = true; // Call Vue.use() to install the plugin into vm

              vue__WEBPACK_IMPORTED_MODULE_9__["default"].use(function () {
                if (!vue__WEBPACK_IMPORTED_MODULE_9__["default"].prototype.hasOwnProperty(key)) {
                  Object.defineProperty(vue__WEBPACK_IMPORTED_MODULE_9__["default"].prototype, key, {
                    get: function get() {
                      return this.$root.$options[key];
                    }
                  });
                }
              });
            };

            if (true) {
              // Replace store state before plugins execution
              if (window.__NUXT__ && window.__NUXT__.state) {
                store.replaceState(window.__NUXT__.state);
              }
            } // Plugin execution


            if (!(typeof nuxt_plugin_i18n_66ff12a5__WEBPACK_IMPORTED_MODULE_19__["default"] === 'function')) {
              _context2.next = 16;
              break;
            }

            _context2.next = 16;
            return Object(nuxt_plugin_i18n_66ff12a5__WEBPACK_IMPORTED_MODULE_19__["default"])(app.context, inject);

          case 16:
            if (!(typeof nuxt_plugin_elementui_d905880e__WEBPACK_IMPORTED_MODULE_20__["default"] === 'function')) {
              _context2.next = 19;
              break;
            }

            _context2.next = 19;
            return Object(nuxt_plugin_elementui_d905880e__WEBPACK_IMPORTED_MODULE_20__["default"])(app.context, inject);

          case 19:
            if (!(typeof nuxt_plugin_filter_6c04580b__WEBPACK_IMPORTED_MODULE_21__["default"] === 'function')) {
              _context2.next = 22;
              break;
            }

            _context2.next = 22;
            return Object(nuxt_plugin_filter_6c04580b__WEBPACK_IMPORTED_MODULE_21__["default"])(app.context, inject);

          case 22:
            if (true) {
              _context2.next = 25;
              break;
            }

            _context2.next = 25;
            return new Promise(function (resolve, reject) {
              router.push(ssrContext.url, resolve, function () {
                // navigated to a different route in router guard
                var unregister = router.afterEach(
                /*#__PURE__*/
                function () {
                  var _ref = Object(_babel_runtime_helpers_esm_asyncToGenerator__WEBPACK_IMPORTED_MODULE_7__["default"])(
                  /*#__PURE__*/
                  regeneratorRuntime.mark(function _callee(to, from, next) {
                    return regeneratorRuntime.wrap(function _callee$(_context) {
                      while (1) {
                        switch (_context.prev = _context.next) {
                          case 0:
                            ssrContext.url = to.fullPath;
                            _context.next = 3;
                            return Object(_utils__WEBPACK_IMPORTED_MODULE_17__["getRouteData"])(to);

                          case 3:
                            app.context.route = _context.sent;
                            app.context.params = to.params || {};
                            app.context.query = to.query || {};
                            unregister();
                            resolve();

                          case 8:
                          case "end":
                            return _context.stop();
                        }
                      }
                    }, _callee);
                  }));

                  return function (_x2, _x3, _x4) {
                    return _ref.apply(this, arguments);
                  };
                }());
              });
            });

          case 25:
            return _context2.abrupt("return", {
              app: app,
              store: store,
              router: router
            });

          case 26:
          case "end":
            return _context2.stop();
        }
      }
    }, _callee2);
  }));
  return _createApp.apply(this, arguments);
}



/***/ }),

/***/ "./.nuxt/middleware.js":
/*!*****************************!*\
  !*** ./.nuxt/middleware.js ***!
  \*****************************/
/*! exports provided: default */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony import */ var core_js_modules_es6_array_iterator__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! core-js/modules/es6.array.iterator */ "./node_modules/core-js/modules/es6.array.iterator.js");
/* harmony import */ var core_js_modules_es6_array_iterator__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es6_array_iterator__WEBPACK_IMPORTED_MODULE_0__);
/* harmony import */ var core_js_modules_es6_promise__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! core-js/modules/es6.promise */ "./node_modules/core-js/modules/es6.promise.js");
/* harmony import */ var core_js_modules_es6_promise__WEBPACK_IMPORTED_MODULE_1___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es6_promise__WEBPACK_IMPORTED_MODULE_1__);
/* harmony import */ var core_js_modules_es6_object_assign__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! core-js/modules/es6.object.assign */ "./node_modules/core-js/modules/es6.object.assign.js");
/* harmony import */ var core_js_modules_es6_object_assign__WEBPACK_IMPORTED_MODULE_2___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es6_object_assign__WEBPACK_IMPORTED_MODULE_2__);
/* harmony import */ var core_js_modules_es7_promise_finally__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! core-js/modules/es7.promise.finally */ "./node_modules/core-js/modules/es7.promise.finally.js");
/* harmony import */ var core_js_modules_es7_promise_finally__WEBPACK_IMPORTED_MODULE_3___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es7_promise_finally__WEBPACK_IMPORTED_MODULE_3__);




var middleware = {};
middleware['i18n'] = __webpack_require__(/*! ../middleware/i18n.js */ "./middleware/i18n.js");
middleware['i18n'] = middleware['i18n'].default || middleware['i18n'];
/* harmony default export */ __webpack_exports__["default"] = (middleware);

/***/ }),

/***/ "./.nuxt/router.js":
/*!*************************!*\
  !*** ./.nuxt/router.js ***!
  \*************************/
/*! exports provided: createRouter */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "createRouter", function() { return createRouter; });
/* harmony import */ var core_js_modules_es6_object_to_string__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! core-js/modules/es6.object.to-string */ "./node_modules/core-js/modules/es6.object.to-string.js");
/* harmony import */ var core_js_modules_es6_object_to_string__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es6_object_to_string__WEBPACK_IMPORTED_MODULE_0__);
/* harmony import */ var vue__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! vue */ "./node_modules/vue/dist/vue.runtime.esm.js");
/* harmony import */ var vue_router__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! vue-router */ "./node_modules/vue-router/dist/vue-router.esm.js");
/* harmony import */ var _utils__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! ./utils */ "./.nuxt/utils.js");





var _690c212a = function _690c212a() {
  return Object(_utils__WEBPACK_IMPORTED_MODULE_3__["interopDefault"])(Promise.all(/*! import() | pages/static/index */[__webpack_require__.e("commons.pages/static/_lang/index.pages/static/_lang/normal.pages/static/index"), __webpack_require__.e("vendors.pages/static/_lang/index.pages/static/_lang/normal.pages/static/index"), __webpack_require__.e("pages/static/_lang/normal"), __webpack_require__.e("pages/static/index")]).then(__webpack_require__.bind(null, /*! ../pages/static/index.vue */ "./pages/static/index.vue")));
};

var _036bc42e = function _036bc42e() {
  return Object(_utils__WEBPACK_IMPORTED_MODULE_3__["interopDefault"])(Promise.all(/*! import() | pages/static/_lang/index */[__webpack_require__.e("commons.pages/static/_lang/index.pages/static/_lang/normal.pages/static/index"), __webpack_require__.e("vendors.pages/static/_lang/index.pages/static/_lang/normal.pages/static/index"), __webpack_require__.e("pages/static/_lang/normal"), __webpack_require__.e("pages/static/_lang/index")]).then(__webpack_require__.bind(null, /*! ../pages/static/_lang/index.vue */ "./pages/static/_lang/index.vue")));
};

var _56391860 = function _56391860() {
  return Object(_utils__WEBPACK_IMPORTED_MODULE_3__["interopDefault"])(Promise.all(/*! import() | pages/static/_lang/normal */[__webpack_require__.e("commons.pages/static/_lang/index.pages/static/_lang/normal.pages/static/index"), __webpack_require__.e("vendors.pages/static/_lang/index.pages/static/_lang/normal.pages/static/index"), __webpack_require__.e("pages/static/_lang/normal")]).then(__webpack_require__.bind(null, /*! ../pages/static/_lang/normal.vue */ "./pages/static/_lang/normal.vue")));
};

vue__WEBPACK_IMPORTED_MODULE_1__["default"].use(vue_router__WEBPACK_IMPORTED_MODULE_2__["default"]);

if (true) {
  if ('scrollRestoration' in window.history) {
    window.history.scrollRestoration = 'manual'; // reset scrollRestoration to auto when leaving page, allowing page reload
    // and back-navigation from other pages to use the browser to restore the
    // scrolling position.

    window.addEventListener('beforeunload', function () {
      window.history.scrollRestoration = 'auto';
    }); // Setting scrollRestoration to manual again when returning to this page.

    window.addEventListener('load', function () {
      window.history.scrollRestoration = 'manual';
    });
  }
}

var scrollBehavior = function scrollBehavior(to, from, savedPosition) {
  // if the returned position is falsy or an empty object,
  // will retain current scroll position.
  var position = false; // if no children detected and scrollToTop is not explicitly disabled

  if (to.matched.length < 2 && to.matched.every(function (r) {
    return r.components.default.options.scrollToTop !== false;
  })) {
    // scroll to the top of the page
    position = {
      x: 0,
      y: 0
    };
  } else if (to.matched.some(function (r) {
    return r.components.default.options.scrollToTop;
  })) {
    // if one of the children has scrollToTop option set to true
    position = {
      x: 0,
      y: 0
    };
  } // savedPosition is only available for popstate navigations (back button)


  if (savedPosition) {
    position = savedPosition;
  }

  return new Promise(function (resolve) {
    // wait for the out transition to complete (if necessary)
    window.$nuxt.$once('triggerScroll', function () {
      // coords will be used if no selector is provided,
      // or if the selector didn't match any element.
      if (to.hash) {
        var hash = to.hash; // CSS.escape() is not supported with IE and Edge.

        if (typeof window.CSS !== 'undefined' && typeof window.CSS.escape !== 'undefined') {
          hash = '#' + window.CSS.escape(hash.substr(1));
        }

        try {
          if (document.querySelector(hash)) {
            // scroll to anchor by returning the selector
            position = {
              selector: hash
            };
          }
        } catch (e) {
          console.warn('Failed to save scroll position. Please add CSS.escape() polyfill (https://github.com/mathiasbynens/CSS.escape).');
        }
      }

      resolve(position);
    });
  });
};

function createRouter() {
  return new vue_router__WEBPACK_IMPORTED_MODULE_2__["default"]({
    mode: 'history',
    base: decodeURI('/'),
    linkActiveClass: 'nuxt-link-active',
    linkExactActiveClass: 'nuxt-link-exact-active',
    scrollBehavior: scrollBehavior,
    routes: [{
      path: "/static",
      component: _690c212a,
      name: "static"
    }, {
      path: "/static/:lang",
      component: _036bc42e,
      name: "static-lang"
    }, {
      path: "/static/:lang/normal",
      component: _56391860,
      name: "static-lang-normal"
    }],
    fallback: false
  });
}

/***/ }),

/***/ "./.nuxt/store.js":
/*!************************!*\
  !*** ./.nuxt/store.js ***!
  \************************/
/*! exports provided: createStore */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "createStore", function() { return createStore; });
/* harmony import */ var core_js_modules_es7_array_includes__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! core-js/modules/es7.array.includes */ "./node_modules/core-js/modules/es7.array.includes.js");
/* harmony import */ var core_js_modules_es7_array_includes__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es7_array_includes__WEBPACK_IMPORTED_MODULE_0__);
/* harmony import */ var core_js_modules_es6_regexp_split__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! core-js/modules/es6.regexp.split */ "./node_modules/core-js/modules/es6.regexp.split.js");
/* harmony import */ var core_js_modules_es6_regexp_split__WEBPACK_IMPORTED_MODULE_1___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es6_regexp_split__WEBPACK_IMPORTED_MODULE_1__);
/* harmony import */ var core_js_modules_es6_regexp_replace__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! core-js/modules/es6.regexp.replace */ "./node_modules/core-js/modules/es6.regexp.replace.js");
/* harmony import */ var core_js_modules_es6_regexp_replace__WEBPACK_IMPORTED_MODULE_2___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es6_regexp_replace__WEBPACK_IMPORTED_MODULE_2__);
/* harmony import */ var vue__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! vue */ "./node_modules/vue/dist/vue.runtime.esm.js");
/* harmony import */ var vuex__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! vuex */ "./node_modules/vuex/dist/vuex.esm.js");





vue__WEBPACK_IMPORTED_MODULE_3__["default"].use(vuex__WEBPACK_IMPORTED_MODULE_4__["default"]);
var VUEX_PROPERTIES = ['state', 'getters', 'actions', 'mutations'];
var store = {};
void function updateModules() {
  store = normalizeRoot(__webpack_require__(/*! ../store/index.js */ "./store/index.js"), 'store/index.js'); // If store is an exported method = classic mode (deprecated)

  if (typeof store === 'function') {
    return console.warn('Classic mode for store/ is deprecated and will be removed in Nuxt 3.');
  } // Enforce store modules


  store.modules = store.modules || {}; // If the environment supports hot reloading...

  if (true) {
    // Whenever any Vuex module is updated...
    module.hot.accept([/*! ../store/index.js */ "./store/index.js"], function(__WEBPACK_OUTDATED_DEPENDENCIES__) { (function () {
      // Update `root.modules` with the latest definitions.
      updateModules(); // Trigger a hot update in the store.

      window.$nuxt.$store.hotUpdate(store);
    })(__WEBPACK_OUTDATED_DEPENDENCIES__); }.bind(this));
  }
}(); // createStore

var createStore = store instanceof Function ? store : function () {
  return new vuex__WEBPACK_IMPORTED_MODULE_4__["default"].Store(Object.assign({
    strict: "development" !== 'production'
  }, store));
};

function resolveStoreModules(moduleData, filename) {
  moduleData = moduleData.default || moduleData; // Remove store src + extension (./foo/index.js -> foo/index)

  var namespace = filename.replace(/\.(js|mjs|ts)$/, '');
  var namespaces = namespace.split('/');
  var moduleName = namespaces[namespaces.length - 1];
  var filePath = "store/".concat(filename);
  moduleData = moduleName === 'state' ? normalizeState(moduleData, filePath) : normalizeModule(moduleData, filePath); // If src is a known Vuex property

  if (VUEX_PROPERTIES.includes(moduleName)) {
    var property = moduleName;

    var _storeModule = getStoreModule(store, namespaces, {
      isProperty: true
    }); // Replace state since it's a function


    mergeProperty(_storeModule, moduleData, property);
    return;
  } // If file is foo/index.js, it should be saved as foo


  var isIndexModule = moduleName === 'index';

  if (isIndexModule) {
    namespaces.pop();
    moduleName = namespaces[namespaces.length - 1];
  }

  var storeModule = getStoreModule(store, namespaces);

  for (var _i = 0, _VUEX_PROPERTIES = VUEX_PROPERTIES; _i < _VUEX_PROPERTIES.length; _i++) {
    var _property = _VUEX_PROPERTIES[_i];
    mergeProperty(storeModule, moduleData[_property], _property);
  }

  if (moduleData.namespaced === false) {
    delete storeModule.namespaced;
  }
}

function normalizeRoot(moduleData, filePath) {
  moduleData = moduleData.default || moduleData;

  if (moduleData.commit) {
    throw new Error("[nuxt] ".concat(filePath, " should export a method that returns a Vuex instance."));
  }

  if (typeof moduleData !== 'function') {
    // Avoid TypeError: setting a property that has only a getter when overwriting top level keys
    moduleData = Object.assign({}, moduleData);
  }

  return normalizeModule(moduleData, filePath);
}

function normalizeState(moduleData, filePath) {
  if (typeof moduleData !== 'function') {
    console.warn("".concat(filePath, " should export a method that returns an object"));
    var state = Object.assign({}, moduleData);
    return function () {
      return state;
    };
  }

  return normalizeModule(moduleData, filePath);
}

function normalizeModule(moduleData, filePath) {
  if (moduleData.state && typeof moduleData.state !== 'function') {
    console.warn("'state' should be a method that returns an object in ".concat(filePath));

    var _state = Object.assign({}, moduleData.state); // Avoid TypeError: setting a property that has only a getter when overwriting top level keys


    moduleData = Object.assign({}, moduleData, {
      state: function state() {
        return _state;
      }
    });
  }

  return moduleData;
}

function getStoreModule(storeModule, namespaces) {
  var _ref = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : {},
      _ref$isProperty = _ref.isProperty,
      isProperty = _ref$isProperty === void 0 ? false : _ref$isProperty;

  // If ./mutations.js
  if (!namespaces.length || isProperty && namespaces.length === 1) {
    return storeModule;
  }

  var namespace = namespaces.shift();
  storeModule.modules[namespace] = storeModule.modules[namespace] || {};
  storeModule.modules[namespace].namespaced = true;
  storeModule.modules[namespace].modules = storeModule.modules[namespace].modules || {};
  return getStoreModule(storeModule.modules[namespace], namespaces, {
    isProperty: isProperty
  });
}

function mergeProperty(storeModule, moduleData, property) {
  if (!moduleData) return;

  if (property === 'state') {
    storeModule.state = moduleData || storeModule.state;
  } else {
    storeModule[property] = Object.assign({}, storeModule[property], moduleData);
  }
}

/***/ }),

/***/ "./.nuxt/utils.js":
/*!************************!*\
  !*** ./.nuxt/utils.js ***!
  \************************/
/*! exports provided: empty, globalHandleError, interopDefault, applyAsyncData, sanitizeComponent, getMatchedComponents, getMatchedComponentsInstances, flatMapComponents, resolveRouteComponents, getRouteData, setContext, middlewareSeries, promisify, getLocation, urlJoin, compile, getQueryDiff, normalizeError */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "empty", function() { return empty; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "globalHandleError", function() { return globalHandleError; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "interopDefault", function() { return interopDefault; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "applyAsyncData", function() { return applyAsyncData; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "sanitizeComponent", function() { return sanitizeComponent; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "getMatchedComponents", function() { return getMatchedComponents; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "getMatchedComponentsInstances", function() { return getMatchedComponentsInstances; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "flatMapComponents", function() { return flatMapComponents; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "resolveRouteComponents", function() { return resolveRouteComponents; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "getRouteData", function() { return getRouteData; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "setContext", function() { return setContext; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "middlewareSeries", function() { return middlewareSeries; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "promisify", function() { return promisify; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "getLocation", function() { return getLocation; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "urlJoin", function() { return urlJoin; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "compile", function() { return compile; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "getQueryDiff", function() { return getQueryDiff; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "normalizeError", function() { return normalizeError; });
/* harmony import */ var core_js_modules_es7_object_get_own_property_descriptors__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! core-js/modules/es7.object.get-own-property-descriptors */ "./node_modules/core-js/modules/es7.object.get-own-property-descriptors.js");
/* harmony import */ var core_js_modules_es7_object_get_own_property_descriptors__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es7_object_get_own_property_descriptors__WEBPACK_IMPORTED_MODULE_0__);
/* harmony import */ var core_js_modules_es6_symbol__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! core-js/modules/es6.symbol */ "./node_modules/core-js/modules/es6.symbol.js");
/* harmony import */ var core_js_modules_es6_symbol__WEBPACK_IMPORTED_MODULE_1___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es6_symbol__WEBPACK_IMPORTED_MODULE_1__);
/* harmony import */ var _babel_runtime_helpers_esm_slicedToArray__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! @babel/runtime/helpers/esm/slicedToArray */ "./node_modules/@babel/runtime/helpers/esm/slicedToArray.js");
/* harmony import */ var core_js_modules_es6_regexp_split__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! core-js/modules/es6.regexp.split */ "./node_modules/core-js/modules/es6.regexp.split.js");
/* harmony import */ var core_js_modules_es6_regexp_split__WEBPACK_IMPORTED_MODULE_3___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es6_regexp_split__WEBPACK_IMPORTED_MODULE_3__);
/* harmony import */ var core_js_modules_es6_string_starts_with__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! core-js/modules/es6.string.starts-with */ "./node_modules/core-js/modules/es6.string.starts-with.js");
/* harmony import */ var core_js_modules_es6_string_starts_with__WEBPACK_IMPORTED_MODULE_4___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es6_string_starts_with__WEBPACK_IMPORTED_MODULE_4__);
/* harmony import */ var core_js_modules_es6_string_repeat__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! core-js/modules/es6.string.repeat */ "./node_modules/core-js/modules/es6.string.repeat.js");
/* harmony import */ var core_js_modules_es6_string_repeat__WEBPACK_IMPORTED_MODULE_5___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es6_string_repeat__WEBPACK_IMPORTED_MODULE_5__);
/* harmony import */ var _babel_runtime_helpers_esm_typeof__WEBPACK_IMPORTED_MODULE_6__ = __webpack_require__(/*! @babel/runtime/helpers/esm/typeof */ "./node_modules/@babel/runtime/helpers/esm/typeof.js");
/* harmony import */ var core_js_modules_es6_regexp_to_string__WEBPACK_IMPORTED_MODULE_7__ = __webpack_require__(/*! core-js/modules/es6.regexp.to-string */ "./node_modules/core-js/modules/es6.regexp.to-string.js");
/* harmony import */ var core_js_modules_es6_regexp_to_string__WEBPACK_IMPORTED_MODULE_7___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es6_regexp_to_string__WEBPACK_IMPORTED_MODULE_7__);
/* harmony import */ var core_js_modules_es6_date_to_string__WEBPACK_IMPORTED_MODULE_8__ = __webpack_require__(/*! core-js/modules/es6.date.to-string */ "./node_modules/core-js/modules/es6.date.to-string.js");
/* harmony import */ var core_js_modules_es6_date_to_string__WEBPACK_IMPORTED_MODULE_8___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es6_date_to_string__WEBPACK_IMPORTED_MODULE_8__);
/* harmony import */ var core_js_modules_es6_regexp_constructor__WEBPACK_IMPORTED_MODULE_9__ = __webpack_require__(/*! core-js/modules/es6.regexp.constructor */ "./node_modules/core-js/modules/es6.regexp.constructor.js");
/* harmony import */ var core_js_modules_es6_regexp_constructor__WEBPACK_IMPORTED_MODULE_9___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es6_regexp_constructor__WEBPACK_IMPORTED_MODULE_9__);
/* harmony import */ var core_js_modules_es6_regexp_search__WEBPACK_IMPORTED_MODULE_10__ = __webpack_require__(/*! core-js/modules/es6.regexp.search */ "./node_modules/core-js/modules/es6.regexp.search.js");
/* harmony import */ var core_js_modules_es6_regexp_search__WEBPACK_IMPORTED_MODULE_10___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es6_regexp_search__WEBPACK_IMPORTED_MODULE_10__);
/* harmony import */ var core_js_modules_es6_regexp_replace__WEBPACK_IMPORTED_MODULE_11__ = __webpack_require__(/*! core-js/modules/es6.regexp.replace */ "./node_modules/core-js/modules/es6.regexp.replace.js");
/* harmony import */ var core_js_modules_es6_regexp_replace__WEBPACK_IMPORTED_MODULE_11___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es6_regexp_replace__WEBPACK_IMPORTED_MODULE_11__);
/* harmony import */ var regenerator_runtime_runtime__WEBPACK_IMPORTED_MODULE_12__ = __webpack_require__(/*! regenerator-runtime/runtime */ "./node_modules/regenerator-runtime/runtime.js");
/* harmony import */ var regenerator_runtime_runtime__WEBPACK_IMPORTED_MODULE_12___default = /*#__PURE__*/__webpack_require__.n(regenerator_runtime_runtime__WEBPACK_IMPORTED_MODULE_12__);
/* harmony import */ var _babel_runtime_helpers_esm_asyncToGenerator__WEBPACK_IMPORTED_MODULE_13__ = __webpack_require__(/*! @babel/runtime/helpers/esm/asyncToGenerator */ "./node_modules/@babel/runtime/helpers/esm/asyncToGenerator.js");
/* harmony import */ var core_js_modules_es6_string_iterator__WEBPACK_IMPORTED_MODULE_14__ = __webpack_require__(/*! core-js/modules/es6.string.iterator */ "./node_modules/core-js/modules/es6.string.iterator.js");
/* harmony import */ var core_js_modules_es6_string_iterator__WEBPACK_IMPORTED_MODULE_14___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es6_string_iterator__WEBPACK_IMPORTED_MODULE_14__);
/* harmony import */ var core_js_modules_web_dom_iterable__WEBPACK_IMPORTED_MODULE_15__ = __webpack_require__(/*! core-js/modules/web.dom.iterable */ "./node_modules/core-js/modules/web.dom.iterable.js");
/* harmony import */ var core_js_modules_web_dom_iterable__WEBPACK_IMPORTED_MODULE_15___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_web_dom_iterable__WEBPACK_IMPORTED_MODULE_15__);
/* harmony import */ var core_js_modules_es6_object_to_string__WEBPACK_IMPORTED_MODULE_16__ = __webpack_require__(/*! core-js/modules/es6.object.to-string */ "./node_modules/core-js/modules/es6.object.to-string.js");
/* harmony import */ var core_js_modules_es6_object_to_string__WEBPACK_IMPORTED_MODULE_16___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es6_object_to_string__WEBPACK_IMPORTED_MODULE_16__);
/* harmony import */ var core_js_modules_es6_object_keys__WEBPACK_IMPORTED_MODULE_17__ = __webpack_require__(/*! core-js/modules/es6.object.keys */ "./node_modules/core-js/modules/es6.object.keys.js");
/* harmony import */ var core_js_modules_es6_object_keys__WEBPACK_IMPORTED_MODULE_17___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es6_object_keys__WEBPACK_IMPORTED_MODULE_17__);
/* harmony import */ var core_js_modules_es6_function_name__WEBPACK_IMPORTED_MODULE_18__ = __webpack_require__(/*! core-js/modules/es6.function.name */ "./node_modules/core-js/modules/es6.function.name.js");
/* harmony import */ var core_js_modules_es6_function_name__WEBPACK_IMPORTED_MODULE_18___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es6_function_name__WEBPACK_IMPORTED_MODULE_18__);
/* harmony import */ var _babel_runtime_helpers_esm_defineProperty__WEBPACK_IMPORTED_MODULE_19__ = __webpack_require__(/*! @babel/runtime/helpers/esm/defineProperty */ "./node_modules/@babel/runtime/helpers/esm/defineProperty.js");
/* harmony import */ var vue__WEBPACK_IMPORTED_MODULE_20__ = __webpack_require__(/*! vue */ "./node_modules/vue/dist/vue.runtime.esm.js");





















function ownKeys(object, enumerableOnly) { var keys = Object.keys(object); if (Object.getOwnPropertySymbols) { var symbols = Object.getOwnPropertySymbols(object); if (enumerableOnly) symbols = symbols.filter(function (sym) { return Object.getOwnPropertyDescriptor(object, sym).enumerable; }); keys.push.apply(keys, symbols); } return keys; }

function _objectSpread(target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i] != null ? arguments[i] : {}; if (i % 2) { ownKeys(source, true).forEach(function (key) { Object(_babel_runtime_helpers_esm_defineProperty__WEBPACK_IMPORTED_MODULE_19__["default"])(target, key, source[key]); }); } else if (Object.getOwnPropertyDescriptors) { Object.defineProperties(target, Object.getOwnPropertyDescriptors(source)); } else { ownKeys(source).forEach(function (key) { Object.defineProperty(target, key, Object.getOwnPropertyDescriptor(source, key)); }); } } return target; }

 // window.{{globals.loadedCallback}} hook
// Useful for jsdom testing or plugins (https://github.com/tmpvar/jsdom#dealing-with-asynchronous-script-loading)

if (true) {
  window.onNuxtReadyCbs = [];

  window.onNuxtReady = function (cb) {
    window.onNuxtReadyCbs.push(cb);
  };
}

function empty() {}
function globalHandleError(error) {
  if (vue__WEBPACK_IMPORTED_MODULE_20__["default"].config.errorHandler) {
    vue__WEBPACK_IMPORTED_MODULE_20__["default"].config.errorHandler(error);
  }
}
function interopDefault(promise) {
  return promise.then(function (m) {
    return m.default || m;
  });
}
function applyAsyncData(Component, asyncData) {
  if ( // For SSR, we once all this function without second param to just apply asyncData
  // Prevent doing this for each SSR request
  !asyncData && Component.options.__hasNuxtData) {
    return;
  }

  var ComponentData = Component.options._originDataFn || Component.options.data || function () {
    return {};
  };

  Component.options._originDataFn = ComponentData;

  Component.options.data = function () {
    var data = ComponentData.call(this);

    if (this.$ssrContext) {
      asyncData = this.$ssrContext.asyncData[Component.cid];
    }

    return _objectSpread({}, data, {}, asyncData);
  };

  Component.options.__hasNuxtData = true;

  if (Component._Ctor && Component._Ctor.options) {
    Component._Ctor.options.data = Component.options.data;
  }
}
function sanitizeComponent(Component) {
  // If Component already sanitized
  if (Component.options && Component._Ctor === Component) {
    return Component;
  }

  if (!Component.options) {
    Component = vue__WEBPACK_IMPORTED_MODULE_20__["default"].extend(Component); // fix issue #6

    Component._Ctor = Component;
  } else {
    Component._Ctor = Component;
    Component.extendOptions = Component.options;
  } // For debugging purpose


  if (!Component.options.name && Component.options.__file) {
    Component.options.name = Component.options.__file;
  }

  return Component;
}
function getMatchedComponents(route) {
  var matches = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : false;
  return Array.prototype.concat.apply([], route.matched.map(function (m, index) {
    return Object.keys(m.components).map(function (key) {
      matches && matches.push(index);
      return m.components[key];
    });
  }));
}
function getMatchedComponentsInstances(route) {
  var matches = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : false;
  return Array.prototype.concat.apply([], route.matched.map(function (m, index) {
    return Object.keys(m.instances).map(function (key) {
      matches && matches.push(index);
      return m.instances[key];
    });
  }));
}
function flatMapComponents(route, fn) {
  return Array.prototype.concat.apply([], route.matched.map(function (m, index) {
    return Object.keys(m.components).reduce(function (promises, key) {
      if (m.components[key]) {
        promises.push(fn(m.components[key], m.instances[key], m, key, index));
      } else {
        delete m.components[key];
      }

      return promises;
    }, []);
  }));
}
function resolveRouteComponents(route) {
  return Promise.all(flatMapComponents(route,
  /*#__PURE__*/
  function () {
    var _ref = Object(_babel_runtime_helpers_esm_asyncToGenerator__WEBPACK_IMPORTED_MODULE_13__["default"])(
    /*#__PURE__*/
    regeneratorRuntime.mark(function _callee(Component, _, match, key) {
      return regeneratorRuntime.wrap(function _callee$(_context) {
        while (1) {
          switch (_context.prev = _context.next) {
            case 0:
              if (!(typeof Component === 'function' && !Component.options)) {
                _context.next = 4;
                break;
              }

              _context.next = 3;
              return Component();

            case 3:
              Component = _context.sent;

            case 4:
              match.components[key] = sanitizeComponent(Component);
              return _context.abrupt("return", match.components[key]);

            case 6:
            case "end":
              return _context.stop();
          }
        }
      }, _callee);
    }));

    return function (_x, _x2, _x3, _x4) {
      return _ref.apply(this, arguments);
    };
  }()));
}
function getRouteData(_x5) {
  return _getRouteData.apply(this, arguments);
}

function _getRouteData() {
  _getRouteData = Object(_babel_runtime_helpers_esm_asyncToGenerator__WEBPACK_IMPORTED_MODULE_13__["default"])(
  /*#__PURE__*/
  regeneratorRuntime.mark(function _callee2(route) {
    return regeneratorRuntime.wrap(function _callee2$(_context2) {
      while (1) {
        switch (_context2.prev = _context2.next) {
          case 0:
            if (route) {
              _context2.next = 2;
              break;
            }

            return _context2.abrupt("return");

          case 2:
            _context2.next = 4;
            return resolveRouteComponents(route);

          case 4:
            return _context2.abrupt("return", _objectSpread({}, route, {
              meta: getMatchedComponents(route).map(function (Component, index) {
                return _objectSpread({}, Component.options.meta, {}, (route.matched[index] || {}).meta);
              })
            }));

          case 5:
          case "end":
            return _context2.stop();
        }
      }
    }, _callee2);
  }));
  return _getRouteData.apply(this, arguments);
}

function setContext(_x6, _x7) {
  return _setContext.apply(this, arguments);
}

function _setContext() {
  _setContext = Object(_babel_runtime_helpers_esm_asyncToGenerator__WEBPACK_IMPORTED_MODULE_13__["default"])(
  /*#__PURE__*/
  regeneratorRuntime.mark(function _callee3(app, context) {
    var _ref2, _ref3, currentRouteData, fromRouteData;

    return regeneratorRuntime.wrap(function _callee3$(_context3) {
      while (1) {
        switch (_context3.prev = _context3.next) {
          case 0:
            // If context not defined, create it
            if (!app.context) {
              app.context = {
                isStatic: false,
                isDev: true,
                isHMR: false,
                app: app,
                store: app.store,
                payload: context.payload,
                error: context.error,
                base: '/',
                env: {} // Only set once

              };

              if (context.req) {
                app.context.req = context.req;
              }

              if (context.res) {
                app.context.res = context.res;
              }

              if (context.ssrContext) {
                app.context.ssrContext = context.ssrContext;
              }

              app.context.redirect = function (status, path, query) {
                if (!status) {
                  return;
                }

                app.context._redirected = true; // if only 1 or 2 arguments: redirect('/') or redirect('/', { foo: 'bar' })

                var pathType = Object(_babel_runtime_helpers_esm_typeof__WEBPACK_IMPORTED_MODULE_6__["default"])(path);

                if (typeof status !== 'number' && (pathType === 'undefined' || pathType === 'object')) {
                  query = path || {};
                  path = status;
                  pathType = Object(_babel_runtime_helpers_esm_typeof__WEBPACK_IMPORTED_MODULE_6__["default"])(path);
                  status = 302;
                }

                if (pathType === 'object') {
                  path = app.router.resolve(path).route.fullPath;
                } // "/absolute/route", "./relative/route" or "../relative/route"


                if (/(^[.]{1,2}\/)|(^\/(?!\/))/.test(path)) {
                  app.context.next({
                    path: path,
                    query: query,
                    status: status
                  });
                } else {
                  path = formatUrl(path, query);

                  if (false) {}

                  if (true) {
                    // https://developer.mozilla.org/en-US/docs/Web/API/Location/replace
                    window.location.replace(path); // Throw a redirect error

                    throw new Error('ERR_REDIRECT');
                  }
                }
              };

              if (false) {}

              if (true) {
                app.context.nuxtState = window.__NUXT__;
              }
            } // Dynamic keys


            _context3.next = 3;
            return Promise.all([getRouteData(context.route), getRouteData(context.from)]);

          case 3:
            _ref2 = _context3.sent;
            _ref3 = Object(_babel_runtime_helpers_esm_slicedToArray__WEBPACK_IMPORTED_MODULE_2__["default"])(_ref2, 2);
            currentRouteData = _ref3[0];
            fromRouteData = _ref3[1];

            if (context.route) {
              app.context.route = currentRouteData;
            }

            if (context.from) {
              app.context.from = fromRouteData;
            }

            app.context.next = context.next;
            app.context._redirected = false;
            app.context._errored = false;
            app.context.isHMR = Boolean(context.isHMR);
            app.context.params = app.context.route.params || {};
            app.context.query = app.context.route.query || {};

          case 15:
          case "end":
            return _context3.stop();
        }
      }
    }, _callee3);
  }));
  return _setContext.apply(this, arguments);
}

function middlewareSeries(promises, appContext) {
  if (!promises.length || appContext._redirected || appContext._errored) {
    return Promise.resolve();
  }

  return promisify(promises[0], appContext).then(function () {
    return middlewareSeries(promises.slice(1), appContext);
  });
}
function promisify(fn, context) {
  var promise;

  if (fn.length === 2) {
    console.warn('Callback-based asyncData, fetch or middleware calls are deprecated. ' + 'Please switch to promises or async/await syntax'); // fn(context, callback)

    promise = new Promise(function (resolve) {
      fn(context, function (err, data) {
        if (err) {
          context.error(err);
        }

        data = data || {};
        resolve(data);
      });
    });
  } else {
    promise = fn(context);
  }

  if (!promise || !(promise instanceof Promise) && typeof promise.then !== 'function') {
    promise = Promise.resolve(promise);
  }

  return promise;
} // Imported from vue-router

function getLocation(base, mode) {
  var path = decodeURI(window.location.pathname);

  if (mode === 'hash') {
    return window.location.hash.replace(/^#\//, '');
  }

  if (base && path.indexOf(base) === 0) {
    path = path.slice(base.length);
  }

  return (path || '/') + window.location.search + window.location.hash;
}
function urlJoin() {
  return Array.prototype.slice.call(arguments).join('/').replace(/\/+/g, '/');
} // Imported from path-to-regexp

/**
 * Compile a string to a template function for the path.
 *
 * @param  {string}             str
 * @param  {Object=}            options
 * @return {!function(Object=, Object=)}
 */

function compile(str, options) {
  return tokensToFunction(parse(str, options));
}
function getQueryDiff(toQuery, fromQuery) {
  var diff = {};

  var queries = _objectSpread({}, toQuery, {}, fromQuery);

  for (var k in queries) {
    if (String(toQuery[k]) !== String(fromQuery[k])) {
      diff[k] = true;
    }
  }

  return diff;
}
function normalizeError(err) {
  var message;

  if (!(err.message || typeof err === 'string')) {
    try {
      message = JSON.stringify(err, null, 2);
    } catch (e) {
      message = "[".concat(err.constructor.name, "]");
    }
  } else {
    message = err.message || err;
  }

  return _objectSpread({}, err, {
    message: message,
    statusCode: err.statusCode || err.status || err.response && err.response.status || 500
  });
}
/**
 * The main path matching regexp utility.
 *
 * @type {RegExp}
 */

var PATH_REGEXP = new RegExp([// Match escaped characters that would otherwise appear in future matches.
// This allows the user to escape special characters that won't transform.
'(\\\\.)', // Match Express-style parameters and un-named parameters with a prefix
// and optional suffixes. Matches appear as:
//
// "/:test(\\d+)?" => ["/", "test", "\d+", undefined, "?", undefined]
// "/route(\\d+)"  => [undefined, undefined, undefined, "\d+", undefined, undefined]
// "/*"            => ["/", undefined, undefined, undefined, undefined, "*"]
'([\\/.])?(?:(?:\\:(\\w+)(?:\\(((?:\\\\.|[^\\\\()])+)\\))?|\\(((?:\\\\.|[^\\\\()])+)\\))([+*?])?|(\\*))'].join('|'), 'g');
/**
 * Parse a string for the raw tokens.
 *
 * @param  {string}  str
 * @param  {Object=} options
 * @return {!Array}
 */

function parse(str, options) {
  var tokens = [];
  var key = 0;
  var index = 0;
  var path = '';
  var defaultDelimiter = options && options.delimiter || '/';
  var res;

  while ((res = PATH_REGEXP.exec(str)) != null) {
    var m = res[0];
    var escaped = res[1];
    var offset = res.index;
    path += str.slice(index, offset);
    index = offset + m.length; // Ignore already escaped sequences.

    if (escaped) {
      path += escaped[1];
      continue;
    }

    var next = str[index];
    var prefix = res[2];
    var name = res[3];
    var capture = res[4];
    var group = res[5];
    var modifier = res[6];
    var asterisk = res[7]; // Push the current path onto the tokens.

    if (path) {
      tokens.push(path);
      path = '';
    }

    var partial = prefix != null && next != null && next !== prefix;
    var repeat = modifier === '+' || modifier === '*';
    var optional = modifier === '?' || modifier === '*';
    var delimiter = res[2] || defaultDelimiter;
    var pattern = capture || group;
    tokens.push({
      name: name || key++,
      prefix: prefix || '',
      delimiter: delimiter,
      optional: optional,
      repeat: repeat,
      partial: partial,
      asterisk: Boolean(asterisk),
      pattern: pattern ? escapeGroup(pattern) : asterisk ? '.*' : '[^' + escapeString(delimiter) + ']+?'
    });
  } // Match any characters still remaining.


  if (index < str.length) {
    path += str.substr(index);
  } // If the path exists, push it onto the end.


  if (path) {
    tokens.push(path);
  }

  return tokens;
}
/**
 * Prettier encoding of URI path segments.
 *
 * @param  {string}
 * @return {string}
 */


function encodeURIComponentPretty(str) {
  return encodeURI(str).replace(/[/?#]/g, function (c) {
    return '%' + c.charCodeAt(0).toString(16).toUpperCase();
  });
}
/**
 * Encode the asterisk parameter. Similar to `pretty`, but allows slashes.
 *
 * @param  {string}
 * @return {string}
 */


function encodeAsterisk(str) {
  return encodeURI(str).replace(/[?#]/g, function (c) {
    return '%' + c.charCodeAt(0).toString(16).toUpperCase();
  });
}
/**
 * Expose a method for transforming tokens into the path function.
 */


function tokensToFunction(tokens) {
  // Compile all the tokens into regexps.
  var matches = new Array(tokens.length); // Compile all the patterns before compilation.

  for (var i = 0; i < tokens.length; i++) {
    if (Object(_babel_runtime_helpers_esm_typeof__WEBPACK_IMPORTED_MODULE_6__["default"])(tokens[i]) === 'object') {
      matches[i] = new RegExp('^(?:' + tokens[i].pattern + ')$');
    }
  }

  return function (obj, opts) {
    var path = '';
    var data = obj || {};
    var options = opts || {};
    var encode = options.pretty ? encodeURIComponentPretty : encodeURIComponent;

    for (var _i = 0; _i < tokens.length; _i++) {
      var token = tokens[_i];

      if (typeof token === 'string') {
        path += token;
        continue;
      }

      var value = data[token.name || 'pathMatch'];
      var segment = void 0;

      if (value == null) {
        if (token.optional) {
          // Prepend partial segment prefixes.
          if (token.partial) {
            path += token.prefix;
          }

          continue;
        } else {
          throw new TypeError('Expected "' + token.name + '" to be defined');
        }
      }

      if (Array.isArray(value)) {
        if (!token.repeat) {
          throw new TypeError('Expected "' + token.name + '" to not repeat, but received `' + JSON.stringify(value) + '`');
        }

        if (value.length === 0) {
          if (token.optional) {
            continue;
          } else {
            throw new TypeError('Expected "' + token.name + '" to not be empty');
          }
        }

        for (var j = 0; j < value.length; j++) {
          segment = encode(value[j]);

          if (!matches[_i].test(segment)) {
            throw new TypeError('Expected all "' + token.name + '" to match "' + token.pattern + '", but received `' + JSON.stringify(segment) + '`');
          }

          path += (j === 0 ? token.prefix : token.delimiter) + segment;
        }

        continue;
      }

      segment = token.asterisk ? encodeAsterisk(value) : encode(value);

      if (!matches[_i].test(segment)) {
        throw new TypeError('Expected "' + token.name + '" to match "' + token.pattern + '", but received "' + segment + '"');
      }

      path += token.prefix + segment;
    }

    return path;
  };
}
/**
 * Escape a regular expression string.
 *
 * @param  {string} str
 * @return {string}
 */


function escapeString(str) {
  return str.replace(/([.+*?=^!:${}()[\]|/\\])/g, '\\$1');
}
/**
 * Escape the capturing group by escaping special characters and meaning.
 *
 * @param  {string} group
 * @return {string}
 */


function escapeGroup(group) {
  return group.replace(/([=!:$/()])/g, '\\$1');
}
/**
 * Format given url, append query to url query string
 *
 * @param  {string} url
 * @param  {string} query
 * @return {string}
 */


function formatUrl(url, query) {
  var protocol;
  var index = url.indexOf('://');

  if (index !== -1) {
    protocol = url.substring(0, index);
    url = url.substring(index + 3);
  } else if (url.startsWith('//')) {
    url = url.substring(2);
  }

  var parts = url.split('/');
  var result = (protocol ? protocol + '://' : '//') + parts.shift();
  var path = parts.filter(Boolean).join('/');
  var hash;
  parts = path.split('#');

  if (parts.length === 2) {
    var _parts = parts;

    var _parts2 = Object(_babel_runtime_helpers_esm_slicedToArray__WEBPACK_IMPORTED_MODULE_2__["default"])(_parts, 2);

    path = _parts2[0];
    hash = _parts2[1];
  }

  result += path ? '/' + path : '';

  if (query && JSON.stringify(query) !== '{}') {
    result += (url.split('?').length === 2 ? '&' : '?') + formatQuery(query);
  }

  result += hash ? '#' + hash : '';
  return result;
}
/**
 * Transform data object to query string
 *
 * @param  {object} query
 * @return {string}
 */


function formatQuery(query) {
  return Object.keys(query).sort().map(function (key) {
    var val = query[key];

    if (val == null) {
      return '';
    }

    if (Array.isArray(val)) {
      return val.slice().map(function (val2) {
        return [key, '=', val2].join('');
      }).join('&');
    }

    return key + '=' + val;
  }).filter(Boolean).join('&');
}

/***/ }),

/***/ "./layouts/default.vue":
/*!*****************************!*\
  !*** ./layouts/default.vue ***!
  \*****************************/
/*! exports provided: default */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony import */ var _default_vue_vue_type_template_id_314f53c6___WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./default.vue?vue&type=template&id=314f53c6& */ "./layouts/default.vue?vue&type=template&id=314f53c6&");
/* harmony import */ var _default_vue_vue_type_style_index_0_lang_css___WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! ./default.vue?vue&type=style&index=0&lang=css& */ "./layouts/default.vue?vue&type=style&index=0&lang=css&");
/* harmony import */ var _node_modules_vue_loader_lib_runtime_componentNormalizer_js__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! ../node_modules/vue-loader/lib/runtime/componentNormalizer.js */ "./node_modules/vue-loader/lib/runtime/componentNormalizer.js");

var script = {}



/* normalize component */

var component = Object(_node_modules_vue_loader_lib_runtime_componentNormalizer_js__WEBPACK_IMPORTED_MODULE_2__["default"])(
  script,
  _default_vue_vue_type_template_id_314f53c6___WEBPACK_IMPORTED_MODULE_0__["render"],
  _default_vue_vue_type_template_id_314f53c6___WEBPACK_IMPORTED_MODULE_0__["staticRenderFns"],
  false,
  null,
  null,
  null
  
)

/* hot reload */
if (true) {
  var api = __webpack_require__(/*! ./node_modules/vue-hot-reload-api/dist/index.js */ "./node_modules/vue-hot-reload-api/dist/index.js")
  api.install(__webpack_require__(/*! vue */ "./node_modules/vue/dist/vue.runtime.esm.js"))
  if (api.compatible) {
    module.hot.accept()
    if (!api.isRecorded('314f53c6')) {
      api.createRecord('314f53c6', component.options)
    } else {
      api.reload('314f53c6', component.options)
    }
    module.hot.accept(/*! ./default.vue?vue&type=template&id=314f53c6& */ "./layouts/default.vue?vue&type=template&id=314f53c6&", function(__WEBPACK_OUTDATED_DEPENDENCIES__) { /* harmony import */ _default_vue_vue_type_template_id_314f53c6___WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./default.vue?vue&type=template&id=314f53c6& */ "./layouts/default.vue?vue&type=template&id=314f53c6&");
(function () {
      api.rerender('314f53c6', {
        render: _default_vue_vue_type_template_id_314f53c6___WEBPACK_IMPORTED_MODULE_0__["render"],
        staticRenderFns: _default_vue_vue_type_template_id_314f53c6___WEBPACK_IMPORTED_MODULE_0__["staticRenderFns"]
      })
    })(__WEBPACK_OUTDATED_DEPENDENCIES__); }.bind(this))
  }
}
component.options.__file = "layouts/default.vue"
/* harmony default export */ __webpack_exports__["default"] = (component.exports);

/***/ }),

/***/ "./layouts/default.vue?vue&type=style&index=0&lang=css&":
/*!**************************************************************!*\
  !*** ./layouts/default.vue?vue&type=style&index=0&lang=css& ***!
  \**************************************************************/
/*! no static exports found */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony import */ var _node_modules_vue_style_loader_index_js_ref_5_oneOf_1_0_node_modules_css_loader_dist_cjs_js_ref_5_oneOf_1_1_node_modules_vue_loader_lib_loaders_stylePostLoader_js_node_modules_postcss_loader_src_index_js_ref_5_oneOf_1_2_node_modules_vue_loader_lib_index_js_vue_loader_options_default_vue_vue_type_style_index_0_lang_css___WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! -!../node_modules/vue-style-loader??ref--5-oneOf-1-0!../node_modules/css-loader/dist/cjs.js??ref--5-oneOf-1-1!../node_modules/vue-loader/lib/loaders/stylePostLoader.js!../node_modules/postcss-loader/src??ref--5-oneOf-1-2!../node_modules/vue-loader/lib??vue-loader-options!./default.vue?vue&type=style&index=0&lang=css& */ "./node_modules/vue-style-loader/index.js?!./node_modules/css-loader/dist/cjs.js?!./node_modules/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/postcss-loader/src/index.js?!./node_modules/vue-loader/lib/index.js?!./layouts/default.vue?vue&type=style&index=0&lang=css&");
/* harmony import */ var _node_modules_vue_style_loader_index_js_ref_5_oneOf_1_0_node_modules_css_loader_dist_cjs_js_ref_5_oneOf_1_1_node_modules_vue_loader_lib_loaders_stylePostLoader_js_node_modules_postcss_loader_src_index_js_ref_5_oneOf_1_2_node_modules_vue_loader_lib_index_js_vue_loader_options_default_vue_vue_type_style_index_0_lang_css___WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(_node_modules_vue_style_loader_index_js_ref_5_oneOf_1_0_node_modules_css_loader_dist_cjs_js_ref_5_oneOf_1_1_node_modules_vue_loader_lib_loaders_stylePostLoader_js_node_modules_postcss_loader_src_index_js_ref_5_oneOf_1_2_node_modules_vue_loader_lib_index_js_vue_loader_options_default_vue_vue_type_style_index_0_lang_css___WEBPACK_IMPORTED_MODULE_0__);
/* harmony reexport (unknown) */ for(var __WEBPACK_IMPORT_KEY__ in _node_modules_vue_style_loader_index_js_ref_5_oneOf_1_0_node_modules_css_loader_dist_cjs_js_ref_5_oneOf_1_1_node_modules_vue_loader_lib_loaders_stylePostLoader_js_node_modules_postcss_loader_src_index_js_ref_5_oneOf_1_2_node_modules_vue_loader_lib_index_js_vue_loader_options_default_vue_vue_type_style_index_0_lang_css___WEBPACK_IMPORTED_MODULE_0__) if(__WEBPACK_IMPORT_KEY__ !== 'default') (function(key) { __webpack_require__.d(__webpack_exports__, key, function() { return _node_modules_vue_style_loader_index_js_ref_5_oneOf_1_0_node_modules_css_loader_dist_cjs_js_ref_5_oneOf_1_1_node_modules_vue_loader_lib_loaders_stylePostLoader_js_node_modules_postcss_loader_src_index_js_ref_5_oneOf_1_2_node_modules_vue_loader_lib_index_js_vue_loader_options_default_vue_vue_type_style_index_0_lang_css___WEBPACK_IMPORTED_MODULE_0__[key]; }) }(__WEBPACK_IMPORT_KEY__));
 /* harmony default export */ __webpack_exports__["default"] = (_node_modules_vue_style_loader_index_js_ref_5_oneOf_1_0_node_modules_css_loader_dist_cjs_js_ref_5_oneOf_1_1_node_modules_vue_loader_lib_loaders_stylePostLoader_js_node_modules_postcss_loader_src_index_js_ref_5_oneOf_1_2_node_modules_vue_loader_lib_index_js_vue_loader_options_default_vue_vue_type_style_index_0_lang_css___WEBPACK_IMPORTED_MODULE_0___default.a); 

/***/ }),

/***/ "./layouts/default.vue?vue&type=template&id=314f53c6&":
/*!************************************************************!*\
  !*** ./layouts/default.vue?vue&type=template&id=314f53c6& ***!
  \************************************************************/
/*! exports provided: render, staticRenderFns */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony import */ var _node_modules_vue_loader_lib_loaders_templateLoader_js_vue_loader_options_node_modules_vue_loader_lib_index_js_vue_loader_options_default_vue_vue_type_template_id_314f53c6___WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! -!../node_modules/vue-loader/lib/loaders/templateLoader.js??vue-loader-options!../node_modules/vue-loader/lib??vue-loader-options!./default.vue?vue&type=template&id=314f53c6& */ "./node_modules/vue-loader/lib/loaders/templateLoader.js?!./node_modules/vue-loader/lib/index.js?!./layouts/default.vue?vue&type=template&id=314f53c6&");
/* harmony reexport (safe) */ __webpack_require__.d(__webpack_exports__, "render", function() { return _node_modules_vue_loader_lib_loaders_templateLoader_js_vue_loader_options_node_modules_vue_loader_lib_index_js_vue_loader_options_default_vue_vue_type_template_id_314f53c6___WEBPACK_IMPORTED_MODULE_0__["render"]; });

/* harmony reexport (safe) */ __webpack_require__.d(__webpack_exports__, "staticRenderFns", function() { return _node_modules_vue_loader_lib_loaders_templateLoader_js_vue_loader_options_node_modules_vue_loader_lib_index_js_vue_loader_options_default_vue_vue_type_template_id_314f53c6___WEBPACK_IMPORTED_MODULE_0__["staticRenderFns"]; });



/***/ }),

/***/ "./locales/ch.json":
/*!*************************!*\
  !*** ./locales/ch.json ***!
  \*************************/
/*! exports provided: SIGNIN, RankTitle, Order, Player, TotalMount, Prize, DepositText, DepositTrx, DepositTrc10, DepositTrc20, DepositTrc721, WithdrawText, WithdrawTrx, WithdrawTrc10, WithdrawTrc20, WithdrawTrc721, MappingText, MappingTrx, MappingTrc20, MappingTrc721, Withdrawal, Mapping, OtherPrize, Play, Result, HowToPlay, InviteLucky, Chinese, English, WinRate, extract, PayOut, Rule, Confirm, LoginTipTitle, LoginTipContent, Login, loginTip, BalanceNotEnough, PleaseInput, LuckyDraw, vip, invite, PrizeRule, totalWin, NoWallet, noTronweb, Roadmap, Token, FairPlay, Recommended, account, autoBet, Mining, marquee, countDown, diceWithDraw, notFindGoogle, whitePaper, openAuto, closeAuto, indruction, mineNum, diceTip, withDrawSuccess, cancel, noLogin, exchange, timeOut, Dividend, day, DividingPool, default */
/***/ (function(module) {

module.exports = JSON.parse("{\"SIGNIN\":\"登陆\",\"RankTitle\":\"每日排行榜\",\"Order\":\"排名\",\"Player\":\"玩家\",\"TotalMount\":\"总投注额\",\"Prize\":\"奖励\",\"DepositText\":\"Deposit\",\"DepositTrx\":\"DepositTrx\",\"DepositTrc10\":\"DepositTrc10\",\"DepositTrc20\":\"DepositTrc20\",\"DepositTrc721\":\"DepositTrc721\",\"WithdrawText\":\"Withdraw\",\"WithdrawTrx\":\"WithdrawTrx\",\"WithdrawTrc10\":\"WithdrawTrc10\",\"WithdrawTrc20\":\"WithdrawTrc20\",\"WithdrawTrc721\":\"WithdrawTrc721\",\"MappingText\":\"Mapping\",\"MappingTrx\":\"MappingTrx\",\"MappingTrc20\":\"MappingTrc20\",\"MappingTrc721\":\"MappingTrc721\",\"Withdrawal\":\"Withdraw\",\"Mapping\":\"Mapping\",\"OtherPrize\":{\"Txt1\":\"下注可获得：\",\"Txt2\":\"现在投注可获得投注货币 100% DICE\"},\"Play\":{\"LuckNum\":\"幸运数\",\"Bet\":{\"Title\":\"投注数量\",\"Left\":\"侧链余额\"},\"WinTitle\":\"预计赢取\",\"WinRate\":\"中奖概率\",\"PayOut\":\"赔率\",\"Less\":\"小于该数获胜\",\"Roll\":\"掷骰子\",\"diceNum\":\"挖矿数量\",\"diceBanlance\":\"dice(TRC20)\",\"lackOfMoneyMistakes\":\"投注失败，略微减少投注额度重试一次吧！\",\"tronminer\":\"挖矿机器人\"},\"Result\":{\"AllBets\":\"所有投注\",\"MyBets\":\"我的投注\",\"Time\":\"时间\",\"Player\":\"玩家\",\"Select\":\"小于该数获胜\",\"Input\":\"投注\",\"Result\":\"预测数/幸运数\",\"Output\":\"收益\",\"LessThan\":\"猜小于\",\"BettingRecord\":\"投注结果\"},\"HowToPlay\":\"怎么玩\",\"InviteLucky\":\"推荐有奖\",\"Chinese\":\"中文\",\"English\":\"英文\",\"WinRate\":\"胜率\",\"extract\":\"提现\",\"PayOut\":\"赔率\",\"Rule\":{\"p1\":\"1. 首先需要一个波场钱包。点击下载<a target='_blank' href='https://chrome.google.com/webstore/detail/tronlink/ibnejdfjmmkpcnlpebklmnkoeoihofec'>TronLink</a>钱包或者<a target='_blank' href='https://dwz.cn/FyIuFVay'>TronPay</a>钱包\",\"p2\":\"（如果您无法访问Google 应用商店，可点击以下链接下载TRONlink安装包及安装教程 <a href='https://s3.amazonaws.com/trondice/Chromewallet/TRONlink.zip' target='_blank'>https://s3.amazonaws.com/trondice/Chromewallet/TRONlink.zip</a>）\",\"p3\":\"2. 设置投注额度，即游戏下注的TRX数量\",\"p4\":\"3. 调整滑块至理想的数字范围。移动滑块时，最多可赢得的TRX、胜率、奖励倍数都将自动调整\",\"p5\":\"4. 点击“摇骰子”开始游戏，智能合约随后将从1-100之间随机生成一个幸运数字，如果幸运数字在您的预测范围之内，您就赢了！\"},\"Confirm\":\"确认\",\"LoginTipTitle\":\"请登录Chrome插件钱包\",\"LoginTipContent\":\"如果您没有安装TRON钱包，请您点击以下链接下载（两款钱包均支持TRONdice）：<br>Tron Link:<a target='_blank' href='https://chrome.google.com/webstore/detail/tronlink/ibnejdfjmmkpcnlpebklmnkoeoihofec'>https://goo.gl/Yb4NRU</a><br/>Tron Pay:<a target='_blank' href='https://dwz.cn/FyIuFVay'>https://dwz.cn/FyIuFVay</a>\",\"Login\":\"登录\",\"loginTip\":\"请注意钱包使用主网节点，勿切换至测试网\",\"BalanceNotEnough\":{\"Title\":\"Insufficient wallet balance\",\"Content\":\"Please check your betting amount or withdraw the funds in the prize pool to your wallet.\"},\"PleaseInput\":\"请输入投注金额\",\"LuckyDraw\":{\"btn\":\"幸运抽奖\",\"title\":\"幸运抽奖\",\"introductions\":\"按下抽奖按钮抽取幸运号码，并根据号码取得对应奖励。\",\"number\":\"幸运号码\",\"reward\":\"幸运奖金\",\"winText\":\"本次幸运抽奖数字\",\"rewardText\":\"幸运奖金\",\"times\":\"次抽奖机会\",\"withdraw\":\"提现\",\"supplement\":{\"p1\":\"每天首次投注可以获得一次抽奖机会！\",\"p2\":\"当你每次投注大于10 TRX，即可额外获得一次免费抽奖机会！\",\"p3\":\"共可累积上限10次！\"},\"explanation\":\"活动最终解释权归 TRONDICE.ORG 所有\"},\"vip\":{\"button\":\"Vip\",\"title\":\"VIP特权\",\"desc\":\"享受VIP的专属身份和福利，每次投注均可获特定回赠！\",\"currentGrade\":\"当前等级\",\"prize\":\"投注奖励\",\"table\":{\"level\":\"等级\",\"amount\":\"累计投注额\",\"prize\":\"奖励\"},\"remainingBet\":\"只需投注 {trx} TRX 即可成为VIP{level}\",\"copyRight\":\"* 活动最终解释权归 TRONDICE.ORG 所有\"},\"invite\":{\"button\":\"邀请\",\"title\":\"邀请好友\",\"desc\":\"邀请好友即可获得他们 0.2% 的投注额！ 快复制您的专属链接发出邀请吧！\",\"prize\":\"邀请奖励\",\"record\":\"邀请记录\",\"prizeRule\":{\"p1\":\"账户邀请返现金额每天结算一次，结算时间为4:00 AM(UTC)。\",\"p2\":\"结算后金额发放到账户，同时展示对应的邀请记录。\"},\"copyButton\":\"复制\",\"table\":{\"time\":\"投注时间\",\"address\":\"被邀请人\",\"bet\":\"被邀请人的投注金额\",\"prize\":\"我的奖金\"},\"result\":{\"number\":\"邀请好友\",\"prize\":\"获得邀请奖励\"},\"inviteTip\":\"邀请人和被邀请人的账号不能相同\"},\"PrizeRule\":{\"title\":\"排行榜奖励\",\"content\":{\"rank\":\"排名\",\"prize\":\"奖励\",\"few\":\"第{no}名\",\"total\":\"共\",\"extra\":\"额外\"},\"footer\":\"奖金将在每日榜单公布后24h内（非工作日72h内），发放至您的地址。\",\"footer2\":\"每日排行结果将于第二天八点结束。\",\"copyright\":\"* 本活动最终解释权归trondice.org所有\",\"holidayTitle\":\"庆圣诞  赢dice\",\"holidayItem1\":\"总下注额排名前30的玩家，瓜分\",\"holidayItem1Num\":\"15万dice\",\"holidayItem2\":\"总下注额排名第12、第25的玩家，每人获得额外幸运奖\",\"holidayItem2Num\":\"12250 dice\",\"holidayItem3\":\"* 本活动不影响每日排名奖励\",\"holidayIndro\":\"* 奖金将在活动结束后3日内发放，本活动最终解释权归trondice.org所有\"},\"totalWin\":\"玩家总盈利\",\"NoWallet\":\"余额不足，请质押资产到侧链！\",\"noTronweb\":\"您当前未下载TRON钱包,请访问<a target='_blank' href='https://chrome.google.com/webstore/detail/tronlink/ibnejdfjmmkpcnlpebklmnkoeoihofec'>https://goo.gl/Yb4NRU</a>并下载chrome扩展程序\",\"Roadmap\":{\"button\":\"路线图\",\"content\":{\"PhaseOne\":\"<span class='page'>第一阶段：</span>（已完成）TRONdice将完成发行dice 通证、开启投注挖矿、上线交易所等功能，至此发币→投注挖矿→交易的游戏闭环已初步形成，并且TRONdice还会开启一系列拉新活动，如邀请新用户奖励活动、通证空投活动等\",\"PhaseTwo\":\"<span class='page'>第二阶段：</span>TRONdice将开启首次分红，持有dice币的用户将获得总计70% TRONdice平台利润的分红。同时，TRONdice将开启一系列活动如幸运抽奖、每日任务等。\",\"PhaseThree\":\"<span class='page'>第三阶段：</span>TRONdice将引入新玩法，并为dice token引入其他应用场景，敬请期待！\"}},\"Token\":{\"button\":\"dice 通证\",\"Dice\":{\"head\":\"简介\",\"content\":{\"p1\":\"dice通证是TRON dice平台的权益体现，持有dice可以获取70% TRON dice利润的分红，并可对平台的重要事项参与投票表决。\",\"p2\":\"dice通证已上线TRON DEX去中心化交易所，玩家可在tronscan（<a href='https://tronscan.org/#/exchange20?token=TRONdice/TRX&id=30' target='_blank'>https://tronscan.org/#/exchange20?token=TRONdice/TRX&id=30</a>）中进行dice交易。\"}},\"Distribution\":{\"head\":\"代币分配\",\"title\":\"DICE（TRC20）发行总量为10,000,000,000（10B），将用于以下方面：\",\"content\":{\"p1\":\"①    投注挖矿：60%\",\"p2\":\"②    私募（用于初始奖池及带宽能量费用）：5%\",\"p3\":\"③    空投及推广15%\",\"p4\":\"④    运营成本10%\",\"p5\":\"⑤    团队和顾问：10%\"}},\"Address\":{\"head\":\"TRONdice平台地址公示，正如白皮书所示：\",\"content\":{\"p1\":\"1. 投注挖矿池（剩余挖矿）：<br>&nbsp;&nbsp;TDsKukoeopFBq1q9HGBXT4axDbp4mvNjcH <br> DICE提取池（当前挖矿）：<br>&nbsp;&nbsp;TW76veo5eQotWrtjP4fjDCDfBDM8fzhoC8\",\"p2\":\"2. 运营成本 ：<br>&nbsp;&nbsp;TRKFhyf4rp8E1b2Z5YV7XU89A1p9wXizbc\",\"p3\":\"3.团队和顾问 ：<br>&nbsp;&nbsp;TWaFwzSmqKiKUiccv4R4G4NWnFENGm9avF\",\"p4\":\"4. 空投及推广 ：<br>&nbsp;&nbsp;TBy7wSrzpeqjHgUZtNTcFkazAZBeectEFY\",\"p5\":\"后三项初始为全部冻结，随挖矿阶段线性解冻，参与分红\"}}},\"FairPlay\":{\"button\":\"玩法与公平\",\"description\":{\"default\":\"当前的玩家种子(sha256):\",\"custom\":\"输入您的自定义种子:\",\"new\":\"新的玩家种子(sha256):\",\"button\":\"生成\",\"p1\":\"一次公平的下注，需要公平的随机数。\",\"p2\":\"TRONdice的做法是，由玩家（Player）和智能合约（House）分别提供一个种子（House Seed和Player Seed)，然后智能合约将这两个Seed混合生成一个随机幸运值。\",\"p3\":\"玩家可以输入自己的自定义种子，点击生成获得自定义的玩家种子。\",\"p4\":\"如果没有进行自定义设置，那么将使用默认的玩家种子。\"}},\"Recommended\":{\"title\":\"新玩家通过你的分享链接注册后，你可获得其挖矿所得dice的 <span class='number'> 15%</span>额外dice奖励。\",\"desc\":\"波场公链最火爆、分红最多的DApp-TRONdice已开启投注挖矿！玩游戏即可得dice，快来一起玩吧！👇\",\"copyBtn\":\"分享\",\"prize\":\"邀请奖励\",\"rule\":{\"p1\":\"账户邀请返现金额每天 4:00 AM（UTC）结算前一日结果\",\"p2\":\"领取将直接打入当前账户地址，1分钟后待区块确认即可在tronscan.org中查到\"},\"widthDraw\":\"提取\",\"explanation\":\"活动最终解释权归 TRONDICE.ORG 所有\",\"sucdess\":\"提取成功\",\"noDice\":\"无可提取\",\"notAllowed\":\"邀请奖励功能暂时维护，但数据仍正常记录，待恢复后即可将原来未提取的奖励和暂停期间的奖励一起提取\"},\"account\":{\"trxWallet\":\"TRX余额\",\"diceWallet\":\"dice余额\",\"bandWidth\":\"已用带宽\",\"energy\":\"已用能量\",\"tip\":\"您的带宽/能量即将耗尽，请尽快冻结TRX以获取相应资源！\",\"resouse\":{\"p1\":\"由于TRON公链机制，每次投骰子调用智能合约需消耗能量(Energy)资源：\",\"p2\":\"--正常情况下，我们会帮助玩家承担100%的能量消耗。\",\"p3\":\"--异常情况如同一时间用户访问量过大等，TRONdice提供的能量可能不足，此时则需要消耗玩家的能量，您可冻结TRX获取能量，或等待TRONdice能量恢复。我们强烈不建议您在没有能量时继续游戏，此时每次下注将消耗TRX\"}},\"autoBet\":{\"name\":\"自动投注\",\"title\":\"自动投注新手引导：\",\"content\":{\"p1\":\"亲爱的玩家，现在你可以通过“自动投注”功能来便捷地参与TRONdice投骰子游戏：\",\"p2\":\"-输入你期望的下注数量，点击“确认”即可开始\",\"p3\":\"-我们将按照你设定的数值进行单倍循环投注，一次结果完成后立即开始第二次投注\",\"p4\":\"-为了获得最好的投注效果，请将我们加入<span class='tronPay'>TronPay</span>钱包的白名单，以免不断授权\",\"p5\":\"-刷新页面操作会中断您的下注及扣费，请勿在投注过程中刷新页面\"}},\"Mining\":{\"content\":{\"p1\":\"时间：2018-11-15 20:11:30 UTC\",\"p2\":\"挖矿阶段：第一轮\",\"p3\":\"挖矿效率：1 TRX ：1 dice\",\"p4\":\"本轮矿池已挖出：500,000,000（57.58%）\",\"p5\":\"总矿池已挖出：6,000,000,000 （70.78%）\"},\"table\":{\"title\":\"投注挖矿规则表\",\"stage\":\"挖矿阶段\",\"proportion\":\"dice / TRX\",\"diceNum\":\"dice数量\"},\"desc1\":\"{date}(UTC) &nbsp;&nbsp;挖矿阶段：第{round}轮\",\"desc2\":\"<span class='br'>挖矿效率：<span class='yellow'> {rate}</span> &nbsp;&nbsp;</span> <span class='br'>本阶段矿池已挖出：<span class='yellow'>{currentPool}</span>（{currentPoolRate}%）&nbsp;&nbsp;</span><span class='br'>总矿池已挖出：<span class='yellow'>{totalPool}</span> （{totalPoolRate}%）</span>\"},\"marquee\":\"恭喜{player}赢取<span class='num'>{trx}</span> TRX 赔率<span class='num'>{level}</span>倍\",\"countDown\":{\"name\":\"倒计时\",\"title\":\"即将开启dice投注挖矿\"},\"diceWithDraw\":{\"btn\":\"dice提取\",\"desc\":\"将dice余额提现到玩家的钱包地址，玩家可以在tronscan.org及所有波场钱包上查询dice的到账情况。\",\"diceNum\":\"dice 余额每五分钟更新一次\",\"diceNum2\":\"提取数量为余额数向下取整\",\"diceNum3\":\"挖矿提现每日可提取3次\",\"notAllow\":{\"p1\":\"1.所有未提取账户dice余额将自动转入新dice(TRC20)。\",\"p2\":\"2.dice(TRC20)挖矿后会自动转至你的账户，无需提取。\",\"p3\":\"3.该提现功能即将下线，保留一周供用户查询自己账户余额\"}},\"notFindGoogle\":\"如果您无法访问Google 应用商店，可点击以下链接下载TRONlink安装包及安装教程。\",\"whitePaper\":\"白皮书\",\"openAuto\":\"您已开启自动投注\",\"closeAuto\":\"您已取消自动投注\",\"indruction\":\"游戏介绍\",\"mineNum\":\"dice数量=挖矿效率*下注数量\",\"diceTip\":\"* dice挖矿到账时间为5min左右\",\"withDrawSuccess\":\"提取成功\",\"cancel\":\"取消\",\"noLogin\":\"登录钱包或切换账户后请刷新页面再开始游戏.\",\"exchange\":\"交易所\",\"timeOut\":\"因事件服务器卡顿，获取智能合约投注结果超时，但并不影响您的投注结果，您可在钱包里查看余额变化。\",\"Dividend\":{\"name\":\"分红\",\"notice\":\"TRONdice进展突飞猛进，即将开启首次分红啦!\",\"rules\":{\"title\":\"分红规则如下：\",\"content\":{\"p1\":\"1.持有dice token即可获得TRX分红，大家请从游戏中尽快取出dice到自己钱包\",\"p2\":\"2.本周六北京时间18:00，我们将对各地址dice余额进行快照，快照后24小时内分红TRX将按快照时的dice比例飞入你的钱包\"}}},\"day\":\"天\",\"DividingPool\":{\"name\":\"每日分红池\",\"dividendTitle\":\"分红\",\"diceNum\":\"dice流通总量\",\"content\":{\"p1\":\"1.当TRONdice平台有任何利润，其中<span>70%</span>将进入分红池，等待下次分红\",\"p2\":\"2.TRONdice将于每天<span>UTC</span>时间<span>00:00</span>，进行所有<span>已质押</span>dice快照\",\"p3\":\"3.分红池中TRX将按快照的质押dice等比例分配给持币者\",\"p4\":\"4.分红TRX将在快照后<span>24h</span>内自动打入您的账户余额\"},\"title\":\"质押\",\"pledge\":\"当前可质押dice余额\",\"unfreeze\":\"解除质押\",\"unfreezeInfo\":\"当前已质押dice总额\",\"unfreezeBtn\":\"解押\",\"unfreezeTip\":\"解除质押全部已质押dice，解除质押后，将不会获得分红奖励。\",\"withdraw\":\"提取\",\"withdrawInfo\":\"当前未提取dice总额\",\"withdrawTip\":\"提取全部已解押dice，每次解押48小时后可以提取。\",\"note\":\"注\",\"sendInfo\":\"已向区块链广播交易，请 1 分钟后查看区块结果\",\"numInfo\":\"请大于零\",\"minInfo\":\"请小于当前总额\",\"cancel\":\"取消\",\"unfreezeMsg\":\"每次解押48小时后可以提取。解除质押后，将不会获得分红奖励\",\"bonus\":\"分红记录\",\"bonusTitle\":\"我的分红累计\",\"bonusTime\":\"时间\",\"bonusNum\":\"数量\",\"earnings\":\"我的预期收益\",\"standardNum\":\"每十万份dice预期TRX收益\",\"ledgeNum\":\"dice质押总量,每24H更新一次\",\"ledgeUpdate\":\"\"}}");

/***/ }),

/***/ "./locales/en.json":
/*!*************************!*\
  !*** ./locales/en.json ***!
  \*************************/
/*! exports provided: SIGNIN, DepositNum, FeeLimit, RankTitle, Order, Player, TotalMount, Prize, operationDeposit, operationWithdraw, DepositText, DepositTrx, DepositTrc10, DepositTrc20, DepositTrc721, WithdrawText, WithdrawTrx, WithdrawTrc10, WithdrawTrc20, WithdrawTrc721, MappingText, MappingTrx, MappingTrc20, MappingTrc721, Withdrawal, Mapping, OtherPrize, Play, Result, HowToPlay, InviteLucky, Chinese, English, WinRate, extract, PayOut, Range, RangeTip, InputCount, InputCountTip, InputMoney, Balance, LuckyValue, LuckyValueTip, LuckyPool, Roll, Withdraw, TRX, AllBets, MyBets, LuckyPrize, Select, Input, Output, ServiceItem, Telegram, Rule, Confirm, LoginTipTitle, LoginTipContent, Login, loginTip, BalanceNotEnough, PleaseInput, LuckyDraw, vip, invite, PrizeRule, totalWin, NoWallet, noTronweb, Roadmap, Token, FairPlay, Recommended, account, autoBet, Mining, marquee, countDown, diceWithDraw, notFindGoogle, whitePaper, openAuto, closeAuto, indruction, mineNum, diceTip, withDrawSuccess, cancel, noLogin, exchange, timeOut, Dividend, day, DividingPool, default */
/***/ (function(module) {

module.exports = JSON.parse("{\"SIGNIN\":\"SIGN IN\",\"DepositNum\":\"Amount\",\"FeeLimit\":\"Fee Limit\",\"RankTitle\":\"Daily Rankings\",\"Order\":\"Rank\",\"Player\":\"Player\",\"TotalMount\":\"Total Wager\",\"Prize\":\"Reward\",\"operationDeposit\":\"Deposit assets will take 5 to 10 seconds, please check later.\",\"operationWithdraw\":\"Withdraw assets will take 5 to 10 seconds, please check later.\",\"DepositText\":\"Deposit\",\"DepositTrx\":\"DepositTrx\",\"DepositTrc10\":\"DepositTrc10\",\"DepositTrc20\":\"DepositTrc20\",\"DepositTrc721\":\"DepositTrc721\",\"WithdrawText\":\"Withdraw\",\"WithdrawTrx\":\"WithdrawTrx\",\"WithdrawTrc10\":\"WithdrawTrc10\",\"WithdrawTrc20\":\"WithdrawTrc20\",\"WithdrawTrc721\":\"WithdrawTrc721\",\"MappingText\":\"Mapping\",\"MappingTrx\":\"MappingTrx\",\"MappingTrc20\":\"MappingTrc20\",\"MappingTrc721\":\"MappingTrc721\",\"Withdrawal\":\"Withdraw\",\"Mapping\":\"Mapping\",\"OtherPrize\":{\"Txt1\":\"下注可获得：\",\"Txt2\":\"现在投注可获得投注货币 100% DICE\"},\"Play\":{\"LuckNum\":\"Lucky Number\",\"Bet\":{\"Title\":\"Bet\",\"Left\":\"Balance of Sidechain\"},\"WinTitle\":\"Expect bonuses\",\"WinRate\":\"Win Chance\",\"PayOut\":\"Payout\",\"Less\":\"Roll Under to Win\",\"Roll\":\"ROLL\",\"diceNum\":\"Mining dice\",\"diceNum2\":\"Fractions are rounded down\",\"diceBanlance\":\"dice(TRC20)\",\"lackOfMoneyMistakes\":\"Bet failured，please try again with slightly reduced bets\",\"tronminer\":\"Mining bot\"},\"Result\":{\"AllBets\":\"All Bets\",\"MyBets\":\"My Bets\",\"Time\":\"Time\",\"Player\":\"Player\",\"Select\":\"Roll Under\",\"Input\":\"Bet Amount\",\"Result\":\"Forecast/Result\",\"Output\":\"Earning\",\"LessThan\":\"Less Than\",\"BettingRecord\":\"BET RESULT\"},\"HowToPlay\":\"How To Play\",\"InviteLucky\":\"Referral reward\",\"Chinese\":\"Chinese\",\"English\":\"English\",\"WinRate\":\"Winning Rate\",\"extract\":\"Extract\",\"PayOut\":\"Payout\",\"Range\":\"Choose betting range\",\"RangeTip\":\"You win if the number on the dice falls in your betting range.\",\"InputCount\":\"Enter the betting amount\",\"InputCountTip\":\"· The number of single bets can not exceed your wallet balance. <br> · The maximum number of single bets can not exceed 10,000 TRX.\",\"InputMoney\":\"Enter the amount\",\"Balance\":\"Your Wallet Balance:\",\"LuckyValue\":\"Lucky Number\",\"LuckyValueTip\":\"1. The lucky number is the number of times you lose in a row. The number will go back to 0 if you win or lose 6 times in a row.<br>2. If you lose, 10% of your betting amount will be added to the lucky pool. When you win or lose 6 times in a row, the amount of funds in the lucky pool will turn to 0.<br>3. When the lucky number hits 3, the current amount of funds in the lucky pool will be added to the prize pool as a reward; <br>when the lucky number hits 6, twice the amount of funds in the lucky pool will be added to the prize pool as a reward.\",\"LuckyPool\":\"Lucky Pool\",\"Roll\":\"Roll the Dice\",\"Withdraw\":\"Withdraw\",\"TRX\":\"TRX\",\"AllBets\":\"All Bets\",\"MyBets\":\"My Bets\",\"LuckyPrize\":\"Lucky Prize\",\"Select\":\"Choose\",\"Input\":\"Bet\",\"Output\":\"Prize\",\"ServiceItem\":\"Terms of Service\",\"Telegram\":\"Telegram\",\"Rule\":{\"p1\":\"1. Make sure you have a Tron wallet. Click to download the <a target='_blank' href='https://chrome.google.com/webstore/detail/tronlink/ibnejdfjmmkpcnlpebklmnkoeoihofec'>TronLink wallet</a>/ <a target='_blank' href='https://chrome.google.com/webstore/detail/tronpay/gjdneabihbmcpobmfhcnljaojmgoihfk'>TronPay</a> wallet\",\"p2\":\"2. Set your bet amount, that is the number of TRX you want to bet on.\",\"p3\":\"3. You can adjust the slider to the predicted number range you want. At the same time most amount you can win, win rate, odds will be automatically adjusted.\",\"p4\":\"4. Click the 'ROLL' to start the game. The smart contract will randomly generate a lucky number from 1 to 100. If the lucky number is within your range, you win!\",\"p5\":\"Still have questions? Welcome to join our <a href='https://t.me/TRONdiceofficial' target='_blank'>telegram group</a>, we are glad to help you!\"},\"Confirm\":\"Confirm\",\"LoginTipTitle\":\"Please log in TRON Chrome Wallet.\",\"LoginTipContent\":\"If you have not downloaded the wallet, please download the Chrome extensions: <br>TronLink <a href='https://chrome.google.com/webstore/detail/tronlink/ibnejdfjmmkpcnlpebklmnkoeoihofec' target='_blank'>https://goo.gl/Yb4NRU</a> <br> TronPay <a href='https://dwz.cn/FyIuFVay' target='_blank'>https://dwz.cn/FyIuFVay</a>\",\"Login\":\"Login\",\"loginTip\":\"Please switch wallet to mainnet node, don't use testnet node\",\"BalanceNotEnough\":{\"Title\":\"Insufficient wallet balance\",\"Content\":\"Please check your betting amount or withdraw the funds in the prize pool to your wallet.\"},\"PleaseInput\":\"Please enter the betting amount !\",\"LuckyDraw\":{\"btn\":\"Lucky draw\",\"title\":\"Lucky draw\",\"introductions\":\"Press the \\\"lucky draw\\\" button to draw a number; claim your prize of the number.\",\"number\":\"Lucky number\",\"reward\":\"Lucky prize\",\"winText\":\"Lucky number for this round\",\"rewardText\":\"Lucky prize\",\"times\":\"to draw\",\"withdraw\":\"Withdraw\",\"supplement\":{\"p1\":\"The first betting of a day will give you 1 opportunity to draw!\",\"p2\":\"You will win an extra opportunity to draw for free for each betting over 10 TRX! \",\"p3\":\"A maximum of 10 opportunities (accumulated) to draw!\"},\"explanation\":\"TRONDICE.ORG reserves the right of final interpretation of the campaign\"},\"vip\":{\"button\":\"Vip\",\"title\":\"VIP privileges\",\"desc\":\"Exclusive VIP benefits with special rewards for each betting!\",\"currentGrade\":\"Current level\",\"prize\":\"Betting rewards\",\"table\":{\"level\":\"Level\",\"amount\":\"Accumulated betting amount\",\"prize\":\"Rewards\"},\"remainingBet\":\"A betting of {trx} TRX will make you VIP\",\"copyRight\":\"* TRONDICE.ORG reserves the right of final interpretation of the campaign\"},\"invite\":{\"button\":\"Invite\",\"title\":\"Refer a friend\",\"desc\":\"Refer a friend and get 0.2% of their betting! Copy your exclusive link and send out your invitation!\",\"prize\":\"Referral bonus\",\"prizeRule\":{\"p1\":\"The amount of cash return for referral reward will be settled by UTC 4:00 AM the next day.\",\"p2\":\"The bonus will be deposited into the corresponding account with a record of the invitation.\"},\"record\":\"Invitation record\",\"copyButton\":\"Copy\",\"table\":{\"time\":\"Betting time\",\"address\":\"Invitee\",\"bet\":\"Betting amount of the invitee\",\"prize\":\"My rewards\"},\"result\":{\"number\":\"Invite friends\",\"prize\":\"Invitation to award\"},\"inviteTip\":\"Inviter and invitee can't be the same one\"},\"PrizeRule\":{\"title\":\"Daily Ranking Reward\",\"header\":\"To thank our supporters, trondice.org has released a new feature of Daily Betting Ranking List, with various amount of TRX reward for the top 1 - 30 players on the list.\",\"content\":{\"rank\":\"Rank\",\"prize\":\"Reward\",\"few\":\"NO.{no}\",\"total\":\"tatal\",\"extra\":\"extra\"},\"footer\":\"Rewards will be sent to your address within 24h upon the release of the Daily list. Nonworkdays rewards will be sent within 72h.\",\"footer2\":\"The result will be announced at 00:00(UTC) everyday.\",\"copyright\":\"* trondice.org reserves the final right to interpret the activity.\",\"holidayTitle\":\"Merry Christmas Play & Win dice\",\"holidayItem1\":\"Top30 players of the total wager divide\",\"holidayItem1Num\":\"150k dice\",\"holidayItem2\":\"The 12th & the 25th players of the total wager win\",\"holidayItem2Num\":\"12.25k dice/person\",\"holidayItem3\":\"* Christmas activity will be sent with daily ranking reward simultaneously\",\"holidayIndro\":\"* Rewards will be sent to your address within 72h after the activity, trondice.org reserves the final right to interpret the activity.\"},\"totalWin\":\"Total Won\",\"NoWallet\":\"Your balance is insufficient, please deposit TRX !\",\"noTronweb\":\"You have not downloaded the wallet, please visit <a href='https://chrome.google.com/webstore/detail/tronlink/ibnejdfjmmkpcnlpebklmnkoeoihofec' target='_blank'>https://goo.gl/Yb4NRU</a> and download the Chrome extensions.\",\"Roadmap\":{\"button\":\"Roadmap\",\"content\":{\"PhaseOne\":\"<span class='page'>Phase One:</span> (Completed) TRONdice will soon complete features such as issuing the dice token, starting betting and mining, launching the exchange, etc., marking the early form of a closed loop of token issuance → betting and mining → trading. Also, TRONdice will launch a series of campaigns to attract new users, including invitation bonus and token airdrop, etc. \",\"PhaseTwo\":\" <span class='page'>Phase Two:</span> TRONdice will distribute its profit for the first time. All dice token holders will share 70% of the profit TRONdice generates. Meanwhile, campaigns like lucky draw, daily task reward, etc. will be available as well. \",\"PhaseThree\":\"<span class='page'>Phase Three:</span> TRONdice will add new features for the game and various application scenarios for the dice token. Please stay tuned!\"}},\"Token\":{\"button\":\"dice Token\",\"Dice\":{\"head\":\"Introduction\",\"content\":{\"p1\":\"dice token represents the users’ rights and interests on TRONdice. All dice token holders can share 70% of the profit TRONdice generates, and vote on important issues on the platform. \",\"p2\":\" dice token has already been listed on TRON DEX. Users can now trade with dice tokens on Tronscan (<a href='https://tronscan.org/#/exchange20?token=TRONdice/TRX&id=30' target='_blank'>https://tronscan.org/#/exchange20?token=TRONdice/TRX&id=30</a>).\"}},\"Distribution\":{\"head\":\"Token Distribution\",\"title\":\"TRONdice will issue a total of 10,000,000,000（10B）DICE tokens ( TRC20) and they will be used in the following aspects: \",\"content\":{\"p1\":\"①    Betting and mining: <span class='number'>60%</spanclass>\",\"p2\":\"②    Private placement (for the initial prize pool and the cost of Bandwidth and Energy): <span class='number'>5%</span>\",\"p3\":\"③    Airdrop and market promotion: <span class='number'>15%</span>\",\"p4\":\"④    Operation cost: <span class='number'>10%</span>\",\"p5\":\"⑤    Team and consultant: <span class='number'>10%</span>\"}},\"Address\":{\"head\":\"Platform address publish. As white paper published rules: \",\"content\":{\"p1\":\"1. Mining Pool (the left rounds):<br> &nbsp;&nbsp;TDsKukoeopFBq1q9HGBXT4axDbp4mvNjcH <br>Withdrawal Account (current round): <br>&nbsp;&nbsp;TW76veo5eQotWrtjP4fjDCDfBDM8fzhoC8\",\"p2\":\"2. Operation Cost : <br>&nbsp;&nbsp;TRKFhyf4rp8E1b2Z5YV7XU89A1p9wXizbc\",\"p3\":\"3. Team and Consultant : <br>&nbsp;&nbsp;TWaFwzSmqKiKUiccv4R4G4NWnFENGm9avF\",\"p4\":\"4. Airdrop and Market Promotion :<br>&nbsp;&nbsp;TBy7wSrzpeqjHgUZtNTcFkazAZBeectEFY\",\"p5\":\"2&3&4 is freezed and will be equal proportion unfreezed when mining step into a new round\"}}},\"FairPlay\":{\"button\":\"Rules and Fair Play\",\"description\":{\"default\":\"Current player seed (sha256):\",\"custom\":\"Enter your customized seed:\",\"new\":\"New player seed (sha256):\",\"button\":\"Generate\",\"p1\":\"A fair bet requires fair random numbers\",\"p2\":\"On TRONdice, both the player and the house will provide a seed respectively(House Seed and Player Seed), and the smart contract will generate a random number using these two seeds combined.\",\"p3\":\"Players can enter their own customized seed, click and generate a customized player seed.\",\"p4\":\"If they do not opt for customized settings, the default player seed will be used.\"}},\"Recommended\":{\"title\":\"Once a new user uses your invite link, you will get a additional reward of <span class='number'> 15%</span> of dice every bet he mined ! \",\"desc\":\"TRONdice, the most popular dice game on TRON blockchain, has started mining! Players earn dice token once they place their bets. Rolling the dice, rolling in money! 👇\",\"copyBtn\":\"Share\",\"prize\":\"Referral bonus\",\"rule\":{\"p1\":\"The amount of cash return for referral reward will be settled by UTC 4:00 AM the next day.\",\"p2\":\"If you would like to withdraw your reward, it will go directly into your account under the current address. You can check it on tronscan.org after the block confirmation in 1 minute.\"},\"widthDraw\":\"Withdraw\",\"explanation\":\"TRONDICE.ORG reserves the right for final explanation. \",\"sucdess\":\"Successful withdrawal\",\"noDice\":\"No dice to withdraw\",\"notAllowed\":\"The inviter bonus function is maintained temporarily, but the data is still recorded normally. After recovery, the previously unextracted reward can be extracted together with the reward during suspension\"},\"account\":{\"trxWallet\":\"TRX balance\",\"diceWallet\":\"dice balance\",\"bandWidth\":\"Used Bandwidth\",\"energy\":\"Used Energy\",\"tip\":\"You are running out of bandwidth/energy, please freeze your TRX to receive new resource! \",\"resouse\":{\"p1\":\"Due to the mechanism of the TRON blockchain, each time you roll the dice, the smart contract will be triggered, which consumes Energy.\",\"p2\":\"--  Under normal circumstances, we will pay 100% of the Energy consumption for players.\",\"p3\":\"-- Under exceptional circumstances, for example, when the website is visited by too many players at the same time, TRONdice may not be able to provide enough Energy, and players will need to use their own Energy. You can freeze TRX to gain Energy, or wait for the Energy at TRONdice to resume. We do not recommend playing the game without Energy, because each betting will consume TRX.\"}},\"autoBet\":{\"name\":\"Auto Betting\",\"title\":\"User guide for Automated Betting:\",\"content\":{\"p1\":\"Dear users, now you can use the “Automated Betting”feature to join TRONdice easily:\",\"p2\":\"-Enter your betting amount, and click “Confirm”to start the game\",\"p3\":\"-We will bet for you continuously with the betting amount you set, one round after another.\",\"p4\":\"-To get the best betting experience and avoid repeated delegation operations, join the White List in the <span class='tronPay'>TronPay</span> wallet.\",\"p5\":\"-Refresh the page may interrupt your betting or payment. Please don’t refresh the page when betting.\"}},\"Mining\":{\"table\":{\"title\":\"Betting & Mining Rules\",\"stage\":\"Round\",\"proportion\":\"dice / TRX\",\"diceNum\":\"dice amount\"},\"desc1\":\"{date}(UTC)  &nbsp;&nbsp;  Round:{round} \",\"desc2\":\"<span class='br'>Mining efficiency:<span class='yellow'> {rate}</span> &nbsp;&nbsp; </span>  <span class='br'>Mining progress of the round:<span class='yellow'>{currentPool}</span>({currentPoolRate}%) &nbsp;&nbsp;</span>   <span class='br'>Total mining progress:<span class='yellow'>{totalPool}</span> ({totalPoolRate}%)</span>\"},\"marquee\":\"Congratulations to {player} for winning <span class='num'>{trx}</span> TRX with <span class='num'>{level}</span> odds\",\"countDown\":{\"name\":\"CountDown\",\"title\":\"dice betting & mining will start soon!\"},\"diceWithDraw\":{\"btn\":\"Withdrawal\",\"desc\":\"The dice balance is withdrawn to the player's address, and the player can check the arrival of dice on tronscan.org and all TRON wallets.\",\"diceNum\":\"dice balance will be updated every 5 minutes\",\"diceNum2\":\"The number of withdrawals is rounded down to the number of balances\",\"diceNum3\":\"Users can only withdraw 3 times a day.\",\"notAllow\":{\"p1\":\"1.The balance of all unextracted dice accounts wil automatically withdrawal new dice(TRC20).\",\"p2\":\"2.dice (TRC20) will be transferred to your account automatically after mining without any withdrawal . \",\"p3\":\"3.The withdrawal function will be offline. Keep it for a week for users to check their account balance.\"}},\"notFindGoogle\":\"If you cannot use the Chrome Web Store for Apps, click on the following link to download TRONlink installation package and tutorial.\",\"whitePaper\":\"White Paper\",\"openAuto\":\"You have enabled Automated Betting\",\"closeAuto\":\"You have cancelled Automated Betting\",\"indruction\":\"Introduction\",\"mineNum\":\"dice amount = mining efficiency * TRX bet amount\",\"diceTip\":\"* dices gained through mining will arrive in your account in about 5 minutes.\",\"withDrawSuccess\":\"Withdraw Successfully\",\"cancel\":\"Cancel\",\"noLogin\":\"No Account Found, Please Login.\",\"exchange\":\"Exchange\",\"timeOut\":\"Because of event services log, we can't load the smart contract betting result. This will not affect your betting result, you can check balance in your TRON wallet.\",\"Dividend\":{\"name\":\"Dividend\",\"notice\":\"TRONdice Is SOON to Initiate Its First Profit Dividend Campaign!\",\"rules\":{\"title\":\"How to get the dividend:\",\"content\":{\"p1\":\"1. You are eligible to get the dividend if you are a dice token holder. Please withdraw dice into your wallet ASAP.\",\"p2\":\"2. 10am, this Saturday, UTC, we will snapshot the dice balance in each address. In 24 hrs, the TRX dividend will be distributed into your wallet based on the proportion of the balance snapshotted.\"}}},\"day\":\"day\",\"DividingPool\":{\"name\":\"Daily Dividend Pool\",\"dividendTitle\":\"Dividend\",\"diceNum\":\"Total amount of dice in circulation\",\"content\":{\"p1\":\"1.When there is any profit on TRONdice platform, <span>70%</span> of the profit will go into the dividend pool for the next dividend distribution.\",\"p2\":\"2.TRONdice will generate snapshot for all the <span>staked</span> dice at <span>00:00 UTC</span> everyday.\",\"p3\":\"3.TRX in the dividend pool will be distributed to dice holders according to their ratio of staked dice amount in the snapshot.\",\"p4\":\"4.The dividend TRX will be distributed to your account automatically within <span>24 hours</span> after snapshot.\"},\"title\":\"Stake\",\"pledge\":\"dice can be staked\",\"unfreeze\":\"Unstake\",\"unfreezeInfo\":\"staked dice balance\",\"unfreezeBtn\":\"Unstake\",\"unfreezeTip\":\"Unstake all of staked dice,After unstaking operation, you will not get TRX dividend.\",\"withdraw\":\"Withdrawal\",\"withdrawInfo\":\"Total undrawn dice\",\"withdrawTip\":\"withdraw all of untaked dice. After unstake, need 48h to withdrawal. Unstaking more will reset the 48h timer.\",\"note\":\"Tip\",\"sendInfo\":\"Broadcast Successfully. Wait one minute to check blockchain confirmation.\",\"numInfo\":\"Need to more than zero\",\"minInfo\":\"Please less than the maximum\",\"cancel\":\"cancel\",\"unfreezeMsg\":\"After unstake, need 48h to withdrawal. Unstaking more will reset the 48h timer.After unstaking operation, you will not get TRX dividend.\",\"bonus\":\"Dividend Record\",\"bonusTitle\":\"My total dividend\",\"bonusTime\":\"Time\",\"bonusNum\":\"TRX amount\",\"earnings\":\"Expected returns of your staked dice\",\"standardNum\":\"Expected returns per 100,000 dice\",\"ledgeNum\":\"Total amount of staked dice\",\"ledgeUpdate\":\"Updated every 24h\"}}");

/***/ }),

/***/ "./middleware/i18n.js":
/*!****************************!*\
  !*** ./middleware/i18n.js ***!
  \****************************/
/*! exports provided: default */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony import */ var core_js_modules_es6_regexp_replace__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! core-js/modules/es6.regexp.replace */ "./node_modules/core-js/modules/es6.regexp.replace.js");
/* harmony import */ var core_js_modules_es6_regexp_replace__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es6_regexp_replace__WEBPACK_IMPORTED_MODULE_0__);
/* harmony import */ var core_js_modules_es6_regexp_constructor__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! core-js/modules/es6.regexp.constructor */ "./node_modules/core-js/modules/es6.regexp.constructor.js");
/* harmony import */ var core_js_modules_es6_regexp_constructor__WEBPACK_IMPORTED_MODULE_1___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es6_regexp_constructor__WEBPACK_IMPORTED_MODULE_1__);


/* harmony default export */ __webpack_exports__["default"] = (function (_ref) {
  var isHMR = _ref.isHMR,
      app = _ref.app,
      store = _ref.store,
      route = _ref.route,
      params = _ref.params,
      error = _ref.error,
      redirect = _ref.redirect;
  var defaultLocale = app.i18n.fallbackLocale; // If middleware is called from hot module replacement, ignore it

  if (isHMR) return; // Get locale from params

  var locale = params.lang || defaultLocale;

  if (store.state.locales.indexOf(locale) === -1) {
    return error({
      message: 'This page could not be found.',
      statusCode: 404
    });
  } // Set locale


  store.commit('SET_LANG', locale);
  app.i18n.locale = store.state.locale; // If route is /<defaultLocale>/... -> redirect to /...

  if (locale === defaultLocale && route.fullPath.indexOf('/' + defaultLocale) === 0) {
    var toReplace = '^/' + defaultLocale + (route.fullPath.indexOf('/' + defaultLocale + '/') === 0 ? '/' : '');
    var re = new RegExp(toReplace);
    return redirect(route.fullPath.replace(re, '/'));
  }
});

/***/ }),

/***/ "./node_modules/babel-loader/lib/index.js?!./node_modules/vue-loader/lib/index.js?!./.nuxt/components/nuxt-build-indicator.vue?vue&type=script&lang=js&":
/*!************************************************************************************************************************************************************************!*\
  !*** ./node_modules/babel-loader/lib??ref--2-0!./node_modules/vue-loader/lib??vue-loader-options!./.nuxt/components/nuxt-build-indicator.vue?vue&type=script&lang=js& ***!
  \************************************************************************************************************************************************************************/
/*! exports provided: default */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
/* harmony default export */ __webpack_exports__["default"] = ({
  name: 'nuxt-build-indicator',
  data: function data() {
    return {
      building: false,
      progress: 0,
      animatedProgress: 0,
      reconnectAttempts: 0
    };
  },
  mounted: function mounted() {
    if (WebSocket === undefined) {
      return; // Unsupported
    }

    this.wsConnect();
  },
  beforeDestroy: function beforeDestroy() {
    this.wsClose();
    clearInterval(this._progressAnimation);
  },
  computed: {
    wsURL: function wsURL() {
      var _path = '/_loading/ws';

      var _protocol = location.protocol === 'https:' ? 'wss' : 'ws';

      return "".concat(_protocol, "://").concat(location.hostname, ":").concat(location.port).concat(_path);
    }
  },
  watch: {
    progress: function progress(val, oldVal) {
      var _this = this;

      // Average progress may decrease but ignore it!
      if (val < oldVal) {
        return;
      } // Cancel old animation


      clearInterval(this._progressAnimation); // Jump to edge immediately

      if (val < 10 || val > 90) {
        this.animatedProgress = val;
        return;
      } // Animate to value


      this._progressAnimation = setInterval(function () {
        var diff = _this.progress - _this.animatedProgress;

        if (diff > 0) {
          _this.animatedProgress++;
        } else {
          clearInterval(_this._progressAnimation);
        }
      }, 50);
    }
  },
  methods: {
    wsConnect: function wsConnect() {
      var _this2 = this;

      if (this._connecting) {
        return;
      }

      this._connecting = true;
      this.wsClose();
      this.ws = new WebSocket(this.wsURL);
      this.ws.onmessage = this.onWSMessage.bind(this);
      this.ws.onclose = this.wsReconnect.bind(this);
      this.ws.onerror = this.wsReconnect.bind(this);
      setTimeout(function () {
        _this2._connecting = false;

        if (_this2.ws.readyState !== WebSocket.OPEN) {
          _this2.wsReconnect();
        }
      }, 5000);
    },
    wsReconnect: function wsReconnect() {
      var _this3 = this;

      if (this._reconnecting || this.reconnectAttempts++ > 10) {
        return;
      }

      this._reconnecting = true;
      setTimeout(function () {
        _this3._reconnecting = false;

        _this3.wsConnect();
      }, 1000);
    },
    onWSMessage: function onWSMessage(message) {
      var _this4 = this;

      var data = JSON.parse(message.data);
      this.progress = Math.round(data.states.reduce(function (p, s) {
        return p + s.progress;
      }, 0) / data.states.length);

      if (!data.allDone) {
        this.building = true;
      } else {
        this.$nextTick(function () {
          _this4.building = false;
          _this4.animatedProgress = 0;
          _this4.progress = 0;
          clearInterval(_this4._progressAnimation);
        });
      }
    },
    wsClose: function wsClose() {
      if (this.ws) {
        this.ws.close();
        delete this.ws;
      }
    }
  }
});

/***/ }),

/***/ "./node_modules/babel-loader/lib/index.js?!./node_modules/vue-loader/lib/index.js?!./.nuxt/components/nuxt-error.vue?vue&type=script&lang=js&":
/*!**************************************************************************************************************************************************************!*\
  !*** ./node_modules/babel-loader/lib??ref--2-0!./node_modules/vue-loader/lib??vue-loader-options!./.nuxt/components/nuxt-error.vue?vue&type=script&lang=js& ***!
  \**************************************************************************************************************************************************************/
/*! exports provided: default */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
/* harmony default export */ __webpack_exports__["default"] = ({
  name: 'NuxtError',
  props: {
    error: {
      type: Object,
      default: null
    }
  },
  head: function head() {
    return {
      title: this.message,
      meta: [{
        name: 'viewport',
        content: 'width=device-width,initial-scale=1.0,minimum-scale=1.0,maximum-scale=1.0,user-scalable=no'
      }]
    };
  },
  computed: {
    statusCode: function statusCode() {
      return this.error && this.error.statusCode || 500;
    },
    message: function message() {
      return this.error.message || "Error";
    }
  }
});

/***/ }),

/***/ "./node_modules/css-loader/dist/cjs.js?!./node_modules/postcss-loader/src/index.js?!./static/css/reset.css":
/*!******************************************************************************************************************************************!*\
  !*** ./node_modules/css-loader/dist/cjs.js??ref--5-oneOf-1-1!./node_modules/postcss-loader/src??ref--5-oneOf-1-2!./static/css/reset.css ***!
  \******************************************************************************************************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

exports = module.exports = __webpack_require__(/*! ../../node_modules/css-loader/dist/runtime/api.js */ "./node_modules/css-loader/dist/runtime/api.js")(true);
// Imports
var urlEscape = __webpack_require__(/*! ../../node_modules/css-loader/dist/runtime/url-escape.js */ "./node_modules/css-loader/dist/runtime/url-escape.js");
var ___CSS_LOADER_URL___0___ = urlEscape(__webpack_require__(/*! ../font/Arial_Black.ttf */ "./static/font/Arial_Black.ttf"));
var ___CSS_LOADER_URL___1___ = urlEscape(__webpack_require__(/*! ../font/Arial_Bold.ttf */ "./static/font/Arial_Bold.ttf"));
var ___CSS_LOADER_URL___2___ = urlEscape(__webpack_require__(/*! ../font/Arial.ttf */ "./static/font/Arial.ttf"));
var ___CSS_LOADER_URL___3___ = urlEscape(__webpack_require__(/*! ../font/Arial_Bold_Italic.ttf */ "./static/font/Arial_Bold_Italic.ttf"));
var ___CSS_LOADER_URL___4___ = urlEscape(__webpack_require__(/*! ../font/Arial_Italic.ttf */ "./static/font/Arial_Italic.ttf"));
var ___CSS_LOADER_URL___5___ = urlEscape(__webpack_require__(/*! ../font/AvenirNext.ttf */ "./static/font/AvenirNext.ttf"));
var ___CSS_LOADER_URL___6___ = urlEscape(__webpack_require__(/*! ../images/slider_normal.png */ "./static/images/slider_normal.png"));

// Module
exports.push([module.i, "@font-face {\n  font-family: \"Arial-Black\";\n  src: url(" + ___CSS_LOADER_URL___0___ + ");\n  /*字体源文件*/\n}\n\n@font-face {\n  font-family: \"Arial-Bold\";\n  src: url(" + ___CSS_LOADER_URL___1___ + ");\n  /*字体源文件*/\n}\n\n@font-face {\n  font-family: \"Arial\";\n  src: url(" + ___CSS_LOADER_URL___2___ + ");\n  /*字体源文件*/\n}\n\n@font-face {\n  font-family: \"Arial-Bold-Italic\";\n  src: url(" + ___CSS_LOADER_URL___3___ + ");\n  /*字体源文件*/\n}\n\n@font-face {\n  font-family: \"Arial-Italic\";\n  src: url(" + ___CSS_LOADER_URL___4___ + ");\n  /*字体源文件*/\n}\n\n@font-face {\n  font-family: \"AvenirNext\";\n  src: url(" + ___CSS_LOADER_URL___5___ + ");\n  /*字体源文件*/\n}\n\nhtml {\n  font-size: 100px;\n  width: 100%;\n  height: 100%;\n  overflow-x: hidden;\n  -webkit-overflow-scrolling: touch;\n}\n\nbody {\n  width: 100%;\n  height: 100%;\n  overflow-x: hidden;\n  -webkit-overflow-scrolling: touch;\n}\n\n.el-dialog a {\n  text-decoration: underline;\n}\n\n.el-dialog__footer {\n  display: -webkit-box;\n  display: -ms-flexbox;\n  display: flex;\n  -webkit-box-pack: center;\n      -ms-flex-pack: center;\n          justify-content: center;\n}\n\n/* .el-button{\n  width: 1.74rem;\n  height: .4rem;\n  background-image: linear-gradient(-135deg, #B643FF 0%, #FF6C81 100%);\n  border-radius: .2rem;\n  color: #fff;\n  border:none;\n} */\n\n.el-button.how {\n  width: auto;\n  height: auto;\n  background: transparent;\n  border: none;\n  font-size: 0.16rem;\n  color: #fff;\n  -webkit-transition: opacity 0.2s ease-in-out;\n  transition: opacity 0.2s ease-in-out;\n  padding: 0;\n}\n\n.how.el-button+.el-button {\n  margin-left: 0;\n}\n\n.img-box .el-switch__core {\n  width: 0.5rem !important;\n  height: 0.24rem;\n  border-radius: 0.415rem;\n}\n\n.img-box .el-switch__core:after {\n  width: 0.24rem;\n  height: 0.24rem;\n  top: -0.01rem;\n}\n\n.img-box .el-switch.is-checked .el-switch__core::after {\n  margin-left: -0.24rem;\n}\n\n.el-message .el-icon-success,\n.el-message--success .el-message__content {\n  color: #fff;\n}\n\n.el-message--success {\n  background: rgba(0, 0, 0, 0.6);\n  border-radius: 8px;\n  border: none;\n}\n\n.how-dialog {\n  background-image: -webkit-gradient(linear, left top, left bottom, color-stop(3%, #aa57c7), to(#472f9a));\n  background-image: linear-gradient(-180deg, #aa57c7 3%, #472f9a 100%);\n  border: 1px solid #ff68fc;\n  border-radius: 0.03rem;\n  font-size: 0.15rem;\n}\n\n.how-dialog .el-dialog__close {\n  height: 38px;\n  width: 38px;\n  /*background-image: url('../images/close.png');*/\n  background-repeat: no-repeat;\n  /* display: none; */\n}\n\n/* .how-dialog .el-dialog__close:before {\n  content: \"\";\n} */\n\n.how-dialog .el-dialog__header {\n  text-align: center;\n  padding-top: 0.3rem;\n  padding-bottom: 0px;\n  font-family: PingFang-SC-Bold;\n  font-size: 0.24rem;\n}\n\n.how-dialog .el-dialog__header .el-dialog__title {\n  color: #fff16b;\n  font-size: 26px;\n}\n\n.how-dialog .lucky-tron {\n  margin: 10px auto 0;\n  display: -webkit-box;\n  display: -ms-flexbox;\n  display: flex;\n  height: 60px;\n  width: 500px;\n  background: rgba(0, 0, 0, 0.45);\n  -webkit-box-shadow: 2px 2px 8px rgba(0, 0, 0, 0.3);\n          box-shadow: 2px 2px 8px rgba(0, 0, 0, 0.3);\n  color: #fff;\n  padding: 0 30px;\n  -webkit-box-pack: justify;\n      -ms-flex-pack: justify;\n          justify-content: space-between;\n  -webkit-box-align: center;\n      -ms-flex-align: center;\n          align-items: center;\n}\n\n.how-dialog .lucky-tron:first-child {\n  margin-top: 0;\n}\n\n.how-dialog .el-dialog__body {\n  font-size: 15px;\n  color: #fff;\n  padding: 0.1rem 0.3rem;\n  -webkit-box-ordinal-group: 2;\n      -ms-flex-order: 1px solid #979797;\n          order: 1px solid #979797;\n  font-family: ArialMT;\n  font-size: 0.16rem;\n  color: #ffffff;\n  letter-spacing: 0;\n  line-height: 0.22rem;\n}\n\n.how-dialog .el-dialog__body p {\n  margin-top: 0.05rem;\n}\n\n.how-dialog button.draw {\n  position: relative;\n  width: 160px;\n  height: 60px;\n  background-image: -webkit-gradient(linear, left bottom, left top, color-stop(27%, #51b7ff), to(#9a35ff));\n  background-image: linear-gradient(0deg, #51b7ff 27%, #9a35ff 100%);\n  border-radius: 3px;\n  font-size: 30px;\n  line-height: 60px;\n  text-align: center;\n  color: #fff;\n  border: none;\n  margin: 0 auto;\n  display: block;\n  cursor: pointer;\n}\n\n.how-dialog .el-button--primary {\n  background-image: -webkit-gradient(linear, left top, left bottom, from(#dd54b5), color-stop(99%, #bc56df));\n  background-image: linear-gradient(-180deg, #dd54b5 0%, #bc56df 99%);\n  border: 0.01rem solid #542099;\n  -webkit-box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.3);\n          box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.3);\n  border-radius: 0.08rem;\n  /* height: 30px;  */\n}\n\n.el-slider {\n  width: 100%;\n}\n\n/* .el-slider__runway {\n  height: 0.18rem;\n  background-color: #df4c51;\n  border-radius: 50px;\n  position: relative;\n  z-index: 1;\n} */\n\n/* .el-slider__bar {\n  background-color: #64e1f6;\n  border-radius: 50px;\n  height: 0.18rem;\n} */\n\n.el-slider__runway {\n  height: 0.18rem;\n  /* background-color: rgba(255, 255, 255, 0); */\n  /* background-color: #df4c51;\n  border-radius: 0.5rem;\n  position: relative;\n  z-index: 1; */\n  /* border: 1px solid #b8b8b8; */\n  background: #481f94;\n  -webkit-box-shadow: inset 0 2px 8px 0 rgba(8, 8, 8, 0.5);\n          box-shadow: inset 0 2px 8px 0 rgba(8, 8, 8, 0.5);\n  border-radius: 0.415rem;\n}\n\n.el-slider__bar {\n  border-radius: 0.5rem;\n  height: 0.18rem;\n  background-image: -webkit-gradient( linear, left top, left bottom, color-stop(24%, #fff16b), color-stop(73%, #ffb253), to(#f19530));\n  background-image: linear-gradient( -180deg, #fff16b 24%, #ffb253 73%, #f19530 100%);\n  border-radius: 41.5px;\n  background-color: none;\n}\n\n.el-slider__button-wrapper {\n  height: 0.4rem;\n  width: 0.4rem;\n  /* 圣诞老人 */\n  /* background-color: #ffffff; */\n  /* box-shadow: 0px 0px 8px 0px rgba(9, 8, 58, 0.45); */\n  border-radius: 100%;\n  /* top: -0.08rem; */\n  /* background-image: url(\"../../assets/images/slide.png\"); */\n  /* top: 0rem; */\n  background-image: url(" + ___CSS_LOADER_URL___6___ + ");\n  background-size: 100% 100%;\n  top: -0.1rem;\n}\n\n.el-slider__button {\n  background-color: transparent;\n  border: none;\n}\n\n/* .el-tooltip__popper.is-is-dark1[x-placement^=\"top\"] .popper__arrow {\n  border-top-color: rgba(0, 0, 0, 0.8);\n  display: block;\n}\n.el-tooltip__popper.is-is-dark1[x-placement^=\"top\"] .popper__arrow:after {\n  border-top-color: rgba(0, 0, 0, 0.8);\n  display: block;\n}\n.vip1 {\n  background: rgba(0, 0, 0, 0.8) !important;\n  color: #fff;\n  display: block;\n} */\n\n.result .el-progress-bar__outer {\n  background: #4c2b9f;\n  border: 1px solid rgba(89, 211, 33, 0.7);\n  -webkit-box-shadow: inset 1px 1px 5px 0 rgba(0, 0, 0, 0.5);\n          box-shadow: inset 1px 1px 5px 0 rgba(0, 0, 0, 0.5);\n  border-radius: 0.05rem;\n  width: 1.68rem;\n  height: 0.07rem;\n}\n\n.result .win .el-progress-bar__inner {\n  background: #59d321;\n  border-radius: 5px;\n}\n\n.result .lose .el-progress-bar__inner {\n  background: #e52e2e;\n  border-radius: 5px;\n}\n\n/* 账号信息弹框 */\n\n.el-tooltip__popper.is-dark {\n  background: rgba(0, 0, 0, 0.8);\n}\n\n.el-tooltip__popper.is-dark .account {\n  padding: 0.15rem;\n  width: 3.69rem;\n}\n\n.el-tooltip__popper.is-dark .account .wallet {\n  font-size: 0.16rem;\n  line-height: 0.3rem;\n}\n\n.el-tooltip__popper.is-dark .account .progress {\n  margin-top: 0.1rem;\n}\n\n.el-tooltip__popper.is-dark .account .el-progress__text {\n  color: #fff;\n}\n\n.el-tooltip__popper.is-dark .account .tip {\n  line-height: 0.2rem;\n  margin-top: 0.1rem;\n  font-size: 0.16rem;\n}\n\n.el-tooltip__popper.is-dark .account .progress {\n  text-align: center;\n}\n\n.el-tooltip__popper.is-dark .account .text-con {\n  text-align: center;\n}\n\n.el-tooltip__popper.is-dark .account .text-con .text {\n  width: 1.26rem;\n  height: 0.3rem;\n  display: inline-block;\n  line-height: 0.3rem;\n  font-size: 0.16rem;\n}\n\n.el-tooltip__popper.is-dark .account .el-progress.el-progress--circle:first-child {\n  margin-right: 0.3rem;\n}\n\n/* 自动投注 */\n\n.el-tooltip__popper.is-dark .autoBet {\n  padding: 0.1rem;\n  line-height: 0.2rem;\n  font-size: 13px;\n}\n\n.el-tooltip__popper.is-dark .autoBet .tronPay {\n  color: #ffb253;\n}\n\n.el-tooltip__popper.is-dark .mining {\n  line-height: 0.2rem;\n}\n\n.el-tooltip__popper.is-dark .mining table td {\n  text-align: center;\n  border: 1px solid rgba(255, 255, 255, 0.3);\n  margin-bottom: 0.02rem;\n}\n\n.el-tooltip__popper.is-dark .mining table tr td:first-child {\n  width: 0.8rem;\n}\n\n.el-tooltip__popper.is-dark .mining table tr td:nth-child(2) {\n  width: 0.8rem;\n}\n\n.el-tooltip__popper.is-dark .mining table tr td:last-child {\n  width: 1.5rem;\n}\n\n.el-popper[x-placement^=\"bottom\"] .popper__arrow {\n  border-bottom-color: rgba(0, 0, 0, 0.5);\n}\n\n/* .el-tooltip__popper.is-dark[x-placement^=\"right\"] .popper__arrow {\n  background: rgba(0, 0, 0, 0.7);\n  display: block;\n}\n.el-tooltip__popper.is-dark[x-placement^=\"right\"] .popper__arrow:after {\n  background: rgba(0, 0, 0, 0.7);\n  display: block;\n} */\n\n.is-dark {\n  background: rgba(0, 0, 0, 0.8);\n}\n\n.el-tooltip__popper.is-dark .recommend {\n  width: 4rem;\n}\n\n.account .el-progress-circle {\n  width: 100px !important;\n  height: 100px !important;\n}\n\n.el-dropdown-menu {\n  background: rgba(0, 0, 0, 0.8);\n  border: none;\n}\n\n.el-dropdown-menu__item {\n  color: #fff;\n}\n\n.el-dropdown-menu__item:hover {\n  background: rgba(255, 255, 255, 0.3) !important;\n  color: #fff !important;\n}\n\n.el-popper[x-placement^=\"bottom\"] .popper__arrow::after {\n  border-bottom-color: rgba(0, 0, 0, 0.8);\n}\n\n@media screen and (max-width: 1024px) {\n  .el-slider__button-wrapper::before {\n    font-size: 12px !important;\n  }\n  .how-dialog .el-dialog__body {\n    line-height: 0.35rem;\n    font-size: 12px;\n  }\n  .el-slider__button-wrapper {\n    /* 圣诞注释 */\n    /* height: 25px; */\n    /* width: 25px; */\n    /* top: -8px; */\n  }\n  .el-slider__bar,\n  .el-slider__runway {\n    height: 0.2rem;\n  }\n}\n\n@media (min-width: 320px) {\n  html {\n    font-size: 42.6667px;\n  }\n}\n\n@media (min-width: 360px) {\n  html {\n    font-size: 48px;\n  }\n}\n\n@media (min-width: 375px) {\n  html {\n    font-size: 50px;\n  }\n}\n\n@media (min-width: 384px) {\n  html {\n    font-size: 51.2px;\n  }\n}\n\n@media (min-width: 414px) {\n  html {\n    font-size: 55.2px;\n  }\n}\n\n@media (min-width: 448px) {\n  html {\n    font-size: 59.7333px;\n  }\n}\n\n@media (min-width: 480px) {\n  html {\n    font-size: 48px;\n  }\n}\n\n@media (min-width: 512px) {\n  html {\n    font-size: 68.2667px;\n  }\n}\n\n@media (min-width: 544px) {\n  html {\n    font-size: 72.5333px;\n  }\n}\n\n@media (min-width: 576px) {\n  html {\n    font-size: 76.8px;\n  }\n}\n\n@media (min-width: 608px) {\n  html {\n    font-size: 81.0667px;\n  }\n}\n\n@media (min-width: 640px) {\n  html {\n    font-size: 85.3333px;\n  }\n}\n\n@media (min-width: 750px) {\n  html {\n    font-size: 100px;\n  }\n}\n\n.custom-dg .el-form-item__label {\n  color: #fff16b;\n}\n\n.custom-dg .dialog-footer .el-button--default {\n    border: 0.01rem solid #542099;\n    -webkit-box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.3);\n            box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.3);\n    border-radius: 0.08rem;\n}", "",{"version":3,"sources":["reset.css"],"names":[],"mappings":"AAAA;EACE,0BAA0B;EAC1B,kCAAmC;EACnC,QAAQ;AACV;;AAEA;EACE,yBAAyB;EACzB,kCAAkC;EAClC,QAAQ;AACV;;AAEA;EACE,oBAAoB;EACpB,kCAA6B;EAC7B,QAAQ;AACV;;AAEA;EACE,gCAAgC;EAChC,kCAAyC;EACzC,QAAQ;AACV;;AAEA;EACE,2BAA2B;EAC3B,kCAAoC;EACpC,QAAQ;AACV;;AAEA;EACE,yBAAyB;EACzB,kCAAkC;EAClC,QAAQ;AACV;;AAEA;EACE,gBAAgB;EAChB,WAAW;EACX,YAAY;EACZ,kBAAkB;EAClB,iCAAiC;AACnC;;AAEA;EACE,WAAW;EACX,YAAY;EACZ,kBAAkB;EAClB,iCAAiC;AACnC;;AAEA;EACE,0BAA0B;AAC5B;;AAEA;EACE,oBAAa;EAAb,oBAAa;EAAb,aAAa;EACb,wBAAuB;MAAvB,qBAAuB;UAAvB,uBAAuB;AACzB;;AAEA;;;;;;;GAOG;;AAEH;EACE,WAAW;EACX,YAAY;EACZ,uBAAuB;EACvB,YAAY;EACZ,kBAAkB;EAClB,WAAW;EACX,4CAAoC;EAApC,oCAAoC;EACpC,UAAU;AACZ;;AAEA;EACE,cAAc;AAChB;;AAEA;EACE,wBAAwB;EACxB,eAAe;EACf,uBAAuB;AACzB;;AAEA;EACE,cAAc;EACd,eAAe;EACf,aAAa;AACf;;AAEA;EACE,qBAAqB;AACvB;;AAEA;;EAEE,WAAW;AACb;;AAEA;EACE,8BAA8B;EAC9B,kBAAkB;EAClB,YAAY;AACd;;AAEA;EACE,uGAAoE;EAApE,oEAAoE;EACpE,yBAAyB;EACzB,sBAAsB;EACtB,kBAAkB;AACpB;;AAEA;EACE,YAAY;EACZ,WAAW;EACX,gDAAgD;EAChD,4BAA4B;EAC5B,mBAAmB;AACrB;;AAEA;;GAEG;;AAEH;EACE,kBAAkB;EAClB,mBAAmB;EACnB,mBAAmB;EACnB,6BAA6B;EAC7B,kBAAkB;AACpB;;AAEA;EACE,cAAc;EACd,eAAe;AACjB;;AAEA;EACE,mBAAmB;EACnB,oBAAa;EAAb,oBAAa;EAAb,aAAa;EACb,YAAY;EACZ,YAAY;EACZ,+BAA+B;EAC/B,kDAA0C;UAA1C,0CAA0C;EAC1C,WAAW;EACX,eAAe;EACf,yBAA8B;MAA9B,sBAA8B;UAA9B,8BAA8B;EAC9B,yBAAmB;MAAnB,sBAAmB;UAAnB,mBAAmB;AACrB;;AAEA;EACE,aAAa;AACf;;AAEA;EACE,eAAe;EACf,WAAW;EACX,sBAAsB;EACtB,4BAAwB;MAAxB,iCAAwB;UAAxB,wBAAwB;EACxB,oBAAoB;EACpB,kBAAkB;EAClB,cAAc;EACd,iBAAiB;EACjB,oBAAoB;AACtB;;AAEA;EACE,mBAAmB;AACrB;;AAEA;EACE,kBAAkB;EAClB,YAAY;EACZ,YAAY;EACZ,wGAAkE;EAAlE,kEAAkE;EAClE,kBAAkB;EAClB,eAAe;EACf,iBAAiB;EACjB,kBAAkB;EAClB,WAAW;EACX,YAAY;EACZ,cAAc;EACd,cAAc;EACd,eAAe;AACjB;;AAEA;EACE,0GAAmE;EAAnE,mEAAmE;EACnE,6BAA6B;EAC7B,kDAA0C;UAA1C,0CAA0C;EAC1C,sBAAsB;EACtB,mBAAmB;AACrB;;AAEA;EACE,WAAW;AACb;;AAEA;;;;;;GAMG;;AAEH;;;;GAIG;;AAEH;EACE,eAAe;EACf,8CAA8C;EAC9C;;;eAGa;EACb,+BAA+B;EAC/B,mBAAmB;EACnB,wDAAgD;UAAhD,gDAAgD;EAChD,uBAAuB;AACzB;;AAEA;EACE,qBAAqB;EACrB,eAAe;EACf,mIAAmF;EAAnF,mFAAmF;EACnF,qBAAqB;EACrB,sBAAsB;AACxB;;AAEA;EACE,cAAc;EACd,aAAa;EACb,SAAS;EACT,+BAA+B;EAC/B,sDAAsD;EACtD,mBAAmB;EACnB,mBAAmB;EACnB,4DAA4D;EAC5D,eAAe;EACf,+CAA8D;EAC9D,0BAA0B;EAC1B,YAAY;AACd;;AAEA;EACE,6BAA6B;EAC7B,YAAY;AACd;;AAEA;;;;;;;;;;;;GAYG;;AAEH;EACE,mBAAmB;EACnB,wCAAwC;EACxC,0DAAkD;UAAlD,kDAAkD;EAClD,sBAAsB;EACtB,cAAc;EACd,eAAe;AACjB;;AAEA;EACE,mBAAmB;EACnB,kBAAkB;AACpB;;AAEA;EACE,mBAAmB;EACnB,kBAAkB;AACpB;;AAEA,WAAW;;AAEX;EACE,8BAA8B;AAChC;;AAEA;EACE,gBAAgB;EAChB,cAAc;AAChB;;AAEA;EACE,kBAAkB;EAClB,mBAAmB;AACrB;;AAEA;EACE,kBAAkB;AACpB;;AAEA;EACE,WAAW;AACb;;AAEA;EACE,mBAAmB;EACnB,kBAAkB;EAClB,kBAAkB;AACpB;;AAEA;EACE,kBAAkB;AACpB;;AAEA;EACE,kBAAkB;AACpB;;AAEA;EACE,cAAc;EACd,cAAc;EACd,qBAAqB;EACrB,mBAAmB;EACnB,kBAAkB;AACpB;;AAEA;EACE,oBAAoB;AACtB;;AAEA,SAAS;;AAET;EACE,eAAe;EACf,mBAAmB;EACnB,eAAe;AACjB;;AAEA;EACE,cAAc;AAChB;;AAEA;EACE,mBAAmB;AACrB;;AAEA;EACE,kBAAkB;EAClB,0CAA0C;EAC1C,sBAAsB;AACxB;;AAEA;EACE,aAAa;AACf;;AAEA;EACE,aAAa;AACf;;AAEA;EACE,aAAa;AACf;;AAEA;EACE,uCAAuC;AACzC;;AAEA;;;;;;;GAOG;;AAEH;EACE,8BAA8B;AAChC;;AAEA;EACE,WAAW;AACb;;AAEA;EACE,uBAAuB;EACvB,wBAAwB;AAC1B;;AAEA;EACE,8BAA8B;EAC9B,YAAY;AACd;;AAEA;EACE,WAAW;AACb;;AAEA;EACE,+CAA+C;EAC/C,sBAAsB;AACxB;;AAEA;EACE,uCAAuC;AACzC;;AAEA;EACE;IACE,0BAA0B;EAC5B;EACA;IACE,oBAAoB;IACpB,eAAe;EACjB;EACA;IACE,SAAS;IACT,kBAAkB;IAClB,iBAAiB;IACjB,eAAe;EACjB;EACA;;IAEE,cAAc;EAChB;AACF;;AAEA;EACE;IACE,oBAAoB;EACtB;AACF;;AAEA;EACE;IACE,eAAe;EACjB;AACF;;AAEA;EACE;IACE,eAAe;EACjB;AACF;;AAEA;EACE;IACE,iBAAiB;EACnB;AACF;;AAEA;EACE;IACE,iBAAiB;EACnB;AACF;;AAEA;EACE;IACE,oBAAoB;EACtB;AACF;;AAEA;EACE;IACE,eAAe;EACjB;AACF;;AAEA;EACE;IACE,oBAAoB;EACtB;AACF;;AAEA;EACE;IACE,oBAAoB;EACtB;AACF;;AAEA;EACE;IACE,iBAAiB;EACnB;AACF;;AAEA;EACE;IACE,oBAAoB;EACtB;AACF;;AAEA;EACE;IACE,oBAAoB;EACtB;AACF;;AAEA;EACE;IACE,gBAAgB;EAClB;AACF;;AAEA;EACE,cAAc;AAChB;;AAEA;IACI,6BAA6B;IAC7B,kDAA0C;YAA1C,0CAA0C;IAC1C,sBAAsB;AAC1B","file":"reset.css","sourcesContent":["@font-face {\n  font-family: \"Arial-Black\";\n  src: url(\"../font/Arial_Black.ttf\");\n  /*字体源文件*/\n}\n\n@font-face {\n  font-family: \"Arial-Bold\";\n  src: url(\"../font/Arial_Bold.ttf\");\n  /*字体源文件*/\n}\n\n@font-face {\n  font-family: \"Arial\";\n  src: url(\"../font/Arial.ttf\");\n  /*字体源文件*/\n}\n\n@font-face {\n  font-family: \"Arial-Bold-Italic\";\n  src: url(\"../font/Arial_Bold_Italic.ttf\");\n  /*字体源文件*/\n}\n\n@font-face {\n  font-family: \"Arial-Italic\";\n  src: url(\"../font/Arial_Italic.ttf\");\n  /*字体源文件*/\n}\n\n@font-face {\n  font-family: \"AvenirNext\";\n  src: url(\"../font/AvenirNext.ttf\");\n  /*字体源文件*/\n}\n\nhtml {\n  font-size: 100px;\n  width: 100%;\n  height: 100%;\n  overflow-x: hidden;\n  -webkit-overflow-scrolling: touch;\n}\n\nbody {\n  width: 100%;\n  height: 100%;\n  overflow-x: hidden;\n  -webkit-overflow-scrolling: touch;\n}\n\n.el-dialog a {\n  text-decoration: underline;\n}\n\n.el-dialog__footer {\n  display: flex;\n  justify-content: center;\n}\n\n/* .el-button{\n  width: 1.74rem;\n  height: .4rem;\n  background-image: linear-gradient(-135deg, #B643FF 0%, #FF6C81 100%);\n  border-radius: .2rem;\n  color: #fff;\n  border:none;\n} */\n\n.el-button.how {\n  width: auto;\n  height: auto;\n  background: transparent;\n  border: none;\n  font-size: 0.16rem;\n  color: #fff;\n  transition: opacity 0.2s ease-in-out;\n  padding: 0;\n}\n\n.how.el-button+.el-button {\n  margin-left: 0;\n}\n\n.img-box .el-switch__core {\n  width: 0.5rem !important;\n  height: 0.24rem;\n  border-radius: 0.415rem;\n}\n\n.img-box .el-switch__core:after {\n  width: 0.24rem;\n  height: 0.24rem;\n  top: -0.01rem;\n}\n\n.img-box .el-switch.is-checked .el-switch__core::after {\n  margin-left: -0.24rem;\n}\n\n.el-message .el-icon-success,\n.el-message--success .el-message__content {\n  color: #fff;\n}\n\n.el-message--success {\n  background: rgba(0, 0, 0, 0.6);\n  border-radius: 8px;\n  border: none;\n}\n\n.how-dialog {\n  background-image: linear-gradient(-180deg, #aa57c7 3%, #472f9a 100%);\n  border: 1px solid #ff68fc;\n  border-radius: 0.03rem;\n  font-size: 0.15rem;\n}\n\n.how-dialog .el-dialog__close {\n  height: 38px;\n  width: 38px;\n  /*background-image: url('../images/close.png');*/\n  background-repeat: no-repeat;\n  /* display: none; */\n}\n\n/* .how-dialog .el-dialog__close:before {\n  content: \"\";\n} */\n\n.how-dialog .el-dialog__header {\n  text-align: center;\n  padding-top: 0.3rem;\n  padding-bottom: 0px;\n  font-family: PingFang-SC-Bold;\n  font-size: 0.24rem;\n}\n\n.how-dialog .el-dialog__header .el-dialog__title {\n  color: #fff16b;\n  font-size: 26px;\n}\n\n.how-dialog .lucky-tron {\n  margin: 10px auto 0;\n  display: flex;\n  height: 60px;\n  width: 500px;\n  background: rgba(0, 0, 0, 0.45);\n  box-shadow: 2px 2px 8px rgba(0, 0, 0, 0.3);\n  color: #fff;\n  padding: 0 30px;\n  justify-content: space-between;\n  align-items: center;\n}\n\n.how-dialog .lucky-tron:first-child {\n  margin-top: 0;\n}\n\n.how-dialog .el-dialog__body {\n  font-size: 15px;\n  color: #fff;\n  padding: 0.1rem 0.3rem;\n  order: 1px solid #979797;\n  font-family: ArialMT;\n  font-size: 0.16rem;\n  color: #ffffff;\n  letter-spacing: 0;\n  line-height: 0.22rem;\n}\n\n.how-dialog .el-dialog__body p {\n  margin-top: 0.05rem;\n}\n\n.how-dialog button.draw {\n  position: relative;\n  width: 160px;\n  height: 60px;\n  background-image: linear-gradient(0deg, #51b7ff 27%, #9a35ff 100%);\n  border-radius: 3px;\n  font-size: 30px;\n  line-height: 60px;\n  text-align: center;\n  color: #fff;\n  border: none;\n  margin: 0 auto;\n  display: block;\n  cursor: pointer;\n}\n\n.how-dialog .el-button--primary {\n  background-image: linear-gradient(-180deg, #dd54b5 0%, #bc56df 99%);\n  border: 0.01rem solid #542099;\n  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.3);\n  border-radius: 0.08rem;\n  /* height: 30px;  */\n}\n\n.el-slider {\n  width: 100%;\n}\n\n/* .el-slider__runway {\n  height: 0.18rem;\n  background-color: #df4c51;\n  border-radius: 50px;\n  position: relative;\n  z-index: 1;\n} */\n\n/* .el-slider__bar {\n  background-color: #64e1f6;\n  border-radius: 50px;\n  height: 0.18rem;\n} */\n\n.el-slider__runway {\n  height: 0.18rem;\n  /* background-color: rgba(255, 255, 255, 0); */\n  /* background-color: #df4c51;\n  border-radius: 0.5rem;\n  position: relative;\n  z-index: 1; */\n  /* border: 1px solid #b8b8b8; */\n  background: #481f94;\n  box-shadow: inset 0 2px 8px 0 rgba(8, 8, 8, 0.5);\n  border-radius: 0.415rem;\n}\n\n.el-slider__bar {\n  border-radius: 0.5rem;\n  height: 0.18rem;\n  background-image: linear-gradient( -180deg, #fff16b 24%, #ffb253 73%, #f19530 100%);\n  border-radius: 41.5px;\n  background-color: none;\n}\n\n.el-slider__button-wrapper {\n  height: 0.4rem;\n  width: 0.4rem;\n  /* 圣诞老人 */\n  /* background-color: #ffffff; */\n  /* box-shadow: 0px 0px 8px 0px rgba(9, 8, 58, 0.45); */\n  border-radius: 100%;\n  /* top: -0.08rem; */\n  /* background-image: url(\"../../assets/images/slide.png\"); */\n  /* top: 0rem; */\n  background-image: url(\"../../static/images/slider_normal.png\");\n  background-size: 100% 100%;\n  top: -0.1rem;\n}\n\n.el-slider__button {\n  background-color: transparent;\n  border: none;\n}\n\n/* .el-tooltip__popper.is-is-dark1[x-placement^=\"top\"] .popper__arrow {\n  border-top-color: rgba(0, 0, 0, 0.8);\n  display: block;\n}\n.el-tooltip__popper.is-is-dark1[x-placement^=\"top\"] .popper__arrow:after {\n  border-top-color: rgba(0, 0, 0, 0.8);\n  display: block;\n}\n.vip1 {\n  background: rgba(0, 0, 0, 0.8) !important;\n  color: #fff;\n  display: block;\n} */\n\n.result .el-progress-bar__outer {\n  background: #4c2b9f;\n  border: 1px solid rgba(89, 211, 33, 0.7);\n  box-shadow: inset 1px 1px 5px 0 rgba(0, 0, 0, 0.5);\n  border-radius: 0.05rem;\n  width: 1.68rem;\n  height: 0.07rem;\n}\n\n.result .win .el-progress-bar__inner {\n  background: #59d321;\n  border-radius: 5px;\n}\n\n.result .lose .el-progress-bar__inner {\n  background: #e52e2e;\n  border-radius: 5px;\n}\n\n/* 账号信息弹框 */\n\n.el-tooltip__popper.is-dark {\n  background: rgba(0, 0, 0, 0.8);\n}\n\n.el-tooltip__popper.is-dark .account {\n  padding: 0.15rem;\n  width: 3.69rem;\n}\n\n.el-tooltip__popper.is-dark .account .wallet {\n  font-size: 0.16rem;\n  line-height: 0.3rem;\n}\n\n.el-tooltip__popper.is-dark .account .progress {\n  margin-top: 0.1rem;\n}\n\n.el-tooltip__popper.is-dark .account .el-progress__text {\n  color: #fff;\n}\n\n.el-tooltip__popper.is-dark .account .tip {\n  line-height: 0.2rem;\n  margin-top: 0.1rem;\n  font-size: 0.16rem;\n}\n\n.el-tooltip__popper.is-dark .account .progress {\n  text-align: center;\n}\n\n.el-tooltip__popper.is-dark .account .text-con {\n  text-align: center;\n}\n\n.el-tooltip__popper.is-dark .account .text-con .text {\n  width: 1.26rem;\n  height: 0.3rem;\n  display: inline-block;\n  line-height: 0.3rem;\n  font-size: 0.16rem;\n}\n\n.el-tooltip__popper.is-dark .account .el-progress.el-progress--circle:first-child {\n  margin-right: 0.3rem;\n}\n\n/* 自动投注 */\n\n.el-tooltip__popper.is-dark .autoBet {\n  padding: 0.1rem;\n  line-height: 0.2rem;\n  font-size: 13px;\n}\n\n.el-tooltip__popper.is-dark .autoBet .tronPay {\n  color: #ffb253;\n}\n\n.el-tooltip__popper.is-dark .mining {\n  line-height: 0.2rem;\n}\n\n.el-tooltip__popper.is-dark .mining table td {\n  text-align: center;\n  border: 1px solid rgba(255, 255, 255, 0.3);\n  margin-bottom: 0.02rem;\n}\n\n.el-tooltip__popper.is-dark .mining table tr td:first-child {\n  width: 0.8rem;\n}\n\n.el-tooltip__popper.is-dark .mining table tr td:nth-child(2) {\n  width: 0.8rem;\n}\n\n.el-tooltip__popper.is-dark .mining table tr td:last-child {\n  width: 1.5rem;\n}\n\n.el-popper[x-placement^=\"bottom\"] .popper__arrow {\n  border-bottom-color: rgba(0, 0, 0, 0.5);\n}\n\n/* .el-tooltip__popper.is-dark[x-placement^=\"right\"] .popper__arrow {\n  background: rgba(0, 0, 0, 0.7);\n  display: block;\n}\n.el-tooltip__popper.is-dark[x-placement^=\"right\"] .popper__arrow:after {\n  background: rgba(0, 0, 0, 0.7);\n  display: block;\n} */\n\n.is-dark {\n  background: rgba(0, 0, 0, 0.8);\n}\n\n.el-tooltip__popper.is-dark .recommend {\n  width: 4rem;\n}\n\n.account .el-progress-circle {\n  width: 100px !important;\n  height: 100px !important;\n}\n\n.el-dropdown-menu {\n  background: rgba(0, 0, 0, 0.8);\n  border: none;\n}\n\n.el-dropdown-menu__item {\n  color: #fff;\n}\n\n.el-dropdown-menu__item:hover {\n  background: rgba(255, 255, 255, 0.3) !important;\n  color: #fff !important;\n}\n\n.el-popper[x-placement^=\"bottom\"] .popper__arrow::after {\n  border-bottom-color: rgba(0, 0, 0, 0.8);\n}\n\n@media screen and (max-width: 1024px) {\n  .el-slider__button-wrapper::before {\n    font-size: 12px !important;\n  }\n  .how-dialog .el-dialog__body {\n    line-height: 0.35rem;\n    font-size: 12px;\n  }\n  .el-slider__button-wrapper {\n    /* 圣诞注释 */\n    /* height: 25px; */\n    /* width: 25px; */\n    /* top: -8px; */\n  }\n  .el-slider__bar,\n  .el-slider__runway {\n    height: 0.2rem;\n  }\n}\n\n@media (min-width: 320px) {\n  html {\n    font-size: 42.6667px;\n  }\n}\n\n@media (min-width: 360px) {\n  html {\n    font-size: 48px;\n  }\n}\n\n@media (min-width: 375px) {\n  html {\n    font-size: 50px;\n  }\n}\n\n@media (min-width: 384px) {\n  html {\n    font-size: 51.2px;\n  }\n}\n\n@media (min-width: 414px) {\n  html {\n    font-size: 55.2px;\n  }\n}\n\n@media (min-width: 448px) {\n  html {\n    font-size: 59.7333px;\n  }\n}\n\n@media (min-width: 480px) {\n  html {\n    font-size: 48px;\n  }\n}\n\n@media (min-width: 512px) {\n  html {\n    font-size: 68.2667px;\n  }\n}\n\n@media (min-width: 544px) {\n  html {\n    font-size: 72.5333px;\n  }\n}\n\n@media (min-width: 576px) {\n  html {\n    font-size: 76.8px;\n  }\n}\n\n@media (min-width: 608px) {\n  html {\n    font-size: 81.0667px;\n  }\n}\n\n@media (min-width: 640px) {\n  html {\n    font-size: 85.3333px;\n  }\n}\n\n@media (min-width: 750px) {\n  html {\n    font-size: 100px;\n  }\n}\n\n.custom-dg .el-form-item__label {\n  color: #fff16b;\n}\n\n.custom-dg .dialog-footer .el-button--default {\n    border: 0.01rem solid #542099;\n    box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.3);\n    border-radius: 0.08rem;\n}"]}]);



/***/ }),

/***/ "./node_modules/css-loader/dist/cjs.js?!./node_modules/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/postcss-loader/src/index.js?!./node_modules/vue-loader/lib/index.js?!./.nuxt/components/nuxt-build-indicator.vue?vue&type=style&index=0&id=71e9e103&scoped=true&lang=css&":
/*!***********************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
  !*** ./node_modules/css-loader/dist/cjs.js??ref--5-oneOf-1-1!./node_modules/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/postcss-loader/src??ref--5-oneOf-1-2!./node_modules/vue-loader/lib??vue-loader-options!./.nuxt/components/nuxt-build-indicator.vue?vue&type=style&index=0&id=71e9e103&scoped=true&lang=css& ***!
  \***********************************************************************************************************************************************************************************************************************************************************************************************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

exports = module.exports = __webpack_require__(/*! ../../node_modules/css-loader/dist/runtime/api.js */ "./node_modules/css-loader/dist/runtime/api.js")(true);
// Module
exports.push([module.i, "\n.nuxt__build_indicator[data-v-71e9e103] {\n  -webkit-box-sizing: border-box;\n          box-sizing: border-box;\n  position: absolute;\n  font-family: monospace;\n  bottom: 20px;\n  right: 20px;\n  background-color: #2E495E;\n  padding: 5px 10px;\n  border-radius: 5px;\n  -webkit-box-shadow: 1px 1px 2px 0px rgba(0,0,0,0.2);\n          box-shadow: 1px 1px 2px 0px rgba(0,0,0,0.2);\n  color: #00C48D;\n  width: 84px;\n  z-index: 2147483647;\n}\n.v-enter-active[data-v-71e9e103], .v-leave-active[data-v-71e9e103] {\n  -webkit-transition-delay: 0.2s;\n          transition-delay: 0.2s;\n  -webkit-transition-property: all;\n  transition-property: all;\n  -webkit-transition-duration: 0.3s;\n          transition-duration: 0.3s;\n}\n.v-leave-to[data-v-71e9e103] {\n  opacity: 0;\n  -webkit-transform: translateY(20px);\n          transform: translateY(20px);\n}\nsvg[data-v-71e9e103] {\n  width: 1.1em;\n  position: relative;\n  top: 1px;\n}\n", "",{"version":3,"sources":["/Users/tron/work/testpage/src/sun-network/demos/PetFront/.nuxt/components/nuxt-build-indicator.vue"],"names":[],"mappings":";AA8HA;EACE,8BAAsB;UAAtB,sBAAsB;EACtB,kBAAkB;EAClB,sBAAsB;EACtB,YAAY;EACZ,WAAW;EACX,yBAAyB;EACzB,iBAAiB;EACjB,kBAAkB;EAClB,mDAA2C;UAA3C,2CAA2C;EAC3C,cAAc;EACd,WAAW;EACX,mBAAmB;AACrB;AACA;EACE,8BAAsB;UAAtB,sBAAsB;EACtB,gCAAwB;EAAxB,wBAAwB;EACxB,iCAAyB;UAAzB,yBAAyB;AAC3B;AACA;EACE,UAAU;EACV,mCAA2B;UAA3B,2BAA2B;AAC7B;AACA;EACE,YAAY;EACZ,kBAAkB;EAClB,QAAQ;AACV","file":"nuxt-build-indicator.vue?vue&type=style&index=0&id=71e9e103&scoped=true&lang=css&","sourcesContent":["\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n.nuxt__build_indicator {\n  box-sizing: border-box;\n  position: absolute;\n  font-family: monospace;\n  bottom: 20px;\n  right: 20px;\n  background-color: #2E495E;\n  padding: 5px 10px;\n  border-radius: 5px;\n  box-shadow: 1px 1px 2px 0px rgba(0,0,0,0.2);\n  color: #00C48D;\n  width: 84px;\n  z-index: 2147483647;\n}\n.v-enter-active, .v-leave-active {\n  transition-delay: 0.2s;\n  transition-property: all;\n  transition-duration: 0.3s;\n}\n.v-leave-to {\n  opacity: 0;\n  transform: translateY(20px);\n}\nsvg {\n  width: 1.1em;\n  position: relative;\n  top: 1px;\n}\n"]}]);



/***/ }),

/***/ "./node_modules/css-loader/dist/cjs.js?!./node_modules/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/postcss-loader/src/index.js?!./node_modules/vue-loader/lib/index.js?!./.nuxt/components/nuxt-error.vue?vue&type=style&index=0&lang=css&":
/*!*************************************************************************************************************************************************************************************************************************************************************************************************!*\
  !*** ./node_modules/css-loader/dist/cjs.js??ref--5-oneOf-1-1!./node_modules/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/postcss-loader/src??ref--5-oneOf-1-2!./node_modules/vue-loader/lib??vue-loader-options!./.nuxt/components/nuxt-error.vue?vue&type=style&index=0&lang=css& ***!
  \*************************************************************************************************************************************************************************************************************************************************************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

exports = module.exports = __webpack_require__(/*! ../../node_modules/css-loader/dist/runtime/api.js */ "./node_modules/css-loader/dist/runtime/api.js")(true);
// Module
exports.push([module.i, "\n.__nuxt-error-page {\n  padding: 1rem;\n  background: #F7F8FB;\n  color: #47494E;\n  text-align: center;\n  display: -webkit-box;\n  display: -ms-flexbox;\n  display: flex;\n  -webkit-box-pack: center;\n      -ms-flex-pack: center;\n          justify-content: center;\n  -webkit-box-align: center;\n      -ms-flex-align: center;\n          align-items: center;\n  -webkit-box-orient: vertical;\n  -webkit-box-direction: normal;\n      -ms-flex-direction: column;\n          flex-direction: column;\n  font-family: sans-serif;\n  font-weight: 100 !important;\n  -ms-text-size-adjust: 100%;\n  -webkit-text-size-adjust: 100%;\n  -webkit-font-smoothing: antialiased;\n  position: absolute;\n  top: 0;\n  left: 0;\n  right: 0;\n  bottom: 0;\n}\n.__nuxt-error-page .error {\n  max-width: 450px;\n}\n.__nuxt-error-page .title {\n  font-size: 1.5rem;\n  margin-top: 15px;\n  color: #47494E;\n  margin-bottom: 8px;\n}\n.__nuxt-error-page .description {\n  color: #7F828B;\n  line-height: 21px;\n  margin-bottom: 10px;\n}\n.__nuxt-error-page a {\n  color: #7F828B !important;\n  text-decoration: none;\n}\n.__nuxt-error-page .logo {\n  position: fixed;\n  left: 12px;\n  bottom: 12px;\n}\n", "",{"version":3,"sources":["/Users/tron/work/testpage/src/sun-network/demos/PetFront/.nuxt/components/nuxt-error.vue"],"names":[],"mappings":";AAqDA;EACE,aAAa;EACb,mBAAmB;EACnB,cAAc;EACd,kBAAkB;EAClB,oBAAa;EAAb,oBAAa;EAAb,aAAa;EACb,wBAAuB;MAAvB,qBAAuB;UAAvB,uBAAuB;EACvB,yBAAmB;MAAnB,sBAAmB;UAAnB,mBAAmB;EACnB,4BAAsB;EAAtB,6BAAsB;MAAtB,0BAAsB;UAAtB,sBAAsB;EACtB,uBAAuB;EACvB,2BAA2B;EAC3B,0BAA0B;EAC1B,8BAA8B;EAC9B,mCAAmC;EACnC,kBAAkB;EAClB,MAAM;EACN,OAAO;EACP,QAAQ;EACR,SAAS;AACX;AACA;EACE,gBAAgB;AAClB;AACA;EACE,iBAAiB;EACjB,gBAAgB;EAChB,cAAc;EACd,kBAAkB;AACpB;AACA;EACE,cAAc;EACd,iBAAiB;EACjB,mBAAmB;AACrB;AACA;EACE,yBAAyB;EACzB,qBAAqB;AACvB;AACA;EACE,eAAe;EACf,UAAU;EACV,YAAY;AACd","file":"nuxt-error.vue?vue&type=style&index=0&lang=css&","sourcesContent":["\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n.__nuxt-error-page {\n  padding: 1rem;\n  background: #F7F8FB;\n  color: #47494E;\n  text-align: center;\n  display: flex;\n  justify-content: center;\n  align-items: center;\n  flex-direction: column;\n  font-family: sans-serif;\n  font-weight: 100 !important;\n  -ms-text-size-adjust: 100%;\n  -webkit-text-size-adjust: 100%;\n  -webkit-font-smoothing: antialiased;\n  position: absolute;\n  top: 0;\n  left: 0;\n  right: 0;\n  bottom: 0;\n}\n.__nuxt-error-page .error {\n  max-width: 450px;\n}\n.__nuxt-error-page .title {\n  font-size: 1.5rem;\n  margin-top: 15px;\n  color: #47494E;\n  margin-bottom: 8px;\n}\n.__nuxt-error-page .description {\n  color: #7F828B;\n  line-height: 21px;\n  margin-bottom: 10px;\n}\n.__nuxt-error-page a {\n  color: #7F828B !important;\n  text-decoration: none;\n}\n.__nuxt-error-page .logo {\n  position: fixed;\n  left: 12px;\n  bottom: 12px;\n}\n"]}]);



/***/ }),

/***/ "./node_modules/css-loader/dist/cjs.js?!./node_modules/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/postcss-loader/src/index.js?!./node_modules/vue-loader/lib/index.js?!./layouts/default.vue?vue&type=style&index=0&lang=css&":
/*!*************************************************************************************************************************************************************************************************************************************************************************************!*\
  !*** ./node_modules/css-loader/dist/cjs.js??ref--5-oneOf-1-1!./node_modules/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/postcss-loader/src??ref--5-oneOf-1-2!./node_modules/vue-loader/lib??vue-loader-options!./layouts/default.vue?vue&type=style&index=0&lang=css& ***!
  \*************************************************************************************************************************************************************************************************************************************************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

exports = module.exports = __webpack_require__(/*! ../node_modules/css-loader/dist/runtime/api.js */ "./node_modules/css-loader/dist/runtime/api.js")(true);
// Module
exports.push([module.i, "\nhtml {\n  font-family: 'Source Sans Pro', -apple-system, BlinkMacSystemFont, 'Segoe UI',\n    Roboto, 'Helvetica Neue', Arial, sans-serif;\n  /* font-size: 16px; */\n  word-spacing: 1px;\n  -ms-text-size-adjust: 100%;\n  -webkit-text-size-adjust: 100%;\n  -moz-osx-font-smoothing: grayscale;\n  -webkit-font-smoothing: antialiased;\n  -webkit-box-sizing: border-box;\n          box-sizing: border-box;\n}\n*,\n*:before,\n*:after {\n  -webkit-box-sizing: border-box;\n          box-sizing: border-box;\n  margin: 0;\n}\n.button--green {\n  display: inline-block;\n  border-radius: 4px;\n  border: 1px solid #3b8070;\n  color: #3b8070;\n  text-decoration: none;\n  padding: 10px 30px;\n}\n.button--green:hover {\n  color: #fff;\n  background-color: #3b8070;\n}\n.button--grey {\n  display: inline-block;\n  border-radius: 4px;\n  border: 1px solid #35495e;\n  color: #35495e;\n  text-decoration: none;\n  padding: 10px 30px;\n  margin-left: 15px;\n}\n.button--grey:hover {\n  color: #fff;\n  background-color: #35495e;\n}\n", "",{"version":3,"sources":["/Users/tron/work/testpage/src/sun-network/demos/PetFront/layouts/default.vue"],"names":[],"mappings":";AAOA;EACE;+CAC6C;EAC7C,qBAAqB;EACrB,iBAAiB;EACjB,0BAA0B;EAC1B,8BAA8B;EAC9B,kCAAkC;EAClC,mCAAmC;EACnC,8BAAsB;UAAtB,sBAAsB;AACxB;AAEA;;;EAGE,8BAAsB;UAAtB,sBAAsB;EACtB,SAAS;AACX;AAEA;EACE,qBAAqB;EACrB,kBAAkB;EAClB,yBAAyB;EACzB,cAAc;EACd,qBAAqB;EACrB,kBAAkB;AACpB;AAEA;EACE,WAAW;EACX,yBAAyB;AAC3B;AAEA;EACE,qBAAqB;EACrB,kBAAkB;EAClB,yBAAyB;EACzB,cAAc;EACd,qBAAqB;EACrB,kBAAkB;EAClB,iBAAiB;AACnB;AAEA;EACE,WAAW;EACX,yBAAyB;AAC3B","file":"default.vue?vue&type=style&index=0&lang=css&","sourcesContent":["\n\n\n\n\n\n\nhtml {\n  font-family: 'Source Sans Pro', -apple-system, BlinkMacSystemFont, 'Segoe UI',\n    Roboto, 'Helvetica Neue', Arial, sans-serif;\n  /* font-size: 16px; */\n  word-spacing: 1px;\n  -ms-text-size-adjust: 100%;\n  -webkit-text-size-adjust: 100%;\n  -moz-osx-font-smoothing: grayscale;\n  -webkit-font-smoothing: antialiased;\n  box-sizing: border-box;\n}\n\n*,\n*:before,\n*:after {\n  box-sizing: border-box;\n  margin: 0;\n}\n\n.button--green {\n  display: inline-block;\n  border-radius: 4px;\n  border: 1px solid #3b8070;\n  color: #3b8070;\n  text-decoration: none;\n  padding: 10px 30px;\n}\n\n.button--green:hover {\n  color: #fff;\n  background-color: #3b8070;\n}\n\n.button--grey {\n  display: inline-block;\n  border-radius: 4px;\n  border: 1px solid #35495e;\n  color: #35495e;\n  text-decoration: none;\n  padding: 10px 30px;\n  margin-left: 15px;\n}\n\n.button--grey:hover {\n  color: #fff;\n  background-color: #35495e;\n}\n"]}]);



/***/ }),

/***/ "./node_modules/vue-loader/lib/loaders/templateLoader.js?!./node_modules/vue-loader/lib/index.js?!./.nuxt/components/nuxt-build-indicator.vue?vue&type=template&id=71e9e103&scoped=true&":
/*!****************************************************************************************************************************************************************************************************************************!*\
  !*** ./node_modules/vue-loader/lib/loaders/templateLoader.js??vue-loader-options!./node_modules/vue-loader/lib??vue-loader-options!./.nuxt/components/nuxt-build-indicator.vue?vue&type=template&id=71e9e103&scoped=true& ***!
  \****************************************************************************************************************************************************************************************************************************/
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
  return _c("transition", { attrs: { appear: "" } }, [
    _vm.building
      ? _c("div", { staticClass: "nuxt__build_indicator" }, [
          _c(
            "svg",
            {
              attrs: {
                viewBox: "0 0 96 72",
                version: "1",
                xmlns: "http://www.w3.org/2000/svg"
              }
            },
            [
              _c("g", { attrs: { fill: "none", "fill-rule": "evenodd" } }, [
                _c("path", {
                  attrs: {
                    d:
                      "M6 66h23l1-3 21-37L40 6 6 66zM79 66h11L62 17l-5 9 22 37v3zM54 31L35 66h38z"
                  }
                }),
                _vm._v(" "),
                _c("path", {
                  attrs: {
                    d:
                      "M29 69v-1-2H6L40 6l11 20 3-6L44 3s-2-3-4-3-3 1-5 3L1 63c0 1-2 3 0 6 0 1 2 2 5 2h28c-3 0-4-1-5-2z",
                    fill: "#00C58E"
                  }
                }),
                _vm._v(" "),
                _c("path", {
                  attrs: {
                    d:
                      "M95 63L67 14c0-1-2-3-5-3-1 0-3 0-4 3l-4 6 3 6 5-9 28 49H79a5 5 0 0 1 0 3c-2 2-5 2-5 2h16c1 0 4 0 5-2 1-1 2-3 0-6z",
                    fill: "#00C58E"
                  }
                }),
                _vm._v(" "),
                _c("path", {
                  attrs: {
                    d:
                      "M79 69v-1-2-3L57 26l-3-6-3 6-21 37-1 3a5 5 0 0 0 0 3c1 1 2 2 5 2h40s3 0 5-2zM54 31l19 35H35l19-35z",
                    fill: "#FFF",
                    "fill-rule": "nonzero"
                  }
                })
              ])
            ]
          ),
          _vm._v("\n    " + _vm._s(_vm.animatedProgress) + "%\n  ")
        ])
      : _vm._e()
  ])
}
var staticRenderFns = []
render._withStripped = true



/***/ }),

/***/ "./node_modules/vue-loader/lib/loaders/templateLoader.js?!./node_modules/vue-loader/lib/index.js?!./.nuxt/components/nuxt-error.vue?vue&type=template&id=74e3df5b&":
/*!******************************************************************************************************************************************************************************************************!*\
  !*** ./node_modules/vue-loader/lib/loaders/templateLoader.js??vue-loader-options!./node_modules/vue-loader/lib??vue-loader-options!./.nuxt/components/nuxt-error.vue?vue&type=template&id=74e3df5b& ***!
  \******************************************************************************************************************************************************************************************************/
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
  return _c("div", { staticClass: "__nuxt-error-page" }, [
    _c("div", { staticClass: "error" }, [
      _c(
        "svg",
        {
          attrs: {
            xmlns: "http://www.w3.org/2000/svg",
            width: "90",
            height: "90",
            fill: "#DBE1EC",
            viewBox: "0 0 48 48"
          }
        },
        [
          _c("path", {
            attrs: {
              d:
                "M22 30h4v4h-4zm0-16h4v12h-4zm1.99-10C12.94 4 4 12.95 4 24s8.94 20 19.99 20S44 35.05 44 24 35.04 4 23.99 4zM24 40c-8.84 0-16-7.16-16-16S15.16 8 24 8s16 7.16 16 16-7.16 16-16 16z"
            }
          })
        ]
      ),
      _vm._v(" "),
      _c("div", { staticClass: "title" }, [_vm._v(_vm._s(_vm.message))]),
      _vm._v(" "),
      _vm.statusCode === 404
        ? _c(
            "p",
            { staticClass: "description" },
            [
              _c(
                "NuxtLink",
                { staticClass: "error-link", attrs: { to: "/" } },
                [_vm._v("Back to the home page")]
              )
            ],
            1
          )
        : _c("p", { staticClass: "description" }, [
            _vm._v(
              "An error occurred while rendering the page. Check developer tools console for details."
            )
          ]),
      _vm._v(" "),
      _vm._m(0)
    ])
  ])
}
var staticRenderFns = [
  function() {
    var _vm = this
    var _h = _vm.$createElement
    var _c = _vm._self._c || _h
    return _c("div", { staticClass: "logo" }, [
      _c(
        "a",
        {
          attrs: {
            href: "https://nuxtjs.org",
            target: "_blank",
            rel: "noopener"
          }
        },
        [_vm._v("Nuxt.js")]
      )
    ])
  }
]
render._withStripped = true



/***/ }),

/***/ "./node_modules/vue-loader/lib/loaders/templateLoader.js?!./node_modules/vue-loader/lib/index.js?!./layouts/default.vue?vue&type=template&id=314f53c6&":
/*!******************************************************************************************************************************************************************************************!*\
  !*** ./node_modules/vue-loader/lib/loaders/templateLoader.js??vue-loader-options!./node_modules/vue-loader/lib??vue-loader-options!./layouts/default.vue?vue&type=template&id=314f53c6& ***!
  \******************************************************************************************************************************************************************************************/
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
  return _c("div", [_c("nuxt")], 1)
}
var staticRenderFns = []
render._withStripped = true



/***/ }),

/***/ "./node_modules/vue-style-loader/index.js?!./node_modules/css-loader/dist/cjs.js?!./node_modules/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/postcss-loader/src/index.js?!./node_modules/vue-loader/lib/index.js?!./.nuxt/components/nuxt-build-indicator.vue?vue&type=style&index=0&id=71e9e103&scoped=true&lang=css&":
/*!*************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
  !*** ./node_modules/vue-style-loader??ref--5-oneOf-1-0!./node_modules/css-loader/dist/cjs.js??ref--5-oneOf-1-1!./node_modules/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/postcss-loader/src??ref--5-oneOf-1-2!./node_modules/vue-loader/lib??vue-loader-options!./.nuxt/components/nuxt-build-indicator.vue?vue&type=style&index=0&id=71e9e103&scoped=true&lang=css& ***!
  \*************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

// style-loader: Adds some css to the DOM by adding a <style> tag

// load the styles
var content = __webpack_require__(/*! !../../node_modules/css-loader/dist/cjs.js??ref--5-oneOf-1-1!../../node_modules/vue-loader/lib/loaders/stylePostLoader.js!../../node_modules/postcss-loader/src??ref--5-oneOf-1-2!../../node_modules/vue-loader/lib??vue-loader-options!./nuxt-build-indicator.vue?vue&type=style&index=0&id=71e9e103&scoped=true&lang=css& */ "./node_modules/css-loader/dist/cjs.js?!./node_modules/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/postcss-loader/src/index.js?!./node_modules/vue-loader/lib/index.js?!./.nuxt/components/nuxt-build-indicator.vue?vue&type=style&index=0&id=71e9e103&scoped=true&lang=css&");
if(typeof content === 'string') content = [[module.i, content, '']];
if(content.locals) module.exports = content.locals;
// add the styles to the DOM
var add = __webpack_require__(/*! ../../node_modules/vue-style-loader/lib/addStylesClient.js */ "./node_modules/vue-style-loader/lib/addStylesClient.js").default
var update = add("6da220d7", content, false, {"sourceMap":true});
// Hot Module Replacement
if(true) {
 // When the styles change, update the <style> tags
 if(!content.locals) {
   module.hot.accept(/*! !../../node_modules/css-loader/dist/cjs.js??ref--5-oneOf-1-1!../../node_modules/vue-loader/lib/loaders/stylePostLoader.js!../../node_modules/postcss-loader/src??ref--5-oneOf-1-2!../../node_modules/vue-loader/lib??vue-loader-options!./nuxt-build-indicator.vue?vue&type=style&index=0&id=71e9e103&scoped=true&lang=css& */ "./node_modules/css-loader/dist/cjs.js?!./node_modules/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/postcss-loader/src/index.js?!./node_modules/vue-loader/lib/index.js?!./.nuxt/components/nuxt-build-indicator.vue?vue&type=style&index=0&id=71e9e103&scoped=true&lang=css&", function() {
     var newContent = __webpack_require__(/*! !../../node_modules/css-loader/dist/cjs.js??ref--5-oneOf-1-1!../../node_modules/vue-loader/lib/loaders/stylePostLoader.js!../../node_modules/postcss-loader/src??ref--5-oneOf-1-2!../../node_modules/vue-loader/lib??vue-loader-options!./nuxt-build-indicator.vue?vue&type=style&index=0&id=71e9e103&scoped=true&lang=css& */ "./node_modules/css-loader/dist/cjs.js?!./node_modules/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/postcss-loader/src/index.js?!./node_modules/vue-loader/lib/index.js?!./.nuxt/components/nuxt-build-indicator.vue?vue&type=style&index=0&id=71e9e103&scoped=true&lang=css&");
     if(typeof newContent === 'string') newContent = [[module.i, newContent, '']];
     update(newContent);
   });
 }
 // When the module is disposed, remove the <style> tags
 module.hot.dispose(function() { update(); });
}

/***/ }),

/***/ "./node_modules/vue-style-loader/index.js?!./node_modules/css-loader/dist/cjs.js?!./node_modules/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/postcss-loader/src/index.js?!./node_modules/vue-loader/lib/index.js?!./.nuxt/components/nuxt-error.vue?vue&type=style&index=0&lang=css&":
/*!***************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
  !*** ./node_modules/vue-style-loader??ref--5-oneOf-1-0!./node_modules/css-loader/dist/cjs.js??ref--5-oneOf-1-1!./node_modules/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/postcss-loader/src??ref--5-oneOf-1-2!./node_modules/vue-loader/lib??vue-loader-options!./.nuxt/components/nuxt-error.vue?vue&type=style&index=0&lang=css& ***!
  \***************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

// style-loader: Adds some css to the DOM by adding a <style> tag

// load the styles
var content = __webpack_require__(/*! !../../node_modules/css-loader/dist/cjs.js??ref--5-oneOf-1-1!../../node_modules/vue-loader/lib/loaders/stylePostLoader.js!../../node_modules/postcss-loader/src??ref--5-oneOf-1-2!../../node_modules/vue-loader/lib??vue-loader-options!./nuxt-error.vue?vue&type=style&index=0&lang=css& */ "./node_modules/css-loader/dist/cjs.js?!./node_modules/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/postcss-loader/src/index.js?!./node_modules/vue-loader/lib/index.js?!./.nuxt/components/nuxt-error.vue?vue&type=style&index=0&lang=css&");
if(typeof content === 'string') content = [[module.i, content, '']];
if(content.locals) module.exports = content.locals;
// add the styles to the DOM
var add = __webpack_require__(/*! ../../node_modules/vue-style-loader/lib/addStylesClient.js */ "./node_modules/vue-style-loader/lib/addStylesClient.js").default
var update = add("b675d82e", content, false, {"sourceMap":true});
// Hot Module Replacement
if(true) {
 // When the styles change, update the <style> tags
 if(!content.locals) {
   module.hot.accept(/*! !../../node_modules/css-loader/dist/cjs.js??ref--5-oneOf-1-1!../../node_modules/vue-loader/lib/loaders/stylePostLoader.js!../../node_modules/postcss-loader/src??ref--5-oneOf-1-2!../../node_modules/vue-loader/lib??vue-loader-options!./nuxt-error.vue?vue&type=style&index=0&lang=css& */ "./node_modules/css-loader/dist/cjs.js?!./node_modules/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/postcss-loader/src/index.js?!./node_modules/vue-loader/lib/index.js?!./.nuxt/components/nuxt-error.vue?vue&type=style&index=0&lang=css&", function() {
     var newContent = __webpack_require__(/*! !../../node_modules/css-loader/dist/cjs.js??ref--5-oneOf-1-1!../../node_modules/vue-loader/lib/loaders/stylePostLoader.js!../../node_modules/postcss-loader/src??ref--5-oneOf-1-2!../../node_modules/vue-loader/lib??vue-loader-options!./nuxt-error.vue?vue&type=style&index=0&lang=css& */ "./node_modules/css-loader/dist/cjs.js?!./node_modules/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/postcss-loader/src/index.js?!./node_modules/vue-loader/lib/index.js?!./.nuxt/components/nuxt-error.vue?vue&type=style&index=0&lang=css&");
     if(typeof newContent === 'string') newContent = [[module.i, newContent, '']];
     update(newContent);
   });
 }
 // When the module is disposed, remove the <style> tags
 module.hot.dispose(function() { update(); });
}

/***/ }),

/***/ "./node_modules/vue-style-loader/index.js?!./node_modules/css-loader/dist/cjs.js?!./node_modules/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/postcss-loader/src/index.js?!./node_modules/vue-loader/lib/index.js?!./layouts/default.vue?vue&type=style&index=0&lang=css&":
/*!***************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
  !*** ./node_modules/vue-style-loader??ref--5-oneOf-1-0!./node_modules/css-loader/dist/cjs.js??ref--5-oneOf-1-1!./node_modules/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/postcss-loader/src??ref--5-oneOf-1-2!./node_modules/vue-loader/lib??vue-loader-options!./layouts/default.vue?vue&type=style&index=0&lang=css& ***!
  \***************************************************************************************************************************************************************************************************************************************************************************************************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

// style-loader: Adds some css to the DOM by adding a <style> tag

// load the styles
var content = __webpack_require__(/*! !../node_modules/css-loader/dist/cjs.js??ref--5-oneOf-1-1!../node_modules/vue-loader/lib/loaders/stylePostLoader.js!../node_modules/postcss-loader/src??ref--5-oneOf-1-2!../node_modules/vue-loader/lib??vue-loader-options!./default.vue?vue&type=style&index=0&lang=css& */ "./node_modules/css-loader/dist/cjs.js?!./node_modules/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/postcss-loader/src/index.js?!./node_modules/vue-loader/lib/index.js?!./layouts/default.vue?vue&type=style&index=0&lang=css&");
if(typeof content === 'string') content = [[module.i, content, '']];
if(content.locals) module.exports = content.locals;
// add the styles to the DOM
var add = __webpack_require__(/*! ../node_modules/vue-style-loader/lib/addStylesClient.js */ "./node_modules/vue-style-loader/lib/addStylesClient.js").default
var update = add("aab9a468", content, false, {"sourceMap":true});
// Hot Module Replacement
if(true) {
 // When the styles change, update the <style> tags
 if(!content.locals) {
   module.hot.accept(/*! !../node_modules/css-loader/dist/cjs.js??ref--5-oneOf-1-1!../node_modules/vue-loader/lib/loaders/stylePostLoader.js!../node_modules/postcss-loader/src??ref--5-oneOf-1-2!../node_modules/vue-loader/lib??vue-loader-options!./default.vue?vue&type=style&index=0&lang=css& */ "./node_modules/css-loader/dist/cjs.js?!./node_modules/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/postcss-loader/src/index.js?!./node_modules/vue-loader/lib/index.js?!./layouts/default.vue?vue&type=style&index=0&lang=css&", function() {
     var newContent = __webpack_require__(/*! !../node_modules/css-loader/dist/cjs.js??ref--5-oneOf-1-1!../node_modules/vue-loader/lib/loaders/stylePostLoader.js!../node_modules/postcss-loader/src??ref--5-oneOf-1-2!../node_modules/vue-loader/lib??vue-loader-options!./default.vue?vue&type=style&index=0&lang=css& */ "./node_modules/css-loader/dist/cjs.js?!./node_modules/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/postcss-loader/src/index.js?!./node_modules/vue-loader/lib/index.js?!./layouts/default.vue?vue&type=style&index=0&lang=css&");
     if(typeof newContent === 'string') newContent = [[module.i, newContent, '']];
     update(newContent);
   });
 }
 // When the module is disposed, remove the <style> tags
 module.hot.dispose(function() { update(); });
}

/***/ }),

/***/ "./plugins/element-ui.js":
/*!*******************************!*\
  !*** ./plugins/element-ui.js ***!
  \*******************************/
/*! exports provided: default */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony import */ var vue__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! vue */ "./node_modules/vue/dist/vue.runtime.esm.js");
/* harmony import */ var element_ui_lib_element_ui_common__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! element-ui/lib/element-ui.common */ "./node_modules/element-ui/lib/element-ui.common.js");
/* harmony import */ var element_ui_lib_element_ui_common__WEBPACK_IMPORTED_MODULE_1___default = /*#__PURE__*/__webpack_require__.n(element_ui_lib_element_ui_common__WEBPACK_IMPORTED_MODULE_1__);
/* harmony import */ var element_ui_lib_locale_lang_en__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! element-ui/lib/locale/lang/en */ "./node_modules/element-ui/lib/locale/lang/en.js");
/* harmony import */ var element_ui_lib_locale_lang_en__WEBPACK_IMPORTED_MODULE_2___default = /*#__PURE__*/__webpack_require__.n(element_ui_lib_locale_lang_en__WEBPACK_IMPORTED_MODULE_2__);



/* harmony default export */ __webpack_exports__["default"] = (function () {
  vue__WEBPACK_IMPORTED_MODULE_0__["default"].use(element_ui_lib_element_ui_common__WEBPACK_IMPORTED_MODULE_1___default.a, {
    locale: element_ui_lib_locale_lang_en__WEBPACK_IMPORTED_MODULE_2___default.a
  });
});

/***/ }),

/***/ "./plugins/filter.js":
/*!***************************!*\
  !*** ./plugins/filter.js ***!
  \***************************/
/*! exports provided: default */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony import */ var vue__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! vue */ "./node_modules/vue/dist/vue.runtime.esm.js");

/* harmony default export */ __webpack_exports__["default"] = (function () {
  vue__WEBPACK_IMPORTED_MODULE_0__["default"].filter("hiddenAddress", function (value) {
    var num = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : 6;
    return value ? value.substr(0, num) + "..." + value.substr(-num) : "";
  });
  vue__WEBPACK_IMPORTED_MODULE_0__["default"].filter("fixed", function (value, number) {
    return parseFloat(value).toFixed(number);
  });
  vue__WEBPACK_IMPORTED_MODULE_0__["default"].filter("toLocaleString", function (value, number) {
    return parseFloat(value).toLocaleString();
  });
});

/***/ }),

/***/ "./plugins/i18n.js":
/*!*************************!*\
  !*** ./plugins/i18n.js ***!
  \*************************/
/*! exports provided: default */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony import */ var vue__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! vue */ "./node_modules/vue/dist/vue.runtime.esm.js");
/* harmony import */ var vue_i18n__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! vue-i18n */ "./node_modules/vue-i18n/dist/vue-i18n.esm.js");


vue__WEBPACK_IMPORTED_MODULE_0__["default"].use(vue_i18n__WEBPACK_IMPORTED_MODULE_1__["default"]);
/* harmony default export */ __webpack_exports__["default"] = (function (_ref) {
  var app = _ref.app,
      store = _ref.store;
  // Set i18n instance on app
  // This way we can use it in middleware and pages asyncData/fetch
  app.i18n = new vue_i18n__WEBPACK_IMPORTED_MODULE_1__["default"]({
    locale: store.state.locale,
    fallbackLocale: 'en',
    messages: {
      'en': __webpack_require__(/*! @/locales/en.json */ "./locales/en.json"),
      'ch': __webpack_require__(/*! @/locales/ch.json */ "./locales/ch.json")
    }
  });

  app.i18n.path = function (link) {
    if (app.i18n.locale === app.i18n.fallbackLocale) {
      return "/".concat(link);
    }

    return "/".concat(app.i18n.locale, "/").concat(link);
  };
});

/***/ }),

/***/ "./static/css/reset.css":
/*!******************************!*\
  !*** ./static/css/reset.css ***!
  \******************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

// style-loader: Adds some css to the DOM by adding a <style> tag

// load the styles
var content = __webpack_require__(/*! !../../node_modules/css-loader/dist/cjs.js??ref--5-oneOf-1-1!../../node_modules/postcss-loader/src??ref--5-oneOf-1-2!./reset.css */ "./node_modules/css-loader/dist/cjs.js?!./node_modules/postcss-loader/src/index.js?!./static/css/reset.css");
if(typeof content === 'string') content = [[module.i, content, '']];
if(content.locals) module.exports = content.locals;
// add the styles to the DOM
var add = __webpack_require__(/*! ../../node_modules/vue-style-loader/lib/addStylesClient.js */ "./node_modules/vue-style-loader/lib/addStylesClient.js").default
var update = add("403ffef3", content, false, {"sourceMap":true});
// Hot Module Replacement
if(true) {
 // When the styles change, update the <style> tags
 if(!content.locals) {
   module.hot.accept(/*! !../../node_modules/css-loader/dist/cjs.js??ref--5-oneOf-1-1!../../node_modules/postcss-loader/src??ref--5-oneOf-1-2!./reset.css */ "./node_modules/css-loader/dist/cjs.js?!./node_modules/postcss-loader/src/index.js?!./static/css/reset.css", function() {
     var newContent = __webpack_require__(/*! !../../node_modules/css-loader/dist/cjs.js??ref--5-oneOf-1-1!../../node_modules/postcss-loader/src??ref--5-oneOf-1-2!./reset.css */ "./node_modules/css-loader/dist/cjs.js?!./node_modules/postcss-loader/src/index.js?!./static/css/reset.css");
     if(typeof newContent === 'string') newContent = [[module.i, newContent, '']];
     update(newContent);
   });
 }
 // When the module is disposed, remove the <style> tags
 module.hot.dispose(function() { update(); });
}

/***/ }),

/***/ "./static/font/Arial.ttf":
/*!*******************************!*\
  !*** ./static/font/Arial.ttf ***!
  \*******************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

module.exports = __webpack_require__.p + "static/font/Arial.ttf";

/***/ }),

/***/ "./static/font/Arial_Black.ttf":
/*!*************************************!*\
  !*** ./static/font/Arial_Black.ttf ***!
  \*************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

module.exports = __webpack_require__.p + "static/font/Arial_Black.ttf";

/***/ }),

/***/ "./static/font/Arial_Bold.ttf":
/*!************************************!*\
  !*** ./static/font/Arial_Bold.ttf ***!
  \************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

module.exports = __webpack_require__.p + "static/font/Arial_Bold.ttf";

/***/ }),

/***/ "./static/font/Arial_Bold_Italic.ttf":
/*!*******************************************!*\
  !*** ./static/font/Arial_Bold_Italic.ttf ***!
  \*******************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

module.exports = __webpack_require__.p + "static/font/Arial_Bold_Italic.ttf";

/***/ }),

/***/ "./static/font/Arial_Italic.ttf":
/*!**************************************!*\
  !*** ./static/font/Arial_Italic.ttf ***!
  \**************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

module.exports = __webpack_require__.p + "static/font/Arial_Italic.ttf";

/***/ }),

/***/ "./static/font/AvenirNext.ttf":
/*!************************************!*\
  !*** ./static/font/AvenirNext.ttf ***!
  \************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

module.exports = __webpack_require__.p + "static/font/AvenirNext.ttf";

/***/ }),

/***/ "./static/images/slider_normal.png":
/*!*****************************************!*\
  !*** ./static/images/slider_normal.png ***!
  \*****************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

module.exports = __webpack_require__.p + "static/images/slider_normal.png";

/***/ }),

/***/ "./store/index.js":
/*!************************!*\
  !*** ./store/index.js ***!
  \************************/
/*! exports provided: strict, state, mutations */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "strict", function() { return strict; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "state", function() { return state; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "mutations", function() { return mutations; });
/* harmony import */ var core_js_modules_es7_object_get_own_property_descriptors__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! core-js/modules/es7.object.get-own-property-descriptors */ "./node_modules/core-js/modules/es7.object.get-own-property-descriptors.js");
/* harmony import */ var core_js_modules_es7_object_get_own_property_descriptors__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es7_object_get_own_property_descriptors__WEBPACK_IMPORTED_MODULE_0__);
/* harmony import */ var core_js_modules_es6_symbol__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! core-js/modules/es6.symbol */ "./node_modules/core-js/modules/es6.symbol.js");
/* harmony import */ var core_js_modules_es6_symbol__WEBPACK_IMPORTED_MODULE_1___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es6_symbol__WEBPACK_IMPORTED_MODULE_1__);
/* harmony import */ var core_js_modules_web_dom_iterable__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! core-js/modules/web.dom.iterable */ "./node_modules/core-js/modules/web.dom.iterable.js");
/* harmony import */ var core_js_modules_web_dom_iterable__WEBPACK_IMPORTED_MODULE_2___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_web_dom_iterable__WEBPACK_IMPORTED_MODULE_2__);
/* harmony import */ var core_js_modules_es6_object_to_string__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! core-js/modules/es6.object.to-string */ "./node_modules/core-js/modules/es6.object.to-string.js");
/* harmony import */ var core_js_modules_es6_object_to_string__WEBPACK_IMPORTED_MODULE_3___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es6_object_to_string__WEBPACK_IMPORTED_MODULE_3__);
/* harmony import */ var core_js_modules_es6_object_keys__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! core-js/modules/es6.object.keys */ "./node_modules/core-js/modules/es6.object.keys.js");
/* harmony import */ var core_js_modules_es6_object_keys__WEBPACK_IMPORTED_MODULE_4___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_es6_object_keys__WEBPACK_IMPORTED_MODULE_4__);
/* harmony import */ var _babel_runtime_helpers_esm_defineProperty__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! @babel/runtime/helpers/esm/defineProperty */ "./node_modules/@babel/runtime/helpers/esm/defineProperty.js");







function ownKeys(object, enumerableOnly) { var keys = Object.keys(object); if (Object.getOwnPropertySymbols) { var symbols = Object.getOwnPropertySymbols(object); if (enumerableOnly) symbols = symbols.filter(function (sym) { return Object.getOwnPropertyDescriptor(object, sym).enumerable; }); keys.push.apply(keys, symbols); } return keys; }

function _objectSpread(target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i] != null ? arguments[i] : {}; if (i % 2) { ownKeys(source, true).forEach(function (key) { Object(_babel_runtime_helpers_esm_defineProperty__WEBPACK_IMPORTED_MODULE_5__["default"])(target, key, source[key]); }); } else if (Object.getOwnPropertyDescriptors) { Object.defineProperties(target, Object.getOwnPropertyDescriptors(source)); } else { ownKeys(source).forEach(function (key) { Object.defineProperty(target, key, Object.getOwnPropertyDescriptor(source, key)); }); } } return target; }

var strict = false;
var state = function state() {
  return {
    locales: ["en", "ch"],
    locale: "en",
    address: {},
    balance: '--',
    mBalance: '--',
    limit: 10000,
    contractAddress: "",
    contractInstance: null,
    dialogLogin: false,
    myBetsLength: 0,
    showLoading: true,
    random: 0,
    activityAddress: "",
    token: "",
    dapp: 1,
    myBets: [],
    platForm: "pc",
    account: {},
    trx20Account: {},
    trx20AccountInfo: {},
    autoBet: {
      switch: false,
      time: ""
    },
    successRecord: [],
    globalSunWeb: {},
    diviend: {} //分红

  };
};
var mutations = {
  SET_LANG: function SET_LANG(state, locale) {
    if (state.locales.indexOf(locale) !== -1) {
      state.locale = locale;
    }
  },
  SET_CONTRACT_ADDRESS: function SET_CONTRACT_ADDRESS(state, address) {
    state.contractAddress = address;
  },
  SET_BALANCE: function SET_BALANCE(state, balance) {
    state.balance = balance;
  },
  SET_MBALANCE: function SET_MBALANCE(state, balance) {
    state.mBalance = balance;
  },
  SET_CONTRACT_INSTANCE: function SET_CONTRACT_INSTANCE(state, obj) {
    state.contractInstance = _objectSpread({}, state.contractInstance, {}, obj);
  },
  SET_DIALOG_LOGIN: function SET_DIALOG_LOGIN(state, dialogLogin) {
    state.dialogLogin = dialogLogin;
  },
  SET_MY_BETS_LENGTH: function SET_MY_BETS_LENGTH(state, myBetsLength) {
    state.myBetsLength = myBetsLength;
  },
  SET_RANDOM: function SET_RANDOM(state, random) {
    state.random = random;
  },
  SET_SHOW_LOADING: function SET_SHOW_LOADING(state, showLoading) {
    state.showLoading = showLoading;
  },
  SET_ACTIVITYADDRESS: function SET_ACTIVITYADDRESS(state, activityAddress) {
    state.activityAddress = activityAddress;
  },
  SET_TOKEN: function SET_TOKEN(state, token) {
    state.token = token;
  },
  SET_MY_BETS: function SET_MY_BETS(state, myBets) {
    state.myBets = myBets;
  },
  SET_PLATFORM: function SET_PLATFORM(state, platForm) {
    state.platForm = platForm;
  },
  SET_ACCOUNT: function SET_ACCOUNT(state, data) {
    state.account = data;
  },
  SET_TRX20ACCOUNT: function SET_TRX20ACCOUNT(state, data) {
    state.trx20Account = data;
  },
  SET_AUTO_BET_TIME: function SET_AUTO_BET_TIME(state, obj) {
    state.autoBet = obj;
  },
  SET_SUCCESS_RECORD: function SET_SUCCESS_RECORD(state, arr) {
    state.successRecord = arr;
  },
  SET_SUNWEB: function SET_SUNWEB(state, obj) {
    state.globalSunWeb = obj;
    state.address = obj.mainchain.defaultAddress;
    // window.sunWeb = obj;
  },
  SET_DIVIEND: function SET_DIVIEND(state, obj) {
    state.diviend = obj;
  },
  SET_TRX20ACCOUNT_INFO: function SET_TRX20ACCOUNT_INFO(state, obj) {
    state.trx20AccountInfo = obj;
  }
};

/***/ }),

/***/ 0:
/*!******************************************************************************************************************************************************************************!*\
  !*** multi eventsource-polyfill webpack-hot-middleware/client?reload=true&timeout=30000&ansiColors=&overlayStyles=&name=client&path=/__webpack_hmr/client ./.nuxt/client.js ***!
  \******************************************************************************************************************************************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

__webpack_require__(/*! eventsource-polyfill */"./node_modules/eventsource-polyfill/dist/browserify-eventsource.js");
__webpack_require__(/*! webpack-hot-middleware/client?reload=true&timeout=30000&ansiColors=&overlayStyles=&name=client&path=/__webpack_hmr/client */"./node_modules/webpack-hot-middleware/client.js?reload=true&timeout=30000&ansiColors=&overlayStyles=&name=client&path=/__webpack_hmr/client");
module.exports = __webpack_require__(/*! /Users/tron/work/testpage/src/sun-network/demos/PetFront/.nuxt/client.js */"./.nuxt/client.js");


/***/ })

},[[0,"runtime","commons.app","vendors.app"]]]);
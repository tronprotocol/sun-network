
export default {
  mode: 'spa',
  /*
  ** Headers of the page
  */
  head: {
    title: process.env.npm_package_name || '',
    meta: [
      { charset: 'utf-8' },
      { name: 'viewport', content: 'width=device-width, initial-scale=1' },
      { hid: 'description', name: 'description', content: process.env.npm_package_description || '' }
    ],
    link: [
      { rel: "icon", type: "image/x-icon", href: "/favicon.ico" },
      {
        rel: "stylesheet",
        type: "text/css",
        href: "//at.alicdn.com/t/font_887145_pjmxwtmmaus.css"
      }
    ],
    script: [
      { src: "/demos/donate/js/vconsole.min.js" }
    ]
  },
  /*
  ** Customize the progress-bar color
  */
  loading: false,

  router: {
    base: "/demos/donate",
    middleware: "i18n"
  },

  generate: {
    routes: ["/", "/ch", "/en",]
  },
  /*
  ** Global CSS
  */
  css: [
    'element-ui/lib/theme-chalk/index.css', 
     "@/static/css/reset.css"
  ],
  /*
  ** Plugins to load before mounting the App
  */
  plugins: ["@/plugins/i18n.js", "@/plugins/element-ui", "@/plugins/filter.js"],

  /*
  ** Nuxt.js dev-modules
  */
  devModules: [
  ],
  /*
  ** Nuxt.js modules
  */
  modules: [
  ],

  env: {
    PATH_TYPE: process.env.PATH_TYPE
  },

  /*
  ** Build configuration
  */
  build: {
    transpile: [/^element-ui/],
    build: {
      // 不管页面引用几次，只打包一次axios
      vendor: ['axios']
    },
    /*
     ** You can extend webpack config here
     */
    extend(config, ctx) {
      // Run ESLint on save
      if (ctx.isDev && ctx.isClient) {
        config.module.rules.concat([
          {
            test: /\.sass$/,
            loaders: ["style", "css", "sass"]
          }
        ]);
      }
    }
  }
}

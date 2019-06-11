module.exports = {
  base: process.env.GITHUB == 'github' ? '/sun-network/' : '/sunnetwork/',
  dest: process.env.GITHUB == 'github' ? 'docs/.vuepress/github' : 'docs/.vuepress/dist',
  title: 'Sun Network',
  head: [['link', { rel: 'icon', href: '/favicon.png' }]],
  locales: {
    '/': {},
    '/zh/': {}
  },
  define: {
    'process.env.GITHUB': process.env.GITHUB
  },
  themeConfig: {
    repo: 'tronprotocol/sun-network',
    locales: {
      '/': {
        selectText: 'Languages',
        label: 'English',
        sidebar: 'auto',
        nav: [{ text: 'Guide', link: '/guide/' }]
      },
      '/zh/': {
        selectText: '选择语言',
        label: '简体中文',
        sidebar: 'auto',
        nav: [{ text: '指南', link: '/zh/guide/' }]
      }
    }
  }
};

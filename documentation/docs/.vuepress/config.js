module.exports = {
  base: '/sunnetwork/',
  title: 'Sun Network',
  head: [['link', { rel: 'icon', href: '/favicon.png' }]],
  locales: {
    '/': {},
    '/zh/': {}
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

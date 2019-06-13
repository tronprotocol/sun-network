# Documentation

## Development

Installation package:

```sh
npm i
```

You can now start writing with:

```sh
npm run docs:dev
```

To generate static assets, run:

```sh
npm run docs:build
```

## Deployment

Installation package:

```sh
npm i --production
```

Run backend server:

```sh
pm2 start index.js
```

Deploy to GitHub Pages:

```sh
npm run docs:deploy
```

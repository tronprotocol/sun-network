#!/usr/bin/env bash

set -e

npm run docs:build

cd docs/.vuepress/dist

git init
git add -A
git commit -m 'deploy'

git push -f git@github.com:tronprotocol/sun-network.git master:gh-pages

cd -
#!/bin/bash

if [ $encrypted_01022d2a4f87_key ];then
  openssl aes-256-cbc -K $encrypted_01022d2a4f87_key -iv $encrypted_01022d2a4f87_iv -in sunnetworkci.enc -out sunnetworkci -d
  cat sunnetworkci > ~/.ssh/id_rsa
  chmod 600 ~/.ssh/id_rsa
  echo "Add docker server success"
  sonar-scanner
fi

cp -f config/checkstyle/checkStyle.xml config/checkstyle/checkStyleAll.xml

#!/bin/bash
if [[ "$TRAVIS_BRANCH" = "develop" || "$TRAVIS_BRANCH" = "master" ]];then
     stestlogname="`date +%Y%m%d%H%M%S`_stest.log"
     stest_server=39.106.81.60
    `ssh java-tron@$stest_server -p 22008 sh /data/databackup/docker_workspace_sideChain/do_stest_sidechain.sh >$stestlogname 2>&1`
     echo "stest start"
     cat $stestlogname | grep "Stest result is:" -A 10000
     echo "stest end"
fi
echo "bye bye"
echo $stest_server
rm -f $stestlogname
exit 0
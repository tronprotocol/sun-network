#!/bin/bash
if [[ "$TRAVIS_BRANCH" = "develop" || "$TRAVIS_BRANCH" = "master" ]];then
     stest_server=39.106.81.60
    `ssh java-tron@$stest_server -p 22008 sh /data/databackup/docker_workspace_sideChain/do_stest_sidechain.sh`
fi
echo "bye bye"
echo $stest_server
rm -f $stestlogname
exit 0
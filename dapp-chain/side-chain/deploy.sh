#!/bin/bash
if [[ "$TRAVIS_BRANCH" = "develop" || "$TRAVIS_BRANCH" = "master" ]];then
     stest_server=47.95.206.44
    `ssh java-tron@$stest_server -p 22008 sh /data/workspace/docker_workspace_sideChain/do_stest_sideChain.sh`
fi
echo "bye bye"
echo $stest_server
rm -f $stestlogname
exit 0
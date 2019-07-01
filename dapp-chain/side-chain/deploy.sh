#!/bin/bash
if [[ "$TRAVIS_BRANCH" = "develop" || "$TRAVIS_BRANCH" = "master" ]];then

     stestlogname="`date +%Y%m%d%H%M%S`_stest.log"
     timeout 10 ping -c 5  39.106.81.60 > /dev/null || exit 1

     stest_server=39.106.81.60
     change_branch_CMD="sed -i '1c branch_name_side=$TRAVIS_BRANCH' /data/databackup/docker_workspace_sideChain/do_stest_sidechain.sh"
     ssh java-tron@$stest_server -p 22008 $change_branch_CMD
     ssh java-tron@$stest_server -p 22008 sh /data/databackup/docker_workspace_sideChain/do_stest_sidechain.sh > $stestlogname
     if [[ `find $stestlogname -type f | xargs grep "Connection refused"` =~ "Connection refused" || `find $stestlogname -type f | xargs grep "stest FAILED"` =~ "stest FAILED" ]];
     then
      rm -f $stestlogname
      echo "first Retry stest task"
      ssh java-tron@$stest_server -p 22008 $change_branch_CMD
      ssh java-tron@$stest_server -p 22008 sh /data/databackup/docker_workspace_sideChain/do_stest_sidechain.sh > $stestlogname
     fi

     echo "stest start"
     #cat $stestlogname
     cat $stestlogname | grep "Stest result is:" -A 10000
     echo "stest end"

    echo $?
    ret=$(cat $stestlogname | grep "stest FAILED" | wc -l)

    if [ $ret != 0 ];then
      echo $ret
      rm -f $stestlogname
      exit 1
    fi
fi
echo "bye bye"
echo $stest_server
rm -f $stestlogname
exit 0
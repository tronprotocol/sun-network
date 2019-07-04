testnet=(
10.40.10.5
10.40.10.4
10.40.10.252
10.40.10.1
10.40.10.11
10.40.10.251
10.40.10.3
)

echo "Start build 3.2.3java-tron"

cd /data/workspace/70_for_master_workspace/java-tron/
git fetch
git reset --hard origin/master
git checkout 46713749a047d2cd8b874862441bb9ed9d810e12
git pull
cp /data/workspace/70_for_master_workspace/FullNode.java /data/workspace/70_for_master_workspace/java-tron/src/main/java/org/tron/program/FullNode.java 
cp /data/workspace/70_for_master_workspace/SolidityNode.java /data/workspace/70_for_master_workspace/java-tron/src/main/java/org/tron/program/SolidityNode.java
cp /data/workspace/70_for_master_workspace/Manager.java /data/workspace/70_for_master_workspace/java-tron/src/main/java/org/tron/core/db/Manager.java

#sed -i "s/for (int i = 1\; i < slot/\/\*for (int i = 1\; i < slot/g" /data/workspace/70_for_master_workspace/java-tron/src/main/java/org/tron/core/db/Manager.java 
#sed -i "s/this.dynamicPropertiesStore.applyBlock(true)/\*\/this.dynamicPropertiesStore.applyBlock(true)/g" /data/workspace/70_for_master_workspace/java-tron/src/main/java/org/tron/core/db/Manager.java
#sed -i "s/long headBlockTime = getHeadBlockTimeStamp()/\/\*long headBlockTime = getHeadBlockTimeStamp()/g" /data/workspace/70_for_master_workspace/java-tron/src/main/java/org/tron/core/db/Manager.java
#sed -i "s/void validateDup(TransactionCapsule/\*\/\}void validateDup(TransactionCapsule/g" /data/workspace/70_for_master_workspace/java-tron/src/main/java/org/tron/core/db/Manager.java
#sed -i "s/validateTapos(trxCap)/\/\/validateTapos(trxCap)/g" /data/workspace/70_for_master_workspace/java-tron/src/main/java/org/tron/core/db/Manager.java
#sed -i "s/validateCommon(trxCap)/\/\/validateCommon(trxCap)/g" /data/workspace/70_for_master_workspace/java-tron/src/main/java/org/tron/core/db/Manager.java
./gradlew build -x test

echo "Build 3.2.3java-tron completed"

for i in ${testnet[@]}; do
  scp -P 22008 -i /data/workspace/docker_workspace/id_rsa /data/workspace/70_for_master_workspace/java-tron/build/libs/FullNode.jar java-tron@$i:/data/databackup/java-tron/java-tron.jar
  scp -P 22008 -i /data/workspace/docker_workspace/id_rsa /data/workspace/70_for_master_workspace/conf/config.conf_true java-tron@$i:/data/databackup/java-tron/config.conf
  scp -P 22008 -i /data/workspace/docker_workspace/id_rsa /data/workspace/70_for_master_workspace/stop.sh java-tron@$i:/data/databackup/java-tron/
  scp -P 22008 -i /data/workspace/docker_workspace/id_rsa /data/workspace/70_for_master_workspace/start.sh java-tron@$i:/data/databackup/java-tron/
  echo "Send java-tron.jar and config.conf and start.sh to ${i} completed"
done
sleep 10
scp -P 22008 -i /data/workspace/docker_workspace/id_rsa /data/workspace/70_for_master_workspace/conf/config.203_conf java-tron@10.40.10.1:/data/databackup/java-tron/config.conf
#scp -P 22008 -i /data/workspace/docker_workspace/id_rsa /data/workspace/70_for_master_workspace/conf/config.194_conf java-tron@10.40.10.18:/data/databackup/java-tron/config.conf
scp -P 22008 -i /data/workspace/docker_workspace/id_rsa /data/workspace/70_for_master_workspace/java-tron/build/libs/FullNode.jar java-tron@10.40.10.18:/data/databackup/java-tron/java-tron.jar
#  scp -P 22008 -i /data/workspace/docker_workspace/id_rsa /data/workspace/70_for_master_workspace/stop.sh java-tron@10.40.10.18:/data/databackup/java-tron/
#  scp -P 22008 -i /data/workspace/docker_workspace/id_rsa /data/workspace/70_for_master_workspace/start.sh java-tron@10.40.10.18:/data/databackup/java-tron/
echo "Start build 3.2.4java-tron"
cd /data/workspace/70_for_master_workspace/java-tron/
git fetch
git reset --hard origin/master
git checkout 4d565780523ac88cbc8193620cb815a94618c004
git pull
cp /data/workspace/70_for_master_workspace/FullNode.java /data/workspace/70_for_master_workspace/java-tron/src/main/java/org/tron/program/FullNode.java
cp /data/workspace/70_for_master_workspace/SolidityNode.java /data/workspace/70_for_master_workspace/java-tron/src/main/java/org/tron/program/SolidityNode.java
cp /data/workspace/70_for_master_workspace/Manager.java /data/workspace/70_for_master_workspace/java-tron/src/main/java/org/tron/core/db/Manager.java
./gradlew build -x test
echo "Build 3.2.4java-tron completed"

scp -P 22008 -i /data/workspace/docker_workspace/id_rsa /data/workspace/70_for_master_workspace/java-tron/build/libs/FullNode.jar java-tron@10.40.10.3:/data/databackup/java-tron/java-tron.jar
scp -P 22008 -i /data/workspace/docker_workspace/id_rsa /data/workspace/70_for_master_workspace/java-tron/build/libs/SolidityNode.jar java-tron@10.40.10.251:/data/databackup/java-tron/java-tron.jar
scp -P 22008 -i /data/workspace/docker_workspace/id_rsa /data/workspace/70_for_master_workspace/start_fullnode.sh java-tron@10.40.10.3:/data/databackup/java-tron/start.sh
scp -P 22008 -i /data/workspace/docker_workspace/id_rsa /data/workspace/70_for_master_workspace/start_solidity.sh java-tron@10.40.10.251:/data/databackup/java-tron/start.sh
scp -P 22008 -i /data/workspace/docker_workspace/id_rsa /data/workspace/70_for_master_workspace/conf/config.conf_false java-tron@10.40.10.5:/data/databackup/java-tron/config.conf
for i in ${testnet[@]}; do
  ssh -p 22008 -i /data/workspace/docker_workspace/id_rsa java-tron@$i 'source ~/.bash_profile && cd /data/databackup/java-tron && sh /data/databackup/java-tron/stop.sh'
  echo "Stop java-tron on ${i} completed"
done
  ssh -p 22008 -i /data/workspace/docker_workspace/id_rsa java-tron@10.40.10.18 'source ~/.bash_profile && cd /data/databackup/java-tron && sh /data/databackup/java-tron/stop.sh'

backup_logname="`date +%Y%m%d%H%M%S`_backup.log"
for i in ${testnet[@]}; do
  ssh -p 22008 -i /data/workspace/docker_workspace/id_rsa java-tron@$i "mv /data/databackup/java-tron/logs/tron.log /data/databackup/java-tron/logs/$backup_logname"
  echo "Backup tron.log of ${i} complete"
done

datanet=(
10.40.10.5
10.40.10.4
10.40.10.252
10.40.10.1
10.40.10.18
10.40.10.11
10.40.10.251
10.40.10.3
)
for i in ${datanet[@]}; do
  ssh -p 22008 -i /data/workspace/docker_workspace/id_rsa java-tron@$i 'sudo rm -rf /data/databackup/java-tron/output-directory/'
  echo "Delete database file of ${i} completed"
done

cd /data/java-tron/
sh /data/java-tron/stop.sh
sleep 10
for node in ${datanet[@]}; do {
tar -c output-directory/ |pigz |ssh -p 22008 -i /data/workspace/docker_workspace/id_rsa java-tron@$node "gzip -d|tar -xC /data/databackup/java-tron/" 
} &
done
wait
sleep 10
cd /data/java-tron/
sh /data/java-tron/start.sh


for i in ${datanet[@]}; do
  ssh -p 22008 -i /data/workspace/docker_workspace/id_rsa java-tron@$i 'source ~/.bash_profile && cd /data/databackup/java-tron && sh /data/databackup/java-tron/start.sh'
  echo "Start java-tron on ${i} completed"
done
sleep 120
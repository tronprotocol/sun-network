### Build JAR

Pull the latest code of [sun-network](https://github.com/tronprotocol/sun-network.git) , and switch to TAG: [SunNetwork-v1.0.2 ](https://github.com/tronprotocol/sun-network/releases/tag/SunNetwork-v1.0.2) .

Or use Release: [SunNetwork-v1.0.2](https://github.com/tronprotocol/sun-network/releases/tag/SunNetwork-v1.0.2) source code.

```
cd sun-network/
cd dapp-chain/side-chain/
./gradlew clean build
ll build/libs/FullNode.jar
```

### Copy JAR

```
#copy Jar package to execute path
cp build/libs/FullNode.jar ../../.. 
```

Copy  Jar package from Release: [SunNetwork-v1.0.2](https://github.com/tronprotocol/sun-network/releases/tag/SunNetwork-v1.0.2) or built from source code to execution path.

Then replace the config.conf file with the [latest version](https://raw.githubusercontent.com/tronprotocol/sun-network/SunNetwork-v1.0.2/documentation/configs/dapp_chain_config.conf). **This is a mandatory operation**.

### Execute JAR

```
# cat old fullnode process
jps -l
# kill -9 old fullnode process id
# execute new java program
nohup java -jar FullNode.jar -c config.conf > start.log 2>&1 & echo $! > fullnode-pid.txt
# cat log with Normal execution
tail -F logs/tron.log
```


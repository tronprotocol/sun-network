# JAVA SDK

使用 sdk 第一步，需初始化 sdk，首先实现 IMultiTransactionSign，IServerConfig 两个接口。

```
sdk = new SunNetwork();
SunNetworkResponse<Integer> ret = sdk.init(new ServerConfigImpl(), new MultiSignTransactionImpl());
```

sdk 中有两个 service。

```java
//主链接口
sdk.getMainChainService();
//侧链接口
sdk.getSideChainService();
//主侧链交互接口
sdk.getCrossChainService();
```

## 链间交互接口介绍

```java
-------------mapping------------------
//由TRC20合约在主链上的部署者调用，完成从主链将TRC20合约映射到侧链，会在侧链部署标准的20合约与主链TRC20合约对应
SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService().mappingTrc20(trxHash, feeLimit);
//参数：trxHash，主链上部署TRC20合约的deployContract交易id；feeLimit，触发合约时的能量消耗上限
//返回值：使用sdk.getSideChainService().getTransactionInfoById(txid)查询resp.getData().getTrxId()获得此次mapping操作的nonce值

//由TRC721合约在主链上的部署者调用，完成从主链将TRC721合约映射到侧链，会在侧链部署标准的TRC721合约与主链TRC721合约对应
SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService().mappingTrc721(trxHash, feeLimit);
//参数：trxHash，主链上部署TRC721合约的deployContract交易id；feeLimit，触发合约时的能量消耗上限
//返回值：使用sdk.getSideChainService().getTransactionInfoById(txid)查询resp.getData().getTrxId()获得此次mapping操作的nonce值

-------------deposit------------------
//从主链质押一定数量的TRX到侧链
SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService().depositTrx(num, feeLimit);
//参数：num，TRX数量(单位SUN)；feeLimit，触发合约时的能量消耗上限
//返回值：使用sdk.getSideChainService().getTransactionInfoById(txid)查询resp.getData().getTrxId()获得此次deposit操作的nonce值

//从主链质押一定数量的指定TRC10到侧链
SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService().depositTrc10(tokenId, tokenValue, feeLimit);
//参数：tokenId，TRC10的TOKENID；tokenValue，TRC10资产的数量；feeLimit，触发合约时的能量消耗上限
//返回值：使用sdk.getSideChainService().getTransactionInfoById(txid)查询resp.getData().getTrxId()获得此次deposit操作的nonce值

//从主链质押一定数量的指定TRC20到侧链
SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService().depositTrc20(contractAddrStr, num, feeLimit);
//参数：contractAddrStr，主链TRC20合约地址；num，TRC20资产数量；feeLimit，触发合约时的能量消耗上限
//返回值：使用sdk.getSideChainService().getTransactionInfoById(txid)查询resp.getData().getTrxId()获得此次deposit操作的nonce值

//从主链质押一定数量的指定TRC721到侧链
SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService().depositTrc721(contractAddrStr, num, feeLimit);
//参数：contractAddrStr，主链TRC721合约地址；num，TRC721资产数量；feeLimit，触发合约时的能量消耗上限
//返回值：使用sdk.getSideChainService().getTransactionInfoById(txid)查询resp.getData().getTrxId()获得此次deposit操作的nonce值

//从主链质押一定数量的指定TRC721到侧链
SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService().depositTrc721(contractAddrStr, num, feeLimit);
//参数：contractAddrStr，主链TRC721合约地址；num，TRC721资产数量；feeLimit，触发合约时的能量消耗上限
//返回值：使用sdk.getSideChainService().getTransactionInfoById(txid)查询resp.getData().getTrxId()获得此次deposit操作的nonce值

-------------withdraw------------------

//从侧链退出一定数量的TRX到主链
SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService().withdrawTrx(num, feeLimit);
//参数：num，TRX数量(单位SUN)；feeLimit，触发合约时的能量消耗上限
//返回值：使用sdk.getSideChainService().getTransactionInfoById(txid)查询resp.getData().getTrxId()获得此次withdraw操作的nonce值

//从侧链质押一定数量的指定TRC10到主链
SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService().withdrawTrc10(tokenId, tokenValue, feeLimit);
//参数：tokenId，TRC10的TOKENID；tokenValue，TRC10资产的数量；feeLimit，触发合约时的能量消耗上限
//返回值：使用sdk.getSideChainService().getTransactionInfoById(txid)查询resp.getData().getTrxId()获得此次withdraw操作的nonce值

//从侧链质押一定数量的指定TRC20到主链
SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService().withdrawTrc20(contractAddrStr, num, feeLimit);
//参数：contractAddrStr，侧链TRC20合约地址；num，TRC20资产数量；feeLimit，触发合约时的能量消耗上限
//返回值：使用sdk.getSideChainService().getTransactionInfoById(txid)查询resp.getData().getTrxId()获得此次withdraw操作的nonce值

//从侧链质押一定数量的指定TRC721到主链
SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService().withdrawTrc721(contractAddrStr, num, feeLimit);
//参数：contractAddrStr，侧链TRC721合约地址；num，TRC721资产数量；feeLimit，触发合约时的能量消耗上限
//返回值：使用sdk.getSideChainService().getTransactionInfoById(txid)查询resp.getData().getTrxId()获得此次withdraw操作的nonce值

-------------retry------------------
//重试没有成功的主链deposit操作
SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService().retryDeposit(nonce, feeLimit);
//参数：nonce，withdraw操作的nonce值；feeLimit，触发合约时的能量消耗上限

//重试没有成功的侧链withdraw操作
SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService().retryWithdraw(nonce, feeLimit);
//参数：nonce，withdraw操作的nonce值；feeLimit，触发合约时的能量消耗上限

//重试没有成功的主链mapping操作
SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService().retryMapping(nonce, feeLimit);
//参数：nonce，mapping操作的nonce值；feeLimit，触发合约时的能量消耗上限
```

## 侧链接口介绍

```java
//由侧链的资助者向侧链基金池注入资金
SunNetworkResponse<TransactionResponse> resp = sdk.getMainChainService().fundInject(amount);
//参数：amount，向侧链基金池注入资金数量（单位SUN）；

//查询侧链所有提案列表信息
SunNetworkResponse<TransactionResponse> resp = sdk.getSideChainService().listProposals();
//参数：无

//根据提案号查询侧链提案信息
SunNetworkResponse<TransactionResponse> resp = getSideChainService().getProposal(id);
//参数：id，提案号；

//查询所有侧链区块链委员会可以设置的参数
SunNetworkResponse<TransactionResponse> resp = getSideChainService().getSideChainParameters();
//参数：无
```

## 主链接口介绍

```java
//查询主链所有提案列表信息
SunNetworkResponse<TransactionResponse> resp = sdk.getMainChainService().listProposals();
//参数：无

//根据提案号查询侧链提案信息
SunNetworkResponse<TransactionResponse> resp = getMainChainService().getProposal(id);
//参数：id，提案号；

//查询所有主链区块链委员会可以设置的参数
SunNetworkResponse<TransactionResponse> resp = getMainChainService().getChainParameters();
//参数：无
```

## 公共接口介绍

```java
//获取sdk当前用户的地址
SunNetworkResponse<byte[]> resp = getChainService().getAddress()

//获取sdk当前用户的TRX
SunNetworkResponse<long> resp = getChainService().getBalance()

//根据地址获得账户信息
SunNetworkResponse<Account> result = getChainService().getAccount(address);

//进行转账TRX操作
SunNetworkResponse<TransactionResponse> = getChainService().sendCoin(toAddress, amount);

//进行转账TRC10操作
SunNetworkResponse<TransactionResponse> = getChainService().transferAsset(toAddress, assertName, amount);
```

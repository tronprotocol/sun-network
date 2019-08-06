# JAVA SDK

To use sdk first step, you need to initialize sdk, first implement IMultiTransactionSign, IServerConfig two interfaces.

```
Sdk = new SunNetwork();
SunNetworkResponse<Integer> ret = sdk.init(new ServerConfigImpl(), new MultiSignTransactionImpl());
```

There are two services in sdk.

```java
//Main chain interface
sdk.getMainChainService();
//side chain interface
sdk.getSideChainService();
//Main side chain interaction interface
sdk.getCrossChainService();
```

## Inter-chain Interface Introduction

```java
-------------mapping------------------
//Called by the deployer of the TRC20 contract on the main chain to complete the mapping of the TRC20 contract from the main chain to the side chain, the standard 20 contract deployed in the side chain corresponds to the main chain TRC20 contract.
SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService().mappingTrc20(trxHash, feeLimit);
//Parameter: trxHash, the deployContract transaction id of the TRC20 contract deployed on the main chain; feeLimit, the upper limit of energy consumption when the contract is triggered
//Return value: Use sdk.getSideChainService().getTransactionInfoById(txid) to query resp.getData().getTrxId() to get the nonce value of this mapping operation.

//Called by the deployer of the TRC721 contract on the main chain to complete the mapping of the TRC721 contract from the main chain to the side chain, the standard TRC721 contract will be deployed in the side chain corresponding to the main chain TRC721 contract.
SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService().mappingTrc721(trxHash, feeLimit);
//Parameter: trxHash, the deployContract transaction id of the TRC721 contract deployed on the main chain; feeLimit, the upper limit of energy consumption when the contract is triggered
//Return value: Use sdk.getSideChainService().getTransactionInfoById(txid) to query resp.getData().getTrxId() to get the nonce value of this mapping operation.

-------------deposit------------------
//Pledge a certain number of TRX from the main chain to the side chain
SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService().depositTrx(num, feeLimit);
//Parameters: num, the number of TRX (unit SUN); feeLimit, the upper limit of energy consumption when the contract is triggered
//Return value: Use sdk.getSideChainService().getTransactionInfoById(txid) to query resp.getData().getTrxId() to get the nonce value of this deposit operation.

//Pledge a certain number of designated TRC10 from the main chain to the side chain
SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService().depositTrc10(tokenId, tokenValue, feeLimit);
//Parameters: tokenId, TOCID of TRC10; tokenValue, the number of TRC10 assets; feeLimit, the upper limit of energy consumption when the contract is triggered
//Return value: Use sdk.getSideChainService().getTransactionInfoById(txid) to query resp.getData().getTrxId() to get the nonce value of this deposit operation.

//Pledge a certain number of designated TRC20 from the main chain to the side chain
SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService().depositTrc20(contractAddrStr, num, feeLimit);
//Parameters: contractAddrStr, main chain TRC20 contract address; num, TRC20 asset quantity; feeLimit, the upper limit of energy consumption when the contract is triggered
//Return value: Use sdk.getSideChainService().getTransactionInfoById(txid) to query resp.getData().getTrxId() to get the nonce value of this deposit operation.

//Pledge a certain number of designated TRC721 from the main chain to the side chain
SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService().depositTrc721(contractAddrStr, num, feeLimit);
//Parameters: contractAddrStr, main chain TRC721 contract address; num, TRC721 asset quantity; feeLimit, the upper limit of energy consumption when the contract is triggered
//Return value: Use sdk.getSideChainService().getTransactionInfoById(txid) to query resp.getData().getTrxId() to get the nonce value of this deposit operation.

//Pledge a certain number of designated TRC721 from the main chain to the side chain
SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService().depositTrc721(contractAddrStr, num, feeLimit);
//Parameters: contractAddrStr, main chain TRC721 contract address; num, TRC721 asset quantity; feeLimit, the upper limit of energy consumption when the contract is triggered
//Return value: Use sdk.getSideChainService().getTransactionInfoById(txid) to query resp.getData().getTrxId() to get the nonce value of this deposit operation.

-------------withdraw------------------

//Exit a certain number of TRX from the side chain to the main chain
SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService().withdrawTrx(num, feeLimit);
//Parameters: num, the number of TRX (unit SUN); feeLimit, the upper limit of energy consumption when the contract is triggered
//Return value: Use sdk.getSideChainService().getTransactionInfoById(txid) to query resp.getData().getTrxId() to get the nonce value of this withdraw operation.

//Pledge a certain number of designated TRC10 from the side chain to the main chain
SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService().withdrawTrc10(tokenId, tokenValue, feeLimit);
//Parameters: tokenId, TOCID of TRC10; tokenValue, the number of TRC10 assets; feeLimit, the upper limit of energy consumption when the contract is triggered
//Return value: Use sdk.getSideChainService().getTransactionInfoById(txid) to query resp.getData().getTrxId() to get the nonce value of this withdraw operation.

//Pledge a certain number of designated TRC20 from the side chain to the main chain
SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService().withdrawTrc20(contractAddrStr, num, feeLimit);
//Parameters: contractAddrStr, side chain TRC20 contract address; num, TRC20 asset quantity; feeLimit, the upper limit of energy consumption when the contract is triggered
//Return value: Use sdk.getSideChainService().getTransactionInfoById(txid) to query resp.getData().getTrxId() to get the nonce value of this withdraw operation.

//Pledge a certain number of designated TRC721 from the side chain to the main chain
SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService().withdrawTrc721(contractAddrStr, num, feeLimit);
//Parameters: contractAddrStr, side chain TRC721 contract address; num, TRC721 asset quantity; feeLimit, the upper limit of energy consumption when the contract is triggered
//Return value: Use sdk.getSideChainService().getTransactionInfoById(txid) to query resp.getData().getTrxId() to get the nonce value of this withdraw operation.

-------------retry------------------
//Retry the unsuccessful main chain deposit operation
SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService().retryDeposit(nonce, feeLimit);
//Parameters: nonce, the nonce value of the withdraw operation; feeLimit, the upper limit of energy consumption when the contract is triggered

//Retry the unsuccessful side chain withdraw operation
SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService().retryWithdraw(nonce, feeLimit);
//Parameters: nonce, the nonce value of the withdraw operation; feeLimit, the upper limit of energy consumption when the contract is triggered

//Retry the unsuccessful main chain mapping operation
SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService().retryMapping(nonce, feeLimit);
//Parameter: nonce, the nonce value of the mapping operation; feeLimit, the upper limit of energy consumption when the contract is triggered
```

## Side chain Interface Introduction

```java
//Investors from the side chain will inject funds into the side chain fund pool
SunNetworkResponse<TransactionResponse> resp = sdk.getMainChainService().fundInject(amount);
//Parameters: amount, the amount of funds injected into the side chain fund pool (unit SUN);

//Query side chain all proposal list information
SunNetworkResponse<TransactionResponse> resp = sdk.getSideChainService().listProposals();
//Parameter: None

//Query side chain proposal information according to the proposal number
SunNetworkResponse<TransactionResponse> resp = getSideChainService().getProposal(id);
//Parameter: id, proposal number;

//Query all the parameters of the side chain blockchain committee can be set
SunNetworkResponse<TransactionResponse> resp = getSideChainService().getSideChainParameters();
//Parameter: None
```

## Main chain Interface Introduction

```java
//Query all the proposal list information of the main chain
SunNetworkResponse<TransactionResponse> resp = sdk.getMainChainService().listProposals();
//Parameter: None

//Query side chain proposal information according to the proposal number
SunNetworkResponse<TransactionResponse> resp = getMainChainService().getProposal(id);
//Parameter: id, proposal number;

//Query all the parameters of the main chain blockchain committee can be set
SunNetworkResponse<TransactionResponse> resp = getMainChainService().getChainParameters();
//Parameter: None
```

## Public interfaces introduction

```java
//Get the address of the current user of sdk
SunNetworkResponse<byte[]> resp = getChainService().getAddress()

//Get the current user's TRX sdk
SunNetworkResponse<long> resp = getChainService().getBalance()

//Get account information based on the address
SunNetworkResponse<Account> result = getChainService().getAccount(address);

//Transfer the TRX operation
SunNetworkResponse<TransactionResponse> = getChainService().sendCoin(toAddress, amount);

//Transfer the TRC10 operation
SunNetworkResponse<TransactionResponse> = getChainService().transferAsset(toAddress, assertName, amount);
```

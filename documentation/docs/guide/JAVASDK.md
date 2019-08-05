# JAVA SDK

The first step in using sdk is to initialize sdk, first implementing IMultiTransactionSign, IServerConfig and two interfaces.

```
Sdk = new SunNetwork();
SunNetworkResponse<Integer> ret = sdk.init(new ServerConfigImpl(), new MultiSignTransactionImpl());
```

There are three services in sdk.

```java
//Main chain interface
sdk.getMainChainService();
//side chain interface
sdk.getSideChainService();
//Main side chain interaction interface
sdk.getCrossChainService();
```

## Inter-chain Interface Introduction

### mapping

```java
// Called by the deployer of the TRC20 contract on the main chain to complete the mapping of the TRC20 contract from the main chain to the side chain, the standard 20 contract deployed in the side chain corresponds to the main chain TRC20 contract.
SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService().mappingTrc20(trxHash, feeLimit);
//Return value: Use sdk.getSideChainService().getTransactionInfoById(txid) to query resp.getData().getTrxId() to get the nonce value of this mapping operation.
```

| Parameter | Description                                                                   |
| --------- | ----------------------------------------------------------------------------- |
| trxHash   | deployContract transaction for deploying TRC20 contracts on the main chain id |
| feeLimit  | Maximum energy consumption when triggering a contract                         |

```java
// Called by the deployer of the TRC721 contract on the main chain to complete the mapping of the TRC721 contract from the main chain to the side chain, the standard TRC721 contract will be deployed in the side chain corresponding to the main chain TRC721 contract.
SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService().mappingTrc721(trxHash, feeLimit);
//Return value: Use sdk.getSideChainService().getTransactionInfoById(txid) to query resp.getData().getTrxId() to get the nonce value of this mapping operation.
```

| Parameter | Description                                                         |
| --------- | ------------------------------------------------------------------- |
| trxHash   | deployContract transaction id for TRC721 contract on the main chain |
| feeLimit  | Maximum energy consumption when triggering a contract               |

### deposit

```java
//Pledge a certain number of TRX from the main chain to the side chain
SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService().depositTrx(num, feeLimit);
//Return value: Use sdk.getSideChainService().getTransactionInfoById(txid) to query resp.getData().getTrxId() to get the nonce value of this deposit operation.
```

| Parameter | Description                                           |
| --------- | ----------------------------------------------------- |
| num       | TRX Quantity (Unit SUN)                               |
| feeLimit  | Maximum energy consumption when triggering a contract |

```java
//Pledge a certain number of designated TRC10 from the main chain to the side chain
SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService().depositTrc10(tokenId, tokenValue, feeLimit);
//Return value: Use sdk.getSideChainService().getTransactionInfoById(txid) to query resp.getData().getTrxId() to get the nonce value of this deposit operation.
```

| Parameter  | Description                                           |
| ---------- | ----------------------------------------------------- |
| tokenId    | TOCID of TRC10                                        |
| tokenValue | TRC10 Quantity of Assets                              |
| feeLimit   | Maximum energy consumption when triggering a contract |

```java
//Pledge a certain number of designated TRC20 from the main chain to the side chain
SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService().depositTrc20(contractAddrStr, num, feeLimit);
//Return value: Use sdk.getSideChainService().getTransactionInfoById(txid) to query resp.getData().getTrxId() to get the nonce value of this deposit operation.
```

| Parameter       | Description                                           |
| --------------- | ----------------------------------------------------- |
| contractAddrStr | Main Chain TRC20 Contract Address                     |
| num             | TRC20 Asset Quantity                                  |
| feeLimit        | Maximum energy consumption when triggering a contract |

```java
//Pledge a certain number of designated TRC721 from the main chain to the side chain
SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService().depositTrc721(contractAddrStr, num, feeLimit);
//Return value: Use sdk.getSideChainService().getTransactionInfoById(txid) to query resp.getData().getTrxId() to get the nonce value of this deposit operation.
```

| Parameter       | Description                                           |
| --------------- | ----------------------------------------------------- |
| ContractAddrStr | Main Chain TRC721 Contract Address                    |
| num             | TRC721 Asset Quantity                                 |
| feeLimit        | Maximum energy consumption when triggering a contract |

### withdraw

```java
//Exit a certain number of TRX from the side chain to the main chain
SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService().withdrawTrx(num, feeLimit);
//Return value: Use sdk.getSideChainService().getTransactionInfoById(txid) to query resp.getData().getTrxId() to get the nonce value of this withdraw operation.
```

| Parameter | Description                                           |
| --------- | ----------------------------------------------------- |
| num       | TRX Quantity (Unit SUN)                               |
| feeLimit  | Maximum energy consumption when triggering a contract |

```java
//Pledge a certain number of designated TRC10 from the side chain to the main chain
SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService().withdrawTrc10(tokenId, tokenValue, feeLimit);
//Return value: Use sdk.getSideChainService().getTransactionInfoById(txid) to query resp.getData().getTrxId() to get the nonce value of this withdraw operation.
```

| Parameter  | Description                                           |
| ---------- | ----------------------------------------------------- |
| tokenId    | TOCID of TRC10                                        |
| tokenValue | TRC10 Quantity of Assets                              |
| feeLimit   | Maximum energy consumption when triggering a contract |

```java
//Pledge a certain number of designated TRC20 from the side chain to the main chain
SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService().withdrawTrc20(contractAddrStr, num, feeLimit);
//Return value: Use sdk.getSideChainService().getTransactionInfoById(txid) to query resp.getData().getTrxId() to get the nonce value of this withdraw operation.
```

| Parameter       | Description                                           |
| --------------- | ----------------------------------------------------- |
| contractAddrStr | Sidechain TRC20 Contract Address                      |
| num             | TRC20 Asset Quantity                                  |
| feeLimit        | Maximum energy consumption when triggering a contract |

```java
//Pledge a certain number of designated TRC721 from the side chain to the main chain
SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService().withdrawTrc721(contractAddrStr, num, feeLimit);
//Return value: Use sdk.getSideChainService().getTransactionInfoById(txid) to query resp.getData().getTrxId() to get the nonce value of this withdraw operation.
```

| Parameter       | Description                                           |
| --------------- | ----------------------------------------------------- |
| contractAddrStr | Sidechain TRC721 Contract Address                     |
| num             | TRC721 Asset Quantity                                 |
| feeLimit        | Maximum energy consumption when triggering a contract |

### retry

```java
//Retry the unsuccessful main chain deposit operation
SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService().retryDeposit(nonce, feeLimit);
```

| Parameter | Description                                           |
| --------- | ----------------------------------------------------- |
| nonce     | deposit operation nonce value                         |
| feeLimit  | Maximum energy consumption when triggering a contract |

```java
//Retry the unsuccessful side chain withdraw operation
SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService().retryWithdraw(nonce, feeLimit);
```

| Parameter | Description                                           |
| --------- | ----------------------------------------------------- |
| nonce     | withdraw operation nonce value                        |
| feeLimit  | Maximum energy consumption when triggering a contract |

```java
//Retry the unsuccessful main chain mapping operation
SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService().retryMapping(nonce, feeLimit);
```

| Parameter | Description                                           |
| --------- | ----------------------------------------------------- |
| nonce     | mapping operation's nonce value                       |
| feeLimit  | Maximum energy consumption when triggering a contract |

## Side chain Interface Introduction

```java
//Investors from the side chain will inject funds into the side chain fund pool
SunNetworkResponse<TransactionResponse> resp = sdk.getMainChainService().fundInject(amount);
```

| Parameter | Description                                                         |
| --------- | ------------------------------------------------------------------- |
| amount    | Quantity of funds injected into the side chain fund pool (unit SUN) |

```java
//Query side chain all proposal list information
SunNetworkResponse<TransactionResponse> resp = sdk.getSideChainService().listProposals();
//Parameter: None

```

```java
//Query side chain proposal information according to the proposal number
SunNetworkResponse<TransactionResponse> resp = getSideChainService().getProposal(id);
```

| Parameter | Description     |
| --------- | --------------- |
| id        | Proposal number |

```java
//Query all the parameters of the side chain blockchain committee can be set
SunNetworkResponse<TransactionResponse> resp = getSideChainService().getSideChainParameters();
```

## Main chain Interface Introduction

```java
//Query all the proposal list information of the main chain
SunNetworkResponse<TransactionResponse> resp = sdk.getMainChainService().listProposals();
```

```java
//Query side chain proposal information according to the proposal number
SunNetworkResponse<TransactionResponse> resp = getMainChainService().getProposal(id);
```

| Parameter | Description     |
| --------- | --------------- |
| id        | Proposal number |

```java
//Query all the parameters of the main chain blockchain committee can be set
SunNetworkResponse<TransactionResponse> resp = getMainChainService().getChainParameters();
```

## Public Interface Introduction

```java
//Get the address of the current user of sdk
SunNetworkResponse<byte[]> resp = getChainService().getAddress()
```

```java
//Get the current user's TRX sdk
SunNetworkResponse<long> resp = getChainService().getBalance()
```

```java
//Get account information based on the address
SunNetworkResponse<Account> result = getChainService().getAccount(address);
```

```java
//Transfer the TRX operation
SunNetworkResponse<TransactionResponse> = getChainService().sendCoin(toAddress, amount);
```

```java
//Transfer the TRC10 operation
SunNetworkResponse<TransactionResponse> = getChainService().transferAsset(toAddress, assertName, amount);
```

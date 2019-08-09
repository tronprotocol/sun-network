# SunWeb

SunWeb 是为Tron Sun-Network 开发的一款 js-sdk 工具，并且继承自 [TronWeb](https://developers.tron.network/docs/tron-web-intro)。 SunWeb 里面封装了mainchain 和 sidechain 两个对象，他们本质上就是 TronWeb 的对象实例，因此里面包含了 TronWeb 实例的所有属性和方法。例如用户可以使用 sunweb.mainchain.trx.getBalance() 来获取主网上的 balance。除此之外，SunWeb 增加了一些新的方法来支持主链和侧链的交互，如 deposit, withdraw, mapping, approve, injectFund 等操作，可参考[源码](https://github.com/tronprotocol/sun-network/tree/develop/js-sdk)。SunWeb 详细使用方法如下。

## SunWeb 类

###### 创建 SubWeb 实例

为了在应用中可以使用 SunWeb，你需要创建一个 sunWeb 实例。你可以定义两个js对象，如 mainOptions 和sideOptions，他们分别包含以下属性：

- fullNode

- solidityNode

- eventServer

为了兼容 TronWeb，我们也可以只设置一个 fullHost 属性，

- fullHost

但是相比于第二种，第一种具有更高的优先级。

除此之外，你还需要提供主网和侧链的 Gateway 地址和侧链 ID：

- mainGatewayAddress

- sideGatewayAddress

- sideChainId

- privateKey (optional)

例如只提供 fullHost 属性创建 SunWeb 实例

```javascript
const sunWeb = new SunWeb({
  fullHost: 'https://mainapi.trongrid.io'
  }, {
  fullHost: 'https://sideapi.trongrid.io'
  },
  mainGatewayAddress,
  sideGatewayAddress,
  sideChainId,
  privateKey: '...');
```

下面是提供不同服务器来创建 SunWeb 实例

```javascript
const sunWeb = new SunWeb({
  fullNode: 'http://fullnode.tron.network',
  solidityNode: 'http://solidity.tron.network',
  eventServer: 'http://mainapi.trongrid.io'
  }, {
  fullNode: 'http://fullnode.sun.network',
  solidityNode: 'http://solidity.sun.network',
  eventServer: 'http://sideapi.trongrid.io'
  },
  mainGatewayAddress,
  sideGatewayAddress,
  sideChainId,
  privateKey: '...');
```

如果需要连接 Sun-Network 测试网，可以创建如下的 SunWeb 实例

```javascript
const sunWeb = new SunWeb({
  fullNode: 'http://47.252.84.158:8090',
  solidityNode: 'http://47.252.84.158:8090',
  eventServer: 'http://47.252.84.141:8080'
  }, {
  fullNode: 'http://47.252.85.90:8090',
  solidityNode: 'http://47.252.85.90:8091',
  eventServer: 'http://47.252.85.90:8090'
  },
  'TGHxhFu4jV4XqMGmk3tEQdSeihWVHE9kBP',
  'TBHr5KpbA7oACUysTKxHiAD7c6X6nkZii1',
  '41455CB714D762DC46D490EAB37BBA67B0BA910A59',
  privateKey);
```

## SunWeb新增函数

## 质押资产

资产质押的作用是将主链资产质押到侧链。

#### depositTrx

###### 质押TRX

```javascript
// Format
sunWeb.depostiTrx(callValue, feeLimit, options);

// example
sunWeb.depositTrx(100000000, 1000000);
```

###### Arguments

| Parameter | Description                             | Type                   | Options  |
| --------- | --------------------------------------- | ---------------------- | -------- |
| callValue | Amount of TRX (Units in SUN) to deposit | Integer (Units in SUN) | Required |
| feeLimit  | Cost limit                              | Integer, long          | Required |
| options   | The permissions Id                      | Object                 | Optional |

#### depositTrc10

###### 质押 TRC10

```javascript
// format
sunWeb.depositTrc10(tokenId, tokenValue, feeLimit, options);

// example
sunWeb.depositTrc10(100059, 10000000, 100000);
```

###### Arguments

| Parameter  | Description                                     | Type                   | Options  |
| ---------- | ----------------------------------------------- | ---------------------- | -------- |
| tokenId    | Token Id of trc10                               | Integer                | Required |
| tokenValue | Amount of trc10 token (Units in SUN) to deposit | Integer (Units in SUN) | Required |
| feeLimit   | Cost limit                                      | Integer, long          | Required |
| options    | The permissions Id                              | Object                 | Optional |

#### depositTrc20

###### 质押 TRC20

在质押 TRC20 之前，必须先 mapping TRC20 合约到侧链，即调用 sunWeb.mappingTrc20(…) 函数。Mapping 合约后，再 approve TRC20, 即调用 sunWeb.approveTrc20(…) 函数。这两步都成功后，才能成功完成depositTrc20 操作。

```javascript
// format
sunWeb.depositTrc10(num, feeLimit, contractAddress, options);

// example
sunWeb.depositTrc10(1000000, 1000000, 'TD9Jrm546pGkzRu7K5nitMxk8nn75wXNkQ');
```

###### Arguments

| Parameter       | Description                               | Type          | Options  |
| --------------- | ----------------------------------------- | ------------- | -------- |
| num             | Amount of TRC20 (Units in SUN) to deposit | Integer       | Required |
| feeLimit        | Cost limit                                | Integer, long | Required |
| contractAddress | Main Chain TRC20 Contract Address         | String        | Required |
| options         | The permissions Id                        | Object        | Optional |

#### depositTrc721

###### 质押 TRC721

与质押 TRC20 类似，先需要 mappingTrc721 和 approveTrc721。

```javascript
// format
sunWeb.depositTrc721(id, feeLimit, contractAddress, options);

// example
sunWeb.depositTrc10(1000000, 1000000, 'TCLRqK6aP2xsCZWhE2smkYzdRHf9uvyz5P');
```

###### Arguments

| Parameter       | Description                        | Type          | Options  |
| --------------- | ---------------------------------- | ------------- | -------- |
| id              | Id of TRC721 to deposit            | Integer       | Required |
| feeLimit        | cost limit                         | Integer, long | Required |
| contractAddress | Main Chain TRC721 Contract Address | String        | Required |
| options         | The permissions Id                 | Object        | Optional |

## 资产授权

在质押 TRC20 和 TRC721 之前，需要先调用 approveTrc20 和 approveTrc721 获得相应资产授权。
### approveTrc20
###### 授权 TRC20
```javascript
// format
sunWeb.approveTrc20(num, feeLimit, contractAddress, options);

// example
sunWeb.approveTrc20(10000, 10000000, 'TGKuXDnvdHv9RNE6BPXUtNLK2FrVMBDAuA');
```

###### Arguments

| Parameter       | Description                       | Type          | Options  |
| --------------- | --------------------------------- | ------------- | -------- |
| num             | Num of TRC20                      | Integer       | Required |
| contractAddress | Main Chain TRC20 Contract Address | String        | Required |
| feeLimit        | Cost limit                        | Integer, long | Required |
| options         | The permissions Id                | Object        | Optional |

### approveTrc721

###### 授权 TRC721

```javascript
// format
sunWeb.approveTrc721(id, feeLimit, contractAddress, options);

// example
sunWeb.approveTrc721(100, 10000000, 'TUxDmFbEceGgjWCb6rLVcrFgnsWwofPdPq');

```

###### Arguments

| Parameter       | Description                        | Type          | Options  |
| --------------- | ---------------------------------- | ------------- | -------- |
| id              | Id of TRC721                       | Integer       | Required |
| contractAddress | Main Chain TRC721 Contract Address | String        | Required |
| feeLimit        | Cost limit                         | Integer, long | Required |
| options         | The permissions Id                 | Object        | Optional |

## 资产映射

用户必须先将主链合约资产 TRC20/TRC721 映射到侧链以后，才能将合约资产质押到侧链。

### mappingTrc20

###### 映射TRC20

```javascript
// format
mappingTrc20(trxHash, mappingFee, feeLimit, options);

// example
mappingTrc20('548442d9080605a60adf1d30cc126a2b9c6308cbe9ec224f8c67a6c2590fa299', 100000, 10000, options);

```

###### Arguments

| Parameter  | Description                                                  | Type                  | Options  |
| ---------- | ------------------------------------------------------------ | --------------------- | -------- |
| trxHash    | The hash value of the transaction for the main chain deployment TRC20 contract | Hex string            | Required |
| mappingFee | Mapping Fee                                                  | Integer(Units in SUN) | Required |
| feeLimit   | cost limit                                                   | Integer, long         | Required |
| options    | The permissions Id                                           | Object                | Optional |

### mappingTrc721

###### 映射 TRC721

```javascript
// format
mappingTrc721(trxHash, mappingFee, feeLimit, options);

// example
mappingTrc721('548442d9080605a60adf1d30cc126a2b9c6308cbe9ec224f8c67a6c2590fa299', 100000, 100000, options);

```

###### Arguments

| Parameter  | Description                                                  | Type                  | Options  |
| ---------- | ------------------------------------------------------------ | --------------------- | -------- |
| trxHash    | The hash value of the transaction for the main chain deployment TRC721 contract | Hex string            | Required |
| mappingFee | Mapping Fee                                                  | Integer(Units in SUN) | Required |
| feeLimit   | cost limit                                                   | Integer, long         | Required |
| options    | The permissions Id                                           | Object                | Optional |

## 提取资产

用户可将资产从侧链提取回主链，提取资产操作由 SunWeb 向侧链发送命令。

#### withdrawTrx

###### 提取TRX

```javascript
// Format
sunWeb.withdrawTrx(callValue, withdrawFee, feeLimit, options);

// example
sunWeb.withdrawTrx(100000000, 1000, 1000000);

```

###### Arguments

| Parameter   | Description                             | Type                   | Options  |
| ----------- | --------------------------------------- | ---------------------- | -------- |
| callValue   | Amount of TRX (Units in SUN) to deposit | Integer (Units in SUN) | Required |
| withdrawFee | Withdraw Fee                            | Integer(Units in SUN)  | Required |
| feeLimit    | Cost limit                              | Integer, long          | Required |
| options     | The permissions Id                      | Object                 | Optional |

### withdrawTrc10

###### 提取 TRC10

```javascript
// format
sunWeb.withdrawTrc10(tokenId, tokenValue, withdrawFee, feeLimit, options);

// example
sunWeb.withdrawTrc10(100059, 10000000, 1000, 100000);

```

###### Arguments

| Parameter   | Description                                     | Type                   | Options  |
| ----------- | ----------------------------------------------- | ---------------------- | -------- |
| tokenId     | Token Id of TRC10                               | Integer                | Required |
| tokenValue  | Amount of TRC10 token (Units in SUN) to deposit | Integer (Units in SUN) | Required |
| withdrawFee | Withdraw Fee                                    | Integer(Units in SUN)  | Required |
| feeLimit    | Cost limit                                      | Integer, long          | Required |
| options     | The permissions Id                              | Object                 | Optional |

#### withdrawTrc20

###### 提取 TRC20

```javascript
// format
sunWeb.withdrawTrc20(num, withdrawFee, feeLimit, contractAddress, options);

// example
sunWeb.withdrawTrc20(10000, 1000, 10000000, 'TWzXQmDoASGodMss7uPD6vUgLHnkQFX7ok');

```

###### Arguments

| Parameter       | Description                                     | Type                  | Options  |
| --------------- | ----------------------------------------------- | --------------------- | -------- |
| Num             | Num of TRC20                                    | Integer               | Required |
| withdrawFee     | Withdraw Fee                                    | Integer(Units in SUN) | Required |
| feeLimit        | Cost limit                                      | Integer, long         | Required |
| contractAddress | Side Chain TRC20 Contract Address after mapping | Integer               | Required |
| options         | The permissions Id                              | Object                | Optional |

#### withdrawTrc721

###### 提取 TRC721

```javascript
// format
sunWeb.withdrawTrc721(id, withdrawFee, feeLimit, contractAddress, options);

// example
sunWeb.withdrawTrc721(101, 1000, 10000000, 'TA2xrVESq2UcEtDtgPzxNJEiLgxmMVdtFR');

```

###### Arguments

| Parameter       | Description                                     | Type                  | Options  |
| --------------- | ----------------------------------------------- | --------------------- | -------- |
| id              | Id of TRC721                                    | Integer               | Required |
| withdrawFee     | Withdraw Fee                                    | Integer(Units in SUN) | Required |
| feeLimit        | Cost limit                                      | Integer, long         | Required |
| contractAddress | Side Chain TRC20 Contract Address after mapping | Integer               | Required |
| options         | The permissions Id                              | Object                | Optional |

## 注资
注资的主要作用是为侧链的基金池注入资金。

###### injectFund

```javascript
// format
sunWeb.injectFund(num, feeLimit, options);

// example
sunWeb.injectFund(1000, 10000000);

```

###### Arguments

| Parameter | Description        | Type          | Options  |
| --------- | ------------------ | ------------- | -------- |
| num       | num of injecting   | Integer       | Required |
| feeLimit  | Cost limit         | Integer, long | Required |
| options   | The permissions Id | Object        | Optional |

## 重试

#### retryDeposit

```javascript
// format
sunWeb.retryDeposit(nonce, feeLimit, options);

// example
sunWeb.retryDeposit(1000, 10000000);

```

###### Arguments

| Parameter | Description                  | Type          | Options  |
| --------- | ---------------------------- | ------------- | -------- |
| nonce     | Nonce value of asset deposit | Integer       | Required |
| feeLimit  | Cost limit                   | Integer, long | Required |
| options   | The permissions Id           | Object        | Optional |

#### retryMapping

```javascript
// format
sunWeb.retryMapping(nonce, feeLimit, options);

// example
sunWeb.retryMapping(1000, 10000000);

```

###### Arguments

| Parameter | Description                  | Type          | Options  |
| --------- | ---------------------------- | ------------- | -------- |
| nonce     | Nonce value of asset deposit | Integer       | Required |
| feeLimit  | Cost limit                   | Integer, long | Required |
| options   | The permissions Id           | Object        | Optional |

#### retryWithdraw

```javascript
// format
sunWeb.retryWithdraw(nonce, feeLimit, options);

// example
sunWeb.retryWithdraw(1000, 10000000);

```

###### Arguments

| Parameter | Description                  | Type          | Options  |
| --------- | ---------------------------- | ------------- | -------- |
| nonce     | Nonce value of asset deposit | Integer       | Required |
| feeLimit  | Cost limit                   | Integer, long | Required |
| options   | The permissions Id           | Object        | Optional |

## 签名

Sun-network 的签名有一些改变，主链的签名逻辑和TronWeb的保持一致，侧链的签名逻辑有更改。因此，如果需要和TronLink一样弹出签名框，需要分别覆盖sunWeb.mainchain.trx.sign()和sunWeb.sidechain.trx.sign().

```javascript
// format
sign(transaction = false, privateKey = this.sidechain.defaultPrivateKey, useTronHeader = true, multisig = false);
```


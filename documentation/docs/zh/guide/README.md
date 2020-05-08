# 开发者文档

## I. 概述

### 1. Sun-network 计划

Sun Network 计划是波场主网的扩容计划, 包含智能合约应用侧链(DAppChain), 跨链通讯等一些列扩容项目。其中 DAppChain 为波场的侧链项目，着重为波场主网提供无限扩容能力,同时帮助 DApp 以更低的 Energy 消耗，安全高效地在波场上运行。

### 2. DAppChain 特性

DAppChain 是 sun-network 计划的第一个产品。它在支持智能合约交易，兼容主网功能的同时，重点突出低消耗、高安全、强效率的特征，其完整特性如下。

#### 2.1 完全兼容 TRON 主网

侧链使用和主网相同的 DPoS 共识机制, 支持主网的几乎所有功能。TRON 主网用户可以非常方便的切换到侧链上，开发者在侧链上也可以快速的移植或开发新的 DApp。

#### 2.2 侧链高定制化

DAppChain 致力于为所有生态参与者提供一个高度灵活、配置参数丰富、高可定制化的多侧链体系。高可定制化也是 DAppChain 中每条侧链区别于主网的特性之一。

#### 2.3 整个生态更高的 TPS

TRON 主网可以接入多条侧链，每加入一条侧链，意味着 TRON 生态上 TPS 的增加。按照主网 TPS 峰值 2000 计算，多条侧链组成的 TRON 生态的 TPS 可以有 2000 \* SideChainNum 的无限扩容能力。

#### 2.4 很低的资源费用

同等情况下的交易，在侧链上的资源费用仅是主网的若干分之一。这可以帮助 DApp 开发者降低开发运营成本，也有助于吸引更多的小开发者团队参与生态建设。

#### 2.5 保障用户资产安全

第一阶段的 DAppChain 采用侧链的 DPoS 共识机制，同时通过主网及侧链的智能合约以及 Oracle 中继，来保证侧链的交易有效和资产安全，以及主侧链链间交互的安全。

#### 2.6 优秀丰富的 API 以及配套的工具集

我们在兼容波场主网接口的同时，提供了更丰富的跨主侧链 API，来降低用户的学习成本，方便
开发者快速接入。我们提供了 Java 和 JavaScript 的开发包，开发者可以基于这些开发包快速和侧链互操作。

## II. DAppChain

### 1. DAppChain 架构及各部分功能

本节解释 DAppChain 的架构。

![dappchain.png](./../../.vuepress/img/dappchain.png)

如上图所述，DAppChain 的组成分别是：

- MainChain Gateway 合约，负责主网 token 的映射（Mapping），主网用户的资产转入（Deposit）和资产转出（Withdraw）等。

- 多 Oracle 中继节点，负责共同完成主网和侧链的链间交互，认证和转发主网的 deposit、mapping 交易到侧链，侧链的 withdraw 交易转发到主网。

- SideChain Gateway 合约，负责在侧链上, 进出侧链资产的管理。

- SideChain，基于 DPos 机制，借鉴主网但是做了深度定制化。

### 2. 侧链

#### 2.1 概要

侧链作为主链扩容计划的核心，每 3 秒产一个块，采用 DPoS 共识机制。和多 oracle 中继节点共同实现侧链交易以及跨主侧链交易的共识。

#### 2.2 共识机制

和主链相同，侧链采用 DPoS 共识，由多个 witness 构成。witness 扮演者侧链验证者的角色，根据票数的多少轮流验证侧链交易并产块，2/3 的 Wintess 确认交易，即对该交易达成共识。

#### 2.3 现有侧链提案

| **提案号** | **描述**                                                          | **备注**                                     |
| ---------- | ----------------------------------------------------------------- | -------------------------------------------- |
| 1,000,000  | 交易计费开关，当提案设置为 1 时，正常收取交易费                   | 初始为 0, 侧链所有交易消耗的能量和带宽不计费 |
| 1,000,001  | 设置侧链 gateway 合约地址                                         |                                              |
| 1,000,003  | 设置提案过期时间                                                  | 默认值为 1 天                                |
| 1,000,004  | 开启给 Witness 投票                                               | 开启后, 侧链支持投票功能                     |
| 1,000,007  | 侧链激励池地址, 默认为全零地址 T9yD14Nj9j7xAB4dbGeiX9h8unkKHxuWwb |                                              |
| 1,000,008  | 侧链 witness 激励开关，默认关闭                                   | 关闭时, 侧链出块无出块激励                   |
| 1,000,009  | 侧链激励池分发系数                                                |                                              |
| 1,000,010  | 侧链收益累积到激励池的比例                                        |                                              |

侧链特有特性：
Fund: 当前侧链激励池。通过激励, 促进社区参与侧链的治理。侧链收益的一部分将进入该激励池，最终实现侧链收益和验证者节点激励的自平衡；
FundInject：侧链系统接口，用于向激励池注资。任何人都可以向激励池注资，但只有被共识的地址才可得到侧链的收益分成，侧链收益的剩余部分将进入激励池；
mine 函数：precompile 新增函数，用于在侧链上生成从主链 deposit 过来的 TRX，类似 coinbase 交易；
mineToken 函数：precompile 新增函数，用于在侧链上生成从主链 deposit 过来的 TRC10 资产，和 mine 类似；

### 3. 合约

#### 3.1 合约分类

合约由三部分构成：

主链 gateway 合约

侧链 gateway 合约

标准 trc20/trc721 合约

由于侧链上生成的合约是通过系统自动生成的，所以合约中并没有关于abi的描述。因此当需要调用查询类的方法时，需要调用者调用提供的triggerconstantcontract方法以避免生成真正的交易造成扣费。

#### 3.2 主链 gateway 合约与侧链 gateway 合约

##### 3.2.1 trc20/trc721 合约映射操作：

```Solidity
//mappingTRC20 为例
//主链 gateway 合约方法
function mappingTRC20(bytes txId) public onlyNotStop onlyNotPause goDelegateCall payable returns (uint256) {
require(msg.value >= mappingFee, "trc20MappingFee not enough");
if (msg.value > 0) {
bonus += msg.value;
}
address trc20Address = calcContractAddress(txId, msg.sender);
require(trc20Address != sunTokenAddress, "mainChainAddress == sunTokenAddress");
require(mainToSideContractMap[trc20Address] != 1, "trc20Address mapped");
uint256 size;
assembly {size := extcodesize(trc20Address)}
require(size > 0);
userMappingList.push(MappingMsg(trc20Address, DataModel.TokenKind.TRC20, DataModel.Status.SUCCESS));
mainToSideContractMap[trc20Address] = 1;
emit TRC20Mapping(trc20Address, userMappingList.length - 1);
return userMappingList.length - 1;
}

//侧链 gateway 合约方法
function deployDAppTRC20AndMapping(address mainChainAddress, string name, string symbol, uint8 decimals) internal returns (address r) {
address sideChainAddress = new DAppTRC20(address(this), name, symbol, decimals);
mainToSideContractMap[mainChainAddress] = sideChainAddress;
sideToMainContractMap[sideChainAddress] = mainChainAddress;
emit DeployDAppTRC20AndMapping(mainChainAddress, sideChainAddress);
r = sideChainAddress;
}
```

在主链上，由主链 trc20/trc721 合约的部署者，将部署该合约时的交易 id，传入合约，合约验证该交易 id 是否真实部署合约。在侧链上，接收由多个 Oracle 确认的主网合约映射操作，为主链对应的 trc20/trc721 合约，在侧链部署相应的标准 trc20/trc721 合约。

##### 3.2.2 资产质押操作：

```Solidity
//depositTRX 为例
//主链 gateway 合约方法
function depositTRX() payable public onlyNotStop onlyNotPause goDelegateCall returns (uint256) {
require(msg.value > 0, "value must be > 0");
require(msg.value >= depositMinTrx, "value must be >= depositMinTrx");
require(msg.value <= uint64Max, "msg.value must <= uint64Max");
userDepositList.push(DepositMsg(msg.sender, uint64(msg.value), 0, address(0), 0, 0, 0));
emit TRXReceived(msg.sender, uint64(msg.value), userDepositList.length - 1);
return userDepositList.length - 1;
}

//侧链 gateway 合约方法
function depositTRX(address to, uint256 value) internal {
mintTRXContract.call(value);
to.transfer(value);
emit DepositTRX(to, value);
}
```

在主链上，调用合约接收 TRX 和 TRC10 或者已经由开发者进行映射过的 TRC20 与 TRC721 资产，用户将想要质押的主链上的资产冻结在主链 gateway 合约里。在侧链上，与 trc20/trc721 合约映射操作类似，接收由多个 Oracle 确认的主网合约映射操作，侧链 gateway 合约会将对应的资产发给用户对应的侧链账号上。

##### 3.2.3 资产退出操作：

```Solidity
//withdrawTRX 为例
//侧链 gateway 合约方法
function withdrawTRX() payable public onlyNotPause onlyNotStop goDelegateCall returns (uint256 r) {
require(msg.value >= withdrawMinTrx + withdrawFee, "value must be >= withdrawMinTrx+withdrawFee");
if (msg.value > 0) {
bonus += withdrawFee;
}
uint256 withdrawValue = msg.value - withdrawFee;
require(withdrawValue > 0, "withdrawValue must be > 0");
userWithdrawList.push(WithdrawMsg(msg.sender, address(0), 0, withdrawValue, DataModel.TokenKind.TRX, DataModel.Status.SUCCESS));
// burn
address(0).transfer(withdrawValue);
emit WithdrawTRX(msg.sender, withdrawValue, userWithdrawList.length - 1);
r = userWithdrawList.length - 1;
}

//主链 gateway 合约方法
function withdrawTRX(address \_to, uint256 value, uint256 nonce, bytes[] oracleSigns)
public onlyNotStop onlyOracle goDelegateCall {
require(oracleSigns.length <= numOracles, "withdraw TRX signs num > oracles num");
bytes32 dataHash = keccak256(abi.encodePacked(\_to, value, nonce));
bool needWithdraw = checkOracles(dataHash, nonce, oracleSigns);
if (needWithdraw) {
\_to.transfer(value);
// ensure it's not reentrant
emit TRXWithdraw(\_to, value, nonce);
}
}
```

资产退出操作与合约映射操作和资产质押操作不同的是资产退出操作首先发生在在侧链上，与资产质押操作类似，由用户调用合约接收 TRX 和 TRC10 或者已经由开发者进行映射过的 TRC20 与 TRC721 资产，用户将想要退出的侧链上的资产通过侧链 gateway 合约销毁。

#### 3.3 侧链标准

trc20/trc721 合约，仅实现官方标准 trc20/trc721 基本接口，增加只能由侧链 gateway 合约地址调用的在质押资产用到的增发功能资产的方法，和在退出时将资产转至 gateway 合约的方法。

```Solidity
//标准 TRC20 中增加方法
function mint(address to, uint256 value) external onlyGateway {
require(to != address(0));

​ \_totalSupply = \_totalSupply.add(value);
​ \_balances[to] = \_balances[to].add(value);
​ emit Transfer(address(0), to, value);
​ }

​ function withdrawal(uint256 value) payable external returns (uint256 r) {
​ transfer(gateway, value);
​ r = ITRC20Receiver(gateway).onTRC20Received.value(msg.value)(msg.sender, value);
​ }
//标准 TRC721 中增加方法
function mint(address to, uint256 tokenId) external onlyGateway {
​ require(to != address(0));
​ require(!\_exists(tokenId));

​ \_tokenOwner[tokenId] = to;
​ \_ownedTokensCount[to] = \_ownedTokensCount[to].add(1);

​ emit Transfer(address(0), to, tokenId);
​ }

​ function withdrawal(uint256 tokenId) payable external returns (uint256 r) {
​ transfer(gateway, tokenId);
​ r = ITRC721Receiver(gateway).onTRC721Received.value(msg.value)(msg.sender, tokenId);
​ }
```

### 4. 主侧链交互所涉及详细步骤

#### 4.1 概述

为了实现主链与侧链之间的交互通信， DAppchain 实现了三个模块：主链的网关合约、Oracle 服务、侧链的网关合约。

其中 Oracle 通过 Kafka 监听来自主链网关合约、侧链网关合约的消息，实现主侧链之间的交互通信。只有当主链与侧链之间的交互被超过 2/3 的 Oracle 共识，才能够被认为有效的交易。

主侧链之间交互动作主要分为三类：TRC20/TRC721 合约映射、存款功能、取款功能。

![crosschain](./../../.vuepress/img/crosschain.png)

#### 4.2 TRC20/TRC721 合约映射(Mapping)

当用户使用 TRC20/TRC721 合约时，通过在主链触发 TRC20/TRC721 合约映射，将在主链部署的 TRC20/TRC721 合约映射到侧链的对应的合约，即用户无需在侧链手动部署 TRC20/TRC721 合约。

![mapping.png](./../../.vuepress/img/mapping.png)

具体流程为：

1. Deployer 先在主链部署 TRC20/TRC721 合约
2. 资产所有者调用 gateway 中的部署映射合约方法去创建相应的 TRC20/TRC721 合约，Gateway 合约验证映射合法性。如果验证通过，将会产生 Deploy 事件
3. oracle 监听侧链 Deploy 事件
4. oracle 调用侧链 Gateway 合约在侧链创建基础功能的 TRC20/TRC721 合约，并将主链与侧链的合约进行映射。

#### 4.3 存款功能(Deposit)

当用户需要将主链上的资产转移到侧链时，需要调用主链网关合约的存款功能。

![deposit.png](./../../.vuepress/img/deposit.png)

具体流程为：

1. 如果使用 TRC20/TRC721 代币，需要调用 TRC20/TRC721 合约 approve 方法，允许 gateway 转移自己资产(使用 TRX 无需此步）。
2. 调用主链网关合约的 deposit 方法
3. Oracle 监听 Deposit 事件
4. Oracle 调用侧链网关合约进行资产转移操作
5. 如果使用 TRC20/TRC721 代币，需要在侧链上 mint 相应数量的代币；如果使用 TRX 直接增加账户余额

#### 4.4 取款功能(Withdraw)

当用户需要将侧链上的资产转移到主链时，需要调用侧链网关合约的取款功能。

![withdraw](./../../.vuepress/img/withdraw.png)

具体流程为：

1. 如果使用 TRC20/TRC721 合约，调用合约的 withdraw 方法，合约会再去调用网关合约。如果使用 TRX，直接调用侧链网关合约进行取款操作。
2. Oracle 监听取款事件
3. 调用主链网关合约的取款方法
4. 如果使用 TRC20/TRC721 合约，则网关合约会再调用合约方法；如果使用 TRX，直接调整账户余额

### 5. 激励

1）在一个维护期内，出块奖励相同

2）不同维护期，出块奖励会发生变化，计算公式为：

payPerBlock = fund / ((86400 / 3 ) \* dayToSustainByFund)

其中，payPerBlock: 每个块出块奖励; fund: 当前 fund 值; dayToSustainByFund: 基金持续天数

3）出块奖励会分为两部分，按照 percentToPayWitness 比例分配给 witness，剩余分配给 founder，计算公式如下：

payForWitnessPerBlock = payPerBlock \* percentToPayWitness / 100

payForFounderPerBlock = payPerBlock - payForWitnessPerBlock

其中，payForWitnessPerBlock: 每个块给 witness 的出块奖励; payForFounderPerBlock: 每个块给 founder 的出块奖励

## III. 如何连接并使用 DAppChain

### 1. DAppChain 主网节点列表

(即将发布......)

### 2. DAppChain 测试网节点列表

#### Full Nodes

Http 接口

- 47.252.85.90:8090
- 47.252.80.185:8090
- 47.252.84.141:8090

RPC 接口

- 47.252.85.90:50051
- 47.252.80.185:50051
- 47.252.84.141:50051

#### Solidity Nodes

Http 接口

- 47.252.85.90:8091
- 47.252.80.185:8091
- 47.252.84.141:8091

RPC 接口

- 47.252.85.90:50060
- 47.252.80.185:50060
- 47.252.84.141:5006

### 3. DAppChain testnet gateway合约 address

- 主链gateway合约地址: TEaRKX1nazX7EiCu3XwaHjLZeCrHRLXoRa
- 侧链gateway合约地址: TDdo671JXH5S74iVvXEDyUXSgADYtg5ns7

### 4. 测试币申请

<token/>

### 5. 链接方式

用户可以使用多种方式链接到 DAppChain

1. 通过 sun-scan 官网 （将要支持, 包括 tronlink)
2. 通过 sun-client （见开发者文档）
3. 通过 SDK （见开发者文档）

## IV. 快速入门

如果您想在侧链上学习如何开发 TRON 智能合约以及 DApp，请参考下文的 JAVA SDK 以及 JS SDK 文档。

如果您是第一次接触 TRON 智能合约的开发，请您移步到我们的 github demo，参考我们的代码进行开发。

## V. JAVA SDK

DAppChain 提供了 java 版本的 sdk 共开发者使用，包含了链上，链间多种功能的封装。

### SDK 的初始化

使用 SDK 需要先初始化一个 SDK 对象。并实现 IServerConfig 接口传入初始化参数。如果需要多重签名功能，则还需要实现 IMultiTransactionSign 这个接口。

```
Sdk = new SunNetwork();
SunNetworkResponse<Integer> ret = sdk.init(new ServerConfigImpl(), new MultiSignTransactionImpl());
```

JAVA SDK 中包含三个 Service。

```java
//Main-chain interface
sdk.getMainChainService();
//Side-chain interface
sdk.getSideChainService();
//Cross-chain interaction interface
sdk.getCrossChainService();
```

### 链间交互接口介绍

#### 合约映射（Mapping）

1. TRC20 映射

```java
// Called by the deployer of the TRC20 contract on the main-chain to complete the mapping of the TRC20 contract from the main-chain to the side-chain, the standard 20 contract deployed in the side-chain corresponds to the main-chain TRC20 contract.
SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService().mappingTrc20(trxHash, feeLimit);
//Return value: Use sdk.getSideChainService().getTransactionInfoById(txid) to query resp.getData().getTrxId() to get the nonce value of this mapping operation.
```

| Parameter | Description                                                                   |
| --------- | ----------------------------------------------------------------------------- |
| trxHash   | deployContract transaction for deploying TRC20 contracts on the main-chain id |
| feeLimit  | Maximum energy consumption when triggering a contract                         |

2. TRC721 映射

```java
// Called by the deployer of the TRC721 contract on the main-chain to complete the mapping of the TRC721 contract from the main-chain to the side-chain, the standard TRC721 contract will be deployed in the side-chain corresponding to the main-chain TRC721 contract.
SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService().mappingTrc721(trxHash, feeLimit);
//Return value: Use sdk.getSideChainService().getTransactionInfoById(txid) to query resp.getData().getTrxId() to get the nonce value of this mapping operation.
```

| Parameter | Description                                                         |
| --------- | ------------------------------------------------------------------- |
| trxHash   | deployContract transaction id for TRC721 contract on the main-chain |
| feeLimit  | Maximum energy consumption when triggering a contract               |

#### 资产转入（Deposit）

1. TRX 资产转入

```java
//Pledge a certain number of TRX from the main-chain to the side-chain
SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService().depositTrx(num, feeLimit);
//Return value: Use sdk.getSideChainService().getTransactionInfoById(txid) to query resp.getData().getTrxId() to get the nonce value of this deposit operation.
```

| Parameter | Description                                           |
| --------- | ----------------------------------------------------- |
| num       | TRX Quantity (Unit SUN)                               |
| feeLimit  | Maximum energy consumption when triggering a contract |

2. TRC10 资产转入

```java
//Pledge a certain number of designated TRC10 from the main-chain to the side-chain
SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService().depositTrc10(tokenId, tokenValue, feeLimit);
//Return value: Use sdk.getSideChainService().getTransactionInfoById(txid) to query resp.getData().getTrxId() to get the nonce value of this deposit operation.
```

| Parameter  | Description                                           |
| ---------- | ----------------------------------------------------- |
| tokenId    | Token ID of TRC10                                     |
| tokenValue | TRC10 Quantity of Assets                              |
| feeLimit   | Maximum energy consumption when triggering a contract |

3. TRC20 资产转入

```java
//Pledge a certain number of designated TRC20 from the main-chain to the side-chain
SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService().depositTrc20(contractAddrStr, num, feeLimit);
//Return value: Use sdk.getSideChainService().getTransactionInfoById(txid) to query resp.getData().getTrxId() to get the nonce value of this deposit operation.
```

| Parameter       | Description                                           |
| --------------- | ----------------------------------------------------- |
| contractAddrStr | Main-chain TRC20 Contract Address                     |
| num             | TRC20 Asset Quantity                                  |
| feeLimit        | Maximum energy consumption when triggering a contract |

4. TRC721 资产转入

```java
//Pledge a certain number of designated TRC721 from the main-chain to the side-chain
SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService().depositTrc721(contractAddrStr, num, feeLimit);
//Return value: Use sdk.getSideChainService().getTransactionInfoById(txid) to query resp.getData().getTrxId() to get the nonce value of this deposit operation.
```

| Parameter       | Description                                           |
| --------------- | ----------------------------------------------------- |
| ContractAddrStr | Main-chain TRC721 Contract Address                    |
| num             | TRC721 Asset Quantity                                 |
| feeLimit        | Maximum energy consumption when triggering a contract |

#### 资产转出（Withdraw）

1. TRX 资产转出

```java
//Exit a certain number of TRX from the side-chain to the main-chain
SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService().withdrawTrx(num, feeLimit);
//Return value: Use sdk.getSideChainService().getTransactionInfoById(txid) to query resp.getData().getTrxId() to get the nonce value of this withdraw operation.
```

| Parameter | Description                                           |
| --------- | ----------------------------------------------------- |
| num       | TRX Quantity (Unit SUN)                               |
| feeLimit  | Maximum energy consumption when triggering a contract |

2. TRC10 资产转出

```java
//Pledge a certain number of designated TRC10 from the side-chain to the main-chain
SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService().withdrawTrc10(tokenId, tokenValue, feeLimit);
//Return value: Use sdk.getSideChainService().getTransactionInfoById(txid) to query resp.getData().getTrxId() to get the nonce value of this withdraw operation.
```

| Parameter  | Description                                           |
| ---------- | ----------------------------------------------------- |
| tokenId    | Token ID of TRC10                                     |
| tokenValue | TRC10 Quantity of Assets                              |
| feeLimit   | Maximum energy consumption when triggering a contract |

3. TRC20 资产转出

```java
//Pledge a certain number of designated TRC20 from the side-chain to the main-chain
SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService().withdrawTrc20(contractAddrStr, num, feeLimit);
//Return value: Use sdk.getSideChainService().getTransactionInfoById(txid) to query resp.getData().getTrxId() to get the nonce value of this withdraw operation.
```

| Parameter       | Description                                           |
| --------------- | ----------------------------------------------------- |
| contractAddrStr | Sidechain TRC20 Contract Address                      |
| num             | TRC20 Asset Quantity                                  |
| feeLimit        | Maximum energy consumption when triggering a contract |

4. TRC721 资产转出

```java
//Pledge a certain number of designated TRC721 from the side-chain to the main-chain
SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService().withdrawTrc721(contractAddrStr, num, feeLimit);
//Return value: Use sdk.getSideChainService().getTransactionInfoById(txid) to query resp.getData().getTrxId() to get the nonce value of this withdraw operation.
```

| Parameter       | Description                                           |
| --------------- | ----------------------------------------------------- |
| contractAddrStr | Sidechain TRC721 Contract Address                     |
| num             | TRC721 Asset Quantity                                 |
| feeLimit        | Maximum energy consumption when triggering a contract |

#### 重试（Retry）

1. 资产转入重试

```java
//Retry the unsuccessful main-chain deposit operation
SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService().retryDeposit(nonce, feeLimit);
```

| Parameter | Description                                           |
| --------- | ----------------------------------------------------- |
| nonce     | deposit operation nonce value                         |
| feeLimit  | Maximum energy consumption when triggering a contract |

2. 资产转出重试

```java
//Retry the unsuccessful side-chain withdraw operation
SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService().retryWithdraw(nonce, feeLimit);
```

| Parameter | Description                                           |
| --------- | ----------------------------------------------------- |
| nonce     | withdraw operation nonce value                        |
| feeLimit  | Maximum energy consumption when triggering a contract |

3. 资产映射重试

```java
//Retry the unsuccessful main-chain mapping operation
SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService().retryMapping(nonce, feeLimit);
```

| Parameter | Description                                           |
| --------- | ----------------------------------------------------- |
| nonce     | mapping operation's nonce value                       |
| feeLimit  | Maximum energy consumption when triggering a contract |

### 侧链接口介绍

1. 激励池注入

```java
//Investors from the side-chain will inject funds into the side-chain fund pool
SunNetworkResponse<TransactionResponse> resp = sdk.getMainChainService().fundInject(amount);
```

| Parameter | Description                                                         |
| --------- | ------------------------------------------------------------------- |
| amount    | Quantity of funds injected into the side-chain fund pool (unit SUN) |

2. 侧链提案

```java
//Query side-chain all proposal list information
SunNetworkResponse<TransactionResponse> resp = sdk.getSideChainService().listProposals();
//Parameter: None

```

```java
//Query side-chain proposal information according to the proposal number
SunNetworkResponse<TransactionResponse> resp = getSideChainService().getProposal(id);
```

| Parameter | Description     |
| --------- | --------------- |
| id        | Proposal number |

3. 侧链参数

```java
//Query all the parameters of the side-chain blockchain committee can be set
SunNetworkResponse<TransactionResponse> resp = getSideChainService().getSideChainParameters();
```

### 主链接口介绍

1. 主链提案

```java
//Query all the proposal list information of the main-chain
SunNetworkResponse<TransactionResponse> resp = sdk.getMainChainService().listProposals();
```

```java
//Query side-chain proposal information according to the proposal number
SunNetworkResponse<TransactionResponse> resp = getMainChainService().getProposal(id);
```

| Parameter | Description     |
| --------- | --------------- |
| id        | Proposal number |

2. 主链参数

```java
//Query all the parameters of the main-chain blockchain committee can be set
SunNetworkResponse<TransactionResponse> resp = getMainChainService().getChainParameters();
```

### 公用部分接口介绍

1. 获得当前用户地址

```java
//Get the address of the current user of sdk
SunNetworkResponse<byte[]> resp = getChainService().getAddress()
```

2. 获得当前用户 TRX 账户余额

```java
//Get the current user's TRX sdk
SunNetworkResponse<long> resp = getChainService().getBalance()
```

3. 获得当前用户账户状态

```java
//Get account information based on the address
SunNetworkResponse<Account> result = getChainService().getAccount(address);
```

4. 链上 TRX 资产转移

```java
//Transfer the TRX operation
SunNetworkResponse<TransactionResponse> = getChainService().sendCoin(toAddress, amount);
```

5. 链上 TRC10 资产转移

```java
//Transfer the TRC10 operation
SunNetworkResponse<TransactionResponse> = getChainService().transferAsset(toAddress, assertName, amount);
```

## VI. SunWeb

SunWeb 是为 Tron Sun-Network 开发的一款 js-sdk 工具，并且继承自 [TronWeb](https://developers.tron.network/docs/tron-web-intro)。 SunWeb 里面封装了 main-chain 和 side-chain 两个对象，他们本质上就是 TronWeb 的对象实例，因此里面包含了 TronWeb 实例的所有属性和方法。例如用户可以使用 sunweb.mainchain.trx.getBalance() 来获取主网上的 balance。除此之外，SunWeb 增加了一些新的方法来支持主链和侧链的交互，如 deposit, withdraw, mapping, approve, injectFund 等操作，可参考[源码](https://github.com/tronprotocol/sun-network/tree/develop/js-sdk)。SunWeb 详细使用方法如下。

### SunWeb 类

#### 创建 SubWeb 实例

为了在应用中可以使用 SunWeb，你需要创建一个 sunWeb 实例。你可以定义两个 js 对象，如 mainOptions 和 sideOptions，他们分别包含以下属性：

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
const sunWeb = new SunWeb(
  {
    fullNode: 'http://47.252.84.158:8090',
    solidityNode: 'http://47.252.84.158:8090',
    eventServer: 'http://47.252.84.141:8080'
  },
  {
    fullNode: 'http://47.252.85.90:8090',
    solidityNode: 'http://47.252.85.90:8091',
    eventServer: 'http://47.252.85.90:8090'
  },
  'TGHxhFu4jV4XqMGmk3tEQdSeihWVHE9kBP',
  'TBHr5KpbA7oACUysTKxHiAD7c6X6nkZii1',
  '41455CB714D762DC46D490EAB37BBA67B0BA910A59',
  privateKey
);
```

### 质押资产

资产质押的作用是将主链资产质押到侧链。

#### depositTrx

##### 质押 TRX

```javascript
// Format
sunWeb.depostiTrx(callValue, feeLimit, options);

// example
sunWeb.depositTrx(100000000, 1000000);
```

##### Arguments

| Parameter | Description                             | Type                   | Options  |
| --------- | --------------------------------------- | ---------------------- | -------- |
| callValue | Amount of TRX (Units in SUN) to deposit | Integer (Units in SUN) | Required |
| feeLimit  | Cost limit                              | Integer, long          | Required |
| options   | The permissions Id                      | Object                 | Optional |

#### depositTrc10

##### 质押 TRC10

```javascript
// format
sunWeb.depositTrc10(tokenId, tokenValue, feeLimit, options);

// example
sunWeb.depositTrc10(100059, 10000000, 100000);
```

##### Arguments

| Parameter  | Description                                     | Type                   | Options  |
| ---------- | ----------------------------------------------- | ---------------------- | -------- |
| tokenId    | Token Id of trc10                               | Integer                | Required |
| tokenValue | Amount of trc10 token (Units in SUN) to deposit | Integer (Units in SUN) | Required |
| feeLimit   | Cost limit                                      | Integer, long          | Required |
| options    | The permissions Id                              | Object                 | Optional |

#### depositTrc20

##### 质押 TRC20

在质押 TRC20 之前，必须先 mapping TRC20 合约到侧链，即调用 sunWeb.mappingTrc20(…) 函数。Mapping 合约后，再 approve TRC20, 即调用 sunWeb.approveTrc20(…) 函数。这两步都成功后，才能成功完成 depositTrc20 操作。

```javascript
// format
sunWeb.depositTrc10(num, feeLimit, contractAddress, options);

// example
sunWeb.depositTrc10(1000000, 1000000, 'TD9Jrm546pGkzRu7K5nitMxk8nn75wXNkQ');
```

##### Arguments

| Parameter       | Description                               | Type          | Options  |
| --------------- | ----------------------------------------- | ------------- | -------- |
| num             | Amount of TRC20 (Units in SUN) to deposit | Integer       | Required |
| feeLimit        | Cost limit                                | Integer, long | Required |
| contractAddress | Main-chain TRC20 Contract Address         | String        | Required |
| options         | The permissions Id                        | Object        | Optional |

#### depositTrc721

##### 质押 TRC721

与质押 TRC20 类似，先需要 mappingTrc721 和 approveTrc721。

```javascript
// format
sunWeb.depositTrc721(id, feeLimit, contractAddress, options);

// example
sunWeb.depositTrc10(1000000, 1000000, 'TCLRqK6aP2xsCZWhE2smkYzdRHf9uvyz5P');
```

##### Arguments

| Parameter       | Description                        | Type          | Options  |
| --------------- | ---------------------------------- | ------------- | -------- |
| id              | Id of TRC721 to deposit            | Integer       | Required |
| feeLimit        | cost limit                         | Integer, long | Required |
| contractAddress | Main-chain TRC721 Contract Address | String        | Required |
| options         | The permissions Id                 | Object        | Optional |

### 资产授权

在质押 TRC20 和 TRC721 之前，需要先调用 approveTrc20 和 approveTrc721 获得相应资产授权。

#### approveTrc20

##### 授权 TRC20

```javascript
// format
sunWeb.approveTrc20(num, feeLimit, contractAddress, options);

// example
sunWeb.approveTrc20(10000, 10000000, 'TGKuXDnvdHv9RNE6BPXUtNLK2FrVMBDAuA');
```

##### Arguments

| Parameter       | Description                       | Type          | Options  |
| --------------- | --------------------------------- | ------------- | -------- |
| num             | Num of TRC20                      | Integer       | Required |
| contractAddress | Main-chain TRC20 Contract Address | String        | Required |
| feeLimit        | Cost limit                        | Integer, long | Required |
| options         | The permissions Id                | Object        | Optional |

#### approveTrc721

##### 授权 TRC721

```javascript
// format
sunWeb.approveTrc721(id, feeLimit, contractAddress, options);

// example
sunWeb.approveTrc721(100, 10000000, 'TUxDmFbEceGgjWCb6rLVcrFgnsWwofPdPq');
```

##### Arguments

| Parameter       | Description                        | Type          | Options  |
| --------------- | ---------------------------------- | ------------- | -------- |
| id              | Id of TRC721                       | Integer       | Required |
| contractAddress | Main-chain TRC721 Contract Address | String        | Required |
| feeLimit        | Cost limit                         | Integer, long | Required |
| options         | The permissions Id                 | Object        | Optional |

### 资产映射

用户必须先将主链合约资产 TRC20/TRC721 映射到侧链以后，才能将合约资产质押到侧链。

#### mappingTrc20

##### 映射 TRC20

```javascript
// format
mappingTrc20(trxHash, mappingFee, feeLimit, options);

// example
mappingTrc20('548442d9080605a60adf1d30cc126a2b9c6308cbe9ec224f8c67a6c2590fa299', 100000, 10000, options);
```

##### Arguments

| Parameter  | Description                                                                    | Type                  | Options  |
| ---------- | ------------------------------------------------------------------------------ | --------------------- | -------- |
| trxHash    | The hash value of the transaction for the main-chain deployment TRC20 contract | Hex string            | Required |
| mappingFee | Mapping Fee                                                                    | Integer(Units in SUN) | Required |
| feeLimit   | cost limit                                                                     | Integer, long         | Required |
| options    | The permissions Id                                                             | Object                | Optional |

#### mappingTrc721

##### 映射 TRC721

```javascript
// format
mappingTrc721(trxHash, mappingFee, feeLimit, options);

// example
mappingTrc721('548442d9080605a60adf1d30cc126a2b9c6308cbe9ec224f8c67a6c2590fa299', 100000, 1000, options);
```

##### Arguments

| Parameter  | Description                                                                     | Type                  | Options  |
| ---------- | ------------------------------------------------------------------------------- | --------------------- | -------- |
| trxHash    | The hash value of the transaction for the main-chain deployment TRC721 contract | Hex string            | Required |
| mappingFee | Mapping Fee                                                                     | Integer(units in SUN) | Required |
| feeLimit   | cost limit                                                                      | Integer, long         | Required |
| options    | The permissions Id                                                              | Object                | Optional |

### 提取资产

用户可将资产从侧链提取回主链，提取资产操作由 SunWeb 向侧链发送命令。

#### withdrawTrx

##### 提取 TRX

```javascript
// Format
sunWeb.withdrawTrx(callValue, withdrawFee, feeLimit, options);

// example
sunWeb.withdrawTrx(100000000, 10000, 1000000);
```

##### Arguments

| Parameter   | Description                             | Type                   | Options  |
| ----------- | --------------------------------------- | ---------------------- | -------- |
| callValue   | Amount of TRX (Units in SUN) to deposit | Integer (Units in SUN) | Required |
| withdrawFee | Withdraw Fee                            | Integer(Units in SUN)  | Required |
| feeLimit    | Cost limit                              | Integer, long          | Required |
| options     | The permissions Id                      | Object                 | Optional |

#### withdrawTrc10

##### 提取 TRC10

```javascript
// format
sunWeb.withdrawTrc10(tokenId, tokenValue, withdrawFee, feeLimit, options);

// example
sunWeb.withdrawTrc10(100059, 10000000, 10000, 100000);
```

##### Arguments

| Parameter   | Description                                     | Type                   | Options  |
| ----------- | ----------------------------------------------- | ---------------------- | -------- |
| tokenId     | Token Id of TRC10                               | Integer                | Required |
| tokenValue  | Amount of TRC10 token (Units in SUN) to deposit | Integer (Units in SUN) | Required |
| withdrawFee | Withdraw Fee                                    | Integer(Units in SUN)  | Required |
| feeLimit    | Cost limit                                      | Integer, long          | Required |
| options     | The permissions Id                              | Object                 | Optional |

#### withdrawTrc20

##### 提取 TRC20

```javascript
// format
sunWeb.withdrawTrc20(num, withdrawFee, feeLimit, contractAddress, options);

// example
sunWeb.withdrawTrc20(10000, 10000, 10000000, 'TWzXQmDoASGodMss7uPD6vUgLHnkQFX7ok');
```

##### Arguments

| Parameter       | Description                                     | Type                  | Options  |
| --------------- | ----------------------------------------------- | --------------------- | -------- |
| Num             | Num of TRC20                                    | Integer               | Required |
| withdrawFee     | Withdraw Fee                                    | Integer(units in SUN) | Required |
| contractAddress | Side-chain TRC20 Contract Address after mapping | Integer               | Required |
| feeLimit        | Cost limit                                      | Integer, long         | Required |
| options         | The permissions Id                              | Object                | Optional |

#### withdrawTrc721

##### 提取 TRC721

```javascript
// format
sunWeb.withdrawTrc721(id, withdrawFee, feeLimit, contractAddress, options);

// example
sunWeb.withdrawTrc721(101, 1000, 10000000, 'TA2xrVESq2UcEtDtgPzxNJEiLgxmMVdtFR');
```

##### Arguments

| Parameter       | Description                                     | Type                  | Options  |
| --------------- | ----------------------------------------------- | --------------------- | -------- |
| id              | Id of TRC721                                    | Integer               | Required |
| contractAddress | Side-chain TRC20 Contract Address after mapping | Integer               | Required |
| withdrawFee     | Withdraw Fee                                    | Integer(units in SUN) | Required |
| feeLimit        | Cost limit                                      | Integer, long         | Required |
| options         | The permissions Id                              | Object                | Optional |

### 注资

注资的主要作用是为侧链的基金池注入资金。

#### injectFund

```javascript
// format
sunWeb.injectFund(num, feeLimit, options);

// example
sunWeb.injectFund(1000, 10000000);
```

#### Arguments

| Parameter | Description        | Type          | Options  |
| --------- | ------------------ | ------------- | -------- |
| num       | num of injecting   | Integer       | Required |
| feeLimit  | Cost limit         | Integer, long | Required |
| options   | The permissions Id | Object        | Optional |

### 重试

#### retryDeposit

```javascript
// format
sunWeb.retryDeposit(nonce, feeLimit, options);

// example
sunWeb.retryDeposit(1000, 10000000);
```

#### Arguments

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

#### Arguments

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

#### Arguments

| Parameter | Description                  | Type          | Options  |
| --------- | ---------------------------- | ------------- | -------- |
| nonce     | Nonce value of asset deposit | Integer       | Required |
| feeLimit  | Cost limit                   | Integer, long | Required |
| options   | The permissions Id           | Object        | Optional |

### 签名

Sun-network 的签名有一些改变，主链的签名逻辑和 TronWeb 的保持一致，侧链的签名逻辑有更改。因此，如果需要和 TronLink 一样弹出签名框，需要分别覆盖 sunWeb.mainchain.trx.sign()和 sunWeb.sidechain.trx.sign().

```javascript
// format
sign((transaction = false), (privateKey = this.sidechain.defaultPrivateKey), (useTronHeader = true), (multisig = false));
```

## VII. RoadMap

DAppChain 作为 TRON 扩容计划的一部分，肩负着去中心化，繁荣 TRON 生态的使命。对于整个生态的所有建设者而言，将会伴随 DAppChain 的发展度过 L1，L2，L3 三个阶段。我们将会随着计划地开展以及开发工作的进一步完成，为社区开放更多的角色，让社区可以以不同的形式参与到整个生态体系中来。

![roadmap](./../../.vuepress/img/roadmap.png)

- L1: First DAppChain （第一侧链）

在 L1 阶段，TRON 将对外公布自己的第一条同构侧链，DAppChain。对 TRON 主网进行扩容，同时更好地支持 DApp 的生态发展。

在 L1 阶段，基金会对社区所开放的角色有：

User：链基本功能的使用者

Developer：合约和 DApp 开发者

Witness: 链交易验证者，负责侧链交易打包

在这个阶段，DAppChain 初始由官方搭建。官方鼓励社区参选 witness，参与侧链的治理，并最终实现社区自治的 DAppChain。

- L2: Autonomous Chains （多条社区运营的侧链）

Sun-network 侧链有初始 DAppChain 的同时，将允许开发者建立自己的侧链，可以使用自己的代币进行该侧链生态的治理。侧链可通过定制参数，让侧链更适合自身 DApp 的业务特性，比如更低的资源消耗，更快的产块速度等。

L2 阶段将鼓励社区自主运营多条侧链。

- L3: Fair Network（仲裁者机制主导的完整网络体系）

在生态扩大之后，链间的行为将会由社区 Judger 节点进行验证治理。将以 zk-snark/Truebit-liked 的形式相结合，对 Judger 的行为给予奖惩，保证整个网络的正常工作。

L3 阶段将把侧链的仲裁者角色开放给社区。

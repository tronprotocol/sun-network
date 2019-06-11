# Sun Network 使用文档

## Sun Network 计划

Sun Network 计划是为繁荣波场主网生态而生的扩容计划, 包含智能合约应用侧链(DAppChain), 跨链通讯等一些列扩容项目。其中 DAppChain 为波场的侧链扩容项目，着重以可定制化方式，实现 DApp 以极低 Energy 消耗、高效、安全地上链运行，为波场主网提供无限扩容的能力，为后续日益增长的交易提供支持。

## DAppChain 的特点

通过定制 DAppChain 的参数，实现更高的免费 Energy 上限，更廉价的 Energy 燃烧单价，更快的节点共识，更短的提案生效间隔，更长的智能合约执行时限。

- 更高的免费 Energy 上限：
  DAppChain 的交易压力和主网解耦，从而可以提供更高的免费 Energy 上限，甚至侧链上的所有交易完全免费。

- 更廉价的 Energy 燃烧单价：
  DAppChain 可以设置 Energy 燃烧单价为主链 1/100 甚至更低，支持极低能量成本的智能合约交易。

- 更快的节点共识：
  通过调整参数，DAppChain 可以加快出块速度；减少 Witness 数量，加快交易固化速度。今后会加入更多共识机制, 进一步加快共识。

- 更短的提案生效间隔：
  DAppChain 可以通过配置，将提案生效时间从数天降低为数小时。

- 更长的智能合约执行时限：
  配合不同参数，DAppChain 智能合约交易，可以将交易最长执行时限从 50ms 提高 10 倍甚至更多，从而支持复杂的智能合约交易。

- 保障用户资产安全：
  第一阶段的 DAppChain 采用侧链的 DPoS 机制，来保证侧链自身交易的共识和正确性, 同时通过主链及侧链的智能合约，来保证侧链的资产安全，以及侧链资产能正确提款退出。

- 完整的生态体系:
  Sun Network 将围绕 DAppChain 配套完善的开发工具集。兼容主链，支持多平台 降低开发者的学习成本，提高效率。

## 连接 DAppChain

### Sun Client RPC 接口

#### fullnode/solidityNode

- 47.252.85.90:50051
- 47.252.80.185:50051
- 47.252.84.141:50051

### Http 接口

#### fullnode

- 47.252.85.90:8090
- 47.252.80.185:8090
- 47.252.84.141:8090

#### solidityNode

- 47.252.85.90:8091
- 47.252.80.185:8091
- 47.252.84.141:8091

## 申请测试币

<token/>

## Sun Client 简介

Sun Client 是 Sun Network 官方提供给用户用于跟 DAppChain 交互的命令行工具，使用该工具可以完成资产的质押、提取、合约部署等操作。

## 质押资产

用户可以使用 Sun Client 的 deposit 命令将主链的资产质押到侧链，支持的资产类型有 TRX，TRC10 资产、TRC20 合约资产、TRC721 合约资产，如：质押 TRX 可使用"deposit trx"命令。

质押资产操作由 Sun Client 向主链发送命令。

<details open>
<summary>命令列表</summary>

#### 1. deposit TRX:

- 命令：deposit trx mainGatewayAddress num feelmit
- 参数说明：
  参数|含义|备注
  --|--|--
  deposit trx|命令|
  mainGatewayAddress|主链 gateway 地址|
  num|质押的 TRX 数量|
  feelimit|费用限制|
- 用例： deposit trx TUGgrkC2CoAG2xd31BY6VnyTTRfbaWeiPb 1000000000 100000000

#### 2. deposit TRC10

- 命令：deposit trc10 mainGatewayAddress trc10id num feelmit
- 参数说明：
  参数|含义|备注
  --|--|--
  deposit trc10|命令|
  mainGatewayAddress|主链 gateway 地址|
  trc10id|TRC10 的 token ID|
  num|质押的 TRC10 数量|
  feelimit|费用限制|
- 用例：deposit trc10 TUGgrkC2CoAG2xd31BY6VnyTTRfbaWeiPb 1000001 100000 100000000

#### 3. deposit TRC20

- 命令：deposit trc20 mainTrc20ContractAddress mainGatewayAddress num feelmit
- 参数说明：
  参数|含义|备注
  --|--|--
  deposit trc721|命令|
  mainTrc20ContractAddress|主链 TRC20 合约地址|
  mainGatewayAddress|主链 gateway 地址|
  num|质押的 TRC20 数量|
  feelimit|费用限制|
- 用例：deposit trc20 TAWM7tRr4JgEy4adsaAdbJPCjxYHNW81mg TUGgrkC2CoAG2xd31BY6VnyTTRfbaWeiPb 1000 1000000000

#### 4. deposit TRC721

- 命令：deposit trc721 mainTrc721ContractAddress mainGatewayAddress num feelmit
- 参数说明：
  参数|含义|备注
  --|--|--
  deposit trc20|命令|
  mainTrc20ContractAddress|主链 TRC20 合约地址|
  mainGatewayAddress|主链 gateway 地址|
  num|质押的 TRC721 数量|
  feelimit|费用限制|
- 用例：deposit trc721 TYVejg16UpC2J1fpqy1rKpBSfPSRKdGArf TUGgrkC2CoAG2xd31BY6VnyTTRfbaWeiPb 1234567890 1000000000

</details>

## 资产映射

用户必须要先将主链合约资产 TRC20/TRC721 映射到 DAppChain 以后，才能将合约资产的质押到 DAppChain。合约资产的映射使用 Sun Client 的 mapping 命令，如：映射 TRC20 合约资产可使用"mapping trc20"命令。

映射资产操作由 Sun Client 向侧链发送命令。

<details open>
<summary>命令列表</summary>

#### 1. mapping TRC20

- 命令：mapping trc20 sideGatewayAddress mainTrxHash name symbol decimal feelmit
- 参数说明：
  参数|含义|备注
  --|--|--
  mapping trc20|命令|
  sideGatewayAddress|侧链 gateway 地址|
  mainTrxHash|主链部署 TRC20 合约的交易的 hash 值|
  name|侧链 TRC20 合约名|
  symbol|侧链 TRC20 合约符号|
  decimal|token 精度|
  feelimit|费用限制|
- 用例：mapping trc20 TP7N1844p7uVrTR2JMT6tJeJCfaRGVEPwg 548442d9080605a60adf1d30cc126a2b9c6308cbe9ec224f8c67a6c2590fa299 sidetrc20trontoken trc20_1 6 100000000

#### 2. mapping TRC721

- 命令：mapping trc721 sideGatewayAddress mainTrxHash name symbol feelmit
- 参数说明：
  参数|含义|备注
  --|--|--
  mapping trc721|命令|
  sideGatewayAddress|侧链 gateway 地址|
  mainTrxHash|主链部署 TRC721 合约的交易的 hash 值|
  name|侧链 TRC721 合约名|
  symbol|侧链 TRC721 合约符号|
  feelimit|费用限制|
- 用例：mapping trc721 TP7N1844p7uVrTR2JMT6tJeJCfaRGVEPwg abe4149e4ad9b736e078e19b0469ecdb55f494b5cf15faf73d312c259052924c trc721 trc721_1 1000000000

</details>

## 提取资产

用户可以使用 Sun Client 的 withdraw 命令将侧链的资产提取到主链，支持的资产类型有 TRX，TRC10 资产、TRC20 合约资产、TRC721 合约资产，如：提取 TRX 可使用"withdraw trx"命令。

提取资产操作由 Sun Client 向侧链发送命令。

<details open>
<summary>命令列表</summary>

#### 1. withdraw TRX

- 命令：withdraw trx trx_num feelmit
- 参数说明：
  参数|含义|备注
  --|--|--
  withdraw trx|命令|
  trx_num|提取的 trx 数量|
  feelimit|费用限制|
- 用例：withdraw trx 1000 1000000000

#### 2. withdraw TRC10

- 命令：withdraw trc10 trc10Id value feelmit
- 参数说明：
  参数|含义|备注
  --|--|--
  withdraw trc10|命令|
  trc10Id|TRC10 的 token ID|
  value|提取的 TRC10 数量|
  feelimit|费用限制|
- 用例：withdraw trc10 1000001 1000 10000000

#### 3. withdraw TRC20

- 命令：withdraw trc20 mainTrc20ContractAddress value feelmit
- 参数说明：
  参数|含义|备注
  --|--|--
  withdraw trc20|命令|
  mainTrc20ContractAddress|主链 TRC20 的合约地址|
  value|提取的 TRC20 数量|
  feelimit|费用限制|
- 用例：withdraw trc20 TAWM7tRr4JgEy4adsaAdbJPCjxYHNW81mg 12345 100000000

#### 4. withdraw TRC721

- 命令：withdraw trc721 mainTrc721ContractAddress uid feelmit
- 参数说明：
  参数|含义|备注
  --|--|--
  withdraw trc721|命令|
  mainTrc20ContractAddress|主链 TRC721 的合约地址|
  uid|提取的 TRC721 token ID|
  feelimit|费用限制|
- 用例：withdraw trc721 TYVejg16UpC2J1fpqy1rKpBSfPSRKdGArf 1234567890 100000000

</details>

## 部署 DApp

开发者可以通过使用 Sun Client 在 DAppchain 上部署开发所需要的智能合约。

## 资源链接

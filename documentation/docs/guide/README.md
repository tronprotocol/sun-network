# Sun Network Documentation

## Sun Network Plan

Sun Network Plan is an expansion plan for TRON main network ecosystem, including smart contract application oriented side chain(DAppChain), cross-chain communication and other expansion projects. DAppChain is a customizable side chain expansion project related to TRON smart contracts which allow DApps to run with extremely low energy consumption, high security, and high efficiency online operation. Sun Network is also designed to provide unlimited capacity for TRON main network to support the growing number of transactions.

## DAppChain Features

By customizing the chain parameters, DAppChain can achieve a higher free energy limit, a cheaper energy burning, a faster consensus process, a shorter proposal effective interval, and a longer smart contract execution time limit.

- Higher free Energy limit:
  Trading pressure on DAppChain will not affect the main chain and it decouples with TRON main chain. DAppChain provideS a higher free energy limit, and even all transactions on the side chain are completely free.

- Cheaper unit price for energy burning:
  By setting the energy unit price to be 1/100 or lower compares to the main chain, Dapp chain can provide developers with a very low-cost development experience.

- Faster consensus:
  By adjusting the parameters, DAppChain can speed up the block production process, reduce the number of Witness, and speed up the transaction confirmed process. In the future, more consensus mechanisms will be added to further accelerate consensus.

- Shorter proposal effective intervals:
  DAppChain can be configured to reduce the effective interval of a proposal from days to hours.

- Longer smart contract execution time limit:
  With different parameters, DAppChain smart contract transaction can increase the maximum execution time of the transaction by 10 times or more from 50ms, thus supporting complex intelligent contract transactions.

- Ensure the security of users' assets
  The first phase of DAppChain use the DPoS mechanism to ensure transaction consensus and correctness on side chain. With the smart contracts on both main chain and side chain, user assets can be secured and also correctly deposited and withdrew between different chains.

- Complete ecosystem:
  Sunnetwork will provide a comprehensive set of development tools, which compatible with the main chain and support running on multiple platforms, which is aiming to reduce developer's learning costs and improve efficiency.

## Sun Client Introduction

Sun Client is a command-line tool that Sun Network officially provides to users to interact with DAppChain. This tool can be used to call asset deposit, withdraw, contract deployment and other operations.

## Connect DAppChain

### Sun Client RPC Interface

#### fullnode/solidityNode

- 47.252.85.90:50051
- 47.252.80.185:50051
- 47.252.84.141:50051

### Http Interface

#### fullnode

- 47.252.85.90:8090
- 47.252.80.185:8090
- 47.252.84.141:8090

#### solidityNode

- 47.252.85.90:8091
- 47.252.80.185:8091
- 47.252.84.141:8091

## Receive Test (TRX) Coins

<token/>

## Sun Client Introduction

Sun Client is a command-line tool that Sun Network officially provides to users to interact with DAppChain. This tool can be used to complete asset deposit, withdraw, contract deployment and other operations.

## Deposit Assets

Users can deposit the assets of the main chain to the side chain by the Sun Client deposit command . The Dappchain support TRX, TRC10 assets, TRC20 contract assets, and TRC721 contract assets of main chain to deposit. For example, to deposit TRX can use the "deposit trx" command.

Deposit asset operations are sent by Sun Client to the main chain.

<details open>
<summary>Command list</summary>

#### 1. deposit TRX:

- Command: deposit trx mainGatewayAddress num feelmit
- Parameter Description:
  Parameter|meaning|note
  --|--|--
  deposit trx|command|
  mainGatewayAddress|main chain gateway address|
  num| number of TRX deposit|
  feelimit|cost limit|
- Use case: deposit trx TUGgrkC2CoAG2xd31BY6VnyTTRfbaWeiPb 1000000000 100000000

#### 2. deposit TRC10

- Command: deposit trc10 mainGatewayAddress trc10id num feelmit
- Parameter Description:
  Parameter|meaning|note
  --|--|--
  deposit trc10|command|
  mainGatewayAddress|main chain gateway address|
  trc10Id|Token ID of TRC10
  num| number of TRC10|
  feelimit|cost limit|
- Use case: deposit trc10 TUGgrkC2CoAG2xd31BY6VnyTTRfbaWeiPb 1000001 100000 100000000

#### 3. deposit TRC20

- Command: deposit trc20 mainTrc20ContractAddress mainGatewayAddress num feelmit
- Parameter Description:
  Parameter|meaning|note
  --|--|--
  deposit trc721|command|
  mainTrc20ContractAddress|Main Chain TRC20 Contract Address|
  mainGatewayAddress|main chain gateway address|
  num| number of TRC20|
  feelimit|cost limit|
- Use case: deposit trc20 TAWM7tRr4JgEy4adsaAdbJPCjxYHNW81mg TUGgrkC2CoAG2xd31BY6VnyTTRfbaWeiPb 1000 1000000000

#### 4. deposit TRC721

- Command: deposit trc721 mainTrc721ContractAddress mainGatewayAddress num feelmit
- Parameter Description:
  Parameter|meaning|note
  --|--|--
  deposit trc20|command|
  mainTrc20ContractAddress|Main Chain TRC20 Contract Address|
  mainGatewayAddress|main chain gateway address|
  num| number of TRC721|
  feelimit|cost limit|
- Use case: deposit trc721 TYVejg16UpC2J1fpqy1rKpBSfPSRKdGArf TUGgrkC2CoAG2xd31BY6VnyTTRfbaWeiPb 1234567890 1000000000

</details>

## Asset Mapping

The user must map the main contract asset TRC20/TRC721 to DAppChain before the deposit of the contract asset to DAppChain. The mapping of contract assets uses the Sun Client mapping command. For example, mapping the TRC20 contract asset can use the "mapping trc20" command.

Map asset operations are sent by Sun Client to the side chain.

<details open>
<summary>Command list</summary>

#### 1. mapping TRC20

- Command: mapping trc20 sideGatewayAddress mainTrxHash name symbol decimal feelmit
- Parameter Description:
  Parameter|meaning|note
  --|--|--
  mapping trc20|command|
  sideGatewayAddress|side chain gateway address|
  mainTrxHash|The hash value of the transaction for the main chain deployment TRC20 contract|
  name|side chain TRC20 contract name|
  symbol|side chain TRC20 contract symbol|
  decimal|token precision|
  feelimit|cost limit|
- Use case: mapping trc20 TP7N1844p7uVrTR2JMT6tJeJCfaRGVEPwg 548442d9080605a60adf1d30cc126a2b9c6308cbe9ec224f8c67a6c2590fa299 sidetrc20trontoken trc20_1 6 100000000

#### 2. mapping TRC721

- Command: mapping trc721 sideGatewayAddress mainTrxHash name symbol feelmit
- Parameter Description:
  Parameter|meaning|note
  --|--|--
  mapping trc721|command|
  sideGatewayAddress|side chain gateway address|
  mainTrxHash|The hash value of the transaction for the main chain deployment TRC721 contract|
  name|side chain TRC721 contract name|
  symbol|side chain TRC721 contract symbol|
  feelmit|cost limit|
- Use case: mapping trc721 TP7N1844p7uVrTR2JMT6tJeJCfaRGVEPwg abe4149e4ad9b736e078e19b0469ecdb55f494b5cf15faf73d312c259052924c trc721 trc721_1 1000000000

</details>

## Withdraw Assets

Users can withdraw the assets of the main chain to the side chain by the Sun Client withdraw command . The Dappchain support TRX, TRC10 assets, TRC20 contract assets, and TRC721 contract assets of main chain to withdraw. For example, extract TRX can use the "withdraw trx" command.

Extracting asset operations is sent by Sun Client to the side chain.

<details open>
<summary>Command list</summary>

#### 1. withdraw TRX

- Command: withdraw trx trx_num feelmit
- Parameter Description:
  Parameter|meaning|note
  --|--|--
  withdraw trx|command|
  trx_num|number of trx|
  feelmit|cost limit|
- Use case: withdraw trx 1000 1000000000

#### 2. withdraw TRC10

- Command: withdraw trc10 trc10Id value feelmit
- Parameter Description:
  Parameter|meaning|note
  --|--|--
  withdraw trc10|command|
  trc10Id|Token ID of TRC10|
  value| number of TRC10|
  feelmit|cost limit|
- Use case: withdraw trc10 1000001 1000 10000000

#### 3. withdraw TRC20

- Command: withdraw trc20 mainTrc20ContractAddress value feelmit
- Parameter Description:
  Parameter|meaning|note
  --|--|--
  withdraw trc20|command|
  mainTrc20ContractAddress|The contract address of TRC20 of the main chain|
  value| number of TRC20|
  feelmit|cost limit|
- Use Case: withdraw trc20 TAWM7tRr4JgEy4adsaAdbJPCjxYHNW81mg 12345 100000000

#### 4. withdraw TRC721

- Command: withdraw trc721 mainTrc721ContractAddress uid feelmit
- Parameter Description:
  Parameter|meaning|note
  --|--|--
  withdraw trc721|command|
  mainTrc20ContractAddress|The contract address of TRC721 of the main chain|
  uid| token ID TRC721|
  feelmit|cost limit|
- Use Case: withdraw trc721 TYVejg16UpC2J1fpqy1rKpBSfPSRKdGArf 1234567890 100000000

</details>

## Deploy DApps

Developers can use Sun Client to deploy smart contracts.

## Resource Link

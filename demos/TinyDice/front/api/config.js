const env = process.env.PATH_TYPE;

let interfaceData = {
}

switch (env) {
  case "pro":
    interfaceData = {
      mainOptions: {
        fullNode: 'http://47.252.84.158:50052/mf',
        solidityNode: 'http://47.252.84.158:50052/ms',
        eventServer: 'http://47.252.84.158:50052/mf'
      },
      sideOptions: {
        fullNode: 'http://47.252.84.158:50052/sf',
        solidityNode: 'http://47.252.84.158:50052/ss',
        eventServer: 'http://47.252.84.141:8080'  
      },
      mainGatewayAddress: 'TEaRKX1nazX7EiCu3XwaHjLZeCrHRLXoRa',
      sideGatewayAddress: 'TDdo671JXH5S74iVvXEDyUXSgADYtg5ns7',
      chainId: '410A6DBD0780EA9B136E3E9F04EBE80C6C288B80EE',

      SERVER_API: 'https://api.trondice.org', // 邀请 历史排行
      // 排行榜websocket

      TRON_GRID: "https://api.trongrid.io", // 获取账户信息
      // 游戏合约
      contractAddress: "TXK6pbKRV7i6fPNdTfAHY7AsTs4URJruhV",
      // 代币合约
      trx20ContractAddress: 'THvZvKPLHKLJhEFYKiyqj6j8G8nGgfg7ur',
      depositFee: 0,
      withdrawFee: 0,
      privateKey: '3AB9813D96EBCAC2B678AC01DB766D1CB063115C0AE4A0E3AFF11DFA0DDB144F'
      
    }
    break;
    case "local":
    interfaceData = {
      mainOptions: {
        fullNode: 'http://fullnode.tron.network',
        solidityNode: 'http://solidity.tron.network',
        eventServer: 'http://fullnode.tron.network',
      },
      sideOptions: {
        fullNode: 'http://fullnode.sun.network',
        solidityNode: 'http://solidity.sun.network',
        eventServer: 'http://47.252.84.141:8080',
      },
      mainGatewayAddress: 'TEaRKX1nazX7EiCu3XwaHjLZeCrHRLXoRa',
      sideGatewayAddress: 'TDdo671JXH5S74iVvXEDyUXSgADYtg5ns7',
      chainId: '410A6DBD0780EA9B136E3E9F04EBE80C6C288B80EE',
      
      SERVER_API: 'https://api.trondice.org', // 邀请 历史排行
      // 排行榜websocket

      TRON_GRID: "https://api.trongrid.io", // 获取账户信息
      // 游戏合约
      contractAddress: "TXK6pbKRV7i6fPNdTfAHY7AsTs4URJruhV",
      // 代币合约
      trx20ContractAddress: 'THvZvKPLHKLJhEFYKiyqj6j8G8nGgfg7ur',
      depositFee: 0,
      withdrawFee: 0,
      privateKey: '3AB9813D96EBCAC2B678AC01DB766D1CB063115C0AE4A0E3AFF11DFA0DDB144F'

    }
    break;
  default:
    interfaceData = {
        mainOptions: {
            fullNode: 'http://47.252.84.158:50052/mf',
            solidityNode: 'http://47.252.84.158:50052/ms',
            eventServer: 'http://47.252.84.158:50052/mf'
          },
          sideOptions: {
            fullNode: 'http://47.252.84.158:50052/sf',
            solidityNode: 'http://47.252.84.158:50052/ss',
            eventServer: 'http://47.252.84.141:8080'  
          },
        mainGatewayAddress: 'TEaRKX1nazX7EiCu3XwaHjLZeCrHRLXoRa',
        sideGatewayAddress: 'TDdo671JXH5S74iVvXEDyUXSgADYtg5ns7',
        chainId: '410A6DBD0780EA9B136E3E9F04EBE80C6C288B80EE',

        SERVER_API: 'https://api.trondice.org', // 邀请 历史排行
        // 排行榜websocket

        TRON_GRID: "https://api.trongrid.io", // 获取账户信息
        // 游戏合约
        contractAddress: "TXK6pbKRV7i6fPNdTfAHY7AsTs4URJruhV",
        // 代币合约
        trx20ContractAddress: 'THvZvKPLHKLJhEFYKiyqj6j8G8nGgfg7ur',
        depositFee: 0,
        withdrawFee: 0,
        privateKey: '3AB9813D96EBCAC2B678AC01DB766D1CB063115C0AE4A0E3AFF11DFA0DDB144F'

    }
    break;
}
export default interfaceData;
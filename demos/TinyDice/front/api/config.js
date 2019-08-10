const env = process.env.PATH_TYPE;

let interfaceData = {
}

switch (env) {
  case "pro":
    interfaceData = {
      mainOptions: {
        fullNode: 'http://47.252.84.158:8090',
        solidityNode: 'http://47.252.84.158:8090',
        eventServer: 'http://47.252.84.158:8090'
      },
      sideOptions: {
        fullNode: 'http://47.252.85.90:8090',
        solidityNode: 'http://47.252.85.90:8091',
        eventServer: 'http://47.252.84.141:8080'  
      },
      mainGatewayAddress: 'TAvMDjZpb3MNUJNjXmnYo17MHkLChAu5nT',
      sideGatewayAddress: 'TJ4apMhB5fhmAwqPcgX9i43SUJZuK6eZj4',
      chainId: '410A6DBD0780EA9B136E3E9F04EBE80C6C288B80EE',

      SERVER_API: 'https://api.trondice.org', // 邀请 历史排行
      // 排行榜websocket

      TRON_GRID: "https://api.trongrid.io", // 获取账户信息
      // 游戏合约
      contractAddress: "TXK6pbKRV7i6fPNdTfAHY7AsTs4URJruhV",
      // 代币合约
      trx20ContractAddress: 'THvZvKPLHKLJhEFYKiyqj6j8G8nGgfg7ur'
      
    }
    break;
  default:
    interfaceData = {
    mainOptions: {
        fullNode: 'http://47.252.84.158:8090',
        solidityNode: 'http://47.252.84.158:8090',
        eventServer: 'http://47.252.84.158:8090'
        },
        sideOptions: {
        fullNode: 'http://47.252.85.90:8090',
        solidityNode: 'http://47.252.85.90:8091',
        eventServer: 'http://47.252.84.141:8080'  
        },
        mainGatewayAddress: 'TAvMDjZpb3MNUJNjXmnYo17MHkLChAu5nT',
        sideGatewayAddress: 'TJ4apMhB5fhmAwqPcgX9i43SUJZuK6eZj4',
        chainId: '410A6DBD0780EA9B136E3E9F04EBE80C6C288B80EE',

        SERVER_API: 'https://api.trondice.org', // 邀请 历史排行
        // 排行榜websocket

        TRON_GRID: "https://api.trongrid.io", // 获取账户信息
        // 游戏合约
        contractAddress: "TXK6pbKRV7i6fPNdTfAHY7AsTs4URJruhV",
        // 代币合约
        trx20ContractAddress: 'THvZvKPLHKLJhEFYKiyqj6j8G8nGgfg7ur'
    }
    break;
}
export default interfaceData;
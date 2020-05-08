const env = process.env.PATH_TYPE;

let interfaceData = {
}

switch (env) {
  case "pro":
    interfaceData = {
      mainOptions: {
        fullNode: 'https://api.trongrid.io',
        solidityNode: 'https://api.trongrid.io',
        eventServer: 'https://api.trongrid.io'
      },
      sideOptions: {
        fullNode: 'https://sun.tronex.io',
        solidityNode: 'https://sun.tronex.io',
        eventServer: 'https://sun.tronex.io'
      },
      mainGatewayAddress: 'TWaPZru6PR5VjgT4sJrrZ481Zgp3iJ8Rfo',
      sideGatewayAddress: 'TGKotco6YoULzbYisTBuP6DWXDjEgJSpYz',
      chainId: '41E209E4DE650F0150788E8EC5CAFA240A23EB8EB7',

      SERVER_API: 'https://api.trondice.org', // 邀请 历史排行
      // 排行榜websocket

      TRON_GRID: "https://api.trongrid.io", // 获取账户信息
      // 游戏合约
      contractAddress: "TWTwnZRxcMWAnsfqZUYHJ3R9P5dbyGD9CC",
      // 代币合约
      trx20ContractAddress: 'THvZvKPLHKLJhEFYKiyqj6j8G8nGgfg7ur',
      depositFee: 0,
      withdrawFee: 10,
      privateKey: '',
      ownAddr: 'TCvLPwVZUeDA5SDAdFXgPCxTcsLCTQ7FuM',
    }
    break;
    case "local":
    interfaceData = {
      mainOptions: {
        fullNode: 'http://47.252.84.158:8070',
        solidityNode: 'http://47.252.84.158:8071',
        eventServer: 'http://47.252.81.14:8080'
      },
      sideOptions: {
        fullNode: 'http://47.252.85.90:8070',
        solidityNode: 'http://47.252.85.90:8071',
        eventServer: 'http://47.252.87.129:8080'
      },
      mainGatewayAddress: 'TFLtPoEtVJBMcj6kZPrQrwEdM3W3shxsBU',
      sideGatewayAddress: 'TRDepx5KoQ8oNbFVZ5sogwUxtdYmATDRgX',
      chainId: '413AF23F37DA0D48234FDD43D89931E98E1144481B',

      SERVER_API: 'https://api.trondice.org', // 邀请 历史排行
      // 排行榜websocket

      TRON_GRID: "https://api.trongrid.io", // 获取账户信息
      // 游戏合约
      contractAddress: "TBaAtBDnXYjbW93udgdXXtPLFmfRrTzQbf",
      // 代币合约
      trx20ContractAddress: 'THvZvKPLHKLJhEFYKiyqj6j8G8nGgfg7ur',
      depositFee: 0,
      withdrawFee: 10,
      privateKey: '',
      ownAddr: 'TNR8HtC7FHB9QSjpR5sc736cYJZpYXEYz5',
    }
    break;
  default:
    interfaceData = {
      mainOptions: {
        fullNode: 'http://47.252.84.158:8070',
        solidityNode: 'http://47.252.84.158:8071',
        eventServer: 'http://47.252.81.14:8080'
      },
      sideOptions: {
        fullNode: 'http://47.252.85.90:8070',
        solidityNode: 'http://47.252.85.90:8071',
        eventServer: 'http://47.252.87.129:8080'
      },
      mainGatewayAddress: 'TFLtPoEtVJBMcj6kZPrQrwEdM3W3shxsBU',
      sideGatewayAddress: 'TRDepx5KoQ8oNbFVZ5sogwUxtdYmATDRgX',
      chainId: '413AF23F37DA0D48234FDD43D89931E98E1144481B',


      SERVER_API: 'https://api.trondice.org', // 邀请 历史排行
      // 排行榜websocket

      TRON_GRID: "https://api.trongrid.io", // 获取账户信息
      // 游戏合约
      contractAddress: "TBaAtBDnXYjbW93udgdXXtPLFmfRrTzQbf",
      // 代币合约
      trx20ContractAddress: 'THvZvKPLHKLJhEFYKiyqj6j8G8nGgfg7ur',
      depositFee: 0,
      withdrawFee: 10,
      privateKey: '',
      ownAddr: 'TNR8HtC7FHB9QSjpR5sc736cYJZpYXEYz5',
    }
    break;
}
export default interfaceData;

const env = process.env.PATH_TYPE;

let interfaceData = {
}

switch (env) {
  case "pro":
    interfaceData = {
      mainOptions: {
        fullNode: 'http://47.252.84.158:8070',
        solidityNode: 'http://47.252.84.158:8071',
        eventServer: 'http://47.252.81.14:8070'
      },
      sideOptions: {
        fullNode: 'http://47.252.85.90:8070',
        solidityNode: 'http://47.252.85.90:8071',
        eventServer: 'http://47.252.87.129:8070'  
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
    case "local":
    interfaceData = {
      mainOptions: {
        fullNode: 'http://47.252.84.158:8070',
        solidityNode: 'http://47.252.84.158:8071',
        eventServer: 'http://47.252.81.14:8070'
      },
      sideOptions: {
        fullNode: 'http://47.252.85.90:8070',
        solidityNode: 'http://47.252.85.90:8071',
        eventServer: 'http://47.252.87.129:8070'  
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
        eventServer: 'http://47.252.81.14:8070'
      },
      sideOptions: {
        fullNode: 'http://47.252.85.90:8070',
        solidityNode: 'http://47.252.85.90:8071',
        eventServer: 'http://47.252.87.129:8070'  
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
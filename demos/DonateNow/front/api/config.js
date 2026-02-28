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

      contractAddress: "TFiuRoT2nKTKFJ5Ls7tgSXoikqRqmMatyY",
      privateKey: '',
      ownAddr: 'TCvLPwVZUeDA5SDAdFXgPCxTcsLCTQ7FuM',
      depositFee: 0,
      withdrawFee: 10,
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
      contractAddress: "TM4L5bZLAnMMZrRLoMycQJr1c6UYd5gXN3",
      privateKey: '3AB9813D96EBCAC2B678AC01DB766D1CB063115C0AE4A0E3AFF11DFA0DDB144F',
      ownAddr: 'TNR8HtC7FHB9QSjpR5sc736cYJZpYXEYz5',
      depositFee: 0,
      withdrawFee: 10,
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
      contractAddress: "TM4L5bZLAnMMZrRLoMycQJr1c6UYd5gXN3",
      privateKey: '3AB9813D96EBCAC2B678AC01DB766D1CB063115C0AE4A0E3AFF11DFA0DDB144F',
      ownAddr: 'TNR8HtC7FHB9QSjpR5sc736cYJZpYXEYz5',
      depositFee: 0,
      withdrawFee: 10,
    }
    break;
}
export default interfaceData;

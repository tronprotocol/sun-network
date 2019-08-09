const env = process.env.PATH_TYPE;

let interfaceData = {
}

switch (env) {
  case "pro":
    interfaceData = {
      mainOptions: {
        fullNode: 'http://fullnode.tron.network',
        solidityNode: 'http://solidity.tron.network',
        eventServer: 'http://fullnode.tron.network'
      },
      sideOptions: {
        fullNode: 'http://fullnode.sun.network',
        solidityNode: 'http://solidity.sun.network',
        eventServer: 'http://47.252.84.141:8080'  
      },
      mainGatewayAddress: 'TGHxhFu4jV4XqMGmk3tEQdSeihWVHE9kBP',
      sideGatewayAddress: 'TBHr5KpbA7oACUysTKxHiAD7c6X6nkZii1',
      chainId: '41455cb714d762dc46d490eab37bba67b0ba910a59',
      contractAddress: "TKwqhYfr7bzWy1aGBHziUeJJ4jWq7g5kPj"
    }
    break;
  default:
    interfaceData = {
      mainOptions: {
        fullNode: 'http://fullnode.tron.network',
        solidityNode: 'http://solidity.tron.network',
        eventServer: 'http://fullnode.tron.network'
      },
      sideOptions: {
        fullNode: 'http://fullnode.sun.network',
        solidityNode: 'http://solidity.sun.network',
        eventServer: 'http://47.252.84.141:8080'  
      },
      mainGatewayAddress: 'TGHxhFu4jV4XqMGmk3tEQdSeihWVHE9kBP',
      sideGatewayAddress: 'TBHr5KpbA7oACUysTKxHiAD7c6X6nkZii1',
      chainId: '41455cb714d762dc46d490eab37bba67b0ba910a59',
      contractAddress: "TWdczkpcnG71X6zYCSXpWNJn6fUfFNEwLT"
    }
    break;
}
export default interfaceData;
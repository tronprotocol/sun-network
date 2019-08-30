const SunWeb = require('sunweb');
const mainOptions = {
    fullNode: 'http://fullnode.tron.network',
    solidityNode: 'http://solidity.tron.network',
    eventServer: 'http://fullnode.tron.network'
};
const sideOptions = {
    fullNode: 'http://fullnode.sun.network',
    solidityNode: 'http://solidity.sun.network',
    eventServer: 'http://fullnode.sun.network'
};
const mainGatewayAddress = 'TGHxhFu4jV4XqMGmk3tEQdSeihWVHE9kBP';
const sideGatewayAddress = 'TBHr5KpbA7oACUysTKxHiAD7c6X6nkZii1';
const chainID = '41455cb714d762dc46d490eab37bba67b0ba910a59';
const privateKey = 'e901ef62b241b6f1577fd6ea34ef8b1c4b3ddee1e3c051b9e63f5ff729ad47a1';

const sunWeb = new SunWeb(
    mainOptions,
    sideOptions,
    mainGatewayAddress,
    sideGatewayAddress,
    chainID,
    privateKey
);
sunWeb.depositTrx(1000, 1000000).then(data => {
    console.log(data);
}).catch(err => console.log(err));
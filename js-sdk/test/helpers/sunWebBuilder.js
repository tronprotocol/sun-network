// const chalk = require('chalk');
const TronWeb = require('tronweb');
const SunWeb = require('../setup/SunWeb');
const { PRIVATE_KEY, CONSUME_USER_RESOURCE_PERCENT, FEE_LIMIT, MAIN_FULL_NODE_API, MAIN_SOLIDITY_NODE_API, MAIN_EVENT_API, SIDE_FULL_NODE_API, SIDE_SOLIDITY_NODE_API, SIDE_EVENT_API, MAIN_GATEWAY_ADDRESS, SIDE_GATEWAY_ADDRESS, ADDRESS_HEX, ADDRESS_BASE58, SIDE_CHAIN_ID} = require('./config');

const createInstance = () => {
    return new SunWeb(
    TronWeb, {
        fullNode: MAIN_FULL_NODE_API,
        solidityNode: MAIN_SOLIDITY_NODE_API,
        eventServer: MAIN_EVENT_API,
    }, {
        fullNode: SIDE_FULL_NODE_API,
        solidityNode: SIDE_SOLIDITY_NODE_API,
        eventServer: SIDE_EVENT_API,
    },
    MAIN_GATEWAY_ADDRESS,
    SIDE_GATEWAY_ADDRESS,
    SIDE_CHAIN_ID,
    PRIVATE_KEY)
};

let instance;

const getInstance = () => {
    if (!instance) {
        instance = createInstance();
    }
    return instance;
}

module.exports = {
    createInstance,
    getInstance,
    SunWeb
}

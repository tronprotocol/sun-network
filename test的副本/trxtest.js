const {TIMEOUT, PRIVATE_KEY, ACCOUNTADDRESS,ADDRESS_HEX1,MAIN_GATEWAY_ADDRESS_HEX,ADDRESS_HEX2,CONTRACT_ADDRESS20,ADDRESS20_MAPPING,ADDRESS721_MAPPING,CONTRACT_ADDRESS721, FEE_LIMIT, MAIN_FULL_NODE_API, MAIN_SOLIDITY_NODE_API, MAIN_EVENT_API, SIDE_FULL_NODE_API, SIDE_SOLIDITY_NODE_API, SIDE_EVENT_API, MAIN_GATEWAY_ADDRESS, SIDE_GATEWAY_ADDRESS, ADDRESS_HEX, ADDRESS_BASE58, TOKEN_ID,  HASH20, HASH721, SIDE_CHAIN_ID} = require('./helpers/config');

const sunBuilder = require('./helpers/sunWebBuilder');
const SunWeb = sunBuilder.SunWeb;

const chai = require('chai');
const assert = chai.assert;
const assertThrow = require('./helpers/assertThrow');

describe('SunWeb Instance', function() {
    describe('#constructor', function() {
        it('should create an instance using an options object with private key', function() {
            const sunWeb = sunBuilder.createInstance();
            assert.instanceOf(sunWeb, SunWeb);
            assert.equal(sunWeb.mainchain.defaultPrivateKey, PRIVATE_KEY);
            assert.equal(sunWeb.sidechain.defaultPrivateKey, PRIVATE_KEY);
        });

        it('should create an instance using an options object without private key', function() {
            const mainOptions = {
                fullNode: MAIN_FULL_NODE_API,
                solidityNode: MAIN_SOLIDITY_NODE_API,
                eventServer: MAIN_EVENT_API
            };
            const sideOptions = {
                fullNode: SIDE_FULL_NODE_API,
                solidityNode: SIDE_SOLIDITY_NODE_API,
                eventServer: SIDE_EVENT_API
            };
            const sunWeb = new SunWeb(
                mainOptions,
                sideOptions,
                MAIN_GATEWAY_ADDRESS,
                SIDE_GATEWAY_ADDRESS,
                SIDE_CHAIN_ID
            );
            assert.equal(sunWeb.mainchain.defaultPrivateKey, false);
            assert.equal(sunWeb.sidechain.defaultPrivateKey, false);
        });

        it('should create an instance using an options object which only constains a fullhost without private key', function() {
            const mainOptions = {
                fullHost: MAIN_FULL_NODE_API
            };
            const sideOptions = {
                fullHost: SIDE_FULL_NODE_API
            };
            const sunWeb = new SunWeb(
                mainOptions,
                sideOptions,
                MAIN_GATEWAY_ADDRESS,
                SIDE_GATEWAY_ADDRESS,
                SIDE_CHAIN_ID
            );
            assert.instanceOf(sunWeb, SunWeb);
            assert.equal(sunWeb.mainchain.defaultPrivateKey, false);
            assert.equal(sunWeb.sidechain.defaultPrivateKey, false);
        });

        it('should create an instance using an options object which only constains a fullhost with private key', function() {
            const mainOptions = {
                fullHost: MAIN_FULL_NODE_API
            };
            const sideOptions = {
                fullHost: SIDE_FULL_NODE_API
            };
            const sunWeb = new SunWeb(
                mainOptions,
                sideOptions,
                MAIN_GATEWAY_ADDRESS,
                SIDE_GATEWAY_ADDRESS,
                SIDE_CHAIN_ID,
                PRIVATE_KEY
            );
            assert.instanceOf(sunWeb, SunWeb);
            assert.equal(sunWeb.mainchain.defaultPrivateKey, PRIVATE_KEY);
            assert.equal(sunWeb.sidechain.defaultPrivateKey, PRIVATE_KEY);
        });
    });

    describe('#trx', function() {

        describe('#depositTrx()', function () {
            const sunWeb = sunBuilder.createInstance();

            it('deposit trx from main chain to side chain', async function () {
                const mdepositBalancebefore = await sunWeb.mainchain.trx.getBalance();
                const sdepositBalancebefore = await sunWeb.sidechain.trx.getBalance();
                console.log('mBefore:' + mdepositBalancebefore);
                console.log('sBefore:' + sdepositBalancebefore);
                const callValue = 100000;
                const txID = await sunWeb.depositTrx(callValue, FEE_LIMIT);
                await TIMEOUT(80000);
                const result =await sunWeb.mainchain.trx.getTransactionInfo(txID);
                console.log(result)
                const fee = result.fee;
                console.log('fee:' + fee)


                const mdepositBalanceafter = await sunWeb.mainchain.trx.getBalance();
                const sdepositBalanceafter = await sunWeb.sidechain.trx.getBalance();

                console.log('mdepositBalanceafter: ' +  mdepositBalanceafter)
                console.log('sdepositBalanceafter: ' +  sdepositBalanceafter)
                assert.equal(txID.length, 64);
                assert.equal(mdepositBalanceafter, mdepositBalancebefore - callValue - fee);
                // assert.equal(sdepositBalanceafter, sdepositBalancebefore + callValue);
            });

            // describe('#withdrawTrx()', function () {
            //     const sunWeb = sunBuilder.createInstance();
            //     it('withdraw trx from side chain to main chain', async function () {
            //         const mwithdrawBalancebefore = await sunWeb.mainchain.trx.getBalance();
            //         const swithdrawBalancebefore = await sunWeb.sidechain.trx.getBalance();
            //         const callValue = 10000000;
            //         const txID = await sunWeb.withdrawTrx(callValue, 0,100000000);
            //         await TIMEOUT(80000);
            //         const mwithdrawBalanceafter = await sunWeb.mainchain.trx.getBalance();
            //         const swithdrawBalanceafter = await sunWeb.sidechain.trx.getBalance();
            //
            //         assert.equal(txID.length, 64);
            //         assert.equal(mwithdrawBalancebefore +callValue, mwithdrawBalanceafter);
                    // assert.equal(swithdrawBalanceafter, swithdrawBalancebefore -callValue);
            //     });
            //
            // });
        });
    });
});


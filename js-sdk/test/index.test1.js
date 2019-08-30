const {TIMEOUT, PRIVATE_KEY, CONSUME_USER_RESOURCE_PERCENT, FEE_LIMIT, MAIN_FULL_NODE_API, MAIN_SOLIDITY_NODE_API, MAIN_EVENT_API, SIDE_FULL_NODE_API, SIDE_SOLIDITY_NODE_API, SIDE_EVENT_API, MAIN_GATEWAY_ADDRESS, SIDE_GATEWAY_ADDRESS, ADDRESS_HEX, ADDRESS_BASE58, TOKEN_ID, CONTRACT_ADDRESS20, HASH20, HASH721, CONTRACT_ADDRESS721, ADDRESS20_MAPPING, ADDRESS721_MAPPING, SIDE_CHAIN_ID} = require('./helpers/config');

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

        describe('#depositTrc10()', function () {
            const sunWeb = sunBuilder.createInstance();
            it('deposit trc10 from main chain to side chain', async function () {
                const saccountafter =await  sunWeb.sidechain.trx.getAccount(ACCOUNTADDRESS);
                const maccountafter=await  sunWeb.mainchain.trx.getAccount(ACCOUNTADDRESS);

                console.log(saccountafter);
                console.log(maccountafter);
                const  maccountafterinfo = maccountafter.assetV2.filter(function(item) {
                    return item.key == TOKEN_ID;
                });
                console.log(maccountafterinfo);
                const mtokenvalueafter= maccountafterinfo[0].value;
                console.log(mtokenvalueafter)
                console.log(maccountafter);
                const  saccountafterinfo = saccountafter.assetV2.filter(function(item) {
                    return item.key == TOKEN_ID;
                });
                console.log(saccountafterinfo);
                const stokenvalueafter= saccountafterinfo[0].value;
                console.log(stokenvalueafter)
                console.log(saccountafter);

                const tokenValue = 1000;

                const txID = await sunWeb.depositTrc10(TOKEN_ID, tokenValue, FEE_LIMIT);
                assert.equal(txID.length, 64);
                await TIMEOUT(60000);

                const saccountbefore =await  sunWeb.sidechain.trx.getAccount(ACCOUNTADDRESS);
                const maccountbefore=await  sunWeb.mainchain.trx.getAccount(ACCOUNTADDRESS);

                console.log(saccountbefore);
                console.log(maccountbefore);
                const  maccountbeforeinfo = maccountbefore.assetV2.filter(function(item) {
                    return item.key == TOKEN_ID;
                });
                console.log(maccountbeforeinfo);
                const mtokenvaluebefore= maccountbeforeinfo[0].value;
                console.log(mtokenvaluebefore)
                console.log(maccountbefore);
                const  saccountbeforeinfo = saccountbefore.assetV2.filter(function(item) {
                    return item.key == TOKEN_ID;
                });
                console.log(saccountbeforeinfo);
                const stokenvaluebefore= saccountbeforeinfo[0].value;
                console.log(mtokenvaluebefore);
                console.log(stokenvaluebefore);

                assert.equal(stokenvalueafter+tokenValue, stokenvaluebefore);
                assert.equal(mtokenvalueafter-tokenValue, mtokenvaluebefore);






            });

        });
        describe('#withdrawTrc10()', function () {
            const sunWeb = sunBuilder.createInstance();
            it('withdraw trc10 from side chain to main chain', async function () {


                const swithdrawbefore =await  sunWeb.sidechain.trx.getAccount(ACCOUNTADDRESS);
                const mwithdrawbefore=await  sunWeb.mainchain.trx.getAccount(ACCOUNTADDRESS);

                console.log(swithdrawbefore);
                console.log(mwithdrawbefore);
                const  mwithdrawbeforeinfo = mwithdrawbefore.assetV2.filter(function(item) {
                    return item.key == TOKEN_ID;
                });
                console.log(mwithdrawbeforeinfo);
                const mwithdrawtokenvaluebefore= mwithdrawbeforeinfo[0].value;
                console.log(mwithdrawtokenvaluebefore)

                const  swithdrawbeforeinfo = swithdrawbefore.assetV2.filter(function(item) {
                    return item.key == TOKEN_ID;
                });
                console.log(swithdrawbeforeinfo);
                const swithdrawtokenvaluebefore= swithdrawbeforeinfo[0].value;
                console.log(swithdrawtokenvaluebefore);
                const tokenValue1 = 1;
                const txID = await sunWeb.withdrawTrc10(TOKEN_ID, tokenValue1, 0,FEE_LIMIT);
                await TIMEOUT(60000);
                assert.equal(txID.length, 64);
                const swithdrawafter =await  sunWeb.sidechain.trx.getAccount(ACCOUNTADDRESS);
                const mwithdrawafter=await  sunWeb.mainchain.trx.getAccount(ACCOUNTADDRESS);

                console.log(swithdrawafter);
                console.log(mwithdrawafter);
                const  mwithdrawafterinfo = mwithdrawafter.assetV2.filter(function(item) {
                    return item.key == TOKEN_ID;
                });
                console.log(mwithdrawafterinfo);
                const mwithdrawtokenvalueafter= mwithdrawafterinfo[0].value;
                console.log(mwithdrawtokenvalueafter)

                const  swithdrawafterinfo = swithdrawafter.assetV2.filter(function(item) {
                    return item.key == TOKEN_ID;
                });
                console.log(swithdrawafterinfo);
                const swithdrawtokenvalueafter= swithdrawafterinfo[0].value;
                console.log(swithdrawtokenvalueafter);
                // await TIMEOUT(80000);
                assert.equal(swithdrawtokenvaluebefore-tokenValue1, swithdrawtokenvalueafter);

                assert.equal(mwithdrawtokenvaluebefore+tokenValue1, mwithdrawtokenvalueafter);



            });

        });

        it('should throw if an invalid token value is passed', async function () {
            const tokenValue = 100.01;
            await assertThrow(
                sunWeb.depositTrc10(TOKEN_ID, tokenValue, 1000000),
                'Invalid tokenValue provided'
            );
        });

        it('should throw if an invalid fee limit is passed', async function () {
            const feeLimit = 100000000000;
            await assertThrow(
                sunWeb.depositTrc10(TOKEN_ID, 100, feeLimit),
                'Invalid feeLimit provided'
            );
        });
    });
});

    //     describe('#depositTrc20', function() {
    //         const sunWeb = sunBuilder.createInstance();
    //         it('deposit trc20 from main chain to side chain', async function() {
    //             const num = 100;
    //             const txID = await sunWeb.depositTrc20(num, FEE_LIMIT, CONTRACT_ADDRESS20);
    //             assert.equal(txID.length, 64);
    //             async function depass(){
    //                 a=await  sunWeb.mainchain.contract().at('TGKuXDnvdHv9RNE6BPXUtNLK2FrVMBDAuA');
    //                 console.log(a)
    //                 b=await contractInstance.balanceOf('TG5wFVvrJiTkBA1WaZN3pzyJDfkgHMnFrp').call()
    //                 console.log(b)
    //
    //             }
    //
    //
    //
    //         });
    //
    //         it('depositTrc20 with the defined private key', async function() {
    //             const num = 100;
    //             const options = {};
    //             const txID = await sunWeb.depositTrc20(num, FEE_LIMIT, CONTRACT_ADDRESS20, options, PRIVATE_KEY);
    //             assert.equal(txID.length, 64);
    //         });
    //
    //         it('depositTrc20 with permissionId in options object', async function() {
    //             const num = 100;
    //             const options = { permissionId: 0 };
    //             const txID = await sunWeb.depositTrc20(num, FEE_LIMIT, CONTRACT_ADDRESS20, options);
    //             assert.equal(txID.length, 64);
    //         });
    //
    //         it('depositTrc20 with permissionId in options object and the defined private key', async function() {
    //             const num = 100;
    //             const options = { permissionId: 0 };
    //             const txID = await sunWeb.depositTrc20(num, FEE_LIMIT, CONTRACT_ADDRESS20, options, PRIVATE_KEY);
    //             assert.equal(txID.length, 64);
    //         });
    //
    //         it('should throw if an invalid num is passed', async function() {
    //             const num = 100.01;
    //             await assertThrow(
    //                 sunWeb.depositTrc20(num, FEE_LIMIT, CONTRACT_ADDRESS20),
    //                 'Invalid num provided'
    //             );
    //         });
    //
    //         it('should throw if an invalid fee limit is passed', async function() {
    //             const num = 100;
    //             const feeLimit = 100000000000;
    //             await assertThrow(
    //                 sunWeb.depositTrc20(num, feeLimit, CONTRACT_ADDRESS20),
    //                 'Invalid feeLimit provided'
    //             );
    //         });
    //
    //         it('should throw if an invalid contract address is passed', async function() {
    //             await assertThrow(
    //                 sunWeb.depositTrc20(100, FEE_LIMIT, 'aaaaaaaaaa'),
    //                 'Invalid contractAddress address provided'
    //             );
    //         });
    //     });
    //
    //     describe('#depositTrc721', function() {
    //         const sunWeb = sunBuilder.createInstance();
    //         it('deposit trc721 from main chain to side chain', async function () {
    //             const id = 100;
    //             const txID = await sunWeb.depositTrc20(id, FEE_LIMIT, CONTRACT_ADDRESS721);
    //             assert.equal(txID.length, 64);
    //         });
    //     });
    // });
    //
    // describe('#mappingTrc', function() {
    //     const sunWeb = sunBuilder.createInstance();
    //     it('mappingTrc20', async function() {
    //         const txID = await sunWeb.mappingTrc20(HASH20, FEE_LIMIT);
    //         assert.equal(txID.length, 64);
    //     });
    //
    //     it('mappingTrc20 with the defined private key', async function() {
    //         const options = {};
    //         const txID = await sunWeb.mappingTrc20(HASH20, FEE_LIMIT, options, PRIVATE_KEY);
    //         assert.equal(txID.length, 64);
    //     });
    //
    //     it('mappingTrc20 with permissionId in options object', async function() {
    //         const options = { permissionId: 0 };
    //         const txID = await sunWeb.mappingTrc20(HASH20, FEE_LIMIT, options);
    //         assert.equal(txID.length, 64);
    //     });
    //
    //     it('mappingTrc20 with permissionId in options object and the defined private key', async function() {
    //         const options = { permissionId: 0 };
    //         const txID = await sunWeb.mappingTrc20(HASH20, FEE_LIMIT, options, PRIVATE_KEY);
    //         assert.equal(txID.length, 64);
    //     });
    //
    //     it('should throw if an invalid trxHash', async function() {
    //         const trxHash = '';
    //         await assertThrow(
    //             sunWeb.mappingTrc20(trxHash, FEE_LIMIT),
    //             'Invalid trxHash provided'
    //         );
    //     });
    //
    //     it('should throw if an invalid fee limit is passed', async function() {
    //         const feeLimit = 100000000000;
    //         await assertThrow(
    //             sunWeb.mappingTrc20(HASH20, feeLimit),
    //             'Invalid feeLimit provided'
    //         );
    //     });
    //
    //     it('mappingTrc721', async function() {
    //         const txID = await sunWeb.mappingTrc721(HASH721, FEE_LIMIT);
    //         assert.equal(txID.length, 64);
    //     })
    // });
    //
    // describe('#withdraw', function() {
    //     describe('#withdrawTrx()', function() {
    //         const sunWeb = sunBuilder.createInstance();
    //         it('withdraw trx from side chain to main chain', async function() {
    //             const txID = await sunWeb.withdrawTrx(10000000, 10000000);
    //             assert.equal(txID.length, 64);
    //         });
    //
    //         it('withdrawTrx with the defined private key', async function() {
    //             const callValue = 10000000;
    //             const options = {};
    //             const txID = await sunWeb.withdrawTrx(callValue, FEE_LIMIT, options, PRIVATE_KEY);
    //             assert.equal(txID.length, 64);
    //         });
    //
    //         it('withdrawTrx with permissionId in options object', async function() {
    //             const callValue = 10000000;
    //             const options = { permissionId: 0 };
    //             const txID = await sunWeb.withdrawTrx(callValue, FEE_LIMIT, options);
    //             assert.equal(txID.length, 64);
    //         });
    //
    //         it('withdrawTrx with permissionId in options object and the defined private key', async function() {
    //             const callValue = 10;
    //             const options = { permissionId: 0 };
    //             const txID = await sunWeb.withdrawTrx(callValue, FEE_LIMIT, options, PRIVATE_KEY);
    //             assert.equal(txID.length, 64);
    //         });
    //
    //         it('should throw if an invalid trx number is passed', async function() {
    //             await assertThrow(
    //                 sunWeb.withdrawTrx(1000.01, FEE_LIMIT),
    //                 'Invalid callValue provided'
    //             );
    //         });
    //
    //         it('should throw if an invalid fee limit is passed', async function() {
    //             await assertThrow(
    //                 sunWeb.withdrawTrx(10000, 0),
    //                 'Invalid feeLimit provided'
    //             );
    //         });
    //     });
    //
    //     describe('#withdrawTrc10()', function() {
    //         const sunWeb = sunBuilder.createInstance();
    //         it('withdraw trc10 from side chain to main chain', async function() {
    //             const tokenValue = 10;
    //             const txID = await sunWeb.withdrawTrc10(TOKEN_ID, tokenValue, FEE_LIMIT);
    //             assert.equal(txID.length, 64);
    //         });
    //
    //         it('withdrawTrc10 with the defined private key', async function() {
    //             const tokenValue = 10;
    //             const options = {};
    //             const txID = await sunWeb.withdrawTrc10(TOKEN_ID, tokenValue, FEE_LIMIT, options, PRIVATE_KEY);
    //             assert.equal(txID.length, 64);
    //         });
    //
    //         it('withdrawTrc10 with permissionId in options object', async function() {
    //             const tokenValue = 10;
    //             const options = { permissionId: 0 };
    //             const txID = await sunWeb.withdrawTrc10(TOKEN_ID, tokenValue, FEE_LIMIT, options);
    //             assert.equal(txID.length, 64);
    //         });
    //
    //         it('withdrawTrc10 with permissionId in options object and the defined private key', async function() {
    //             const tokenValue = 10;
    //             const options = { permissionId: 0 };
    //             const txID = await sunWeb.withdrawTrc10(TOKEN_ID, tokenValue, FEE_LIMIT, options, PRIVATE_KEY);
    //             assert.equal(txID.length, 64);
    //         });
    //
    //         it('should throw if an invalid token id is passed', async function() {
    //             const tokenId = -10;
    //             await assertThrow(
    //                 sunWeb.withdrawTrc10(tokenId, 100, 1000000),
    //                 'Invalid tokenId provided'
    //             )
    //         });
    //
    //         it('should throw if an invalid token value is passed', async function() {
    //             const tokenValue = 10.01;
    //             await assertThrow(
    //                 sunWeb.withdrawTrc10(TOKEN_ID, tokenValue, FEE_LIMIT),
    //                 'Invalid tokenValue provided'
    //             );
    //         });
    //
    //         it('should throw if an invalid fee limit is passed', async function() {
    //             const feeLimit = 100000000000;
    //             await assertThrow(
    //                 sunWeb.withdrawTrc10(TOKEN_ID, 100, feeLimit),
    //                 'Invalid feeLimit provided'
    //             );
    //         });
    //     });
    //
    //     describe('#withdrawTrc', function() {
    //         describe('#withdrawTrc20', function() {
    //             const sunWeb = sunBuilder.createInstance();
    //             it('withdraw trc20 from side chain to main chain', async function() {
    //                 const num = 10;
    //                 const txID = await sunWeb.withdrawTrc20(num, FEE_LIMIT, ADDRESS20_MAPPING);
    //                 assert.equal(txID.length, 64);
    //             });
    //
    //             it('withdrawTrc20 with the defined private key', async function() {
    //                 const num = 10;
    //                 const options = {};
    //                 const txID = await sunWeb.withdrawTrc20(num, FEE_LIMIT, ADDRESS20_MAPPING, options, PRIVATE_KEY);
    //                 assert.equal(txID.length, 64);
    //             });
    //
    //             it('withdrawTrc20 with permissionId in options object', async function() {
    //                 const num = 10;
    //                 const options = { permissionId: 0 };
    //                 const txID = await sunWeb.withdrawTrc20(num, FEE_LIMIT, ADDRESS20_MAPPING, options);
    //                 assert.equal(txID.length, 64);
    //             });
    //
    //             it('withdrawTrc20 with permissionId in options object and the defined private key', async function() {
    //                 const num = 10;
    //                 const options = { permissionId: 0 };
    //                 const txID = await sunWeb.withdrawTrc20(num, FEE_LIMIT, ADDRESS20_MAPPING, options, PRIVATE_KEY);
    //                 assert.equal(txID.length, 64);
    //             });
    //
    //             it('should throw if an invalid num is passed', async function() {
    //                 const num = 10.01;
    //                 await assertThrow(
    //                     sunWeb.withdrawTrc20(num, FEE_LIMIT, ADDRESS20_MAPPING),
    //                     'Invalid numOrId provided'
    //                 );
    //             });
    //
    //             it('should throw if an invalid fee limit is passed', async function() {
    //                 const feeLimit = 100000000000;
    //                 await assertThrow(
    //                     sunWeb.withdrawTrc20(100, feeLimit, ADDRESS20_MAPPING),
    //                     'Invalid feeLimit provided'
    //                 );
    //             });
    //
    //             it('should throw if an invalid contract address is passed', async function() {
    //                 await assertThrow(
    //                     sunWeb.withdrawTrc20(100, FEE_LIMIT, 'aaaaaaaaaa'),
    //                     'Invalid contractAddress address provided'
    //                 );
    //             });
    //         });
    //
    //         describe('#withdrawTrc721', async function() {
    //             const sunWeb = sunBuilder.createInstance();
    //             it('withdraw trc721 from side chain to main chain', async function() {
    //                 const id = 100;
    //                 const txID = await sunWeb.withdrawTrc721(id, FEE_LIMIT, ADDRESS20_MAPPING);
    //                 assert.equal(txID.length, 64);
    //             });
//             });
//         });
//     });
// });



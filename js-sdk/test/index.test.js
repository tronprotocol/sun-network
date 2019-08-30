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

        // describe('#depositTrx()', function () {
        //     const sunWeb = sunBuilder.createInstance();
        //
        //     it('deposit trx from main chain to side chain', async function () {
        //         const mdepositBalancebefore = await sunWeb.mainchain.trx.getBalance();
        //         const sdepositBalancebefore = await sunWeb.sidechain.trx.getBalance();
        //         const callValue = 10000000;
        //         const txID = await sunWeb.depositTrx(callValue, FEE_LIMIT);
        //         await TIMEOUT(60000);
        //         const result =await sunWeb.mainchain.trx.getTransactionInfo(txID);
        //         console.log(result)
        //         const fee = result.fee;
        //         console.log('fee:' + fee)
        //
        //         console.log('mBefore:' + mdepositBalancebefore);
        //         console.log('sBefore:' + sdepositBalancebefore);
        //         const mdepositBalanceafter = await sunWeb.mainchain.trx.getBalance();
        //         const sdepositBalanceafter = await sunWeb.sidechain.trx.getBalance();
        //
        //         console.log('mAfter: ' +  mdepositBalanceafter)
        //         console.log('sAfter: ' +  sdepositBalanceafter)
        //         assert.equal(txID.length, 64);
        //         assert.equal(mdepositBalanceafter, mdepositBalancebefore - callValue - fee);
        //         console.log(124388888)
        //         assert.equal(sdepositBalanceafter, sdepositBalancebefore + callValue);
        //     });
        //
        //     describe('#withdrawTrx()', function () {
        //         const sunWeb = sunBuilder.createInstance();
        //         it('withdraw trx from side chain to main chain', async function () {
        //             const mwithdrawBalancebefore = await sunWeb.mainchain.trx.getBalance();
        //             const swithdrawBalancebefore = await sunWeb.sidechain.trx.getBalance();
        //             const callValue = 10000000;
        //             const txID = await sunWeb.withdrawTrx(callValue, 0,100000000);
        //             await TIMEOUT(30000);
        //             const mwithdrawBalanceafter = await sunWeb.mainchain.trx.getBalance();
        //             const swithdrawBalanceafter = await sunWeb.sidechain.trx.getBalance();
        //
        //             assert.equal(txID.length, 64);
        //             assert.equal(mwithdrawBalancebefore +callValue, mwithdrawBalanceafter);
        //             assert.equal(swithdrawBalanceafter, swithdrawBalancebefore -callValue);
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
                //         const sunWeb = sunBuilder.createInstance();
                //         it('withdraw trc10 from side chain to main chain', async function () {
                //
                //
                //             const swithdrawbefore =await  sunWeb.sidechain.trx.getAccount("TG5wFVvrJiTkBA1WaZN3pzyJDfkgHMnFrp");
                //             const mwithdrawbefore=await  sunWeb.mainchain.trx.getAccount("TG5wFVvrJiTkBA1WaZN3pzyJDfkgHMnFrp");
                //
                //             console.log(swithdrawbefore);
                //             console.log(mwithdrawbefore);
                //             const  mwithdrawbeforeinfo = mwithdrawbefore.assetV2.filter(function(item) {
                //                 return item.key == TOKEN_ID;
                //             });
                //             console.log(mwithdrawbeforeinfo);
                //             const mwithdrawtokenvaluebefore= mwithdrawbeforeinfo[0].value;
                //             console.log(mwithdrawtokenvaluebefore)
                //
                //             const  swithdrawbeforeinfo = swithdrawbefore.assetV2.filter(function(item) {
                //                 return item.key == TOKEN_ID;
                //             });
                //             console.log(swithdrawbeforeinfo);
                //             const swithdrawtokenvaluebefore= swithdrawbeforeinfo[0].value;
                //             console.log(swithdrawtokenvaluebefore);
                //             const tokenValue1 = 1;
                //             const txID = await sunWeb.withdrawTrc10(TOKEN_ID, tokenValue1, 0,FEE_LIMIT);
                //             await TIMEOUT(60000);
                //             assert.equal(txID.length, 64);
                //             const swithdrawafter =await  sunWeb.sidechain.trx.getAccount("TG5wFVvrJiTkBA1WaZN3pzyJDfkgHMnFrp");
                //             const mwithdrawafter=await  sunWeb.mainchain.trx.getAccount("TG5wFVvrJiTkBA1WaZN3pzyJDfkgHMnFrp");
                //
                //             console.log(swithdrawafter);
                //             console.log(mwithdrawafter);
                //             const  mwithdrawafterinfo = mwithdrawafter.assetV2.filter(function(item) {
                //                 return item.key == TOKEN_ID;
                //             });
                //             console.log(mwithdrawafterinfo);
                //             const mwithdrawtokenvalueafter= mwithdrawafterinfo[0].value;
                //             console.log(mwithdrawtokenvalueafter)
                //
                //             const  swithdrawafterinfo = swithdrawafter.assetV2.filter(function(item) {
                //                 return item.key == TOKEN_ID;
                //             });
                //             console.log(swithdrawafterinfo);
                //             const swithdrawtokenvalueafter= swithdrawafterinfo[0].value;
                //             console.log(swithdrawtokenvalueafter);
                //             // await TIMEOUT(80000);
                //             assert.equal(swithdrawtokenvaluebefore-tokenValue1, swithdrawtokenvalueafter);
                //
                //             assert.equal(mwithdrawtokenvaluebefore+tokenValue1, mwithdrawtokenvalueafter);
                //
                //
                //
                //         });
                //
                //     });
                //
                //     it('should throw if an invalid token value is passed', async function () {
                //         const tokenValue = 100.01;
                //         await assertThrow(
                //             sunWeb.depositTrc10(TOKEN_ID, tokenValue, 1000000),
                //             'Invalid tokenValue provided'
                //         );
                //     });
                //
                //     it('should throw if an invalid fee limit is passed', async function () {
                //         const feeLimit = 100000000000;
                //         await assertThrow(
                //             sunWeb.depositTrc10(TOKEN_ID, 100, feeLimit),
                //             'Invalid feeLimit provided'
                //         );
                //     });
                // });
                //
                //
                describe('#depositTrc20', function () {
                    const sunWeb = sunBuilder.createInstance();
                    it('deposit trc20 from main chain to side chain', async function () {
                        mdeposittrc20beforeinfo = await sunWeb.mainchain.contract().at('TT6uSbPHb95Rm9PdQByPfKgtGr4fZrsqZa');
                        mdeposittrc20before = await mdeposittrc20beforeinfo.balanceOf('TG5wFVvrJiTkBA1WaZN3pzyJDfkgHMnFrp').call();

                        indobyid1before=await sunWeb.sidechain.transactionBuilder.triggerSmartContract('TLiUjcoZZ4B5NcmsprpPijU3NHayhCXjxs', 'balanceOf(address)'
                            , {feeLimit: 10000000}, [{type: 'address', value: 'TG5wFVvrJiTkBA1WaZN3pzyJDfkgHMnFrp'}]);

                        infobyid2before=await sunWeb.sidechain.trx.sign(indobyid1before.transaction)

                        infobyid3before=await sunWeb.sidechain.trx.sendRawTransaction(infobyid2before)
                        sdepositTXIDbefore=infobyid3before.transaction.txID;

                        console.log({sdepositTXIDbefore: sdepositTXIDbefore})


                        await TIMEOUT(30000)
                        infobyid4before=  await sunWeb.sidechain.trx.getTransactionInfo(sdepositTXIDbefore);
                        console.log({infobyid4before:infobyid4before})
                        infobyidinfobefore= infobyid4before.contractResult;
                        console.log({infobyidinfobefore:infobyidinfobefore})

                        sdeposittrc20before = parseInt(infobyidinfobefore, 16)
                        console.log({sdeposittrc20before:sdeposittrc20before})

                        // sunWeb.approveTrc20(2000, 10000000, 'TT6uSbPHb95Rm9PdQByPfKgtGr4fZrsqZa')
                        const num = 100;
                        const txID = await sunWeb.depositTrc20(num, FEE_LIMIT, CONTRACT_ADDRESS20);
                        await  TIMEOUT(80000);
                        assert.equal(txID.length, 64);
                        mdeposittrc20afterinfo = await sunWeb.mainchain.contract().at('TT6uSbPHb95Rm9PdQByPfKgtGr4fZrsqZa');
                        mdeposittrc20after = await mdeposittrc20afterinfo.balanceOf('TG5wFVvrJiTkBA1WaZN3pzyJDfkgHMnFrp').call();
                        console.log("mdeposittrc20after："+mdeposittrc20after);

                        indobyid1after=await sunWeb.sidechain.transactionBuilder.triggerSmartContract('TLiUjcoZZ4B5NcmsprpPijU3NHayhCXjxs', 'balanceOf(address)'
                            , {feeLimit: 10000000}, [{type: 'address', value: 'TG5wFVvrJiTkBA1WaZN3pzyJDfkgHMnFrp'}]);

                        indobyid2after=await sunWeb.sidechain.trx.sign(indobyid1after.transaction)

                        indobyid3after=await sunWeb.sidechain.trx.sendRawTransaction(indobyid2after)
                        sdepositTXIDafter=indobyid3after.transaction.txID;

                        console.log({sdepositTXIDafter: sdepositTXIDafter})
                        await TIMEOUT(30000)
                        infobyid4after=  await sunWeb.sidechain.trx.getTransactionInfo(sdepositTXIDafter);
                        console.log({infobyid4after:infobyid4after})
                        infobyidinfoafter= infobyid4after.contractResult;
                        console.log({infobyidinfoafter:infobyidinfoafter})

                        sdeposittrc20after = parseInt(infobyidinfoafter, 16)
                        console.log({sdeposittrc20after:sdeposittrc20after})
                        assert(mdeposittrc20before-num,mdeposittrc20after);
                        assert(sdeposittrc20before+num,sdeposittrc20after);
                    });

                    it('should throw if an invalid num is passed', async function () {
                        const num = 100.01;
                        await assertThrow(
                            sunWeb.depositTrc20(num, FEE_LIMIT, CONTRACT_ADDRESS20),
                            'Invalid num provided'
                        );
                    });

                    it('should throw if an invalid fee limit is passed', async function () {
                        const num = 100;
                        const feeLimit = 100000000000;
                        await assertThrow(
                            sunWeb.depositTrc20(num, feeLimit, CONTRACT_ADDRESS20),
                            'Invalid feeLimit provided'
                        );
                    });

                    it('should throw if an invalid contract address is passed', async function () {
                        await assertThrow(
                            sunWeb.depositTrc20(100, FEE_LIMIT, 'aaaaaaaaaa'),
                            'Invalid contractAddress address provided'
                        );
                    });
                });
                // describe('#withdrawTrc', function () {
                //     describe('#withdrawTrc20', function () {
                //         const sunWeb = sunBuilder.createInstance();
                //         it('withdraw trc20 from side chain to main chain', async function () {
                //             mwithdraw20beforeinfo = await sunWeb.mainchain.contract().at('TT6uSbPHb95Rm9PdQByPfKgtGr4fZrsqZa');
                //             mdwithdrawtrc20before = await mwithdraw20beforeinfo.balanceOf('TG5wFVvrJiTkBA1WaZN3pzyJDfkgHMnFrp').call();
                //             console.log("mwithdrawtrc20before："+mdwithdrawtrc20before);
                //
                //             indobyidwithdrawbefore1=await sunWeb.sidechain.transactionBuilder.triggerSmartContract('TLiUjcoZZ4B5NcmsprpPijU3NHayhCXjxs', 'balanceOf(address)'
                //                 , {feeLimit: 10000000}, [{type: 'address', value: 'TG5wFVvrJiTkBA1WaZN3pzyJDfkgHMnFrp'}]);
                //
                //             indobyidwithdrawbefore2=await sunWeb.sidechain.trx.sign(indobyidwithdrawbefore1.transaction)
                //
                //             indobyidwithdrawbefore3=await sunWeb.sidechain.trx.sendRawTransaction(indobyidwithdrawbefore2)
                //             swithdrawTXIDbefore=indobyidwithdrawbefore3.transaction.txID;
                //
                //             console.log({swithdrawTXIDbefore: swithdrawTXIDbefore})
                //
                //
                //             await TIMEOUT(30000)
                //             indobyidwithdrawbefore4=  await sunWeb.sidechain.trx.getTransactionInfo(swithdrawTXIDbefore);
                //             console.log({indobyidwithdrawbefore4:indobyidwithdrawbefore4})
                //             infowithdrawbyidinfobefore= indobyidwithdrawbefore4.contractResult;
                //             console.log({infowithdrawbyidinfobefore:infowithdrawbyidinfobefore})
                //
                //             swithdrawtrc20before = parseInt(infowithdrawbyidinfobefore, 16)
                //             console.log({swithdrawtrc20before:swithdrawtrc20before})
                //
                //             const num = 10;
                //             const txID = await sunWeb.withdrawTrc20(num, 0,FEE_LIMIT, ADDRESS20_MAPPING);
                //             assert.equal(txID.length, 64);
                //             await TIMEOUT(30000);
                //             mwithdrawtrc20afterinfo = await sunWeb.mainchain.contract().at('TT6uSbPHb95Rm9PdQByPfKgtGr4fZrsqZa');
                //             mdwithdrawtrc20after = await mwithdrawtrc20afterinfo.balanceOf('TG5wFVvrJiTkBA1WaZN3pzyJDfkgHMnFrp').call();
                //             console.log("mwithdrawtrc20after："+mdwithdrawtrc20after);
                //
                //             indobyidwithdrawafter1=await sunWeb.sidechain.transactionBuilder.triggerSmartContract('TLiUjcoZZ4B5NcmsprpPijU3NHayhCXjxs', 'balanceOf(address)'
                //                 , {feeLimit: 10000000}, [{type: 'address', value: 'TG5wFVvrJiTkBA1WaZN3pzyJDfkgHMnFrp'}]);
                //
                //             indobyidwithdrawafter2=await sunWeb.sidechain.trx.sign(indobyidwithdrawafter1.transaction)
                //
                //             indobyidwithdrawafter3=await sunWeb.sidechain.trx.sendRawTransaction(indobyidwithdrawafter2)
                //             swithdrawTXIDafter=indobyidwithdrawafter3.transaction.txID;
                //
                //             console.log({swithdrawTXIDafter: swithdrawTXIDafter})
                //
                //
                //             await TIMEOUT(30000)
                //             indobyidwithdrawafter4=  await sunWeb.sidechain.trx.getTransactionInfo(swithdrawTXIDafter);
                //             console.log({indobyidwithdrawafter4:indobyidwithdrawafter4})
                //             infowithdrawbyidinfoafter= indobyidwithdrawafter4.contractResult;
                //             console.log({infowithdrawbyidinfoafter:infowithdrawbyidinfoafter})
                //
                //             swithdrawtrc20after = parseInt(infowithdrawbyidinfoafter, 16)
                //             console.log({swithdrawtrc20after:swithdrawtrc20after})
                //             assert.equal(swithdrawtrc20before-num,swithdrawtrc20after);
                //
                //             assert.equal(mdwithdrawtrc20after+num,mdwithdrawtrc20after);
                //
                //         });
                //
                //
                //         describe('#depositTrc721', function () {
                //             const sunWeb = sunBuilder.createInstance();
                //             it('deposit trc721 from main chain to side chain', async function () {
                //
                //
                //                 sunWeb.approveTrc721(1001, 1000000000, 'TUs9B3KppxR2s8ZzNinG2sG5tkM3RVLue7')
                //                 await TIMEOUT(20000)
                //                 const tokenid = 1001;
                //                 const txID = await sunWeb.depositTrc721(tokenid, FEE_LIMIT, 'TUs9B3KppxR2s8ZzNinG2sG5tkM3RVLue7');
                //                 assert.equal(txID.length, 64);
                //                 await TIMEOUT(60000)
                //                 mdeposittrc721address1 = await sunWeb.mainchain.contract().at('TUs9B3KppxR2s8ZzNinG2sG5tkM3RVLue7');
                //                 console.log(mdeposittrc721address1)
                //
                //                 mdeposittrc721address2 = await mdeposittrc721address1.ownerOf('1001').call()
                //                 console.log("mdeposittrc721address2："+mdeposittrc721address2);
                //
                //
                //                 sdeposittrc721address1 = await sunWeb.sidechain.transactionBuilder.triggerSmartContract('TPcQ8ZRvaPAPx8YCh3drZmvvZHh7WXUyLa', 'ownerOf(uint256)'
                //                     , {feeLimit: 10000000}, [{type: 'uint256', value: '1001'}]);
                //                 console.log({sdeposittrc721address1: sdeposittrc721address1})
                //
                //                 sdeposittrc721address2 = await sunWeb.sidechain.trx.sign(sdeposittrc721address1.transaction)
                //                 console.log({sdeposittrc721address2: sdeposittrc721address2})
                //
                //                 sdeposittrc721address3 = await sunWeb.sidechain.trx.sendRawTransaction(sdeposittrc721address2)
                //                 console.log({sdeposittrc721address3: sdeposittrc721address3})
                //
                //                 sdeposittrc721txid = sdeposittrc721address3.transaction.txID;
                //                 console.log({sdeposittrc721txid: sdeposittrc721txid})
                //                 await TIMEOUT(100000)
                //
                //                 sdeposittrc721info = await sunWeb.sidechain.trx.getTransactionInfo(sdeposittrc721txid);
                //
                //                 console.log({sdeposittrc721info: sdeposittrc721info})
                //
                //                 sdeposittrc721inforesult= sdeposittrc721info.contractResult;
                //                 console.log({sdeposittrc721inforesult: sdeposittrc721inforesult})


                                // const g= '000000000000000000000000431684a4f6bd07dacffac4bcc89c0af1c0016f19'
                                // g1=g.substr(24,40)
                                //
                                // console.log({g1: g1})

                        //         assert.equal(mdeposittrc721address2,MAIN_GATEWAY_ADDRESS_HEX)
                        //         // alert(sdeposittrc721inforesult.substring(4))
                        //
                        //
                        //         assert.equal(ADDRESS_HEX1,sdeposittrc721inforesult)
                        //
                        //
                        //
                        //     });
                        // });
                        // describe('#withdrawTrc721', async function () {
                        //     const sunWeb = sunBuilder.createInstance();
                        //     it('withdraw trc721 from side chain to main chain', async function () {
                        //
                        //         const tokenid = 1001;
                        //         const txID = await sunWeb.withdrawTrc721(tokenid,0, FEE_LIMIT, 'TPcQ8ZRvaPAPx8YCh3drZmvvZHh7WXUyLa');
                        //         assert.equal(txID.length, 64);
                        //         await TIMEOUT(60000)
                        //
                        //         mwithdrawtrc721address1 = await sunWeb.mainchain.contract().at('TUs9B3KppxR2s8ZzNinG2sG5tkM3RVLue7');
                        //
                        //         mwithdrawtrc721address2 = await mwithdrawtrc721address1.ownerOf('1001').call()
                        //         console.log("mdeposittrc721address2："+mwithdrawtrc721address2);
                        //
                        //
                        //         swithdrawtrc721address1 = await sunWeb.sidechain.transactionBuilder.triggerSmartContract('TPcQ8ZRvaPAPx8YCh3drZmvvZHh7WXUyLa', 'ownerOf(uint256)'
                        //             , {feeLimit: 10000000}, [{type: 'uint256', value: '1001'}]);
                        //
                        //         swithdrawtrc721address2 = await sunWeb.sidechain.trx.sign(swithdrawtrc721address1.transaction)
                        //
                        //         swithdrawtrc721address3 = await sunWeb.sidechain.trx.sendRawTransaction(swithdrawtrc721address2)
                        //         swithdrawtrc721txid = swithdrawtrc721address3.transaction.txID;
                        //         console.log({swithdrawtrc721txid: swithdrawtrc721txid})
                        //         await TIMEOUT(20000)
                        //
                        //         swithdrawtrc721info = await sunWeb.sidechain.trx.getTransactionInfo(swithdrawtrc721txid);
                        //
                        //         console.log({swithdrawtrc721info: swithdrawtrc721info})
                        //
                        //         swithdrawtrc721inforesult= swithdrawtrc721info.result;
                        //         console.log({swithdrawtrc721inforesult: swithdrawtrc721inforesult})
                        //
                        //
                        //         assert.equal(mwithdrawtrc721address2,'41431684a4f6bd07dacffac4bcc89c0af1c0016f19')
                        //         assert.equal('FAILED',swithdrawtrc721inforesult)
                        //
                        //     });

                            // describe('#mappingTrc', function () {
                            //     const sunWeb = sunBuilder.createInstance();
                            //     it('mappingTrc20', async function () {
                            //         const txID = await sunWeb.mappingTrc20(HASH20, FEE_LIMIT);
                            //         assert.equal(txID.length, 64);
                            //     });
                            //
                            //     it('mappingTrc20 with the defined private key', async function () {
                            //         const options = {};
                            //         const txID = await sunWeb.mappingTrc20(HASH20, FEE_LIMIT, options, PRIVATE_KEY);
                            //         assert.equal(txID.length, 64);
                            //     });
                            //
                            //     it('mappingTrc20 with permissionId in options object', async function () {
                            //         const options = {permissionId: 0};
                            //         const txID = await sunWeb.mappingTrc20(HASH20, FEE_LIMIT, options);
                            //         assert.equal(txID.length, 64);
                            //     });
                            //
                            //     it('mappingTrc20 with permissionId in options object and the defined private key', async function () {
                            //         const options = {permissionId: 0};
                            //         const txID = await sunWeb.mappingTrc20(HASH20, FEE_LIMIT, options, PRIVATE_KEY);
                            //         assert.equal(txID.length, 64);
                            //     });
                            //
                            //     it('should throw if an invalid trxHash', async function () {
                            //         const trxHash = '';
                            //         await assertThrow(
                            //             sunWeb.mappingTrc20(trxHash, FEE_LIMIT),
                            //             'Invalid trxHash provided'
                            //         );
                            //     });
                            //
                            //     it('should throw if an invalid fee limit is passed', async function () {
                            //         const feeLimit = 100000000000;
                            //         await assertThrow(
                            //             sunWeb.mappingTrc20(HASH20, feeLimit),
                            //             'Invalid feeLimit provided'
                            //         );
                            //     });
                            //
                            //     it('mappingTrc721', async function () {
                            //         const txID = await sunWeb.mappingTrc721(HASH721, FEE_LIMIT);
                            //         assert.equal(txID.length, 64);
                            //     })
                            // });
                            //
                            // describe('#withdraw', function () {
                            //     describe('#withdrawTrx()', function () {
                            //         const sunWeb = sunBuilder.createInstance();
                            //         it('withdraw trx from side chain to main chain', async function () {
                            //             const txID = await sunWeb.withdrawTrx(10000000, 10000000);
                            //             assert.equal(txID.length, 64);
                            //         });
                            //
                            //         it('withdrawTrx with the defined private key', async function () {
                            //             const callValue = 10000000;
                            //             const options = {};
                            //             const txID = await sunWeb.withdrawTrx(callValue, FEE_LIMIT, options, PRIVATE_KEY);
                            //             assert.equal(txID.length, 64);
                            //         });
                            //
                            //         it('withdrawTrx with permissionId in options object', async function () {
                            //             const callValue = 10000000;
                            //             const options = {permissionId: 0};
                            //             const txID = await sunWeb.withdrawTrx(callValue, FEE_LIMIT, options);
                            //             assert.equal(txID.length, 64);
                            //         });
                            //
                            //         it('withdrawTrx with permissionId in options object and the defined private key', async function () {
                            //             const callValue = 10000000;
                            //             const options = {permissionId: 0};
                            //             const txID = await sunWeb.withdrawTrx(callValue, FEE_LIMIT, options, PRIVATE_KEY);
                            //             assert.equal(txID.length, 64);
                            //         });
                            //
                            //         it('should throw if an invalid trx number is passed', async function () {
                            //             await assertThrow(
                            //                 sunWeb.withdrawTrx(1000.01, FEE_LIMIT),
                            //                 'Invalid callValue provided'
                            //             );
                            //         });
                            //
                            //         it('should throw if an invalid fee limit is passed', async function () {
                            //             await assertThrow(
                            //                 sunWeb.withdrawTrx(10000, 0),
                            //                 'Invalid feeLimit provided'
                            //             );
                            //         });
                            //     });
                            //
                            //     describe('#withdrawTrc10()', function () {
                            //         const sunWeb = sunBuilder.createInstance();
                            //         it('withdraw trc10 from side chain to main chain', async function () {
                            //             const tokenValue = 10;
                            //             const txID = await sunWeb.withdrawTrc10(TOKEN_ID, tokenValue, FEE_LIMIT);
                            //             assert.equal(txID.length, 64);
                            //         });
                            //
                            //         it('withdrawTrc10 with the defined private key', async function () {
                            //             const tokenValue = 10;
                            //             const options = {};
                            //             const txID = await sunWeb.withdrawTrc10(TOKEN_ID, tokenValue, FEE_LIMIT, options, PRIVATE_KEY);
                            //             assert.equal(txID.length, 64);
                            //         });
                            //
                            //         it('withdrawTrc10 with permissionId in options object', async function () {
                            //             const tokenValue = 10;
                            //             const options = {permissionId: 0};
                            //             const txID = await sunWeb.withdrawTrc10(TOKEN_ID, tokenValue, FEE_LIMIT, options);
                            //             assert.equal(txID.length, 64);
                            //         });
                            //
                            //         it('withdrawTrc10 with permissionId in options object and the defined private key', async function () {
                            //             const tokenValue = 10;
                            //             const options = {permissionId: 0};
                            //             const txID = await sunWeb.withdrawTrc10(TOKEN_ID, tokenValue, FEE_LIMIT, options, PRIVATE_KEY);
                            //             assert.equal(txID.length, 64);
                            //         });
                            //
                            //         it('should throw if an invalid token id is passed', async function () {
                            //             const tokenId = -10;
                            //             await assertThrow(
                            //                 sunWeb.withdrawTrc10(tokenId, 100, 1000000),
                            //                 'Invalid tokenId provided'
                            //             )
                            //         });
                            //
                            //         it('should throw if an invalid token value is passed', async function () {
                            //             const tokenValue = 10.01;
                            //             await assertThrow(
                            //                 sunWeb.withdrawTrc10(TOKEN_ID, tokenValue, FEE_LIMIT),
                            //                 'Invalid tokenValue provided'
                            //             );
                            //         });
                            //
                            //         it('should throw if an invalid fee limit is passed', async function () {
                            //             const feeLimit = 100000000000;
                            //             await assertThrow(
                            //                 sunWeb.withdrawTrc10(TOKEN_ID, 100, feeLimit),
                            //                 'Invalid feeLimit provided'
                            //             );
                            //         });
                            //     });
                            //
                            //     describe('#withdrawTrc', function () {
                            //         describe('#withdrawTrc20', function () {
                            //             const sunWeb = sunBuilder.createInstance();
                            //             it('withdraw trc20 from side chain to main chain', async function () {
                            //                 const num = 10;
                            //                 const txID = await sunWeb.withdrawTrc20(num, FEE_LIMIT, ADDRESS20_MAPPING);
                            //                 assert.equal(txID.length, 64);
                            //             });
                            //
                            //             it('withdrawTrc20 with the defined private key', async function () {
                            //                 const num = 10;
                            //                 const options = {};
                            //                 const txID = await sunWeb.withdrawTrc20(num, FEE_LIMIT, ADDRESS20_MAPPING, options, PRIVATE_KEY);
                            //                 assert.equal(txID.length, 64);
                            //             });
                            //
                            //             it('withdrawTrc20 with permissionId in options object', async function () {
                            //                 const num = 10;
                            //                 const options = {permissionId: 0};
                            //                 const txID = await sunWeb.withdrawTrc20(num, FEE_LIMIT, ADDRESS20_MAPPING, options);
                            //                 assert.equal(txID.length, 64);
                            //             });
                            //
                            //             it('withdrawTrc20 with permissionId in options object and the defined private key', async function () {
                            //                 const num = 10;
                            //                 const options = {permissionId: 0};
                            //                 const txID = await sunWeb.withdrawTrc20(num, FEE_LIMIT, ADDRESS20_MAPPING, options, PRIVATE_KEY);
                            //                 assert.equal(txID.length, 64);
                            //             });
                            //
                            //             it('should throw if an invalid num is passed', async function () {
                            //                 const num = 10.01;
                            //                 await assertThrow(
                            //                     sunWeb.withdrawTrc20(num, FEE_LIMIT, ADDRESS20_MAPPING),
                            //                     'Invalid numOrId provided'
                            //                 );
                            //             });
                            //
                            //             it('should throw if an invalid fee limit is passed', async function () {
                            //                 const feeLimit = 100000000000;
                            //                 await assertThrow(
                            //                     sunWeb.withdrawTrc20(100, feeLimit, ADDRESS20_MAPPING),
                            //                     'Invalid feeLimit provided'
                            //                 );
                            //             });
                            //
                            //             it('should throw if an invalid contract address is passed', async function () {
                            //                 await assertThrow(
                            //                     sunWeb.withdrawTrc20(100, FEE_LIMIT, 'aaaaaaaaaa'),
                            //                     'Invalid contractAddress address provided'
                            //                 );
                            //             });
                            //         });
                            //
                            //         describe('#withdrawTrc721', async function () {
                            //             const sunWeb = sunBuilder.createInstance();
                            //             it('withdraw trc721 from side chain to main chain', async function () {
                            //                 const id = 100;
                            //                 const txID = await sunWeb.withdrawTrc721(id, FEE_LIMIT, ADDRESS20_MAPPING);
                            //                 assert.equal(txID.length, 64);
        //                 });
    //                 });
    //             });
    //         });
        });
    });
});



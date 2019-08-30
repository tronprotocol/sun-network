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

    describe('#depositTrc20', function () {
        const sunWeb = sunBuilder.createInstance();
        it('deposit trc20 from main chain to side chain', async function () {
            mdeposittrc20beforeinfo = await sunWeb.mainchain.contract().at(CONTRACT_ADDRESS20);
            mdeposittrc20before = await mdeposittrc20beforeinfo.balanceOf(ACCOUNTADDRESS).call();

            console.log("mdeposittrc20before："+mdeposittrc20before);




            indobyid1before=await sunWeb.sidechain.transactionBuilder.triggerSmartContract(ADDRESS20_MAPPING, 'balanceOf(address)'
                , {feeLimit: 10000000}, [{type: 'address', value: ACCOUNTADDRESS}]);

            infobyid2before=await sunWeb.sidechain.trx.sign(indobyid1before.transaction)

            infobyid3before=await sunWeb.sidechain.trx.sendRawTransaction(infobyid2before)
            sdepositTXIDbefore=infobyid3before.transaction.txID;



            await TIMEOUT(30000)
            infobyid4before=  await sunWeb.sidechain.trx.getTransactionInfo(sdepositTXIDbefore);
            infobyidinfobefore= infobyid4before.contractResult;
            console.log({infobyidinfobefore:infobyidinfobefore})

            sdeposittrc20before = parseInt(infobyidinfobefore, 16)
            console.log({sdeposittrc20before:sdeposittrc20before})

            sunWeb.approveTrc20(200000, 10000000, CONTRACT_ADDRESS20)
            const num = 100;
            const txID = await sunWeb.depositTrc20(num, 0,FEE_LIMIT, CONTRACT_ADDRESS20);
            await  TIMEOUT(80000);
            assert.equal(txID.length, 64);
            mdeposittrc20afterinfo = await sunWeb.mainchain.contract().at(CONTRACT_ADDRESS20);
            mdeposittrc20after = await mdeposittrc20afterinfo.balanceOf(ACCOUNTADDRESS).call();
            console.log("mdeposittrc20after："+mdeposittrc20after);

            indobyid1after=await sunWeb.sidechain.transactionBuilder.triggerSmartContract(ADDRESS20_MAPPING, 'balanceOf(address)'
                , {feeLimit: 10000000}, [{type: 'address', value: ACCOUNTADDRESS}]);

            indobyid2after=await sunWeb.sidechain.trx.sign(indobyid1after.transaction)

            indobyid3after=await sunWeb.sidechain.trx.sendRawTransaction(indobyid2after)
            sdepositTXIDafter=indobyid3after.transaction.txID;

            await TIMEOUT(30000)
            infobyid4after=  await sunWeb.sidechain.trx.getTransactionInfo(sdepositTXIDafter);
            console.log({infobyid4after:infobyid4after})
            infobyidinfoafter= infobyid4after.contractResult;

            sdeposittrc20after = parseInt(infobyidinfoafter, 16)
            console.log({sdeposittrc20after:sdeposittrc20after})
            assert.equal(mdeposittrc20before-num,mdeposittrc20after);
            assert.equal(mdeposittrc20before-num,mdeposittrc20after);

            assert.equal(sdeposittrc20before+num,sdeposittrc20after);
        });

        it('should throw if an invalid num is passed', async function () {
            const num = 100.01;
            await assertThrow(
                sunWeb.depositTrc20(num,10, FEE_LIMIT, CONTRACT_ADDRESS20),
                'Invalid num provided'
            );
        });

        it('should throw if an invalid fee limit is passed', async function () {
            const num = 100;
            const feeLimit = 100000000000;
            await assertThrow(
                sunWeb.depositTrc20(num, 0,feeLimit, CONTRACT_ADDRESS20),
                'Invalid feeLimit provided'
            );
        });

        it('should throw if an invalid contract address is passed', async function () {
            await assertThrow(
                sunWeb.depositTrc20(100, 0,FEE_LIMIT, 'CONTRACT_ADDRESS20'),
                'Invalid contractAddress address provided'
            );
        });
    });
    describe('#withdrawTrc20', function () {
        describe('#withdrawTrc20', function () {
            const sunWeb = sunBuilder.createInstance();
            it('withdraw trc20 from side chain to main chain', async function () {
                mwithdraw20beforeinfo = await sunWeb.mainchain.contract().at(CONTRACT_ADDRESS20);
                mwithdrawtrc20before = await mwithdraw20beforeinfo.balanceOf(ACCOUNTADDRESS).call();
                console.log("mwithdrawtrc20before："+mwithdrawtrc20before);


                indobyidwithdrawbefore1=await sunWeb.sidechain.transactionBuilder.triggerSmartContract(ADDRESS20_MAPPING, 'balanceOf(address)'
                    , {feeLimit: 10000000}, [{type: 'address', value: ACCOUNTADDRESS}]);

                indobyidwithdrawbefore2=await sunWeb.sidechain.trx.sign(indobyidwithdrawbefore1.transaction)

                indobyidwithdrawbefore3=await sunWeb.sidechain.trx.sendRawTransaction(indobyidwithdrawbefore2)
                swithdrawTXIDbefore=indobyidwithdrawbefore3.transaction.txID;



                await TIMEOUT(30000)
                indobyidwithdrawbefore4=  await sunWeb.sidechain.trx.getTransactionInfo(swithdrawTXIDbefore);
                infowithdrawbyidinfobefore= indobyidwithdrawbefore4.contractResult;

                swithdrawtrc20before = parseInt(infowithdrawbyidinfobefore, 16)
                console.log({swithdrawtrc20before:swithdrawtrc20before})

                const num = 10;
                const txID = await sunWeb.withdrawTrc20(num, 0,FEE_LIMIT, ADDRESS20_MAPPING);
                assert.equal(txID.length, 64);
                await TIMEOUT(30000);
                mwithdrawtrc20afterinfo = await sunWeb.mainchain.contract().at(CONTRACT_ADDRESS20);
                mwithdrawtrc20after = await mwithdrawtrc20afterinfo.balanceOf(ACCOUNTADDRESS).call();
                console.log("mwithdrawtrc20after："+mwithdrawtrc20after);




                indobyidwithdrawafter1=await sunWeb.sidechain.transactionBuilder.triggerSmartContract(ADDRESS20_MAPPING, 'balanceOf(address)'
                    , {feeLimit: 10000000}, [{type: 'address', value: ACCOUNTADDRESS}]);

                indobyidwithdrawafter2=await sunWeb.sidechain.trx.sign(indobyidwithdrawafter1.transaction)

                indobyidwithdrawafter3=await sunWeb.sidechain.trx.sendRawTransaction(indobyidwithdrawafter2)
                swithdrawTXIDafter=indobyidwithdrawafter3.transaction.txID;



                await TIMEOUT(30000)
                indobyidwithdrawafter4=  await sunWeb.sidechain.trx.getTransactionInfo(swithdrawTXIDafter);
                infowithdrawbyidinfoafter= indobyidwithdrawafter4.contractResult;

                swithdrawtrc20after = parseInt(infowithdrawbyidinfoafter, 16)
                console.log({swithdrawtrc20after:swithdrawtrc20after})
                assert.equal(swithdrawtrc20before-num,swithdrawtrc20after);
                const c= parseInt(mwithdrawtrc20before)+parseInt(10);
                console.log("c："+c);
                assert.equal(c,mwithdrawtrc20after);


            });

        });
    });
});





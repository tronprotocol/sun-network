const {TIMEOUT, PRIVATE_KEY, ACCOUNTADDRESS,ADDRESS_HEX1,MAIN_GATEWAY_ADDRESS_HEX,ADDRESS_HEX2,CONTRACT_ADDRESS20,ADDRESS20_MAPPING,ADDRESS721_MAPPING,CONTRACT_ADDRESS721, DEPOSIT_FEE, WITHDRAW_FEE, FEE_LIMIT, MAIN_FULL_NODE_API, MAIN_SOLIDITY_NODE_API, MAIN_EVENT_API, SIDE_FULL_NODE_API, SIDE_SOLIDITY_NODE_API, SIDE_EVENT_API, MAIN_GATEWAY_ADDRESS, SIDE_GATEWAY_ADDRESS, ADDRESS_HEX, ADDRESS_BASE58, TOKEN_ID,  HASH20, HASH721, SIDE_CHAIN_ID} = require('./helpers/config');

const sunBuilder = require('./helpers/sunWebBuilder');
const SunWeb = sunBuilder.SunWeb;

const chai = require('chai');
const assert = chai.assert;
const assertThrow = require('./helpers/assertThrow');
const TronWeb = require('tronweb');

describe('SunWeb Instance', function() {
    describe('#constructor', function() {
        it('should create an instance using an options object with private key', function() {
            const sunWeb = sunBuilder.createInstance();
            assert.instanceOf(sunWeb, SunWeb);
            assert.equal(sunWeb.mainchain.defaultPrivateKey, PRIVATE_KEY);
            assert.equal(sunWeb.sidechain.defaultPrivateKey, PRIVATE_KEY);
        });

        it('should create an instance using an options object without private key', function() {
            const mainchain = new TronWeb({
                fullNode: MAIN_FULL_NODE_API,
                solidityNode: MAIN_SOLIDITY_NODE_API,
                eventServer: MAIN_EVENT_API,
            });
            const sidechain = new TronWeb({
                fullNode: SIDE_FULL_NODE_API,
                solidityNode: SIDE_SOLIDITY_NODE_API,
                eventServer: SIDE_EVENT_API,
            });
            return new SunWeb(
                mainchain,
                sidechain,
                MAIN_GATEWAY_ADDRESS,
                SIDE_GATEWAY_ADDRESS,
                SIDE_CHAIN_ID
            );
            assert.equal(sunWeb.mainchain.defaultPrivateKey, false);
            assert.equal(sunWeb.sidechain.defaultPrivateKey, false);
        });

        it('should create an instance using an options object which only constains a fullhost without private key', function() {
            const mainchain = new TronWeb({
                fullHost: MAIN_FULL_NODE_API
            });
            const sidechain = new TronWeb({
                fullHost: SIDE_FULL_NODE_API
            });
            const sunWeb = new SunWeb(
                mainchain,
                sidechain,
                MAIN_GATEWAY_ADDRESS,
                SIDE_GATEWAY_ADDRESS,
                SIDE_CHAIN_ID
            );
            assert.instanceOf(sunWeb, SunWeb);
            assert.equal(sunWeb.mainchain.defaultPrivateKey, false);
            assert.equal(sunWeb.sidechain.defaultPrivateKey, false);
        });

        it('should create an instance using an options object which only constains a fullhost with private key', function() {
            const mainchain = new TronWeb({
                fullHost: MAIN_FULL_NODE_API,
                privateKey: PRIVATE_KEY
            });
            const sidechain = new TronWeb({
                fullHost: SIDE_FULL_NODE_API,
                privateKey: PRIVATE_KEY
            });
            const sunWeb = new SunWeb(
                mainchain,
                sidechain,
                MAIN_GATEWAY_ADDRESS,
                SIDE_GATEWAY_ADDRESS,
                SIDE_CHAIN_ID
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
                const mdepositBalancebefore = await sunWeb.mainchain.trx.getBalance();
                const sdepositBalancebefore = await sunWeb.sidechain.trx.getBalance();

                const saccountbefore =await  sunWeb.sidechain.trx.getAccount(ACCOUNTADDRESS);
                const maccountbefore=await  sunWeb.mainchain.trx.getAccount(ACCOUNTADDRESS);
                console.log("saccountbefore: "+saccountbefore);
                console.log("maccountbefore: "+maccountbefore);
                const maccountbeforeinfo = maccountbefore.assetV2.filter(function(item) {
                    return item.key == TOKEN_ID;
                });
                console.log(maccountbeforeinfo);
                const mtokenvaluebefore= maccountbeforeinfo[0].value;
                console.log("mtokenvaluebefore: "+mtokenvaluebefore)
                const saccountbeforeinfo = saccountbefore.assetV2.filter(function(item) {
                   return item.key == TOKEN_ID;
                });
                const stokenvaluebefore = saccountbeforeinfo[0].value;
                // const  stokenvaluebefore = 0;
                console.log("stokenvaluebefore: "+stokenvaluebefore)
                const tokenValue = 1000;
                const txID = await sunWeb.depositTrc10(TOKEN_ID, tokenValue, DEPOSIT_FEE,FEE_LIMIT);
                await TIMEOUT(80000);
                console.log("txID： "+txID)
                const result2 =await sunWeb.mainchain.trx.getTransactionInfo(txID.toString());
                console.log("txID.toString():"+txID.toString())
                console.log("result2:"+result2)
                JSON.stringify(result2)
                console.log("result2:"+result2)
                assert.equal(txID.length, 64);

                const saccountafter =await  sunWeb.sidechain.trx.getAccount(ACCOUNTADDRESS);
                const maccountafter =await  sunWeb.mainchain.trx.getAccount(ACCOUNTADDRESS);
                const maccountafterinfo = maccountafter.assetV2.filter(function(item) {
                    return item.key == TOKEN_ID;
                });
                console.log(maccountafterinfo);
                const mtokenvalueafter = maccountafterinfo[0].value;
                console.log("mtokenvalueafter： "+mtokenvalueafter)
                const  saccountafterinfo = saccountafter.assetV2.filter(function(item) {
                    return item.key == TOKEN_ID;
                });
                console.log(saccountafterinfo);
                const stokenvalueafter= saccountafterinfo[0].value;
                console.log("stokenvalueafter："+stokenvalueafter);

                const mdepositBalanceafter = await sunWeb.mainchain.trx.getBalance();
                const sdepositBalanceafter = await sunWeb.sidechain.trx.getBalance();
                console.log('mBefore: ' + mdepositBalancebefore);
                console.log('sBefore: ' + sdepositBalancebefore);
                console.log('mdepositBalanceafter: ' +  mdepositBalanceafter)
                console.log('sdepositBalanceafter: ' +  sdepositBalanceafter)

                var id = txID;
                const result1 =await sunWeb.mainchain.trx.getTransactionInfo(id);
                console.log("result1:"+result1)

                // assert.equal(mdepositBalanceafter, mdepositBalancebefore - DEPOSIT_FEE);
                assert.equal(sdepositBalanceafter, sdepositBalancebefore);
                assert.equal(stokenvaluebefore+tokenValue, stokenvalueafter);
                assert.equal(mtokenvaluebefore-tokenValue, mtokenvalueafter);
            });

            describe('#withdrawTrc10()', function () {
                const sunWeb = sunBuilder.createInstance();
                it('withdraw trc10 from side chain to main chain', async function () {
                    const mwithdrawBalancebefore = await sunWeb.mainchain.trx.getBalance();
                    const swithdrawBalancebefore = await sunWeb.sidechain.trx.getBalance();

                    const swithdrawbefore =await  sunWeb.sidechain.trx.getAccount(ACCOUNTADDRESS);
                    const mwithdrawbefore=await  sunWeb.mainchain.trx.getAccount(ACCOUNTADDRESS);
                    console.log("swithdrawbefore: "+swithdrawbefore);
                    console.log("mwithdrawbefore: "+mwithdrawbefore);
                    const  mwithdrawbeforeinfo = mwithdrawbefore.assetV2.filter(function(item) {
                        return item.key == TOKEN_ID;
                    });
                    console.log(mwithdrawbeforeinfo);
                    const mwithdrawtokenvaluebefore= mwithdrawbeforeinfo[0].value;
                    console.log("mwithdrawtokenvaluebefore: "+mwithdrawtokenvaluebefore)

                    const  swithdrawbeforeinfo = swithdrawbefore.assetV2.filter(function(item) {
                        return item.key == TOKEN_ID;
                    });
                    console.log(swithdrawbeforeinfo);
                    const swithdrawtokenvaluebefore= swithdrawbeforeinfo[0].value;
                    console.log("swithdrawtokenvaluebefore: "+swithdrawtokenvaluebefore);
                    const tokenValue1 = 1000;
                    const txID = await sunWeb.withdrawTrc10(TOKEN_ID, tokenValue1, WITHDRAW_FEE,FEE_LIMIT);
                    await TIMEOUT(60000);
                    console.log("txID: "+txID)
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
                    console.log("mwithdrawtokenvalueafter: "+mwithdrawtokenvalueafter)

                    const  swithdrawafterinfo = swithdrawafter.assetV2.filter(function(item) {
                        return item.key == TOKEN_ID;
                    });
                    console.log(swithdrawafterinfo);
                    const swithdrawtokenvalueafter= swithdrawafterinfo[0].value;
                    console.log("swithdrawtokenvalueafter: "+swithdrawtokenvalueafter);

                    const mwithdrawBalanceafter = await sunWeb.mainchain.trx.getBalance();
                    const swithdrawBalanceafter = await sunWeb.sidechain.trx.getBalance();
                    console.log('mBefore: ' + mwithdrawBalancebefore);
                    console.log('sBefore: ' + swithdrawBalancebefore);
                    console.log('mwithdrawBalanceafter: ' +  mwithdrawBalanceafter)
                    console.log('swithdrawBalanceafter: ' +  swithdrawBalanceafter)

                    assert.equal(mwithdrawBalancebefore, mwithdrawBalanceafter);
                    // assert.equal(swithdrawBalanceafter, swithdrawBalancebefore - WITHDRAW_FEE);

                    assert.equal(swithdrawtokenvaluebefore-tokenValue1, swithdrawtokenvalueafter);
                    assert.equal(mwithdrawtokenvaluebefore+tokenValue1, mwithdrawtokenvalueafter);
                });
            });

        });
    });
});


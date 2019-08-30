const sunBuilder = require('./helpers/sunWebBuilder');
const sunWeb = sunBuilder.createInstance();

var a,b;
//

sunWeb.depositTrx(10000,1000000000).then(data => {
    console.log(data);
}).catch(err => console.log(err));


// sunWeb.depositTrc10(1000004,800,1000000000).then(data => {
//     console.log(data);
// }).catch(err => console.log(err));


//
// sunWeb.withdrawTrc10(1000004, 20, 0,1000000000).then(data => {
//     console.log(data);
//
// }).catch(err => console.log(err));

// sunWeb.mappingTrc20("e5fba6f06d99b18fc84d5f3fae13e1a73c7a83d6f28aece1aea0c1d112c8f50e",100,100000000,0).then(data => {
//      console.log(data);
//
// }).catch(err => console.log(err));

// sunWeb.approveTrc20(2000, 1000000000, 'TUhcrusXLbfHdNSBDsUakh6jAsp6rnYin3').then(data => {
//      console.log(data);
//
// }).catch(err => console.log(err));


// async function f1() {
//
//     a = await sunWeb.mainchain.contract().at('TT6uSbPHb95Rm9PdQByPfKgtGr4fZrsqZa');
//     console.log(a)
//     b = await a.getApprioved().call()
//     console.log(b)
//
//
// }
// f1()

// sunWeb.approveTrc721(200000, 10000000, 'TPZE6AvdTBd3EUf2TZjHd9QQHMYDq3F6UX').then(data => {
// }).catch(err => console.log(err));
// sunWeb.depositTrc20(100, 1000000000, 'TT6uSbPHb95Rm9PdQByPfKgtGr4fZrsqZa').then(data => {
// }).catch(err => console.log(err));

    // async function f1() {
    //
    //     a = await sunWeb.mainchain.contract().at('TT6uSbPHb95Rm9PdQByPfKgtGr4fZrsqZa');
    //     console.log(a)
    //     b = await a.balanceOf('TG5wFVvrJiTkBA1WaZN3pzyJDfkgHMnFrp').call()
    //     console.log("b："+b)
    //
    //
    // }
    // f1();

// async function f1() {
//
//     a = await sunWeb.mainchain.contract().at('TPZE6AvdTBd3EUf2TZjHd9QQHMYDq3F6UX');
//     console.log(a)
//
//     b = await a.ownerOf('1001').call()
//     console.log("b："+b)
//
//    const s = Base58.encode58Check(ByteArray.fromHexString(b));
//     console.log("s："+s)
//
//
// }
// f1();


  // async  function f2() {
      // a = await sunWeb.sidechain.transactionBuilder.triggerSmartContract('TCaGsi2WjsGBM5FPHcjde9u1qE8gna69YB', 'ownerOf(unit256)'
      //     , {feeLimit: 10000000}, [{type: 'unit256', value: '1001'}]);
      // console.log({a: a})
      //
      // b = await sunWeb.sidechain.trx.sign(a.transaction)
      // console.log({b: b})
      // s = b.txID;
      // console.log({s: s})
      //
      // c = await sunWeb.sidechain.trx.sendRawTransaction(b)
      // console.log({c: c})
      // s1 = c.transaction.txID;
      //
      // console.log({s1: s1})
      //
      // s = c.transaction.txid;
      // console.log(s1)
      // e = await sunWeb.sidechain.trx.getTransactionInfo('855deeb2e7be2cf2c7f8a6ee5ae82e74c6b12c0335e72d41344033fd3cdecda2');
      //
      // console.log({e: e})
      // f = e.contractResult;
      // console.log({f: f})


      // assert.equal(mdeposittrc721address2,'413df84fc63013d609da4b5220777fb91e85862374')
    // const g= '000000000000000000000000431684a4f6bd07dacffac4bcc89c0af1c0016f19'
    //   g1=g.substr(24,40)
    //
    //   console.log({g1: g1})
      //
      // f1 = parseInt(f, 16)
      // console.log({f1: f1})
  // }
  //     f2();
//   async  function f2(){
//       a=await sunWeb.sidechain.transactionBuilder.triggerSmartContract('TT6uSbPHb95Rm9PdQByPfKgtGr4fZrsqZa', 'balanceOf(address)'
//           , {feeLimit: 10000000}, [{type: 'address', value: 'TG5wFVvrJiTkBA1WaZN3pzyJDfkgHMnFrp'}]);
//       console.log({a: a})
//
//       b=await sunWeb.sidechain.trx.sign(a.transaction)
//       console.log({b: b})
// s=b.txID;
//       console.log({s: s})
//
//       c=await sunWeb.sidechain.trx.sendRawTransaction(b)
//       console.log({c: c})
//  s1=c.transaction.txID;
//
//       console.log({s1: s1})
//
//       s=c.transaction.txid;
//       console.log(s1)
//       s1="b6b841be0a60c1cdefbdc55a9f2b5f25fe2752dc4e1055d67f72c46827a5ab80"
//       e=  await sunWeb.mainchain.trx.getTransactionInfo(s1);
//       console.log({e:e})
//    f= e.contractResult;
//       console.log({f:f})
//
//       f1 = parseInt(f, 16)
//       console.log({f1:f1})


      // const txID = await sunWeb.depositTrx(callValue, FEE_LIMIT);
      //         await TIMEOUT(60000);
      //         const result =await sunWeb.mainchain.trx.getTransactionInfo(txID);
      //         console.log(result)
      //         const fee = result.fee;
      //         console.log('fee:' + fee)


   // f  = parseInt(hexString, 16)


      // b.get
      // e=await
      // const  mwithdrawbeforeinfo = mwithdrawbefore.assetV2.filter(function(item) {
          //                 return item.key == TOKEN_ID;
          //             });
          //             console.log(mwithdrawbeforeinfo);
          //             const mwithdrawtokenvaluebefore= mwithdrawbeforeinfo[0].value;
          //             console.log(mwithdrawtokenvaluebefore)




  // }
  // f2();

//         const maccountafter=await  sunWeb.mainchain.trx.getAccount("TG5wFVvrJiTkBA1WaZN3pzyJDfkgHMnFrp");





//
// }).catch(err => console.log(err));


// sunWeb.withdrawTrc20(50, 0,1000000000,"TPZsGUTB9hCGcHB6MopYM17whdKjY4pT8H").then(data => {
//     console.log(data);
//
// }).catch(err => console.log(err));


// sunWeb.mappingTrc721("bfc4f73e4bd6bd068c07be16d43d0890ccbd98fad238958b7da474becd1e2d18",10,100000000).then(data => {
//      console.log(data);
//
// }).catch(err => console.log(err));

// sunWeb.approveTrc721(1001, 1000000000, 'TPZE6AvdTBd3EUf2TZjHd9QQHMYDq3F6UX').then(data => {
//      console.log(data);
//
// }).catch(err => console.log(err));
// sunWeb.depositTrc721(1001, 1000000000, 'TPZE6AvdTBd3EUf2TZjHd9QQHMYDq3F6UX').then(data => {
//      console.log(data);
//
// }).catch(err => console.log(err));



// async function f1() {
//
//     a = await sunWeb.mainchain.contract().at('TPZE6AvdTBd3EUf2TZjHd9QQHMYDq3F6UX');
//     console.log(a)
//     b = await a.balanceOf('TG5wFVvrJiTkBA1WaZN3pzyJDfkgHMnFrp').call()
//     console.log("b："+b)
//
//
// }
// f1();
// sunWeb.withdrawTrc721(1001, 0, 1000000000,'TCaGsi2WjsGBM5FPHcjde9u1qE8gna69YB').then(data => {
//     console.log(data);
//
// }).catch(err => console.log(err));
//
//
//
// sunWeb.retryDeposit(321, 1000000000).then(data => {
//     console.log(data);
// }).catch(err => console.log(err));
// //
// sunWeb.retryMapping(232, 1000000000).then(data => {
//     console.log(data);
// }).catch(err => console.log(err));
//

// sunWeb.retryWithdraw(149,1000000000).then(data => {
//     console.log(data);
// }).catch(err => console.log(err));









const sunBuilder = require('./helpers/sunWebBuilder');
const sunWeb = sunBuilder.createInstance();




sunWeb.injectFund(1000000000, 10000000).then(data => {
    console.log(data);
}).catch(err => console.log(err))
// const testMainChain = async() =>{
//     const ownerIdx = 0;
//     const accounts = {
//         pks:['0528dc17428585fc4dece68b79fa7912270a1fe8e85f244372f59eb7e8912345','0528dc17428585fc4dece68b79fa7912270a1fe8e85f244372f59eb7e8912346','0528dc17428585fc4dece68b79fa7912270a1fe8e85f244372f59eb7e8912347'],
//         b58:['417F9F8A807C931F6115170C98110DB44E1599774F'],
//         pm: ['0', '0', '0']
//     };

//     try {
//         const transaction = await sunWeb.mainchain.transactionBuilder.freezeBalance(10e5, 3, 'BANDWIDTH', accounts.b58[ownerIdx],undefined,{permissionId: 0});
//         console.log(transaction)
//         console.log(transaction.raw_data.contract)
//         let signedTransaction = transaction;
//         for (let i = 0; i < accounts.pks.length; i++) {
//             signedTransaction = await sunWeb.mainchain.trx.multiSign(signedTransaction, accounts.pks[i]);
//         }
//         console.log(signedTransaction)
//         console.log(transaction.raw_data.contract)
//         // broadcast multi-sign transaction
//         const result = await sunWeb.mainchain.trx.broadcast(signedTransaction);
//         console.log(result)
//     } catch (error) {
//         console.log('error:' +  error)
//     }}
//
// testMainChain();



// const testSideChain = async() =>{
//     const ownerIdx = 0;
//     const accounts = {
//         pks:['0528dc17428585fc4dece68b79fa7912270a1fe8e85f244372f59eb7e8912345','0528dc17428585fc4dece68b79fa7912270a1fe8e85f244372f59eb7e8912346','0528dc17428585fc4dece68b79fa7912270a1fe8e85f244372f59eb7e8912347'],
//         b58:['417F9F8A807C931F6115170C98110DB44E1599774F'],
//         pm: ['0', '0', '0']
//     };
//
//     try {
//         const transaction = await sunWeb.sidechain.transactionBuilder.freezeBalance(10e5, 3, 'BANDWIDTH', accounts.b58[ownerIdx],undefined,{permissionId: 0});
//         let signedTransaction = transaction;
//         for (let i = 0; i < accounts.pks.length; i++) {
//             signedTransaction = await sunWeb.sidechain.trx.multiSign(signedTransaction, accounts.pks[i], 0);
//         }
//         console.log(signedTransaction)
//         console.log(transaction.raw_data.contract)
//         // broadcast multi-sign transaction
//         const result = await sunWeb.sidechain.trx.broadcast(signedTransaction);
//         console.log(123)
//         console.log(result)
//     } catch (error) {
//         console.log('error:' +  error)
//     }}
//
// testSideChain();


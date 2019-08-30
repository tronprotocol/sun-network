const sunBuilder = require('./helpers/sunWebBuilder');
const sunWeb = sunBuilder.createInstance();

var a,b;
//node

// sunWeb.depositTrx(100,0,1000000000).then(data => {
//     console.log(data);
// }).catch(err => console.log(err));
//
//
// sunWeb.depositTrc10(1000006,100,0,1000000000).then(data => {
//     console.log(data);
// }).catch(err => console.log(err));

// sunWeb.withdrawTrx(100, 0, 1000000000,).then(data => {
//     console.log(data);
//
// }).catch(err => console.log(err));


//
// sunWeb.withdrawTrc10(1000006, 10, 0,1000000000).then(data => {
//     console.log(data);
//
// }).catch(err => console.log(err));

// sunWeb.mappingTrc20("e5fba6f06d99b18fc84d5f3fae13e1a73c7a83d6f28aece1aea0c1d112c8f50e",100,100000000,0).then(data => {
//      console.log(data);
//
// }).catch(err => console.log(err));


// sunWeb.approveTrc721(1001, 10000000, 'TJ26M9TR3MxCyFc6Pij5ycPbzR87gqFGqV').then(data => {
//     console.log(data);
// }).catch(err => console.log(err));

// sunWeb.depositTrc721(1001, 0, 1000000000
// ,'TJ26M9TR3MxCyFc6Pij5ycPbzR87gqFGqV').then(data => {
//      console.log(data);
//
// }).catch(err => console.log(err));
// sunWeb.approveTrc20(200000, 1000000000, 'TDq9Vjnsdx7JQzrYghePvYhc9hUb27fxSc').then(data => {
//     console.log(data);
//
// }).catch(err => console.log(err));

// sunWeb.depositTrc20(100, 0,1000000000, 'TDq9Vjnsdx7JQzrYghePvYhc9hUb27fxSc').then(data => {
// console.log(data);
//
// }).catch(err => console.log(err));



// sunWeb.withdrawTrc20(100, 0,100000000,"TWDyPdZnUUdcGbqM5QvfbQp5x9Tk1pgEEc").then(data => {
//     console.log(data);
//
// }).catch(err => console.log(err));


// sunWeb.mappingTrc721("78b513f432893495c697fe86b23e4697242d0bdef8cfb97a976bfc94f14fae27",10,100000000).then(data => {
//      console.log(data);
//
// }).catch(err => console.log(err));

// sunWeb.approveTrc721(1001, 1000000000, 'TTZDVk8gXGH8Rt297LvUTmnxVzk7kLktv1').then(data => {
//      console.log(data);
//
// }).catch(err => console.log(err));




// sunWeb.withdrawTrc721(1001, 0, 1000000000,'TMYsR1AqhuDddan9oxDPH1aWFkjjW4DWgb').then(data => {
//     console.log(data);
//
// }).catch(err => console.log(err));
//
//
//
// sunWeb.retryDeposit(113, 1,1000000000).then(data => {
//     console.log(data);
// }).catch(err => console.log(err));
// //
sunWeb.retryMapping(35, 10,1000000000).then(data => {
    console.log(data);
}).catch(err => console.log(err));
//


// sunWeb.retryWithdraw(24,10,1000000000).then(data => {
//     console.log(data);
// }).catch(err => console.log(err));









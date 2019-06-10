const express = require('express');
const TronWeb = require('tronweb');
const axios = require('axios');
const config = require('./config');
const app = express();
const port = process.argv[2] ? process.argv[2] : 8080;

const owner_address = TronWeb.address.toHex(TronWeb.address.fromPrivateKey(config.privateKey));
const giveMap = {};

app.all('*', function(req, res, next) {
  res.header('Access-Control-Allow-Origin', '*');
  res.header('Access-Control-Allow-Headers', 'Content-Type, Content-Length, Authorization, Accept, X-Requested-With');
  res.header('Access-Control-Allow-Methods', 'PUT, POST, GET, DELETE, OPTIONS');

  if (req.method == 'OPTIONS') {
    res.sendStatus(200);
  } else {
    next();
  }
});

app.use(express.json());
app.post('/token', async (req, res) => {
  let to_address = '';
  let amount = 1e10;
  let maxAmountDaily = 5e10;
  let curTime = new Date().getTime();
  let result = { ok: false };
  try {
    to_address = TronWeb.address.toHex(req.body.addr);

    if (giveMap[to_address]) {
      let _time = giveMap[to_address].time;
      let today = new Date();
      today.setHours(0, 0, 0, 0);
      if (_time < today.getTime()) {
        giveMap[to_address].amount = amount;
        giveMap[to_address].time = curTime;
      } else {
        if (giveMap[to_address].amount + amount > maxAmountDaily) {
          return res.send(result);
        } else {
          giveMap[to_address].amount += amount;
          giveMap[to_address].time = curTime;
        }
      }
    } else {
      giveMap[to_address] = { amount, time: curTime };
    }

    let transaction = await axios.post(`${config.server}/wallet/createtransaction`, {
      to_address,
      owner_address,
      amount
    });
    let sign = await axios.post(`${config.server}/wallet/gettransactionsign`, {
      privateKey: config.privateKey,
      transaction: transaction.data
    });
    let broadcast = await axios.post(`${config.server}/wallet/broadcasttransaction`, sign.data);

    if (broadcast.data.result) {
      result.ok = true;
    }
  } catch (error) {
    console.error('e', error);
  }
  res.send(result);
});

app.listen(port, () => console.log(`App listening on port ${port}!`));

const express = require('express');
const TronWeb = require('tronweb');
const axios = require('axios');
const config = require('./config');
const app = express();
const port = process.argv[2] ? process.argv[2] : 9090;

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
app.use('/sunnetwork', express.static('home/dist'));
app.use('/sunnetwork/doc', express.static('docs/.vuepress/dist'));
app.post('/sunnetwork/token', async (req, res) => {
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
          result.msg = 'maxAmountDaily';
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
    } else {
      result.msg = broadcast.data;
    }
  } catch (error) {
    console.error(`${new Date().toLocaleString()} -- e: `, error);
    result.msg = error.message;
  }
  res.send(result);
});

app.use(function(req, res, next) {
  res.status(404).sendFile('docs/.vuepress/dist/404.html', { root: __dirname });
});

app.listen(port, () => console.log(`${new Date().toLocaleString()} App listening on port ${port}!`));

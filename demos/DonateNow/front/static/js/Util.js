//计算赔率（与区块链算法保持一致）
const getOdds = point => {
  let r = 98; //返现率
  if (point == 1) {
    return r;
  }
  return r / (point - 1);
};
const getPoint = random => {
  let r = 1;
  if (random > 0 && random < 17) {
    r = 1;
  } else if (random >= 17 && random < 33) {
    r = 2;
  } else if (random >= 33 && random < 50) {
    r = 3;
  } else if (random >= 50 && random < 67) {
    r = 4;
  } else if (random >= 67 && random < 84) {
    r = 5;
  } else {
    r = 6;
  }
  return r;
};

const formatTime = ns => {
  const d = new Date(ns);
  const dformat = [
    d.getHours(),
    d.getMinutes() < 10 ? "0" + d.getMinutes() : d.getMinutes(),
    d.getSeconds() < 10 ? "0" + d.getSeconds() : d.getSeconds()
  ].join(":");
  return dformat;
};

const getBalance = value => {
  return value;
}
const hiddenAddress = value => {
  let val = value ? value.substr(0, 25) + "..." + value.substr(-6) : "";
  return val;
};


export { getOdds, formatTime, getPoint, getBalance, hiddenAddress };

# What is SunWeb

SunWeb inherits from TronWeb and services for Sun-network. We  encapsulated two objects (mainchain and sidechain) based on TronWeb. The methods and attributes in mainchain or sidechain are exactly the same as the tronweb instance. For example, users can use sunweb.mainchain.trx.getBalance() to get balance from the mainchain. Futhermore, we add some new methods which are as follows in SunWeb class so that users can use them to contact between the main chain and the side chain. 

# Installation

<strong>Node.js</strong>

```javascript
npm install sunweb
```

or

```javascript
yarn add sunweb
```

<strong>Browser</strong>

Then easiest way to use SunWeb in a browser is to install it as above and copy the dist file to your working folder. For example:

```javascript
cp node_modules/sunweb/dist/SunWeb.js ./js/SunWeb.js
```

so that you can call it in your HTML page as

```javascript
<script src="./js/SunWeb.js"><script>
```
# Test cases

```javascript
npm run test
```
But before run test cases, you must add some info in test/config.js, such fullnode, solidity node, eventsever and private key.

# Documentation

[SunWeb](http://47.252.84.158:8080/sunnetwork/guide/SUNWEB.html#sunweb-class)
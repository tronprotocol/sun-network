import TronWeb from 'tronweb';
import {sha256} from './helper/ethersUtils';

export default class SunWeb {
    static TronWeb = TronWeb;
    constructor(mainOptions = false, sideOptions = false, mainGatewayAddress = false, sideGatewayAddress = false, sideChainId = false, privateKey = false) {
        mainOptions = {...mainOptions, privateKey};
        sideOptions = {...sideOptions, privateKey};
        this.mainchain = new TronWeb(mainOptions);
        this.sidechain = new TronWeb(sideOptions);
        this.isAddress = this.mainchain.isAddress;
        this.utils = this.mainchain.utils;
        this.setMainGatewayAddress(mainGatewayAddress);
        this.setSideGatewayAddress(sideGatewayAddress);
        this.setChainId(sideChainId);
        this.injectPromise = this.utils.promiseInjector(this);
        this.validator = this.mainchain.trx.validator;
        
        const self = this;
        this.sidechain.trx.sign = (...args) => {
            return self.sign(...args);
        };
    }
    setMainGatewayAddress(mainGatewayAddress) {
        if (!this.isAddress(mainGatewayAddress))
            throw new Error('Invalid main gateway address provided');
        this.mainGatewayAddress = mainGatewayAddress;
    }

    setSideGatewayAddress(sideGatewayAddress) {
        if (!this.isAddress(sideGatewayAddress))
            throw new Error('Invalid side gateway address provided');
        this.sideGatewayAddress = sideGatewayAddress;
    }

    setChainId(sideChainId) {
        if (!this.utils.isString(sideChainId) || !sideChainId)
            throw new Error('Invalid side chainId provided');
        this.chainId = sideChainId;
    }

    signTransaction(priKeyBytes, transaction) {
        if (typeof priKeyBytes === 'string') {
            priKeyBytes = this.utils.code.hexStr2byteArray(priKeyBytes);
        }
        let chainIdByteArr = this.utils.code.hexStr2byteArray(this.chainId);

        let byteArr = this.utils.code.hexStr2byteArray(transaction.txID).concat(chainIdByteArr);
        let byteArrHash = sha256(byteArr);
        const signature = this.utils.crypto.ECKeySign(this.utils.code.hexStr2byteArray(byteArrHash.replace(/^0x/, '')), priKeyBytes);

        if (Array.isArray(transaction.signature)) {
            if (!transaction.signature.includes(signature))
                transaction.signature.push(signature);
        } else
            transaction.signature = [signature];
        return transaction;
    }

    async sign(transaction = false, privateKey = this.sidechain.defaultPrivateKey, useTronHeader = true, multisig = false, callback = false) {
        if (this.utils.isFunction(multisig)) {
            callback = multisig;
            multisig = false;
        }

        if (this.utils.isFunction(useTronHeader)) {
            callback = useTronHeader;
            useTronHeader = true;
            multisig = false;
        }

        if (this.utils.isFunction(privateKey)) {
            callback = privateKey;
            privateKey = this.sidechain.defaultPrivateKey;
            useTronHeader = true;
            multisig = false;
        }

        if (!callback)
            return this.injectPromise(this.sign, transaction, privateKey, useTronHeader, multisig);

        // Message signing
        if (this.utils.isString(transaction)) {

            if (!this.utils.isHex(transaction))
                return callback('Expected hex message input');

            try {
                const signatureHex = this.sidechain.trx.signString(transaction, privateKey, useTronHeader);
                return callback(null, signatureHex);
            } catch (ex) {
                callback(ex);
            }
        }

        if (!this.utils.isObject(transaction))
            return callback('Invalid transaction provided');

        if (!multisig && transaction.signature)
            return callback('Transaction is already signed');

        try {
            if (!multisig) {
                const address = this.sidechain.address.toHex(
                    this.sidechain.address.fromPrivateKey(privateKey)
                ).toLowerCase();
                if (address !== transaction.raw_data.contract[0].parameter.value.owner_address.toLowerCase())
                    return callback('Private key does not match address in transaction');
            }
            return callback(null,
                this.signTransaction(privateKey, transaction)
            );
        } catch (ex) {
            callback(ex);
        }
    }

     /**
     * deposit asset to sidechain
     */
    async depositTrx(
        callValue,
        feeLimit,
        options = {},
        privateKey = this.mainchain.defaultPrivateKey,
        callback = false
    ) {
        if (this.utils.isFunction(privateKey)) {
            callback = privateKey;
            privateKey = this.mainchain.defaultPrivateKey;
        }
        if (this.utils.isFunction(options)) {
            callback = options;
            options = {};
        }
        if (!callback) {
            return this.injectPromise(this.depositTrx, callValue, feeLimit, options, privateKey);
        }
        if (this.validator.notValid([
            {
                name: 'callValue',
                type: 'integer',
                value: callValue,
                gte: 0
            },
            {
                name: 'feeLimit',
                type: 'integer',
                value: feeLimit,
                gt: 0,
                lte: 1_000_000_000
            }
        ], callback)) {
            return;
        }
        options = {
            callValue,
            feeLimit,
            ...options
        };
        try {
            const contractInstance = await this.mainchain.contract().at(this.mainGatewayAddress);
            const result = await contractInstance.depositTRX().send(options, privateKey);
            return callback(null, result);
        } catch (ex) {
            return callback(ex);
        }
    }

    async depositTrc10(
        tokenId,
        tokenValue,
        feeLimit,
        options = {},
        privateKey = this.mainchain.defaultPrivateKey,
        callback = false) {
        if (this.utils.isFunction(privateKey)) {
            callback = privateKey;
            privateKey = this.mainchain.defaultPrivateKey;
        }
        if (this.utils.isFunction(options)) {
            callback = options;
            options = {};
        }
        if (!callback) {
            return this.injectPromise(this.depositTrc10, tokenId, tokenValue, feeLimit, options, privateKey);
        }
        if (this.validator.notValid([
            {
                name: 'tokenValue',
                type: 'integer',
                value: tokenValue,
                gte: 0
            },
            {
                name: 'feeLimit',
                type: 'integer',
                value: feeLimit,
                gt: 0,
                lte: 1_000_000_000
            },
            {
                name: 'tokenId',
                type: 'integer',
                value: tokenId,
                gte: 0
            }
        ], callback)) {
            return;
        }
        options = {
            tokenId,
            tokenValue,
            feeLimit,
            ...options
        };
        try {
            const contractInstance = await this.mainchain.contract().at(this.mainGatewayAddress);
            const result = await contractInstance.depositTRC10(tokenId, tokenValue).send(options, privateKey);
            callback(null, result);
        } catch (ex) {
            return callback(ex);
        }
    }

    async depositTrc(
        functionSelector,
        num,
        feeLimit,
        contractAddress,
        options = {},
        privateKey = this.mainchain.defaultPrivateKey,
        callback = false
    ) {
        if (this.utils.isFunction(privateKey)) {
            callback = privateKey;
            privateKey = this.mainchain.defaultPrivateKey;
        }
        if (this.utils.isFunction(options)) {
            callback = options;
            options = {};
        }
        if (!callback) {
            return this.injectPromise(this.depositTrc, functionSelector, num, feeLimit, contractAddress, options, privateKey);
        }
        if (this.validator.notValid([
            {
                name: 'functionSelector',
                type: 'not-empty-string',
                value: functionSelector
            },
            {
                name: 'num',
                type: 'integer',
                value: num,
                gte: 0
            },
            {
                name: 'feeLimit',
                type: 'integer',
                value: feeLimit,
                gt: 0,
                lte: 1_000_000_000
            },
            {
                name: 'contractAddress',
                type: 'address',
                value: contractAddress
            }
        ], callback)) {
            return;
        }
        options = {
            feeLimit,
            ...options,
            callValue: 0,
            tokenId: '',
            tokenValue: 0
        };
        try {
            let result = null;
            if (functionSelector === 'approve') {
                const approveInstance = await this.mainchain.contract().at(contractAddress);
                result = await approveInstance.approve(this.mainGatewayAddress, num).send(options, privateKey)
            } else {
                const contractInstance = await this.mainchain.contract().at(this.mainGatewayAddress);
                if (functionSelector === 'depositTRC20') {
                    result = await contractInstance.depositTRC20(contractAddress, num).send(options, privateKey);
                } else if (functionSelector === 'depositTRC721') {
                    result = await contractInstance.depositTRC721(contractAddress, num).send(options, privateKey);
                }
            }
            callback(null, result);
        } catch (ex) {
            return callback(ex);
        }
    }

    async approveTrc20(
        num,
        feeLimit,
        contractAddress,
        options = {},
        privateKey = this.mainchain.defaultPrivateKey,
        callback = false
    ) {
        const functionSelector = 'approve';
        return this.depositTrc(
            functionSelector,
            num,
            feeLimit,
            contractAddress,
            options,
            privateKey,
            callback
        );
    }

    async approveTrc721(
        id,
        feeLimit,
        contractAddress,
        options = {},
        privateKey = this.mainchain.defaultPrivateKey,
        callback = false
    ) {
        const functionSelector = 'approve';
        return this.depositTrc(
            functionSelector,
            id,
            feeLimit,
            contractAddress,
            options,
            privateKey,
            callback
        );
    }

    async depositTrc20(
        num,
        feeLimit,
        contractAddress,
        options = {},
        privateKey = this.mainchain.defaultPrivateKey,
        callback = false
    ) {
        const functionSelector = 'depositTRC20';
        return this.depositTrc(
            functionSelector,
            num,
            feeLimit,
            contractAddress,
            options,
            privateKey,
            callback
        );
    }

    async depositTrc721(
        id,
        feeLimit,
        contractAddress,
        options = {},
        privateKey = this.mainchain.defaultPrivateKey,
        callback = false
    ) {
        const functionSelector = 'depositTRC721';
        return this.depositTrc(
            functionSelector,
            id,
            feeLimit,
            contractAddress,
            options,
            privateKey,
            callback
        );
    }

    /**
     * mapping asset TRC20 or TRC721 to DAppChain
     */
    async mappingTrc(
        trxHash,
        feeLimit,
        functionSelector,
        options = {},
        privateKey = this.mainchain.defaultPrivateKey,
        callback
    ) {
        if (this.utils.isFunction(privateKey)) {
            callback = privateKey;
            privateKey = this.mainchain.defaultPrivateKey;
        }
        if (this.utils.isFunction(options)) {
            callback = options;
            options = {};
        }
        if (!callback) {
            return this.injectPromise(this.mappingTrc, trxHash, feeLimit, functionSelector, options, privateKey);
        }
        if (this.validator.notValid([
            {
                name: 'trxHash',
                type: 'not-empty-string',
                value: trxHash
            },
            {
                name: 'feeLimit',
                type: 'integer',
                value: feeLimit,
                gt: 0,
                lte: 1_000_000_000
            }
        ], callback)) {
            return;
        }
        trxHash = trxHash.startsWith('0x') ? trxHash : ('0x' + trxHash);
        options = {
            feeLimit,
            ...options,
            callValue: 0
        };
        try {
            const contractInstance = await this.mainchain.contract().at(this.mainGatewayAddress);
            let result = null;
            if (functionSelector === 'mappingTRC20') {
                result = await contractInstance.mappingTRC20(trxHash).send(options, privateKey);
            } else if (functionSelector === 'mappingTRC721') {
                result = await contractInstance.mappingTRC721(trxHash).send(options, privateKey);
            } else {
                callback(new Error('type must be trc20 or trc721'));
            }
            callback(null, result);
        } catch (ex) {
            return callback(ex);
        }
    }

    async mappingTrc20(
        trxHash,
        feeLimit,
        options = {},
        privateKey = this.mainchain.defaultPrivateKey,
        callback = false
    ) {
        const functionSelector = 'mappingTRC20';
        return this.mappingTrc(
            trxHash,
            feeLimit,
            functionSelector,
            options,
            privateKey,
            callback);
    }

    async mappingTrc721(
        trxHash,
        feeLimit,
        options = {},
        privateKey = this.mainchain.defaultPrivateKey,
        callback = false
    ) {
        const functionSelector = 'mappingTRC721';
        return this.mappingTrc(
            trxHash,
            feeLimit,
            functionSelector,
            options,
            privateKey,
            callback);
    }

    /**
     * withdraw trx from sidechain to mainchain
     */
    async withdrawTrx(
        callValue,
        feeLimit,
        options = {},
        privateKey = this.mainchain.defaultPrivateKey,
        callback = false
    ) {
        if (this.utils.isFunction(privateKey)) {
            callback = privateKey;
            privateKey = this.mainchain.defaultPrivateKey;
        }
        if (this.utils.isFunction(options)) {
            callback = options;
            options = {};
        }
        if (!callback) {
            return this.injectPromise(this.withdrawTrx, callValue, feeLimit, options, privateKey);
        }
        if (this.validator.notValid([
            {
                name: 'callValue',
                type: 'integer',
                value: callValue,
                gte: 0
            },
            {
                name: 'feeLimit',
                type: 'integer',
                value: feeLimit,
                gt: 0,
                lte: 1_000_000_000
            }
        ], callback)) {
            return;
        }
        options = {
            callValue,
            feeLimit,
            ...options
        };
        try {
            const contractInstance = await this.sidechain.contract().at(this.sideGatewayAddress);
            const result = await contractInstance.withdrawTRX().send(options, privateKey);
            return callback(null, result);
        } catch (ex) {
            return callback(ex);
        }
    }

    async withdrawTrc10(
        tokenId,
        tokenValue,
        feeLimit,
        options = {},
        privateKey = this.mainchain.defaultPrivateKey,
        callback = false
    ) {
        if (this.utils.isFunction(privateKey)) {
            callback = privateKey;
            privateKey = this.mainchain.defaultPrivateKey;
        }
        if (this.utils.isFunction(options)) {
            callback = options;
            options = {};
        }
        if (!callback) {
            return this.injectPromise(this.withdrawTrc10, tokenId, tokenValue, feeLimit, options, privateKey);
        }
        if (this.validator.notValid([
            {
                name: 'tokenId',
                type: 'integer',
                value: tokenId,
                gte: 0
            },
            {
                name: 'tokenValue',
                type: 'integer',
                value: tokenValue,
                gte: 0
            },
            {
                name: 'feeLimit',
                type: 'integer',
                value: feeLimit,
                gt: 0,
                lte: 1_000_000_000
            }
        ], callback)) {
            return;
        }
        options = {
            tokenValue,
            tokenId,
            callValue: 0,
            feeLimit,
            ...options
        };
        try {
            const contractInstance = await this.sidechain.contract().at(this.sideGatewayAddress);
            const result = await contractInstance.withdrawTRC10(tokenId, tokenValue).send(options, privateKey);
            return callback(null, result);
        } catch (ex) {
            return callback(ex);
        }
    }

    async withdrawTrc(
        numOrId,
        feeLimit,
        contractAddress,  // side chain trc20 contract address after mapping
        options = {},
        privateKey = this.mainchain.defaultPrivateKey,
        callback = false
    ) {
        if (this.utils.isFunction(privateKey)) {
            callback = privateKey;
            privateKey = this.mainchain.defaultPrivateKey;
        }
        if (this.utils.isFunction(options)) {
            callback = options;
            options = {};
        }
        if (!callback) {
            return this.injectPromise(this.withdrawTrc, numOrId, feeLimit, contractAddress, options, privateKey);
        }
        if (this.validator.notValid([
            {
                name: 'numOrId',
                type: 'integer',
                value: numOrId,
                gte: 0
            },
            {
                name: 'feeLimit',
                type: 'integer',
                value: feeLimit,
                gt: 0,
                lte: 1_000_000_000
            },
            {
                name: 'contractAddress',
                type: 'address',
                value: contractAddress
            }
        ], callback)) {
            return;
        }
        options = {
            feeLimit,
            ...options,
            callValue: 0
        };
        const parameters = [
            {
                type: 'uint256',
                value: numOrId
            }
        ];
        const functionSelector = 'withdrawal(uint256)';

        try {
            const address = privateKey ? this.sidechain.address.fromPrivateKey(privateKey) : this.sidechain.defaultAddress.base58;
            const transaction = await this.sidechain.transactionBuilder.triggerSmartContract(
                contractAddress,
                functionSelector,
                options,
                parameters,
                this.sidechain.address.toHex(address)
            );
            if (!transaction.result || !transaction.result.result) {
                return callback('Unknown error: ' + JSON.stringify(transaction.transaction, null, 2));
            }

            const signedTransaction = await this.sidechain.trx.sign(transaction.transaction, privateKey);

            if (!signedTransaction.signature) {
                if (!privateKey)
                    return callback('Transaction was not signed properly');

                return callback('Invalid private key provided');
            }

            const broadcast = await this.sidechain.trx.sendRawTransaction(signedTransaction);
            if (broadcast.code) {
                const err = {
                    error: broadcast.code,
                    message: broadcast.code
                };
                if (broadcast.message)
                    err.message = this.tronWeb.toUtf8(broadcast.message);
                return callback(err)
            }

            if (!options.shouldPollResponse)
                return callback(null, signedTransaction.txID);

            const checkResult = async (index = 0) => {
                if (index == 20) {
                    return callback({
                        error: 'Cannot find result in solidity node',
                        transaction: signedTransaction
                    });
                }

                const output = await this.sidechain.trx.getTransactionInfo(signedTransaction.txID);

                if (!Object.keys(output).length) {
                    return setTimeout(() => {
                        checkResult(index + 1);
                    }, 3000);
                }

                if (output.result && output.result == 'FAILED') {
                    return callback({
                        error: this.tronWeb.toUtf8(output.resMessage),
                        transaction: signedTransaction,
                        output
                    });
                }

                if (!utils.hasProperty(output, 'contractResult')) {
                    return callback({
                        error: 'Failed to execute: ' + JSON.stringify(output, null, 2),
                        transaction: signedTransaction,
                        output
                    });
                }

                if (options.rawResponse)
                    return callback(null, output);

                let decoded = decodeOutput(this.outputs, '0x' + output.contractResult[0]);

                if (decoded.length === 1)
                    decoded = decoded[0];

                return callback(null, decoded);
            }

            checkResult();
        } catch (ex) {
            return callback(ex);
        }
    }

    async withdrawTrc20(
        num,
        feeLimit,
        contractAddress,
        options,
        privateKey = this.mainchain.defaultPrivateKey,
        callback = false
    ) {
        return this.withdrawTrc(
            num,
            feeLimit,
            contractAddress,
            options,
            privateKey,
            callback);
    }

    async withdrawTrc721(
        id,
        feeLimit,
        contractAddress,
        options,
        privateKey = this.mainchain.defaultPrivateKey,
        callback = false
    ) {
        return this.withdrawTrc(
            id,
            feeLimit,
            contractAddress,
            options,
            privateKey,
            callback);
    }


    async injectFund(
        num,
        feeLimit,
        options,
        privateKey = this.mainchain.defaultPrivateKey,
        callback = false
    ) {
        if (this.utils.isFunction(privateKey)) {
            callback = privateKey;
            privateKey = this.mainchain.defaultPrivateKey;
        }

        if (this.utils.isFunction(options)) {
            callback = options;
            options = {};
        }
        if (!callback) {
            return this.injectPromise(this.injectFund, num, feeLimit, options, privateKey);
        }
        if (this.validator.notValid([
            {
                name: 'num',
                type: 'integer',
                value: num,
                gte: 0
            },
            {
                name: 'feeLimit',
                type: 'integer',
                value: feeLimit,
                gt: 0,
                lte: 1_000_000_000
            }
        ], callback)) {
            return;
        }

        try {
            const address = this.sidechain.address.fromPrivateKey(privateKey);
            const hexAddress = this.sidechain.address.toHex(address);
            const transaction = await this.sidechain.fullNode.request('/wallet/fundinject', {
                owner_address: hexAddress,
                amount: num
            }, 'post');
            console.log(transaction)

            const signedTransaction = await this.sidechain.trx.sign(transaction, privateKey);

            if (!signedTransaction.signature) {
                if (!privateKey)
                    return callback('Transaction was not signed properly');

                return callback('Invalid private key provided');
            }

            const broadcast = await this.sidechain.trx.sendRawTransaction(signedTransaction);
            if (broadcast.code) {
                const err = {
                    error: broadcast.code,
                    message: broadcast.code
                };
                if (broadcast.message)
                    err.message = this.sunWeb.mainchain.toUtf8(broadcast.message);
                return callback(err)
            }
            return callback(null, signedTransaction.txID);
        } catch (ex) {
            return callback(ex);
        }
    }
}


package org.tron.common.runtime.vm;

import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;
import org.testng.Assert;
import org.tron.common.runtime.TVMTestResult;
import org.tron.common.runtime.TvmTestUtils;
import org.tron.common.runtime.config.VMConfig;
import org.tron.common.runtime.vm.program.ProgramResult;
import org.tron.core.Wallet;
import org.tron.core.capsule.ReceiptCapsule;
import org.tron.core.exception.ContractExeException;
import org.tron.core.exception.ContractValidateException;
import org.tron.core.exception.ReceiptCheckErrException;
import org.tron.core.exception.VMIllegalException;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Protocol.Transaction.Result.contractResult;
import stest.tron.wallet.common.client.utils.AbiUtil;

public class TransferFailedEnergyTest extends VMTestBase {
/*
pragma solidity ^0.5.4;
contract EnergyOfTransferFailedTest {
    constructor() payable public {

    }
    // InsufficientBalance
    function testTransferTrxInsufficientBalance() payable public{
        msg.sender.transfer(10);
    }

    function testSendTrxInsufficientBalance() payable public{
        msg.sender.send(10);
    }

    function testTransferTokenInsufficientBalance(trcToken tokenId) payable public{
        msg.sender.transferToken(10, tokenId);
    }

    function testCallTrxInsufficientBalance(address payable caller) public {
        caller.call.value(10)(abi.encodeWithSignature("test()"));
    }

    function testCreateTrxInsufficientBalance() payable public {
        (new Caller).value(10)();
    }

    // NonexistentTarget
    function testTransferTrxNonexistentTarget(address payable nonexistentTarget) payable public {
        require(address(this).balance >= 10);
        nonexistentTarget.transfer(10);
    }

    function testTransferTokenNonexistentTarget(address payable nonexistentTarget, trcToken tokenId) payable public {
        require(address(this).balance >= 10);
        nonexistentTarget.transferToken(10, tokenId);
    }

    function testCallTrxNonexistentTarget(address payable nonexistentTarget) payable public {
        require(address(this).balance >= 10);
        nonexistentTarget.call.value(10)(abi.encodeWithSignature("test()"));
    }

    function testSuicideNonexistentTarget(address payable nonexistentTarget) payable public {
         selfdestruct(nonexistentTarget);
    }

    // target is self
    function testTransferTrxSelf() payable public{
        require(address(this).balance >= 10);
        address payable self = address(uint160(address(this)));
        self.transfer(10);
    }

    function testSendTrxSelf() payable public{
        require(address(this).balance >= 10);
        address payable self = address(uint160(address(this)));
        self.send(10);
    }

    function testTransferTokenSelf(trcToken tokenId) payable public{
        require(address(this).balance >= 10);
        address payable self = address(uint160(address(this)));
        self.transferToken(10, tokenId);
    }
}



contract Caller {
    constructor() payable public {}
    function test() payable public {}
}
 */

/*
// 0.4.25
contract EnergyOfTransferFailedTest {

    constructor() payable public {

    }

    // InsufficientBalance
    function testTransferTrxInsufficientBalance() payable public{
        msg.sender.transfer(10);
    }

    function testSendTrxInsufficientBalance() payable public{
        msg.sender.send(10);
    }

    function testTransferTokenInsufficientBalance(trcToken tokenId) payable public{
        msg.sender.transferToken(10, tokenId);
    }

    function testCallTrxInsufficientBalance(address caller) payable public {
        caller.call.value(10)(abi.encodeWithSignature("test()"));
    }

    function testCreateTrxInsufficientBalance() payable public {
        (new Caller).value(10)();
    }

    // NonexistentTarget
    function testTransferTrxNonexistentTarget(address nonexistentTarget) payable public {
        require(address(this).balance >= 10);
        nonexistentTarget.transfer(10);
    }

    function testTransferTokenNonexistentTarget(address nonexistentTarget, trcToken tokenId) payable public {
        require(address(this).balance >= 10);
        nonexistentTarget.transferToken(10, tokenId);
    }

    function testCallTrxNonexistentTarget(address nonexistentTarget) public {
        require(address(this).balance >= 10);
        nonexistentTarget.call.value(10)(abi.encodeWithSignature("test()"));
    }

    function testSuicideNonexistentTarget(address nonexistentTarget) public {
         selfdestruct(nonexistentTarget);
    }

    // target is self
    function testTransferTrxSelf() payable public{
        require(address(this).balance >= 10);
        address self = address(uint160(address(this)));
        self.transfer(10);
    }

    function testSendTrxSelf() payable public{
        require(address(this).balance >= 10);
        address self = address(uint160(address(this)));
        self.send(10);
    }

    function testTransferTokenSelf(trcToken tokenId) payable public{
        require(address(this).balance >= 10);
        address self = address(uint160(address(this)));
        self.transferToken(10, tokenId);
    }
}



contract Caller {
    constructor() payable public {}
    function test() payable public {}
}
 */

  @Data
  @AllArgsConstructor
  @ToString
  static class TestCase {
    String method;
    List<Object> params;
    boolean allEnergy;
    contractResult receiptResult;
  }

  private static final String nonExistAddress = "27k66nycZATHzBasFT9782nTsYWqVtxdtAc";  // 21 char

  TestCase[] testCasesAfterAllowTvmConstantinop = {
      new TestCase("testTransferTrxSelf()", Collections.emptyList(), false, contractResult.TRANSFER_FAILED),
      new TestCase("testSendTrxSelf()", Collections.emptyList(), false, contractResult.TRANSFER_FAILED),
      new TestCase("testSuicideNonexistentTarget(address)", Collections.singletonList(nonExistAddress), false, contractResult.TRANSFER_FAILED),
      new TestCase("testTransferTrxNonexistentTarget(address)", Collections.singletonList(nonExistAddress), false, contractResult.TRANSFER_FAILED),
      new TestCase("testCallTrxNonexistentTarget(address)", Collections.singletonList(nonExistAddress), false, contractResult.TRANSFER_FAILED),
  };

  TestCase[] testCasesBeforeAllowTvmConstantinop = {
      new TestCase("testTransferTrxSelf()", Collections.emptyList(), true, contractResult.UNKNOWN),
      new TestCase("testSendTrxSelf()", Collections.emptyList(), true, contractResult.UNKNOWN),
      new TestCase("testSuicideNonexistentTarget(address)", Collections.singletonList(nonExistAddress), true, contractResult.UNKNOWN),
      new TestCase("testTransferTrxNonexistentTarget(address)", Collections.singletonList(nonExistAddress), true, contractResult.UNKNOWN),
      new TestCase("testCallTrxNonexistentTarget(address)", Collections.singletonList(nonExistAddress), true, contractResult.UNKNOWN),
  };

  TestCase[] testCasesInsufficientBalance = {
      new TestCase("testTransferTrxInsufficientBalance()", Collections.emptyList(), false, contractResult.REVERT),
      new TestCase("testSendTrxInsufficientBalance()", Collections.emptyList(), false, contractResult.SUCCESS),
      new TestCase("testCreateTrxInsufficientBalance()", Collections.emptyList(), false, contractResult.REVERT),
      new TestCase("testCallTrxInsufficientBalance()", Collections.emptyList(), false, contractResult.REVERT),
      new TestCase("testTransferTokenInsufficientBalance(trcToken)", Collections.singletonList(1000001), false, contractResult.REVERT),
  };



  @Test
  public void testTransferFailedAfterAllowTvmConstantinopl()
      throws ContractExeException, ReceiptCheckErrException, VMIllegalException, ContractValidateException {

    String contractName = "EnergyOfTransferFailedTest";
    byte[] address = Hex.decode(OWNER_ADDRESS);
    String ABI = "[]";
    String code = "608060405261060c806100136000396000f3fe6080604052600436106100c4576000357c01000000000000000000000000000000000000000000000000000000009004806344e51c131161008157806344e51c131461017c57806362098f73146101a257806387a9d735146101aa5780639d24d299146101c75780639da1bf8a146101f3578063f10d5077146101fb576100c4565b806307ed3f71146100c957806308e4ab99146100d35780630d0def85146100db5780630de991bb1461012857806317b6ad5b1461013057806322970e1814610156575b600080fd5b6100d1610218565b005b6100d1610244565b3480156100e757600080fd5b50d380156100f457600080fd5b50d2801561010157600080fd5b506100d16004803603602081101561011857600080fd5b5035600160a060020a0316610272565b6100d161036f565b6100d16004803603602081101561014657600080fd5b5035600160a060020a03166103ad565b6100d16004803603602081101561016c57600080fd5b5035600160a060020a03166103b9565b6100d16004803603602081101561019257600080fd5b5035600160a060020a03166103fa565b6100d1610409565b6100d1600480360360208110156101c057600080fd5b5035610423565b6100d1600480360360408110156101dd57600080fd5b50600160a060020a038135169060200135610496565b6100d16104c1565b6100d16004803603602081101561021157600080fd5b50356104ed565b6040513390600090600a9082818181858883f19350505050158015610241573d6000803e3d6000fd5b50565b600a60405161025290610550565b6040518091039082f08015801561026d573d6000803e3d6000fd5b505050565b60408051600481526024810182526020810180517bffffffffffffffffffffffffffffffffffffffffffffffffffffffff167ff8a8fd6d0000000000000000000000000000000000000000000000000000000017815291518151600160a060020a03851693600a9392918291908083835b602083106103025780518252601f1990920191602091820191016102e3565b6001836020036101000a03801982511681845116808217855250505050505090500191505060006040518083038185875af1925050503d8060008114610364576040519150601f19603f3d011682016040523d82523d6000602084013e610369565b606091505b50505050565b600a3031101561037e57600080fd5b60405130908190600090600a9082818181858883f193505050501580156103a9573d6000803e3d6000fd5b5050565b80600160a060020a0316ff5b600a303110156103c857600080fd5b604051600160a060020a03821690600090600a9082818181858883f193505050501580156103a9573d6000803e3d6000fd5b600a3031101561027257600080fd5b6040513390600090600a9082818181858883f15050505050565b600a3031101561043257600080fd5b30806000600a848015801561044657600080fd5b50806780000000000000001115801561045e57600080fd5b5080620f42401015801561047157600080fd5b50604051600081818185878a8ad094505050505015801561026d573d6000803e3d6000fd5b600a303110156104a557600080fd5b600160a060020a0382166000600a838015801561044657600080fd5b600a303110156104d057600080fd5b60405130908190600090600a9082818181858883f1505050505050565b336000600a838015801561050057600080fd5b50806780000000000000001115801561051857600080fd5b5080620f42401015801561052b57600080fd5b50604051600081818185878a8ad09450505050501580156103a9573d6000803e3d6000fd5b60848061055d8339019056fe608060405260738060116000396000f3fe6080604052600436106038577c01000000000000000000000000000000000000000000000000000000006000350463f8a8fd6d8114603d575b600080fd5b60436045565b005b56fea165627a7a72305820ae73d633cf81f32e8c6917d5faea925dd9c04abcecf002617d6ec1440f1349c90029a165627a7a723058201f0cab76b7df6e1900e7524fd40e9257a915692888b74c2e2e68df9d9a4a5a910029";
    long value = 100000;
    long fee = 100000000;
    long consumeUserResourcePercent = 0;

//     deploy contract
    Transaction trx = TvmTestUtils.generateDeploySmartContractAndGetTransaction(
        contractName, address, ABI, code, value, fee, consumeUserResourcePercent, null, 1);
    byte[] addressWithSufficientBalance = Wallet.generateContractAddress(trx);
    runtime = TvmTestUtils.processTransactionAndReturnRuntime(trx, rootDeposit, null);
    Assert.assertNull(runtime.getRuntimeError());

    for (TestCase testCase : testCasesAfterAllowTvmConstantinop) {
      checkResult(testCase, addressWithSufficientBalance);
    }

    trx = TvmTestUtils.generateDeploySmartContractAndGetTransaction(
        contractName, address, ABI, code, 0, fee, consumeUserResourcePercent, null, 1);
    byte[] addressWithoutBalance = Wallet.generateContractAddress(trx);
    runtime = TvmTestUtils.processTransactionAndReturnRuntime(trx, rootDeposit, null);
    Assert.assertNull(runtime.getRuntimeError());

    for (TestCase testCase : testCasesInsufficientBalance) {
      checkResult(testCase, addressWithoutBalance);
    }
  }

  private void checkResult(TestCase testCase, byte[] factoryAddress)
      throws ContractExeException, ReceiptCheckErrException, VMIllegalException, ContractValidateException {
    String hexInput = AbiUtil.parseMethod(testCase.getMethod(), testCase.getParams());
    long fee = 100000000;
    long allEnergy = 1000000;
    TVMTestResult result = TvmTestUtils
        .triggerContractAndReturnTvmTestResult(Hex.decode(OWNER_ADDRESS),
            factoryAddress, Hex.decode(hexInput), 0, fee, manager, null);
    ProgramResult programResult = result.getRuntime().getResult();
    ReceiptCapsule receiptCapsule = result.getReceipt();
    Assert.assertEquals(receiptCapsule.getResult(), testCase.getReceiptResult(),
        testCase.getMethod());
    if (testCase.allEnergy) {
      Assert.assertEquals(programResult.getEnergyUsed(), 1000000, testCase.getMethod());
    } else {
      Assert.assertTrue(programResult.getEnergyUsed() < allEnergy, testCase.getMethod());
    }
  }
}

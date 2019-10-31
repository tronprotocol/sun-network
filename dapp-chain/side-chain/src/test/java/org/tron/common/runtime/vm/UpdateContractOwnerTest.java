package org.tron.common.runtime.vm;

import com.google.protobuf.ByteString;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;
import org.testng.Assert;
import org.tron.common.crypto.ECKey;
import org.tron.common.runtime.vm.PrecompiledContracts.UpdateContractOwner;
import org.tron.core.Wallet;
import org.tron.core.capsule.ContractCapsule;
import org.tron.protos.Protocol.SmartContract;
import org.tron.protos.Protocol.SmartContract.Builder;
import stest.tron.wallet.common.client.utils.AbiUtil;

@Slf4j
public class UpdateContractOwnerTest extends VMTestBase {

  private static final String METHOD_SIGN = "updatecontractowner(address,address)";


  @Test
  public void correctionTest() {

    UpdateContractOwner contract = new UpdateContractOwner();

    ECKey fakeGateWayKey = new ECKey();
    manager.getDynamicPropertiesStore()
        .saveSideChainGateWayList(Arrays.asList(fakeGateWayKey.getAddress()));

    ECKey contractAddressKey = new ECKey();
    ECKey previousOwnerKey = new ECKey();
    ECKey updateOwnerKey = new ECKey();
    Builder builder = SmartContract.newBuilder();
    builder.setContractAddress(ByteString.copyFrom(contractAddressKey.getAddress()))
        .setOriginAddress(ByteString.copyFrom(previousOwnerKey.getAddress()));
    SmartContract newSmartContract = builder.build();
    rootDeposit
        .createContract(contractAddressKey.getAddress(), new ContractCapsule(newSmartContract));

    contract.setStaticCall(false);
    contract.setDeposit(rootDeposit);
    contract.setCallerAddress(fakeGateWayKey.getAddress());

    byte[] input = Hex.decode(AbiUtil.parseParameters(METHOD_SIGN, Arrays.asList(
        Wallet.encode58Check(contractAddressKey.getAddress()),
        Wallet.encode58Check(updateOwnerKey.getAddress())
    )));

    contract.execute(input);

    Assert.assertEquals(
        Arrays.equals(rootDeposit.getContract(contractAddressKey.getAddress()).getOriginAddress(),
            updateOwnerKey.getAddress())
        , true
    );

    Assert.assertNotNull(rootDeposit.getAccount(updateOwnerKey.getAddress()));


  }


}

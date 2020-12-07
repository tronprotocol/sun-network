package org.tron.common.utils;

import java.util.Arrays;
import lombok.Getter;
import org.tron.common.utils.Hash;

public enum ShieldedTRC20EventsEnum {

  MINT_NEW_LEAF(1, "MintNewLeaf(uint256,bytes32,bytes32,bytes32,bytes32[21])", "mint"),
  TRANSFER_NEW_LEAF(2, "TransferNewLeaf(uint256,bytes32,bytes32,bytes32,bytes32[21])", "transfer"),
  BURN_NEW_LEAF(3, "BurnNewLeaf(uint256,bytes32,bytes32,bytes32,bytes32[21])", "burn"),
  TOKEN_MINT(5, "TokenMint(address,uint256)", "mint"),
  TOKEN_BURN(4, "TokenBurn(address,uint256,bytes32[3])", "burn"),
  NOTE_SPENT(6, "NoteSpent(bytes32)", "unknown");


  @Getter
  private int typeId;
  @Getter
  private String signature;
  @Getter
  private String method;
  @Getter
  private byte[] hash;

  ShieldedTRC20EventsEnum(int typeId, String signature, String method) {
    this.typeId = typeId;
    this.signature = signature;
    this.hash = Hash.sha3(ByteArray.fromString(signature));
    this.method = method;
  }

  public static int getShieldedTRC20EventsTypeIdByTopicBytes(byte[] topicsBytes) {
    for (ShieldedTRC20EventsEnum e : ShieldedTRC20EventsEnum.values()) {
      if (Arrays.equals(topicsBytes, e.hash)) {
        return e.getTypeId();
      }
    }
    return 0;
  }

}

package org.tron.sunapi.service.impl;

import org.tron.api.GrpcAPI.AssetIssueList;
import org.tron.protos.Contract.AssetIssueContract;
import org.tron.sunapi.SunNetworkResponse;
import org.tron.sunapi.request.AssertIssueRequest;
import org.tron.sunapi.request.UpdateAssetRequest;
import org.tron.sunapi.service.AssetIssue;

public class AssetIssueImpl implements AssetIssue {

  @Override
  public SunNetworkResponse<Integer> updateAsset(UpdateAssetRequest request) {
    return null;
  }

  @Override
  public SunNetworkResponse<AssetIssueList> getAssetIssueByAccount(String address) {
    return null;
  }

  @Override
  public SunNetworkResponse<AssetIssueContract> getAssetIssueByName(String assetName) {
    return null;
  }

  @Override
  public SunNetworkResponse<AssetIssueList> getAssetIssueListByName(String assetName) {
    return null;
  }

  @Override
  public SunNetworkResponse<AssetIssueContract> getAssetIssueById(String assetId) {
    return null;
  }

  @Override
  public SunNetworkResponse<Integer> transferAsset(String toAddress, String assertName,
      String amountStr) {
    return null;
  }

  @Override
  public SunNetworkResponse<Integer> participateAssetIssue(String toAddress, String assertName,
      String amountStr) {
    return null;
  }

  @Override
  public SunNetworkResponse<Integer> assetIssue(AssertIssueRequest request) {
    return null;
  }

  @Override
  public SunNetworkResponse<AssetIssueList> getAssetIssueList() {
    return null;
  }

  @Override
  public SunNetworkResponse<AssetIssueList> getAssetIssueList(int offset, int limit) {
    return null;
  }
}

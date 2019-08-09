package org.tron.sunapi.service;

import org.tron.api.GrpcAPI.AssetIssueList;
import org.tron.protos.Contract.AssetIssueContract;
import org.tron.sunapi.SunNetworkResponse;
import org.tron.sunapi.request.AssertIssueRequest;
import org.tron.sunapi.request.UpdateAssetRequest;

public interface AssetIssue {
  SunNetworkResponse<Integer> updateAsset(UpdateAssetRequest request);

  SunNetworkResponse<AssetIssueList> getAssetIssueByAccount(String address);

  SunNetworkResponse<AssetIssueContract> getAssetIssueByName(String assetName);

  SunNetworkResponse<AssetIssueList> getAssetIssueListByName(String assetName);

  SunNetworkResponse<AssetIssueContract> getAssetIssueById(String assetId);

  SunNetworkResponse<Integer> transferAsset(String toAddress, String assertName, String amountStr);

  SunNetworkResponse<Integer> participateAssetIssue(String toAddress, String assertName, String amountStr);

  SunNetworkResponse<Integer> assetIssue(AssertIssueRequest request);

  SunNetworkResponse<AssetIssueList> getAssetIssueList();

  SunNetworkResponse<AssetIssueList> getAssetIssueList(int offset, int limit);
}

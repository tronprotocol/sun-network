package org.tron.sunapi.request;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class UpdateAssetRequest {
  public String newLimitString;
  public String newPublicLimitString;
  public String description;
  public String url;

}

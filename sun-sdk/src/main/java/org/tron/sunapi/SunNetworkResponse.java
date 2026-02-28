package org.tron.sunapi;

import com.google.common.collect.Lists;
import java.io.Serializable;
import java.util.List;

public class SunNetworkResponse<T> implements Serializable {

  private static final long serialVersionUID = 1L;

  private String code;
  private String desc;
  private T data;
  private List<T> dataList = Lists.newArrayList();

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getDesc() {
    return desc;
  }

  public void setDesc(String desc) {
    this.desc = desc;
  }

  public T getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }

  public List<T> getDataList() {
    return dataList;
  }

  public void setDataList(List<T> dataList) {
    this.dataList = dataList;
  }

  private SunNetworkResponse<T> code(String code) {
    this.code = code;
    return this;
  }

  private SunNetworkResponse<T> desc(String desc) {
    this.desc = desc;
    return this;
  }

  private SunNetworkResponse<T> data(T data) {
    this.data = data;
    return this;
  }

  private SunNetworkResponse<T> dataList(List<T> dataList) {
    this.dataList = dataList;
    return this;
  }

  public SunNetworkResponse<T> success(T data) {
    this.code = ErrorCodeEnum.SUCCESS.getCode();
    this.desc = ErrorCodeEnum.SUCCESS.getDesc();
    this.data = data;
    return this;
  }

  public void success(List<T> dataList) {
    this.code = ErrorCodeEnum.SUCCESS.getCode();
    this.desc = ErrorCodeEnum.SUCCESS.getDesc();
    this.dataList = dataList;
  }

//  public SunNetworkResponse<T> success(List<T> dataList){
//    return code(ErrorCodeEnum.SUCCESS.getCode()).desc(ErrorCodeEnum.SUCCESS.getDesc()).dataList(dataList);
//  }

  public SunNetworkResponse<T> failed(ErrorCodeEnum errorCodeEnum) {
    this.code = errorCodeEnum.getCode();
    this.desc = errorCodeEnum.getDesc();
    return this;
    //return code(errorCodeEnum.getCode()).desc(errorCodeEnum.getDesc());
  }

}

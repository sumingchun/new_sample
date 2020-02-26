package com.example;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class StaffForm {

  @NotBlank(message="職員の名称を記入してください。")
  private String name;

  @Min(value=1, message="1以上の数値を入力してください。")
  @Max(value=4, message="4以下の数値を入力してください。")
  private String staffId;

  @Size(max=3, message="年齢は3桁数字を超えないでください。")
  private String age;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getStaffId() {
    return staffId;
  }

  public void setStaffId(String staffId) {
    this.staffId = staffId;
  }

  public String getAge() {
    return age;
  }

  public void setAge(String age) {
    this.age = age;
  }
}
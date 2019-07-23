package board.core.common;

public enum RespondCodeEnum {
  SUCCESS("200,Successful operation!"),
  FAILURE("500,Operation failed...");

  private String code;

  RespondCodeEnum(String code) {
    this.code = code;
  }

  public int getCode() {
    return Integer.parseInt(this.code.split(",")[0]);
  }

  public String getMessage() {
    return this.code.split(",")[1];
  }
}

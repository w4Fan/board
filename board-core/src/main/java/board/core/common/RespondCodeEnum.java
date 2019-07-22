package board.core.common;

public enum RespondCodeEnum {
  SUCCESS(200),
  FAILURE(500);

  private int code;

  private RespondCodeEnum(int code) {
    this.code = code;
  }

  public int getCode() {
    return code;
  }
}

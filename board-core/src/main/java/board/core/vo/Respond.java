package board.core.vo;

import lombok.Data;

@Data
public class Respond {

  private int code;
  private Object data;
  private String message;

  public Respond(int code, Object data, String message) {
    this.code = code;
    this.data = data;
    this.message = message;
  }
}

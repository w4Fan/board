package board.core.actor.msg;

import board.core.vo.Respond;

public abstract class ReplyMessage implements Message {

  private ReplyMessage() {
  }

  public static final class Reply extends ReplyMessage {

    public final Respond respond;

    public Reply(Respond respond) {
      this.respond = respond;
    }
  }
}

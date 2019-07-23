package board.core.actor.msg;

import akka.actor.typed.ActorRef;

public abstract class ProjectMessage implements Message {

  private ProjectMessage() {
  }

  public static final class Get extends ProjectMessage {

    public final ActorRef<Message> replyTo;

    public Get(ActorRef<Message> replyTo) {
      this.replyTo = replyTo;
    }
  }

  public static final class Delete extends ProjectMessage {

    public final ActorRef<Message> replyTo;

    public Delete(ActorRef<Message> replyTo) {
      this.replyTo = replyTo;
    }
  }
}

package board.core.actor.msg;

import akka.actor.typed.ActorRef;
import board.core.entity.Project;
import board.core.vo.Respond;

public abstract class ProjectMessage implements Message {

  private ProjectMessage() {
  }

  public static final class Reply extends ProjectMessage {

    public final Respond respond;

    public Reply(Respond respond) {
      this.respond = respond;
    }
  }

  public static final class Create extends ProjectMessage {

    public final Project project;
    public final ActorRef<Reply> replyTo;

    public Create(Project project, ActorRef<Reply> replyTo) {
      this.project = project;
      this.replyTo = replyTo;
    }
  }
}

package board.core.actor.msg;

import akka.actor.typed.ActorRef;
import board.core.entity.Project;
import com.sun.xml.internal.messaging.saaj.soap.MessageImpl;

public abstract class BoardMessage implements Message {

  private BoardMessage() {
  }

  public static final class GetProject extends BoardMessage {

    public final int projectId;
    public final ActorRef<Message> replyTo;

    public GetProject(int projectId, ActorRef<Message> replyTo) {
      this.projectId = projectId;
      this.replyTo = replyTo;
    }
  }

  public static final class GetProjects extends BoardMessage {

    public final ActorRef<Message> replyTo;

    public GetProjects(ActorRef<Message> replyTo) {
      this.replyTo = replyTo;
    }
  }

  public static final class CreateProject extends BoardMessage {

    public final Project project;
    public final ActorRef<Message> replyTo;

    public CreateProject(Project project, ActorRef<Message> replyTo) {
      this.project = project;
      this.replyTo = replyTo;
    }
  }

  public static final class DeleteProject extends BoardMessage {

    public final int projectId;
    public final ActorRef<Message> replyTo;

    public DeleteProject(int projectId, ActorRef<Message> replyTo) {
      this.projectId = projectId;
      this.replyTo = replyTo;
    }
  }
}

package board.actor.protocol;

import akka.actor.typed.ActorRef;
import board.actor.protocol.ProjectProtocol.ProjectMessage;

public abstract class ProjectManagerProtocol {

  private ProjectManagerProtocol() {
  }

  public interface ProjectManagerMessage {

  }

  public static final class ProjectTerminated implements ProjectManagerMessage {

    public final ActorRef<ProjectMessage> project;
    public final String projectId;

    public ProjectTerminated(ActorRef<ProjectMessage> project, String projectId) {
      this.project = project;
      this.projectId = projectId;
    }
  }

  public static final class RequestTrackProject implements ProjectManagerMessage {

    public final String projectId;
    public final ActorRef<ProjectRegistered> replyTo;

    public RequestTrackProject(String projectId,
        ActorRef<ProjectRegistered> replyTo) {
      this.projectId = projectId;
      this.replyTo = replyTo;
    }
  }

  public static final class ProjectRegistered {

    public final ActorRef<ProjectMessage> project;

    public ProjectRegistered(ActorRef<ProjectMessage> project) {
      this.project = project;
    }
  }
}

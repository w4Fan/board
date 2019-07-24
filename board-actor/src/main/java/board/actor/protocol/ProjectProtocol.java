package board.actor.protocol;

import akka.actor.typed.ActorRef;
import board.common.project.ProjectStatus;

public abstract class ProjectProtocol {

  private ProjectProtocol() {
  }

  public interface ProjectMessage {

  }

  public static enum Terminate implements ProjectMessage {
    INSTANCE
  }

  public static final class RequestProject implements ProjectMessage {

    public final ActorRef<RespondProject> replyTo;

    public RequestProject(ActorRef<RespondProject> replyTo) {
      this.replyTo = replyTo;
    }
  }

  public static final class RespondProject {

    public final String projectId;
    public final ProjectStatus projectStatus;

    public RespondProject(String projectId, ProjectStatus projectStatus) {
      this.projectId = projectId;
      this.projectStatus = projectStatus;
    }
  }

  public static final class RecordProject implements ProjectMessage {

    public final ProjectStatus projectStatus;
    public final ActorRef<RespondProject> replyTo;

    public RecordProject(ProjectStatus projectStatus, ActorRef<RespondProject> replyTo) {
      this.projectStatus = projectStatus;
      this.replyTo = replyTo;
    }
  }
}

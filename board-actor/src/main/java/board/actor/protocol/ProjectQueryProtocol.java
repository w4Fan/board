package board.actor.protocol;

import akka.actor.typed.ActorRef;
import board.actor.protocol.ProjectManagerProtocol.ProjectManagerMessage;
import board.common.project.ProjectStatus;
import java.util.Map;

public abstract class ProjectQueryProtocol {

  private ProjectQueryProtocol() {
  }

  public interface ProjectQueryMessage {

  }

  public interface ProjectReading {

  }

  public static final class RequestAllProject implements ProjectQueryMessage,
      ProjectManagerMessage {

    public final ActorRef<RespondAllProject> replyTo;

    public RequestAllProject(
        ActorRef<RespondAllProject> replyTo) {
      this.replyTo = replyTo;
    }
  }

  public static final class RespondAllProject {

    public final Map<String, ProjectReading> projects;

    public RespondAllProject(Map<String, ProjectReading> projects) {
      this.projects = projects;
    }
  }

  public static final class ProjectRecord implements ProjectReading {

    public final String projectId;
    public final ProjectStatus projectStatus;

    public ProjectRecord(String projectId, ProjectStatus projectStatus) {
      this.projectId = projectId;
      this.projectStatus = projectStatus;
    }
  }

  public enum ProjectNotAvailable implements ProjectReading {
    INSTANCE
  }

  public enum ProjectTimedOut implements ProjectReading {
    INSTANCE
  }
}

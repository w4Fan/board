package board.actor.project;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import board.actor.protocol.ProjectManagerProtocol.ProjectManagerMessage;
import board.actor.protocol.ProjectManagerProtocol.ProjectRegistered;
import board.actor.protocol.ProjectManagerProtocol.ProjectTerminated;
import board.actor.protocol.ProjectManagerProtocol.RequestTrackProject;
import board.actor.protocol.ProjectProtocol.ProjectMessage;
import board.actor.protocol.ProjectQueryProtocol.RequestAllProject;
import board.common.project.ProjectStatus;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class ProjectManagerActor extends AbstractBehavior<ProjectManagerMessage> {

  public static Behavior<ProjectManagerMessage> createBehavior() {
    return Behaviors.setup(ProjectManagerActor::new);
  }

  private final ActorContext<ProjectManagerMessage> context;
  private final Map<String, ActorRef<ProjectMessage>> projectIdToActor = new HashMap<>();

  private ProjectManagerActor(ActorContext<ProjectManagerMessage> context) {
    this.context = context;
    context.getLog().info("ProjectManager started");
  }

  @Override
  public Receive<ProjectManagerMessage> createReceive() {
    return newReceiveBuilder()
        .onMessage(RequestTrackProject.class, this::onTrackProject)
        .onMessage(ProjectTerminated.class, this::onTerminated)
        .onMessage(RequestAllProject.class, this::onAllProject)
        .onSignal(PostStop.class, signal -> postStop())
        .build();
  }

  private ProjectManagerActor onTrackProject(RequestTrackProject msg) {
    ActorRef<ProjectMessage> project;
    if (projectIdToActor.containsKey(msg.projectId)) {
      context.getLog().info("Project actor is exist for {}", msg.projectId);
      project = projectIdToActor.get(msg.projectId);
    } else {
      context.getLog().info("Creating project actor for {}", msg.projectId);
      project = context.spawn(ProjectActor.createBehavior(
          msg.projectId,
          ProjectStatus.CONNECTED),
          msg.projectId);
      projectIdToActor.put(msg.projectId, project);
    }
    msg.replyTo.tell(new ProjectRegistered(project));
    return this;
  }

  private ProjectManagerActor onTerminated(ProjectTerminated msg) {
    context.getLog().info("Project actor for {} has been terminated", msg.projectId);
    projectIdToActor.remove(msg.projectId);
    return this;
  }

  private ProjectManagerActor onAllProject(RequestAllProject msg) {
    Map<String, ActorRef<ProjectMessage>> projectIdToActorCopy = new HashMap<>(
        this.projectIdToActor);
    context.spawnAnonymous(ProjectQueryActor.createBehavior(
        projectIdToActorCopy, msg.replyTo, Duration.ofSeconds(3)));
    return this;
  }

  private ProjectManagerActor postStop() {
    context.getLog().info("ProjectManager stopped");
    return this;
  }
}

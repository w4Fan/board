package board.actor.project;

import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import board.actor.protocol.ProjectProtocol.ProjectMessage;
import board.actor.protocol.ProjectProtocol.RecordProject;
import board.actor.protocol.ProjectProtocol.RequestProject;
import board.actor.protocol.ProjectProtocol.RespondProject;
import board.actor.protocol.ProjectProtocol.Terminate;
import board.common.project.ProjectStatus;

public class ProjectActor extends AbstractBehavior<ProjectMessage> {

  public static Behavior<ProjectMessage> createBehavior(
      String projectId,
      ProjectStatus projectStatus) {
    return Behaviors.setup(context -> new ProjectActor(context, projectId, projectStatus));
  }

  private final ActorContext<ProjectMessage> context;
  private final String projectId;
  private ProjectStatus projectStatus;

  private ProjectActor(
      ActorContext<ProjectMessage> context,
      String projectId,
      ProjectStatus projectStatus) {
    this.context = context;
    this.projectId = projectId;
    this.projectStatus = projectStatus;
    context.getLog().info("Project actor [{}] started, status is [{}]", projectId, projectStatus);
  }

  @Override
  public Receive<ProjectMessage> createReceive() {
    return newReceiveBuilder()
        .onMessage(RequestProject.class, this::onRequestProject)
        .onMessage(RecordProject.class, this::onRecordProject)
        .onMessage(Terminate.class, this::onTerminate)
        .onSignal(PostStop.class, signal -> postStop())
        .build();
  }

  private Behavior<ProjectMessage> onRequestProject(RequestProject msg) {
    msg.replyTo.tell(new RespondProject(projectId, projectStatus));
    return this;
  }

  private Behavior<ProjectMessage> onRecordProject(RecordProject msg) {
    context.getLog().info("Recorded project status [{}] to [{}]", projectStatus, msg.projectStatus);
    projectStatus = msg.projectStatus;
    msg.replyTo.tell(new RespondProject(projectId, projectStatus));
    return this;
  }

  private Behavior<ProjectMessage> onTerminate(Terminate msg) {
    context.getLog().info("Initiating project shutdown...");
    return Behaviors.stopped(() -> {
      context.getSystem().log().warning("Project id is [{}] cleanup!", projectId);
    });
  }

  private ProjectActor postStop() {
    context.getLog().info("Project actor [{}] stopped, status is [{}]", projectId, projectStatus);
    return this;
  }
}

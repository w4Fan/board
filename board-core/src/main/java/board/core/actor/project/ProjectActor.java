package board.core.actor.project;

import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import board.core.actor.msg.ProjectMessage;
import board.core.entity.Project;

public class ProjectActor extends AbstractBehavior<ProjectMessage> {

  public static Behavior<ProjectMessage> createBehavior(Project project) {
    return Behaviors.setup(context -> new ProjectActor(context, project));
  }

  private final ActorContext<ProjectMessage> context;
  private final Project project;

  private ProjectActor(ActorContext<ProjectMessage> context, Project project) {
    this.context = context;
    this.project = project;
    context.getLog().info("Project id is [{}] started", project.getId());
  }

  @Override
  public Receive<ProjectMessage> createReceive() {
    return newReceiveBuilder().onSignal(PostStop.class, signal -> postStop()).build();
  }

  private ProjectActor postStop() {
    context.getLog().info("Project id is [{}] stopped", project.getId());
    return this;
  }
}

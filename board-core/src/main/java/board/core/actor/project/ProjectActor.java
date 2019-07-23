package board.core.actor.project;

import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import board.core.actor.msg.ProjectMessage;
import board.core.actor.msg.ReplyMessage.Reply;
import board.core.common.RespondCodeEnum;
import board.core.entity.Project;
import board.core.vo.Respond;

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
    return newReceiveBuilder()
        .onMessage(ProjectMessage.Get.class, this::get)
        .onMessage(ProjectMessage.Delete.class, this::delete)
        .onSignal(PostStop.class, signal -> postStop())
        .build();
  }

  private Behavior<ProjectMessage> get(ProjectMessage.Get msg) {
    msg.replyTo.tell(new Reply(new Respond(RespondCodeEnum.SUCCESS.getCode(), project,
        RespondCodeEnum.SUCCESS.getMessage())));
    return this;
  }

  private Behavior<ProjectMessage> delete(ProjectMessage.Delete msg) {
    context.getLog().info("Initiating project shutdown...");
    msg.replyTo
        .tell(new Reply(new Respond(RespondCodeEnum.SUCCESS.getCode(), null,
            RespondCodeEnum.SUCCESS.getMessage())));
    return Behaviors.stopped(() -> {
      context.getSystem().log().warning("Project id is [{}] cleanup!", project.getId());
    });
  }

  private ProjectActor postStop() {
    context.getSystem().log().warning("Project id is [{}] stopped", project.getId());
    return this;
  }
}

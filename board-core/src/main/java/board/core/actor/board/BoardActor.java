package board.core.actor.board;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import board.core.actor.msg.Message;
import board.core.actor.msg.ProjectMessage;
import board.core.actor.project.ProjectActor;
import board.core.common.RespondCodeEnum;
import board.core.vo.Respond;
import java.util.HashMap;
import java.util.Map;

public class BoardActor extends AbstractBehavior<Message> {

  public static Behavior<Message> createBehavior() {
    return Behaviors.setup(BoardActor::new);
  }

  private final ActorContext<Message> context;
  private Map<Integer, ActorRef<ProjectMessage>> projects = new HashMap<>();

  private BoardActor(ActorContext<Message> context) {
    this.context = context;
    context.getLog().info("Board Application started");
  }

  @Override
  public Receive<Message> createReceive() {
    return newReceiveBuilder()
        .onMessage(ProjectMessage.Create.class, this::createProject)
        .onSignal(PostStop.class, signal -> postStop()).build();
  }

  private Behavior<Message> createProject(ProjectMessage.Create msg) {
    context.getLog().info("Create project id is {}", msg.project.getId());
    ActorRef<ProjectMessage> project = context.spawn(ProjectActor.createBehavior(msg.project),
        "project-" + msg.project.getId());
    projects.put(msg.project.getId(), project);
    msg.replyTo.tell(
        new ProjectMessage.Reply(new Respond(RespondCodeEnum.SUCCESS.getCode(), null, "success")));
    return this;
  }

  private BoardActor postStop() {
    context.getLog().info("Board Application stopped");
    return this;
  }
}

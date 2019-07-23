package board.core.actor.board;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import board.core.actor.msg.BoardMessage.CreateProject;
import board.core.actor.msg.BoardMessage.DeleteProject;
import board.core.actor.msg.BoardMessage.GetProject;
import board.core.actor.msg.Message;
import board.core.actor.msg.ProjectMessage;
import board.core.actor.msg.ReplyMessage.Reply;
import board.core.actor.project.ProjectActor;
import board.core.common.Constant;
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
        .onMessage(GetProject.class, this::getProject)
        .onMessage(CreateProject.class, this::createProject)
        .onMessage(DeleteProject.class, this::deleteProject)
        .onSignal(PostStop.class, signal -> postStop())
        .build();
  }

  private Behavior<Message> getProject(GetProject msg) {
    context.getLog().info("Get project by id [{}]", msg.projectId);
    ActorRef<ProjectMessage> project = projects.get(msg.projectId);
    project.tell(new ProjectMessage.Get(msg.replyTo));
    return this;
  }

  private Behavior<Message> createProject(CreateProject msg) {
    context.getLog().info("Create project is [{}]", msg.project.toString());
    ActorRef<ProjectMessage> project = context.spawn(
        ProjectActor.createBehavior(msg.project),
        Constant.PROJECT_NAME_PREFIX + msg.project.getId()
    );
    context.watch(project);
    projects.put(msg.project.getId(), project);
    msg.replyTo.tell(new Reply(new Respond(RespondCodeEnum.SUCCESS.getCode(), null,
        RespondCodeEnum.SUCCESS.getMessage())));
    return this;
  }

  private Behavior<Message> deleteProject(DeleteProject msg) {
    context.getLog().info("Delete project id is [{}]", msg.projectId);
    ActorRef<ProjectMessage> project = projects.get(msg.projectId);
    project.tell(new ProjectMessage.Delete(msg.replyTo));
    context.unwatch(project);
    projects.remove(msg.projectId);
    return this;
  }

  private BoardActor postStop() {
    context.getSystem().log().warning("Board Application stopped");
    return this;
  }
}

package board.core.controller;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import board.core.actor.msg.BoardMessage;
import board.core.actor.msg.BoardMessage.CreateProject;
import board.core.actor.msg.BoardMessage.DeleteProject;
import board.core.actor.msg.Message;
import board.core.actor.msg.ProjectMessage;
import board.core.actor.msg.ReplyMessage.Reply;
import board.core.actor.service.ActorService;
import board.core.common.RespondCodeEnum;
import board.core.entity.Project;
import board.core.vo.Respond;
import com.sun.corba.se.impl.protocol.giopmsgheaders.ReplyMessage;
import java.time.Duration;
import java.util.concurrent.CompletionStage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class ProjectController {

  @Autowired
  private ActorService actorService;

  private ActorSystem<Void> system = actorService.getSystem();
  private ActorRef<Message> board = actorService.getBoard();
  private Duration duration = Duration.ofSeconds(3);

  @GetMapping("/projects")
  String all() {
    return "test api ...";
  }

  @PostMapping("/projects")
  Respond newProject(@RequestBody Project project) {
    log.info("Create project request: {}", project.toString());
    Respond respond = new Respond(RespondCodeEnum.FAILURE.getCode(), null,
        RespondCodeEnum.FAILURE.getMessage());
    CompletionStage<Message> result = AskPattern.ask(
        board,
        (ActorRef<Message> replyTo) -> new CreateProject(project, replyTo),
        duration,
        system.scheduler()
    );
    result.whenComplete((message, failure) -> {
      if (failure == null) {
        Reply reply = (Reply) message;
        BeanUtils.copyProperties(reply.respond, respond);
      }
    });
    return respond;
  }

  @DeleteMapping("/projects/{id}")
  Respond deleteProject(@PathVariable int id) {
    log.info("Delete project request: {}", id);
    Respond respond = new Respond(RespondCodeEnum.FAILURE.getCode(), null,
        RespondCodeEnum.FAILURE.getMessage());
    CompletionStage<Message> result = AskPattern.ask(
        board,
        (ActorRef<Message> replyTo) -> new DeleteProject(id, replyTo),
        duration,
        system.scheduler()
    );
    result.whenComplete((message, failure) -> {
      if (failure == null) {
        Reply reply = (Reply) message;
        BeanUtils.copyProperties(reply.respond, respond);
      }
    });
    return respond;
  }
}

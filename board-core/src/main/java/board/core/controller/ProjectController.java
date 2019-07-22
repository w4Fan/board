package board.core.controller;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import board.core.actor.msg.Message;
import board.core.actor.msg.ProjectMessage;
import board.core.actor.msg.ProjectMessage.Create;
import board.core.actor.msg.ProjectMessage.Reply;
import board.core.actor.service.ActorService;
import board.core.common.RespondCodeEnum;
import board.core.entity.Project;
import board.core.vo.Respond;
import java.time.Duration;
import java.util.concurrent.CompletionStage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class ProjectController {

  @Autowired
  private ActorService actorService;

  private Duration duration = Duration.ofSeconds(3);

  @GetMapping("/projects")
  String all() {
    return "test api ...";
  }

  @PostMapping("/projects")
  Respond newProject(@RequestBody Project project) {
    log.info("Create project request: {}", project.toString());
    Respond respond = new Respond(RespondCodeEnum.FAILURE.getCode(), null, "");
    ActorSystem<Void> system = actorService.getSystem();
    ActorRef<Message> board = actorService.getBoard();
    CompletionStage<ProjectMessage.Reply> result = AskPattern
        .ask(board, (ActorRef<ProjectMessage.Reply> replyTo) -> new Create(project, replyTo),
            duration, system.scheduler());
    result.whenComplete((reply, failure) -> {
      BeanUtils.copyProperties(reply.respond, respond);
    });
    return respond;
  }
}

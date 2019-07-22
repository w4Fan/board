package board.core.actor.service;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.Behaviors;
import board.core.actor.board.BoardActor;
import board.core.actor.msg.Message;
import board.core.common.Constant;
import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ActorService {

  @Getter
  private ActorSystem<Void> system;

  @Getter
  private ActorRef<Message> board;

  @PostConstruct
  public void initActorSystem() {
    Behavior<Void> app = Behaviors.setup(context -> {
      board = context.spawn(BoardActor.createBehavior(), Constant.APPLICATION_NAME);
      return Behaviors.<Void>receiveSignal((ctx, sig) -> {
        if (sig instanceof Terminated) {
          return Behaviors.stopped();
        } else {
          return Behaviors.unhandled();
        }
      });
    });
    system = ActorSystem.create(app, Constant.SYSTEM_NAME);
  }
}

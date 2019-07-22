package actor;

import static org.junit.Assert.assertEquals;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import board.core.actor.board.BoardActor;
import board.core.actor.msg.Message;
import board.core.actor.msg.ProjectMessage.Create;
import board.core.actor.msg.ProjectMessage.Reply;
import board.core.common.Constant;
import board.core.common.RespondCodeEnum;
import board.core.entity.Project;
import org.junit.ClassRule;
import org.junit.Test;

public class ActorTest {

  @ClassRule
  public static final TestKitJunitResource testKit = new TestKitJunitResource();

  @Test
  public void testActorSystem() {
    int projectId = 1;
    TestProbe<Reply> probe = testKit.createTestProbe(Reply.class);
    ActorRef<Message> board = testKit.spawn(BoardActor.createBehavior(), Constant.APPLICATION_NAME);
    board.tell(new Create(new Project(projectId), probe.getRef()));
    Reply reply = probe.receiveMessage();
    assertEquals(RespondCodeEnum.SUCCESS.getCode(), reply.respond.getCode());
  }
}

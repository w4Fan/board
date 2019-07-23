package actor;

import static org.junit.Assert.assertEquals;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import board.core.actor.board.BoardActor;
import board.core.actor.msg.BoardMessage;
import board.core.actor.msg.BoardMessage.DeleteProject;
import board.core.actor.msg.BoardMessage.GetProject;
import board.core.actor.msg.Message;
import board.core.actor.msg.ReplyMessage.Reply;
import board.core.common.Constant;
import board.core.common.RespondCodeEnum;
import board.core.entity.Project;
import lombok.extern.slf4j.Slf4j;
import org.junit.ClassRule;
import org.junit.Test;

@Slf4j
public class ActorTest {

  @ClassRule
  public static final TestKitJunitResource testKit = new TestKitJunitResource();

  @Test
  public void testActorSystem() {
    int projectId = 1;
    TestProbe<Message> probe = testKit.createTestProbe(Message.class);
    ActorRef<Message> board = testKit.spawn(BoardActor.createBehavior(), Constant.APPLICATION_NAME);
    board.tell(new BoardMessage.CreateProject(new Project(projectId), probe.getRef()));
    Reply createMsg = (Reply) probe.receiveMessage();
    assertEquals(RespondCodeEnum.SUCCESS.getCode(), createMsg.respond.getCode());
    board.tell(new GetProject(projectId, probe.getRef()));
    Reply getMsg = (Reply) probe.receiveMessage();
    Project project = (Project) getMsg.respond.getData();
    assertEquals(projectId, project.getId());
    board.tell(new DeleteProject(projectId, probe.getRef()));
    Reply deleteMsg = (Reply) probe.receiveMessage();
    assertEquals(RespondCodeEnum.SUCCESS.getCode(), deleteMsg.respond.getCode());
  }
}

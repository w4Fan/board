package board.actor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import board.actor.project.ProjectActor;
import board.actor.project.ProjectManagerActor;
import board.actor.protocol.ProjectManagerProtocol.ProjectManagerMessage;
import board.actor.protocol.ProjectManagerProtocol.ProjectRegistered;
import board.actor.protocol.ProjectManagerProtocol.RequestTrackProject;
import board.actor.protocol.ProjectProtocol.ProjectMessage;
import board.actor.protocol.ProjectProtocol.RecordProject;
import board.actor.protocol.ProjectProtocol.RequestProject;
import board.actor.protocol.ProjectProtocol.RespondProject;
import board.actor.protocol.ProjectProtocol.Terminate;
import board.actor.protocol.ProjectQueryProtocol.ProjectReading;
import board.actor.protocol.ProjectQueryProtocol.ProjectRecord;
import board.actor.protocol.ProjectQueryProtocol.RequestAllProject;
import board.actor.protocol.ProjectQueryProtocol.RespondAllProject;
import board.common.project.ProjectStatus;
import java.util.Map;
import org.junit.ClassRule;
import org.junit.Test;

public class TestProjectActor {

  @ClassRule
  public static final TestKitJunitResource testKit = new TestKitJunitResource();

  @Test
  public void testProject() {
    String projectId = "test";
    RespondProject respond;
    TestProbe<RespondProject> probe = testKit.createTestProbe(RespondProject.class);
    ActorRef<ProjectMessage> project = testKit
        .spawn(ProjectActor.createBehavior(projectId, ProjectStatus.CONNECTED), projectId);
    project.tell(new RequestProject(probe.getRef()));
    respond = probe.receiveMessage();
    assertEquals(projectId, respond.projectId);
    respond = null;
    project.tell(new RecordProject(ProjectStatus.DISCONNECTED, probe.getRef()));
    respond = probe.receiveMessage();
    assertEquals(ProjectStatus.DISCONNECTED, respond.projectStatus);
    respond = null;
    project.tell(Terminate.INSTANCE);
    probe.expectTerminated(project, probe.getRemainingOrDefault());
  }

  @Test
  public void testProjectManage() {
    TestProbe<ProjectRegistered> probe = testKit.createTestProbe(ProjectRegistered.class);
    ActorRef<ProjectManagerMessage> projectManagerActor = testKit
        .spawn(ProjectManagerActor.createBehavior(), "project");
    projectManagerActor.tell(new RequestTrackProject("test1", probe.getRef()));
    ProjectRegistered registered1 = probe.receiveMessage();
    projectManagerActor.tell(new RequestTrackProject("test2", probe.getRef()));
    ProjectRegistered registered2 = probe.receiveMessage();
    projectManagerActor.tell(new RequestTrackProject("test1", probe.getRef()));
    ProjectRegistered registered3 = probe.receiveMessage();
    assertNotEquals(registered1.project, registered2.project);
    assertEquals(registered1.project, registered3.project);

    TestProbe<RespondProject> recordProbe = testKit.createTestProbe(RespondProject.class);
    registered1.project.tell(new RecordProject(ProjectStatus.DISCONNECTED, recordProbe.getRef()));
    assertEquals(ProjectStatus.DISCONNECTED, recordProbe.receiveMessage().projectStatus);
    registered2.project.tell(new RecordProject(ProjectStatus.DISCONNECTED, recordProbe.getRef()));
    assertEquals(ProjectStatus.DISCONNECTED, recordProbe.receiveMessage().projectStatus);
  }

  @Test
  public void testProjectQuery() {
    TestProbe<ProjectRegistered> probe = testKit.createTestProbe(ProjectRegistered.class);
    ActorRef<ProjectManagerMessage> projectManagerActor = testKit
        .spawn(ProjectManagerActor.createBehavior(), "project-query");
    projectManagerActor.tell(new RequestTrackProject("test1", probe.getRef()));
    projectManagerActor.tell(new RequestTrackProject("test2", probe.getRef()));
    projectManagerActor.tell(new RequestTrackProject("test3", probe.getRef()));

    TestProbe<RespondAllProject> requester = testKit.createTestProbe(RespondAllProject.class);
    projectManagerActor.tell(new RequestAllProject(requester.getRef()));
    Map<String, ProjectReading> projects = requester.receiveMessage().projects;
    projects.forEach((projectId, projectReading) -> {
      ProjectRecord record = (ProjectRecord) projectReading;
      System.out.println("projectId is " + projectId);
      System.out.println("projectId is " + record.projectStatus);
    });
    assertEquals(3, projects.size());
  }
}

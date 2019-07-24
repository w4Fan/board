package board.actor.project;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.javadsl.TimerScheduler;
import board.actor.protocol.ProjectProtocol.ProjectMessage;
import board.actor.protocol.ProjectProtocol.RequestProject;
import board.actor.protocol.ProjectProtocol.RespondProject;
import board.actor.protocol.ProjectQueryProtocol.ProjectNotAvailable;
import board.actor.protocol.ProjectQueryProtocol.ProjectQueryMessage;
import board.actor.protocol.ProjectQueryProtocol.ProjectReading;
import board.actor.protocol.ProjectQueryProtocol.ProjectRecord;
import board.actor.protocol.ProjectQueryProtocol.ProjectTimedOut;
import board.actor.protocol.ProjectQueryProtocol.RespondAllProject;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ProjectQueryActor extends AbstractBehavior<ProjectQueryMessage> {

  public static Behavior<ProjectQueryMessage> createBehavior(
      Map<String, ActorRef<ProjectMessage>> projectIdToActor,
      ActorRef<RespondAllProject> requester,
      Duration timeout) {
    return Behaviors.setup(context -> Behaviors.withTimers(
        timers -> new ProjectQueryActor(projectIdToActor, requester, timeout, context, timers)));
  }

  private static enum CollectionTimeout implements ProjectQueryMessage {
    INSTANCE
  }

  public static class WrappedRespondProjectRecord implements ProjectQueryMessage {

    final RespondProject response;

    public WrappedRespondProjectRecord(RespondProject response) {
      this.response = response;
    }
  }

  public static class ProjectTerminated implements ProjectQueryMessage {

    final String projectId;

    public ProjectTerminated(String projectId) {
      this.projectId = projectId;
    }
  }

  private final ActorRef<RespondAllProject> requester;
  private Map<String, ProjectReading> repliesSoFar = new HashMap<>();
  private final Set<String> stillWaiting;

  private ProjectQueryActor(
      Map<String, ActorRef<ProjectMessage>> projectIdToActor,
      ActorRef<RespondAllProject> requester,
      Duration timeout,
      ActorContext<ProjectQueryMessage> context,
      TimerScheduler<ProjectQueryMessage> timers
  ) {
    this.requester = requester;
    timers.startSingleTimer(CollectionTimeout.class, CollectionTimeout.INSTANCE, timeout);

    ActorRef<RespondProject> respondProjectRecordAdapter =
        context.messageAdapter(RespondProject.class, WrappedRespondProjectRecord::new);

    for (Map.Entry<String, ActorRef<ProjectMessage>> entry : projectIdToActor.entrySet()) {
      context.watchWith(entry.getValue(), new ProjectTerminated(entry.getKey()));
      entry.getValue().tell(new RequestProject(respondProjectRecordAdapter));
    }
    stillWaiting = new HashSet<>(projectIdToActor.keySet());
  }

  @Override
  public Receive<ProjectQueryMessage> createReceive() {
    return newReceiveBuilder()
        .onMessage(WrappedRespondProjectRecord.class, this::onRespondProjectRecord)
        .onMessage(ProjectTerminated.class, this::onDeviceTerminated)
        .onMessage(CollectionTimeout.class, this::onCollectionTimeout)
        .build();
  }

  private Behavior<ProjectQueryMessage> onRespondProjectRecord(WrappedRespondProjectRecord msg) {
    String projectId = msg.response.projectId;
    ProjectReading reading = new ProjectRecord(msg.response.projectId, msg.response.projectStatus);
    repliesSoFar.put(projectId, reading);
    stillWaiting.remove(projectId);
    return respondWhenAllCollected();
  }

  private Behavior<ProjectQueryMessage> onDeviceTerminated(ProjectTerminated terminated) {
    if (stillWaiting.contains(terminated.projectId)) {
      repliesSoFar.put(terminated.projectId, ProjectNotAvailable.INSTANCE);
      stillWaiting.remove(terminated.projectId);
    }
    return respondWhenAllCollected();
  }

  private Behavior<ProjectQueryMessage> onCollectionTimeout(CollectionTimeout timeout) {
    for (String deviceId : stillWaiting) {
      repliesSoFar.put(deviceId, ProjectTimedOut.INSTANCE);
    }
    stillWaiting.clear();
    return respondWhenAllCollected();
  }

  private Behavior<ProjectQueryMessage> respondWhenAllCollected() {
    if (stillWaiting.isEmpty()) {
      requester.tell(new RespondAllProject(repliesSoFar));
      return Behaviors.stopped();
    } else {
      return this;
    }
  }
}

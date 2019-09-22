import akka.actor.*;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Sender {
    public static class Actor extends AbstractActor {
        long counter;

        private enum PrintCounter {Instance}

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .match(PrintCounter.class, __ -> System.out.println(counter))
                    .matchAny(__ -> counter++)
                    .build();
        }
    }

    public static void main(String[] args) throws TimeoutException, InterruptedException {
        final ActorSystem actorSystem = ActorSystem.apply();
        final ActorRef server = Await.result(actorSystem.actorSelection(
                args.length > 0 ? args[0] : "akka://default@127.0.0.1:50446/user/receiver"
        ).resolveOne(Timeout.apply(21474835, TimeUnit.SECONDS)), Duration.Inf());
        final ActorRef receiver = actorSystem.actorOf(Props.create(Actor.class, Actor::new));
        final long nanos = args.length > 1 ? Long.parseLong(args[1]) : 100000;
        new ScheduledThreadPoolExecutor(1).scheduleAtFixedRate(
                () -> server.tell("tick", receiver),
                0,
                nanos,
                TimeUnit.NANOSECONDS
        );
        actorSystem.scheduler().schedule(
                java.time.Duration.ZERO,
                java.time.Duration.ofSeconds(1),
                receiver,
                Actor.PrintCounter.Instance,
                actorSystem.dispatcher(),
                null
        );
    }
}

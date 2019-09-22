import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;

import java.time.Duration;

public class Receiver {
    static class Actor extends AbstractActor {
        long counter;

        private enum PrintCounter {Instance}

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .match(PrintCounter.class, __ -> System.out.println(counter))
                    .matchAny(any -> {
                        counter++;
                        sender().tell(any, self());
                    })
                    .build();
        }
    }

    public static void main(String[] args) {
        final ActorSystem actorSystem = ActorSystem.apply(
                "default",
                ConfigFactory.defaultApplication()
                        .withValue("akka.remote.artery.canonical.port", ConfigValueFactory.fromAnyRef(50446))
        );
        final ActorRef server = actorSystem.actorOf(Props.create(Actor.class, Actor::new), "receiver");
        actorSystem.scheduler().schedule(
                Duration.ZERO,
                Duration.ofSeconds(1),
                server,
                Actor.PrintCounter.Instance,
                actorSystem.dispatcher(),
                null
        );
    }
}

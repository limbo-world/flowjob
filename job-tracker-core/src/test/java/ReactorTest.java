import org.junit.Test;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ReplayProcessor;

/**
 * @author Brozen
 * @since 2021-05-24
 */
public class ReactorTest {

    @Test
    public void testReplayProcessor() {
        ReplayProcessor<Integer> processor = ReplayProcessor.create();

        Mono.create(sink -> processor
                .filter(i -> i % 2 == 0)
                .subscribe(sink::success, sink::error, sink::success)
        ).subscribe(System.out::println);

        Mono.create(sink -> processor
                .filter(i -> i % 2 == 1)
                .subscribe(sink::success, sink::error, sink::success)
        ).subscribe(System.out::println);

        for (int i = 0; i < 10; i++) {
            processor.onNext(i);
        }
    }

}

package decoding;

import me.legrange.typelink.QueryPredicate1;
import me.legrange.typelink.lambda.parser.LambdaParser;
import org.junit.jupiter.api.Test;
import rec.Invoice;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Decoding a lambda is safe from many threads at once, and holds on to nothing about the lambdas it has decoded.
 *
 * <p><b>The bugs these guard.</b> The decoder kept what it learned in plain static {@code HashMap}s, filled with a
 * check, then a put, then a get. Under concurrency a get could miss the entry that thread had just put, and decoding
 * failed with a {@code NullPointerException} ("lambda" is null) - sporadically, so a service would see it on random
 * requests. The map of lambdas was also keyed by the lambda itself: a lambda that captures something is a new object each
 * time the query is built, so every query added an entry that was never removed, together with what it captured.
 */
class Test_7000_ConcurrentDecoding {

    private static final int THREADS = 16;
    private static final int PER_THREAD = 3000;

    private static QueryPredicate1<Invoice> above(int id, long limit) {
        return i -> i.id() > id && i.amount() > limit;
    }

    @Test
    void manyThreadsDecodeCapturingLambdasAtTheSameTime() throws Exception {
        var barrier = new CyclicBarrier(THREADS);
        var pool = Executors.newFixedThreadPool(THREADS);
        try {
            var futures = new ArrayList<java.util.concurrent.Future<?>>();
            for (var t = 0; t < THREADS; t++) {
                var thread = t;
                futures.add(pool.submit(() -> {
                    barrier.await();
                    for (var n = 0; n < PER_THREAD; n++) {
                        var id = thread * PER_THREAD + n;
                        var lambda = LambdaParser.parse(above(id, 5L));
                        assertEquals(List.of(id, 5L), lambda.arguments(), "the values this lambda captured, not another thread's");
                    }
                    return null;
                }));
            }
            for (var f : futures) {
                f.get();
            }
        } finally {
            pool.shutdownNow();
        }
    }

    /** Decoding a lambda must not keep the lambda, nor what it captured, alive */
    @Test
    void decodingDoesNotKeepTheLambdaOrItsCapturesAlive() throws Exception {
        var captured = new Object[]{new byte[1024]};
        var weak = new WeakReference<Object>(captured[0]);
        decodeCapturing(captured[0]);
        captured[0] = null;
        for (var i = 0; i < 20 && weak.get() != null; i++) {
            System.gc();
            Thread.sleep(50);
        }
        assertNull(weak.get(), "something still holds what the decoded lambda captured");
    }

    private static void decodeCapturing(Object held) {
        QueryPredicate1<Invoice> predicate = i -> held != null && i.id() > 0;
        LambdaParser.parse(predicate);
    }
}

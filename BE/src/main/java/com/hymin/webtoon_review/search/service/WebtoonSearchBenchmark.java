package com.hymin.webtoon_review.search.service;

import com.hymin.webtoon_review.WebtoonReviewApplication;
import com.hymin.webtoon_review.search.repository.WebtoonSearchJpaRepository;
import com.hymin.webtoon_review.search.repository.WebtoonSearchRepository;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class WebtoonSearchBenchmark {

    // @Autowired 대신 수동으로 주입받을 필드 선언
    private WebtoonSearchJpaRepository webtoonSearchJpaRepository;
    private WebtoonSearchRepository webtoonSearchRepository;

    // Spring Boot Context를 담을 변수
    private ConfigurableApplicationContext context;

    private int page;
    private int offset;
    private int[] pages = {1, 2, 3, 4, 5};
    private int index;

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.runner.options.Options opt = new org.openjdk.jmh.runner.options.OptionsBuilder()
            .include(WebtoonSearchBenchmark.class.getSimpleName())
            .build();

        new org.openjdk.jmh.runner.Runner(opt).run();
    }

    @Setup(Level.Trial)
    public void setup() {
        context = new SpringApplicationBuilder(WebtoonReviewApplication.class)
            .run();

        webtoonSearchJpaRepository = context.getBean(WebtoonSearchJpaRepository.class);
        webtoonSearchRepository = context.getBean(WebtoonSearchRepository.class);
    }

    @Setup(Level.Iteration)
    public void setupIteration() {
        page = pages[index++ % pages.length];
        offset = (page - 1) * 20;
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        context.close();
    }

    @Benchmark
    public void measureJpaNative(Blackhole bh) {
        bh.consume(webtoonSearchJpaRepository.findWebtoonIds("레벨업", offset, 20));
    }

    @Benchmark
    public void measureJpaNativeWithEntity(Blackhole bh) {
        bh.consume(webtoonSearchJpaRepository.findWebtoonSearch("레벨업", offset, 20));
    }

    @Benchmark
    public void measureJdbcTemplate(Blackhole bh) {
        bh.consume(webtoonSearchRepository.findWebtoonByQuery("레벨업", page));
    }
}
package com.hymin.webtoon_review.admin.service;

import com.hymin.webtoon_review.webtoon.entity.Author;
import com.hymin.webtoon_review.webtoon.entity.DayOfWeek;
import com.hymin.webtoon_review.webtoon.entity.Genre;
import com.hymin.webtoon_review.webtoon.entity.Platform;
import com.hymin.webtoon_review.webtoon.entity.Webtoon;
import com.hymin.webtoon_review.webtoon.entity.WebtoonAuthor;
import com.hymin.webtoon_review.webtoon.entity.WebtoonDayOfWeek;
import com.hymin.webtoon_review.webtoon.entity.WebtoonGenre;
import com.hymin.webtoon_review.webtoon.entity.enums.Status;
import com.hymin.webtoon_review.webtoon.repository.AuthorRepository;
import com.hymin.webtoon_review.webtoon.repository.DayOfWeekRepository;
import com.hymin.webtoon_review.webtoon.repository.GenreRepository;
import com.hymin.webtoon_review.webtoon.repository.PlatformRepository;
import com.hymin.webtoon_review.webtoon.repository.WebtoonAuthorRepository;
import com.hymin.webtoon_review.webtoon.repository.WebtoonDayOfWeekRepository;
import com.hymin.webtoon_review.webtoon.repository.WebtoonGenreRepository;
import com.hymin.webtoon_review.webtoon.repository.WebtoonRepository;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AuthorRepository authorRepository;
    private final GenreRepository genreRepository;
    private final PlatformRepository platformRepository;
    private final DayOfWeekRepository dayOfWeekRepository;
    private final WebtoonRepository webtoonRepository;
    private final WebtoonAuthorRepository webtoonAuthorRepository;
    private final WebtoonGenreRepository webtoonGenreRepository;
    private final WebtoonDayOfWeekRepository webtoonDayOfWeekRepository;

    @Transactional
    public void dataGenerator(Integer webtoonSize, Integer authorSize) {
        Integer WebtoonSizeOfAuthor = webtoonSize / authorSize;

        List<Genre> genres = genreRepository.findAll();
        List<Platform> platforms = platformRepository.findAll();
        List<DayOfWeek> dayOfWeeks = dayOfWeekRepository.findAll();

        for (int i = 1; i <= authorSize; i++) {
            Author author = Author.builder()
                .name("작가" + i)
                .build();

            authorRepository.save(author);
            for (int k = 0; k < WebtoonSizeOfAuthor; k++) {
                Random random = new Random();
                random.setSeed(System.currentTimeMillis());
                Integer randomStauts = random.nextInt(10);

                Status status;
                Platform randomPlatform = platforms.get(random.nextInt(platforms.size()));

                if (randomStauts < 5) {
                    status = Status.ONGOING;
                } else if (randomStauts < 7) {
                    status = Status.PAUSED;
                } else {
                    status = Status.COMPLETED;
                }

                Webtoon webtoon = Webtoon.builder()
                    .name("웹툰" + i + k)
                    .description("웹툰 설명설명설명설명설명설명설명설명설명설명설명설명설명설명설명설명설명설명설명설명")
                    .thumbnail("썸네일")
                    .status(status)
                    .platform(randomPlatform)
                    .build();

                List<WebtoonDayOfWeek> randomDayOfWeek = IntStream
                    .generate(() -> random.nextInt(7))
                    .distinct()
                    .limit(random.nextLong(7) + 1)
                    .boxed()
                    .map((index) -> WebtoonDayOfWeek.builder()
                        .webtoon(webtoon)
                        .dayOfWeek(dayOfWeeks.get(index))
                        .build())
                    .toList();

                List<WebtoonGenre> randomGenre = IntStream
                    .generate(() -> random.nextInt(18))
                    .distinct()
                    .limit(random.nextLong(5) + 3)
                    .boxed()
                    .map((index) -> WebtoonGenre.builder()
                        .webtoon(webtoon)
                        .genre(genres.get(index))
                        .build())
                    .toList();

                WebtoonAuthor webtoonAuthor = WebtoonAuthor.builder()
                    .author(author)
                    .webtoon(webtoon)
                    .build();

                webtoonRepository.save(webtoon);
                webtoonDayOfWeekRepository.saveAll(randomDayOfWeek);
                webtoonGenreRepository.saveAll(randomGenre);
                webtoonAuthorRepository.save(webtoonAuthor);
            }
        }
    }
}

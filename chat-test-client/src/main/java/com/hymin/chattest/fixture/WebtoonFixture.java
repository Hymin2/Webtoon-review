package com.hymin.chattest.fixture;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class WebtoonFixture {

    private static final String FIND_WEBTOON_SQL = """
        SELECT id
        FROM webtoon
        WHERE name = ?
        ORDER BY id
        LIMIT 1
        """;

    private static final String INSERT_WEBTOON_SQL = """
        INSERT INTO webtoon (
            name,
            description,
            thumbnail,
            episode_count,
            views,
            recommendation_count,
            comment_count,
            authors,
            day_of_weeks,
            genres,
            created_at,
            updated_at
        ) VALUES (?, ?, ?, 0, 0, 0, 0, ?, ?, ?, NOW(), NOW())
        """;

    private final WebtoonFixtureProperties properties;

    public WebtoonFixture(WebtoonFixtureProperties properties) {
        this.properties = properties;
    }

    public long ensureWebtoon() {
        try (Connection connection = DriverManager.getConnection(
            properties.databaseUrl(),
            properties.databaseUsername(),
            properties.databasePassword()
        )) {
            Long webtoonId = findWebtoonId(connection);
            return webtoonId != null ? webtoonId : insertWebtoon(connection);
        } catch (SQLException exception) {
            throw new IllegalStateException("테스트용 웹툰 데이터 준비에 실패했습니다.", exception);
        }
    }

    private Long findWebtoonId(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(FIND_WEBTOON_SQL)) {
            statement.setString(1, properties.webtoonName());

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getLong("id") : null;
            }
        }
    }

    private long insertWebtoon(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
            INSERT_WEBTOON_SQL,
            Statement.RETURN_GENERATED_KEYS
        )) {
            statement.setString(1, properties.webtoonName());
            statement.setString(2, "채팅 E2E 테스트를 위한 웹툰 데이터입니다.");
            statement.setString(3, "https://example.com/chat-test-webtoon.png");
            statement.setString(4, "chat-test");
            statement.setString(5, "TEST");
            statement.setString(6, "TEST");
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }
            }
        }
        throw new SQLException("생성된 웹툰 ID를 확인할 수 없습니다.");
    }
}

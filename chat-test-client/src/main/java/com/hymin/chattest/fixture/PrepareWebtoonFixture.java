package com.hymin.chattest.fixture;

public class PrepareWebtoonFixture {

    public static void main(String[] args) {
        WebtoonFixtureProperties properties = WebtoonFixtureProperties.fromEnvironment();
        long webtoonId = new WebtoonFixture(properties).ensureWebtoon();

        System.out.printf(
            "테스트용 웹툰 준비 완료: id=%d, name=%s%n",
            webtoonId,
            properties.webtoonName()
        );
    }
}

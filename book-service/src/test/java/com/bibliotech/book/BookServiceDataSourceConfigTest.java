package com.bibliotech.book;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class BookServiceDataSourceConfigTest {

    @Test
    void defaultDatasourceUsesPostgreSql() throws Exception {
        String content = Files.readString(Path.of("src/main/resources/application.yml"));

        assertThat(content)
            .contains("bibliotech_books")
            .contains("org.postgresql.Driver");
    }
}

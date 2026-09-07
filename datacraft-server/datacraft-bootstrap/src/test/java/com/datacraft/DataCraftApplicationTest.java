package com.datacraft;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.assertj.core.api.Assertions.assertThat;

class DataCraftApplicationTest {
    @Test
    void isSpringBootEntryPoint() {
        assertThat(DataCraftApplication.class).hasAnnotation(SpringBootApplication.class);
    }
}


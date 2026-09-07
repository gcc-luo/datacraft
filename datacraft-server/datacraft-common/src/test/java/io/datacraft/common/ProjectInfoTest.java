package io.datacraft.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectInfoTest {
    @Test
    void exposesProjectIdentity() {
        assertThat(ProjectInfo.NAME).isEqualTo("DataCraft");
        assertThat(ProjectInfo.VERSION).startsWith("0.1.0");
    }
}


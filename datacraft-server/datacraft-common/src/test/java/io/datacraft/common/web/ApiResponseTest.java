package io.datacraft.common.web;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    @Test
    void createsSuccessfulResponseWithStableEnvelope() {
        ApiResponse<String> response = ApiResponse.success("ok");

        assertThat(response.code()).isEqualTo("0");
        assertThat(response.message()).isEqualTo("success");
        assertThat(response.data()).isEqualTo("ok");
    }

    @Test
    void createsFailureResponseWithoutPayload() {
        ApiResponse<Void> response = ApiResponse.failure("AUTH_INVALID", "用户名或密码错误");

        assertThat(response.code()).isEqualTo("AUTH_INVALID");
        assertThat(response.message()).isEqualTo("用户名或密码错误");
        assertThat(response.data()).isNull();
    }
}

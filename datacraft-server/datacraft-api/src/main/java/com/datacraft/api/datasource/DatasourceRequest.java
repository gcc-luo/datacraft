package com.datacraft.api.datasource;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DatasourceRequest(
        @NotBlank(message = "数据源名称不能为空") @Size(max = 100, message = "数据源名称不能超过 100 个字符") String name,
        @NotNull(message = "数据源类型不能为空") DatasourceType type,
        @NotBlank(message = "主机不能为空") @Size(max = 255, message = "主机不能超过 255 个字符") String host,
        @NotNull(message = "端口不能为空") @Min(value = 1, message = "端口必须在 1-65535 之间") @Max(value = 65535, message = "端口必须在 1-65535 之间") Integer port,
        @NotBlank(message = "数据库不能为空") @Size(max = 255, message = "数据库不能超过 255 个字符") String databaseName,
        @NotBlank(message = "用户名不能为空") @Size(max = 255, message = "用户名不能超过 255 个字符") String username,
        @Size(max = 512, message = "密码不能超过 512 个字符") String password,
        @Size(max = 500, message = "备注不能超过 500 个字符") String remark
) {
}

package io.datacraft.datasource.application;

import io.datacraft.datasource.domain.Datasource;
import io.datacraft.datasource.domain.DatasourceRepository;
import io.datacraft.datasource.domain.JdbcConnection;
import io.datacraft.datasource.domain.JdbcConnectionProvider;
import io.datacraft.datasource.security.SecretCryptoService;
import org.springframework.stereotype.Component;

import java.sql.DriverManager;
import java.sql.SQLException;

@Component
public class JdbcDatasourceConnectionProvider implements JdbcConnectionProvider {
    private final DatasourceRepository repository;
    private final SecretCryptoService crypto;
    private final JdbcUrlBuilder urlBuilder;

    public JdbcDatasourceConnectionProvider(DatasourceRepository repository, SecretCryptoService crypto,
                                            JdbcUrlBuilder urlBuilder) {
        this.repository = repository;
        this.crypto = crypto;
        this.urlBuilder = urlBuilder;
    }

    @Override
    public JdbcConnection open(Long datasourceId) {
        Datasource datasource = repository.findById(datasourceId)
                .orElseThrow(() -> new DatasourceNotFoundException(datasourceId));
        try {
            String password = crypto.decrypt(datasource.passwordCiphertext());
            return new JdbcConnection(datasource.type(), DriverManager.getConnection(
                    urlBuilder.build(datasource), datasource.username(), password));
        } catch (SQLException exception) {
            throw new IllegalStateException("数据源连接失败: " + datasourceId, exception);
        }
    }
}

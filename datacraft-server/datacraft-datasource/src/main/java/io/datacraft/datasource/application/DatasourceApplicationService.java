package io.datacraft.datasource.application;

import io.datacraft.api.datasource.DatasourceRequest;
import io.datacraft.api.datasource.DatasourceResponse;
import io.datacraft.api.datasource.DatasourceStatus;
import io.datacraft.api.datasource.DatasourceTestResponse;
import io.datacraft.datasource.domain.ConnectionTestOutcome;
import io.datacraft.datasource.domain.Datasource;
import io.datacraft.datasource.domain.DatasourceConnectionTester;
import io.datacraft.datasource.domain.DatasourceRepository;
import io.datacraft.datasource.security.SecretCryptoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
public class DatasourceApplicationService {
    private static final String SUCCESS_MESSAGE = "连接成功";
    private static final String FAILURE_MESSAGE = "连接失败，请检查配置与网络";

    private final DatasourceRepository repository;
    private final SecretCryptoService crypto;
    private final DatasourceConnectionTester connectionTester;
    private final Clock clock;

    @Autowired
    public DatasourceApplicationService(DatasourceRepository repository, SecretCryptoService crypto,
                                        DatasourceConnectionTester connectionTester) {
        this(repository, crypto, connectionTester, Clock.systemUTC());
    }

    public DatasourceApplicationService(DatasourceRepository repository, SecretCryptoService crypto,
                                        DatasourceConnectionTester connectionTester, Clock clock) {
        this.repository = repository;
        this.crypto = crypto;
        this.connectionTester = connectionTester;
        this.clock = clock;
    }

    public List<DatasourceResponse> list() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public DatasourceResponse get(Long id) {
        return toResponse(require(id));
    }

    public DatasourceResponse create(DatasourceRequest request) {
        String name = clean(request.name());
        if (repository.existsByName(name, null)) {
            throw new DatasourceDuplicateException(name);
        }
        if (request.password() == null || request.password().isBlank()) {
            throw new DatasourceValidationException("数据源密码不能为空");
        }
        Instant now = Instant.now(clock);
        Datasource datasource = new Datasource(null, name, request.type(), clean(request.host()), request.port(),
                clean(request.databaseName()), clean(request.username()), crypto.encrypt(request.password()), cleanNullable(request.remark()),
                DatasourceStatus.UNKNOWN, null, null, null, now, now);
        return toResponse(repository.save(datasource));
    }

    public DatasourceResponse update(Long id, DatasourceRequest request) {
        Datasource current = require(id);
        String name = clean(request.name());
        if (repository.existsByName(name, id)) {
            throw new DatasourceDuplicateException(name);
        }
        String passwordCiphertext = request.password() == null || request.password().isBlank()
                ? current.passwordCiphertext() : crypto.encrypt(request.password());
        Datasource updated = new Datasource(id, name, request.type(), clean(request.host()), request.port(),
                clean(request.databaseName()), clean(request.username()), passwordCiphertext, cleanNullable(request.remark()),
                current.status(), current.lastTestedAt(), current.lastTestLatencyMs(), current.lastTestMessage(),
                current.createdAt(), Instant.now(clock));
        return toResponse(repository.save(updated));
    }

    public void delete(Long id) {
        require(id);
        repository.deleteById(id);
    }

    public DatasourceTestResponse testConnection(Long id) {
        Datasource current = require(id);
        ConnectionTestOutcome outcome = connectionTester.test(current, crypto.decrypt(current.passwordCiphertext()));
        Instant testedAt = Instant.now(clock);
        DatasourceStatus status = outcome.success() ? DatasourceStatus.SUCCESS : DatasourceStatus.FAILED;
        String message = outcome.success() ? SUCCESS_MESSAGE : FAILURE_MESSAGE;
        repository.save(current.withTestResult(status, testedAt, outcome.latencyMs(), message));
        return new DatasourceTestResponse(outcome.success(), status, outcome.latencyMs(), message, testedAt);
    }

    private Datasource require(Long id) {
        return repository.findById(id).orElseThrow(() -> new DatasourceNotFoundException(id));
    }

    private DatasourceResponse toResponse(Datasource datasource) {
        return new DatasourceResponse(datasource.id(), datasource.name(), datasource.type(), datasource.host(), datasource.port(),
                datasource.databaseName(), datasource.username(), datasource.remark(), datasource.status(), datasource.lastTestedAt(),
                datasource.lastTestLatencyMs(), datasource.lastTestMessage(), datasource.createdAt(), datasource.updatedAt());
    }

    private String clean(String value) {
        return value == null ? null : value.trim();
    }

    private String cleanNullable(String value) {
        String cleaned = clean(value);
        return cleaned == null || cleaned.isBlank() ? null : cleaned;
    }
}

package com.datacraft.datasource.application;

import com.datacraft.api.datasource.DatasourceRequest;
import com.datacraft.api.datasource.DatasourceResponse;
import com.datacraft.api.datasource.DatasourceStatus;
import com.datacraft.api.datasource.DatasourceTestResponse;
import com.datacraft.datasource.domain.ConnectionTestOutcome;
import com.datacraft.datasource.domain.Datasource;
import com.datacraft.datasource.domain.DatasourceConnectionTester;
import com.datacraft.datasource.domain.DatasourceRepository;
import com.datacraft.datasource.security.SecretCryptoService;
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
        DatasourceTestResponse result = testConnection(current, crypto.decrypt(current.passwordCiphertext()));
        repository.save(current.withTestResult(result.status(), result.testedAt(), result.latencyMs(), result.message()));
        return result;
    }

    public DatasourceTestResponse testConnection(DatasourceRequest request) {
        String password = requiredPassword(request.password());
        return testConnection(toCandidate(request, null), password);
    }

    public DatasourceTestResponse testConnection(Long id, DatasourceRequest request) {
        if (request == null) {
            return testConnection(id);
        }
        Datasource current = require(id);
        String password = request.password() == null || request.password().isBlank()
                ? crypto.decrypt(current.passwordCiphertext()) : request.password();
        return testConnection(toCandidate(request, current), password);
    }

    private DatasourceTestResponse testConnection(Datasource datasource, String password) {
        ConnectionTestOutcome outcome = connectionTester.test(datasource, password);
        Instant testedAt = Instant.now(clock);
        DatasourceStatus status = outcome.success() ? DatasourceStatus.SUCCESS : DatasourceStatus.FAILED;
        String message = outcome.success() ? SUCCESS_MESSAGE : FAILURE_MESSAGE;
        return new DatasourceTestResponse(outcome.success(), status, outcome.latencyMs(), message, testedAt);
    }

    private Datasource toCandidate(DatasourceRequest request, Datasource current) {
        Instant createdAt = current == null ? Instant.now(clock) : current.createdAt();
        return new Datasource(current == null ? null : current.id(), clean(request.name()), request.type(), clean(request.host()),
                request.port(), clean(request.databaseName()), clean(request.username()),
                current == null ? null : current.passwordCiphertext(), cleanNullable(request.remark()),
                current == null ? DatasourceStatus.UNKNOWN : current.status(),
                current == null ? null : current.lastTestedAt(), current == null ? null : current.lastTestLatencyMs(),
                current == null ? null : current.lastTestMessage(), createdAt, current == null ? createdAt : current.updatedAt());
    }

    private String requiredPassword(String password) {
        if (password == null || password.isBlank()) {
            throw new DatasourceValidationException("数据源密码不能为空");
        }
        return password;
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

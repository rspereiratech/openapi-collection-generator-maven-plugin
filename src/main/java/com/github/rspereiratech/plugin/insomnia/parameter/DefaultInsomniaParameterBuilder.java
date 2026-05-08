package com.github.rspereiratech.plugin.insomnia.parameter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.rspereiratech.plugin.insomnia.model.InsomniaParameter;
import com.github.rspereiratech.plugin.core.security.applier.SecurityApplier;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;

/**
 * Default implementation of {@link InsomniaParameterBuilder} that extracts query parameters
 * from the OpenAPI operation and merges them with security-injected query parameters.
 */
public class DefaultInsomniaParameterBuilder implements InsomniaParameterBuilder {

    private final SecurityApplier sec;

    /**
     * Constructs a new parameter builder.
     *
     * @param sec the security applier for injecting authentication query parameters
     */
    public DefaultInsomniaParameterBuilder(SecurityApplier sec) {
        this.sec = sec;
    }

    @Override
    public List<InsomniaParameter> build(Operation op, OpenAPI openApi) {
        var inj = sec.apply(op, openApi);
        return Stream.concat(
            Optional.ofNullable(op.getParameters()).orElse(List.of()).stream()
                .filter(p -> "query".equals(p.getIn()))
                .map(p -> new InsomniaParameter(
                    p.getName(), "",
                    Optional.ofNullable(p.getDescription()).orElse(""))),
            inj.queryParams().stream()
                .map(q -> new InsomniaParameter(q.name(), q.value(), "security"))
        ).collect(Collectors.toUnmodifiableList());
    }
}

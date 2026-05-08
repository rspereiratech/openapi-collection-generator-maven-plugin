package com.github.rspereiratech.plugin.postman.body;

import com.github.rspereiratech.plugin.postman.model.PostmanBody;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;

/**
 * Builds a {@link PostmanBody} from an OpenAPI operation's request body definition.
 */
public interface BodyBuilder {

    /**
     * Builds a Postman request body for the given OpenAPI operation.
     *
     * @param operation the OpenAPI operation
     * @param openApi   the full OpenAPI specification
     * @return a {@link PostmanBody}, or {@code null} if the operation has no request body
     */
    PostmanBody build(Operation operation, OpenAPI openApi);
}

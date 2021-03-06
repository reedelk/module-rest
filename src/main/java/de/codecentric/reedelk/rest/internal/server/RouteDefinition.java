package de.codecentric.reedelk.rest.internal.server;

import de.codecentric.reedelk.rest.component.listener.ErrorResponse;
import de.codecentric.reedelk.rest.component.listener.Response;
import de.codecentric.reedelk.rest.component.listener.openapi.v3.OperationObject;
import de.codecentric.reedelk.rest.internal.commons.RestMethod;

public class RouteDefinition {

    private final String path;
    private final RestMethod method;
    private final Response response;
    private final ErrorResponse errorResponse;
    private final OperationObject openApiObject;

    public RouteDefinition(String path, RestMethod method) {
        this(path, method, null, null, null);
    }

    public RouteDefinition(String path, RestMethod method, Response response, ErrorResponse errorResponse, OperationObject openApiObject) {
        this.path = path;
        this.method = method;
        this.response = response;
        this.errorResponse = errorResponse;
        this.openApiObject = openApiObject;
    }

    public String getPath() {
        return path;
    }

    public RestMethod getMethod() {
        return method;
    }

    public Response getResponse() {
        return response;
    }

    public ErrorResponse getErrorResponse() {
        return errorResponse;
    }

    public OperationObject getOpenApiObject() {
        return openApiObject;
    }
}

package com.reedelk.rest.internal.openapi;

import com.reedelk.rest.internal.commons.RestMethod;
import com.reedelk.rest.component.RestListenerConfiguration;
import com.reedelk.rest.component.listener.ErrorResponse;
import com.reedelk.rest.component.listener.Response;
import com.reedelk.rest.component.listener.openapi.OperationObject;
import com.reedelk.rest.internal.server.HttpRequestHandler;
import com.reedelk.rest.internal.server.HttpRouteHandler;
import com.reedelk.rest.internal.server.Server;

import java.util.List;

import static com.reedelk.rest.internal.commons.RestMethod.GET;

public class OpenApiServerDecorator implements Server {

    private static final String openAPIDocument = "/openapi.json";

    private final Server delegate;
    private OpenApiRequestHandler openApiRequestHandler;

    public OpenApiServerDecorator(RestListenerConfiguration configuration, Server delegate) {
        this.delegate = delegate;
        this.openApiRequestHandler = new OpenApiRequestHandler(configuration);
        addOpenApiDocumentRoute();
    }

    @Override
    public void addRoute(String path, RestMethod method, Response response, ErrorResponse errorResponse, OperationObject operationObject, HttpRequestHandler httpHandler) {
        openApiRequestHandler.add(path, method, response, errorResponse, operationObject);
        delegate.addRoute(path, method, response, errorResponse, operationObject, httpHandler);
    }

    @Override
    public void removeRoute(String path, RestMethod method) {
        openApiRequestHandler.remove(path, method);
        delegate.removeRoute(path, method);
    }

    @Override
    public String getBasePath() {
        return delegate.getBasePath();
    }

    @Override
    public boolean hasEmptyRoutes() {
        List<HttpRouteHandler> handlers = delegate.handlers();
        // If there is only one route left, it means that there is only the open API route registered.
        // Therefore the server can be safely shutdown since it would be an empty Open API doucment.
        return handlers.size() == 1;
    }

    @Override
    public List<HttpRouteHandler> handlers() {
        return delegate.handlers();
    }

    @Override
    public void stop() {
        delegate.removeRoute(openAPIDocument, GET);
        delegate.stop();
    }

    private void addOpenApiDocumentRoute() {
        // Response, error response and operation object are not used by the delegate.
        delegate.addRoute(openAPIDocument, GET, null, null, null, openApiRequestHandler);
    }
}
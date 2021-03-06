package de.codecentric.reedelk.rest.internal.server.mapper;

import de.codecentric.reedelk.rest.component.listener.ErrorResponse;
import de.codecentric.reedelk.rest.component.listener.Response;
import de.codecentric.reedelk.runtime.api.flow.FlowContext;
import de.codecentric.reedelk.runtime.api.message.Message;
import de.codecentric.reedelk.runtime.api.message.content.MimeType;
import de.codecentric.reedelk.runtime.api.script.ScriptEngineService;
import de.codecentric.reedelk.runtime.api.script.dynamicvalue.DynamicByteArray;
import de.codecentric.reedelk.runtime.api.script.dynamicvalue.DynamicInteger;
import de.codecentric.reedelk.rest.internal.commons.HttpHeader;
import io.netty.handler.codec.http.HttpResponseStatus;
import reactor.netty.http.server.HttpServerResponse;

import java.util.Map;

import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static java.util.Optional.ofNullable;

public class MessageHttpResponseMapper {

    private final ScriptEngineService scriptEngine;
    private final ErrorResponse errorResponse;
    private final Response response;

    public MessageHttpResponseMapper(ScriptEngineService scriptEngine, Response response, ErrorResponse errorResponse) {
        this.response = response;
        this.errorResponse = errorResponse;
        this.scriptEngine = scriptEngine;
    }

    /**
     * Maps an out Message to a successful http response. It returns the byte stream
     * to be sent back to the client. This mapper DOES HAVE side effects on the HTTP
     * server response object: it sets the values to be returned to the client.
     *
     * @param message        out flow Message
     * @param serverResponse http response to be sent back to the client
     * @param flowContext    the flow context object holding flow variables and other contextual info
     */
    public void map(Message message, HttpServerResponse serverResponse, FlowContext flowContext) {
        // 1. Status code
        DynamicInteger responseStatus = ofNullable(response).map(Response::getStatus).orElse(null);
        HttpResponseStatus status = EvaluateStatusCode.withDefault(OK)
                .withScriptEngine(scriptEngine)
                .withStatus(responseStatus)
                .withContext(flowContext)
                .withMessage(message)
                .evaluate();
        serverResponse.status(status);

        // 2. Content type
        DynamicByteArray responseBody = ofNullable(response).map(Response::getBody).orElse(null);
        ContentType.from(responseBody, message)
                .ifPresent(contentType -> serverResponse.addHeader(HttpHeader.CONTENT_TYPE, contentType));

        // 3. Headers (which might override headers above)
        Map<String,String> evaluatedResponseHeaders = ofNullable(response)
                .map(Response::getHeaders)
                .map(dynamicStringMap -> scriptEngine.evaluate(dynamicStringMap, flowContext, message))
                .orElse(null);
        AdditionalHeader.addAll(serverResponse, evaluatedResponseHeaders);
    }

    /**
     * Maps an exception to a not successful http response. It returns the byte stream
     * to be sent back to the client. This mapper DOES HAVE side effects on the HTTP
     * server response object: it sets the values to be returned to the client.
     *
     * @param exception      the exception we want to map to the HTTP server response
     * @param serverResponse http response to be sent back to the client
     * @param flowContext    the flow context object holding flow variables and other contextual info
     */
    public void map(Throwable exception, HttpServerResponse serverResponse, FlowContext flowContext) {
        // 1. Status code
        DynamicInteger errorResponseStatus = ofNullable(errorResponse).map(ErrorResponse::getStatus).orElse(null);
        HttpResponseStatus status = EvaluateStatusCode.withDefault(INTERNAL_SERVER_ERROR)
                .withScriptEngine(scriptEngine)
                .withStatus(errorResponseStatus)
                .withContext(flowContext)
                .withThrowable(exception)
                .evaluate();
        serverResponse.status(status);

        // 2. Response headers
        DynamicByteArray responseBody = ofNullable(errorResponse).map(ErrorResponse::getBody).orElse(null);
        if (responseBody != null && responseBody.isNotNull()) {
            // Content type is by default text if response is error (exception).
            // If the user wants to output JSON they must override with specific
            // additional headers the content type.
            serverResponse.addHeader(HttpHeader.CONTENT_TYPE, MimeType.TEXT_PLAIN.toString());
        }

        // 3. Headers (which might override headers above)
        Map<String,String> evaluatedResponseHeaders = ofNullable(errorResponse)
                .map(ErrorResponse::getHeaders)
                .map(dynamicStringMap -> scriptEngine.evaluate(dynamicStringMap, flowContext, exception))
                .orElse(null);
        AdditionalHeader.addAll(serverResponse, evaluatedResponseHeaders);
    }
}

package de.codecentric.reedelk.rest.component;

import de.codecentric.reedelk.rest.component.listener.ErrorResponse;
import de.codecentric.reedelk.rest.component.listener.Response;
import de.codecentric.reedelk.rest.component.listener.openapi.v3.OperationObject;
import de.codecentric.reedelk.rest.internal.attribute.RESTListenerAttributes;
import de.codecentric.reedelk.rest.internal.commons.RestMethod;
import de.codecentric.reedelk.rest.internal.commons.StreamingMode;
import de.codecentric.reedelk.rest.internal.server.*;
import de.codecentric.reedelk.runtime.api.annotation.*;
import de.codecentric.reedelk.runtime.api.component.AbstractInbound;
import de.codecentric.reedelk.runtime.api.exception.PlatformException;
import de.codecentric.reedelk.runtime.api.script.ScriptEngineService;
import de.codecentric.reedelk.runtime.api.type.MapOfAttachments;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Optional;

import static de.codecentric.reedelk.rest.internal.commons.Messages.RestListener.LISTENER_CONFIG_MISSING;
import static de.codecentric.reedelk.runtime.api.commons.ComponentPrecondition.Configuration.requireNotNull;
import static de.codecentric.reedelk.runtime.api.commons.ComponentPrecondition.Configuration.requireTrue;
import static de.codecentric.reedelk.runtime.api.commons.StringUtils.isBlank;
import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ModuleComponent("REST Listener")
@ComponentOutput(
        attributes = RESTListenerAttributes.class,
        payload = { byte[].class, String.class, MapOfAttachments.class})
@Description("The REST Listener can be used to create a REST endpoint listening on " +
                "a given port, post and path. The listening path might contain path segments which " +
                "are matched whenever an HTTP request comes in. A REST Listener configuration might be shared " +
                "across different REST Listener whenever there is a need to reuse a common endpoint configuration " +
                "across different REST resources. The REST Listener is an Inbound component and it can only be placed " +
                "at the beginning of a flow.")
@Component(service = RESTListener.class, scope = PROTOTYPE)
public class RESTListener extends AbstractInbound {

    @DialogTitle("REST Listener Configuration")
    @Property("Configuration")
    private RESTListenerConfiguration configuration;

    @Property("Path")
    @Hint("/resource/{id}")
    @Example("/resource/{id}")
    @Description("The rest path this listener will be bound to. If present must start with '/'. " +
            "The path might contain regexp, e.g: /{name:.*} which would match against anything it is compared to, " +
            "or parameters /{group}/{id}. Path parameters are bound to a map in the out message attributes. " +
            "The following script expression can be used to retrieve request path parameters: <code>message.attributes().pathParams</code>.")
    private String path;

    @Property("Method")
    @Example("PUT")
    @InitValue("GET")
    @DefaultValue("GET")
    @Description("The REST Method this listener will be listening from.")
    private RestMethod method;

    @Property("Streaming")
    @Example("ALWAYS")
    @InitValue("AUTO")
    @Description("Determines the way the response body is sent to the client. " +
            "When set to Auto and if the size of the payload is not clear, e.g. it is a stream of data, then it uses <b>Transfer-Encoding: chunked</b> " +
            "when sending data back to the client. Otherwise <b>Content-Length</b> encoding with the size of the payload is used. " +
            "When set to Always <b>Transfer-Encoding: chunked</b> is always used, and when none <b>Content-Length</b> is always used instead.")
    private StreamingMode streaming = StreamingMode.AUTO;

    @Group("Response")
    @Property("Response")
    private Response response;

    @Group("Response error")
    @Property("Response error")
    private ErrorResponse errorResponse;

    @Group("Open API")
    @Property("Open API")
    private OperationObject openApi;

    @Reference
    private ServerProvider provider;
    @Reference
    private ScriptEngineService scriptEngine;

    private RouteDefinition routeDefinition;

    @Override
    public void onStart() {
        requireNotNull(RESTListener.class, configuration, "RESTListener configuration must be defined");
        requireNotNull(RESTListener.class, configuration.getProtocol(), "RESTListener configuration protocol must be defined");
        requireNotNull(RESTListener.class, method, "RESTListener method must be defined");
        requireTrue(RESTListener.class, isBlank(path) || path.startsWith("/") ,"RESTListener path must start with '/'");

        HttpRequestHandler requestHandler = DefaultHttpRequestHandler.builder()
                        .inboundEventListener(RESTListener.this)
                        .errorResponse(errorResponse)
                        .scriptEngine(scriptEngine)
                        .streaming(streaming)
                        .matchingPath(path)
                        .response(response)
                        .build();

        Optional<Server> maybeServer = provider.getOrCreate(configuration);
        requireTrue(RESTListener.class, maybeServer.isPresent(), LISTENER_CONFIG_MISSING.format());

        // The check is done in the require true precondition above.
        Server server = maybeServer.get();

        this.routeDefinition = new RouteDefinition(path, method, response, errorResponse, openApi);
        server.addRoute(routeDefinition, requestHandler);
    }

    @Override
    public void onShutdown() {
        provider.get(configuration).ifPresent(server -> {
            // The route definition might be null when we cannot create a server,
            // because for instance there were two listeners configured on the
            // same port and different path. In that case the route definition would
            // be null, and the route was never added.
            if (routeDefinition != null) server.removeRoute(routeDefinition);
            routeDefinition = null;
            try {
                provider.release(server);
            } catch (Exception exception) {
                throw new PlatformException(exception);
            }
        });
    }

    public void setConfiguration(RESTListenerConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setMethod(RestMethod method) {
        this.method = method;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public void setErrorResponse(ErrorResponse errorResponse) {
        this.errorResponse = errorResponse;
    }

    public void setStreaming(StreamingMode streaming) {
        this.streaming = streaming;
    }

    public void setOpenApi(OperationObject openApi) {
        this.openApi = openApi;
    }
}

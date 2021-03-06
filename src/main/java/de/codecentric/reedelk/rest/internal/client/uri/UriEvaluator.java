package de.codecentric.reedelk.rest.internal.client.uri;

import de.codecentric.reedelk.rest.component.RESTClient;
import de.codecentric.reedelk.rest.component.RESTClientConfiguration;
import de.codecentric.reedelk.rest.internal.commons.HttpProtocol;
import de.codecentric.reedelk.runtime.api.exception.ComponentConfigurationException;
import de.codecentric.reedelk.runtime.api.flow.FlowContext;
import de.codecentric.reedelk.runtime.api.message.Message;
import de.codecentric.reedelk.runtime.api.script.ScriptEngineService;
import de.codecentric.reedelk.runtime.api.script.dynamicmap.DynamicStringMap;
import de.codecentric.reedelk.rest.internal.commons.Messages;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static de.codecentric.reedelk.runtime.api.commons.ComponentPrecondition.Configuration.requireNotNull;
import static de.codecentric.reedelk.runtime.api.commons.StringUtils.isBlank;
import static de.codecentric.reedelk.runtime.api.commons.StringUtils.isNotNull;

public class UriEvaluator {

    private String baseURL;
    private URIPathComponent pathComponent;
    private ScriptEngineService scriptEngine;
    private DynamicStringMap pathParameters;
    private DynamicStringMap queryParameters;

    private static final Map<String, String> EMPTY_MAP = new HashMap<>();

    public UriProvider provider(Message message, FlowContext flowContext) {
        String requestURI = baseURL + evaluateRequestURI(message, flowContext);
        return () -> URI.create(requestURI);
    }

    /**
     * We check which parameters are effectively there to understand what to evaluate.
     * Next optimization could be checking in the map which values
     * are actually scripts and then evaluate only those ones.
     */
    private String evaluateRequestURI(Message message, FlowContext flowContext) {

        if (pathParameters.isEmpty() && queryParameters.isEmpty()) {
            // If path and query parameters are empty, there is nothing to expand.
            return pathComponent.expand(EMPTY_MAP, EMPTY_MAP);

        } else if (pathParameters.isEmpty()) {
            // Only query parameters are present.
            Map<String, String> evaluatedQueryParameters = scriptEngine.evaluate(queryParameters, flowContext, message);
            return pathComponent.expand(EMPTY_MAP, evaluatedQueryParameters);

        } else if (queryParameters.isEmpty()) {
            // Only path parameters are present.
            Map<String, String> evaluatedPathParameters = scriptEngine.evaluate(pathParameters, flowContext, message);
            return pathComponent.expand(evaluatedPathParameters, EMPTY_MAP);

        } else {
            // Both path and query parameters are present.
            Map<String, String> evaluatedPathParameters = scriptEngine.evaluate(pathParameters, flowContext, message);
            Map<String, String> evaluatedQueryParameters = scriptEngine.evaluate(queryParameters, flowContext, message);
            return pathComponent.expand(evaluatedPathParameters, evaluatedQueryParameters);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String path;
        private String baseURL;
        private ScriptEngineService scriptEngine;
        private RESTClientConfiguration configuration;
        private DynamicStringMap pathParameters = DynamicStringMap.empty();
        private DynamicStringMap queryParameters = DynamicStringMap.empty();

        public Builder baseURL(String baseURL) {
            this.baseURL = baseURL;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder scriptEngine(ScriptEngineService scriptEngine) {
            this.scriptEngine = scriptEngine;
            return this;
        }

        public Builder configuration(RESTClientConfiguration configuration) {
            this.configuration = configuration;
            return this;
        }

        public Builder pathParameters(DynamicStringMap pathParameters) {
            this.pathParameters = pathParameters;
            return this;
        }

        public Builder queryParameters(DynamicStringMap queryParameters) {
            this.queryParameters = queryParameters;
            return this;
        }

        public UriEvaluator build() {
            UriEvaluator evaluator = new UriEvaluator();
            evaluator.scriptEngine = scriptEngine;
            evaluator.pathParameters = pathParameters;
            evaluator.queryParameters = queryParameters;
            evaluator.pathComponent = isBlank(path) ?
                    new EmptyURIPathComponent() :
                    new NotEmptyURIPathComponent(path);

            if (isNotNull(baseURL)) {
                // Use base URL
                evaluator.baseURL = baseURL;

            } else {
                // Use config
                requireNotNull(RESTClientConfiguration.class, configuration, Messages.RestClient.CONFIG_CLIENT_NULL_ERROR.format());

                String host = configuration.getHost();
                int port = port(configuration.getPort());
                String basePath = configuration.getBasePath();
                String scheme = scheme(configuration.getProtocol());
                try {
                    URI uri = new URI(scheme, null, host, port, basePath, null, null);
                    evaluator.baseURL = uri.toString();
                } catch (URISyntaxException exception) {
                    String message = Messages.RestClient.REQUEST_URL_ERROR.format(host ,port, basePath, scheme, exception.getMessage());
                    throw new ComponentConfigurationException(RESTClient.class, message, exception);
                }
            }
            return evaluator;
        }

        private int port(Integer port) {
            return port == null ? -1 : port;
        }

        private String scheme(HttpProtocol protocol) {
            return protocol == null ?
                    HttpProtocol.HTTP.toString().toLowerCase() :
                    protocol.toString().toLowerCase();
        }
    }
}

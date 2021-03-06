package de.codecentric.reedelk.rest.internal.server.mapper;

import de.codecentric.reedelk.rest.internal.commons.HttpHeadersAsMap;
import de.codecentric.reedelk.rest.internal.commons.MimeTypeExtract;
import de.codecentric.reedelk.rest.internal.commons.QueryParameters;
import de.codecentric.reedelk.rest.internal.commons.URLDecoderUtil;
import de.codecentric.reedelk.runtime.api.commons.StringUtils;
import de.codecentric.reedelk.runtime.api.message.content.MimeType;
import io.netty.handler.codec.http.HttpHeaders;
import reactor.netty.ByteBufFlux;
import reactor.netty.http.server.HttpServerRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class HttpRequestWrapper {

    private static final String QUERY_PARAMS_START_MARKER = "?";

    private final HttpServerRequest request;

    HttpRequestWrapper(HttpServerRequest request) {
        this.request = request;
    }

    public String version() {
        return request.version().text();
    }

    public String scheme() {
        return request.scheme();
    }

    public String method() {
        return request.method().name();
    }

    public String requestUri() {
        return request.uri();
    }

    public MimeType mimeType() {
        return MimeTypeExtract.from(request);
    }

    public String queryString() {
        // Keep only query parameters from the uri
        int queryParamsStart = request.uri().indexOf("?");
        return queryParamsStart > -1 ?
                request.uri().substring(queryParamsStart + 1) :
                StringUtils.EMPTY;
    }

    public String requestPath() {
        // Remove query parameters from the uri
        int queryParamsStartIndex = request.uri().indexOf(QUERY_PARAMS_START_MARKER);
        return queryParamsStartIndex > -1 ?
                request.uri().substring(0, queryParamsStartIndex) :
                request.uri();
    }

    ByteBufFlux data() {
        return request.receive();
    }

    public String remoteAddress() {
        return request.remoteAddress().toString();
    }

    public HashMap<String, List<String>> queryParams() {
        return QueryParameters.from(request.uri());
    }

    public HashMap<String, String> params() {
        HashMap<String, String> decodedParameters = new HashMap<>();
        if (request.params() == null) return decodedParameters;

        for (Map.Entry<String,String> entry : request.params().entrySet()) {
            String paramKey = entry.getKey();
            String paramValue = entry.getValue();
            decodedParameters.put(paramKey, URLDecoderUtil.decode(paramValue));
        }
        return decodedParameters;
    }

    public TreeMap<String, List<String>> headers() {
        return HttpHeadersAsMap.of(request.requestHeaders());
    }

    HttpHeaders requestHeaders() {
        return request.requestHeaders();
    }
}

package de.codecentric.reedelk.rest.internal.client.strategy;

import de.codecentric.reedelk.rest.internal.client.HttpClient;
import de.codecentric.reedelk.rest.internal.client.HttpClientResultCallback;
import de.codecentric.reedelk.rest.internal.client.body.BodyProvider;
import de.codecentric.reedelk.rest.internal.client.header.HeaderProvider;
import de.codecentric.reedelk.rest.internal.client.response.BufferSizeAwareResponseConsumer;
import de.codecentric.reedelk.runtime.api.flow.FlowContext;
import de.codecentric.reedelk.runtime.api.message.Message;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.reactivestreams.Publisher;

import java.net.URI;
import java.util.concurrent.Future;

import static org.apache.http.client.utils.URIUtils.extractHost;

/**
 * Body is always streamed. Transfer-Encoding header set to chunked.
 */
public class StrategyWithStreamBody implements Strategy {

    private final int requestBufferSize;
    private final int responseBufferSize;
    private final RequestWithBodyFactory requestFactory;

    StrategyWithStreamBody(RequestWithBodyFactory requestFactory, int requestBufferSize, int responseBufferSize) {
        this.requestFactory = requestFactory;
        this.requestBufferSize = requestBufferSize;
        this.responseBufferSize = responseBufferSize;
    }

    @Override
    public Future<HttpResponse> execute(HttpClient client,
                                        Message input,
                                        FlowContext flowContext,
                                        URI uri,
                                        HeaderProvider headerProvider,
                                        BodyProvider bodyProvider,
                                        HttpClientResultCallback callback) {

        Publisher<byte[]> body = bodyProvider.getAsStream(input, flowContext);
        BasicHttpEntity entity = new BasicHttpEntity();

        HttpEntityEnclosingRequestBase request = requestFactory.create();
        request.setURI(uri);
        request.setEntity(entity);

        headerProvider.headers().forEach(request::addHeader);

        StreamRequestProducer requestProducer = new StreamRequestProducer(extractHost(uri), request, body, requestBufferSize);
        HttpAsyncResponseConsumer<HttpResponse> responseConsumer = BufferSizeAwareResponseConsumer.createConsumer(responseBufferSize);

        return client.execute(requestProducer, responseConsumer, callback);
    }
}

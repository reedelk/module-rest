package com.reedelk.rest.client.strategy;

import com.reedelk.rest.client.HttpClient;
import com.reedelk.rest.client.body.BodyProvider;
import com.reedelk.rest.client.header.HeaderProvider;
import com.reedelk.rest.client.uri.UriProvider1;
import com.reedelk.runtime.api.component.OnResult;
import com.reedelk.runtime.api.flow.FlowContext;
import com.reedelk.runtime.api.message.Message;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.reactivestreams.Publisher;

import java.net.URI;

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
    public void execute(HttpClient client, OnResult callback, Message input, FlowContext flowContext,
                        UriProvider1 uriProvider1, HeaderProvider headerProvider, BodyProvider bodyProvider) {

        URI uri = uriProvider1.uri();

        Publisher<byte[]> body = bodyProvider.asStream(input, flowContext);

        BasicHttpEntity entity = new BasicHttpEntity();

        HttpEntityEnclosingRequestBase request = requestFactory.create();

        request.setURI(uri);

        request.setEntity(entity);

        headerProvider.headers().forEach(request::addHeader);

        client.execute(
                new StreamRequestProducer(extractHost(uri), request, body, requestBufferSize),
                HttpAsyncMethods.createConsumer(),
                new HttpClient.ResultCallback(callback, flowContext, uri));

    }
}

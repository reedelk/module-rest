package de.codecentric.reedelk.rest.internal.client.strategy;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.nio.protocol.BasicAsyncRequestProducer;

class EmptyStreamRequestProducer extends BasicAsyncRequestProducer {

    EmptyStreamRequestProducer(HttpHost target, HttpRequest request) {
        super(target, request);
    }
}

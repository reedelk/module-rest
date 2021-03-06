package de.codecentric.reedelk.rest.internal.client.strategy;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

public class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase {

    final static String METHOD_NAME = "DELETE";

    HttpDeleteWithBody() {
        super();
    }

    @Override
    public String getMethod() {
        return METHOD_NAME;
    }

}

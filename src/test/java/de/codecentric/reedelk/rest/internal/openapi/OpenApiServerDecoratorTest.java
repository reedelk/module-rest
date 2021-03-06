package de.codecentric.reedelk.rest.internal.openapi;

import de.codecentric.reedelk.rest.internal.server.HttpRequestHandler;
import de.codecentric.reedelk.rest.internal.server.HttpRouteHandler;
import de.codecentric.reedelk.rest.internal.server.Server;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OpenApiServerDecoratorTest {

    @Mock
    private Server delegate;
    @Mock
    private HttpRouteHandler openApiHandler;
    @Mock
    private HttpRequestHandler requestHandler;

    // TODO: Fixme
    /**
    @Test
    void shouldAddOpenApiRouteToDelegate() {
        // Given
        RESTListenerConfiguration configuration = new RESTListenerConfiguration();

        // When
        new OpenApiServerDecorator(configuration, delegate);

        // Then
        verify(delegate).addRoute(eq("/openapi.json"),
                eq(GET),
                eq(null),
                eq(null),
                eq(null),
                any(OpenApiRequestHandler.class));
    }

    @Test
    void shouldReturnHasEmptyRoutesTrue() {
        // Given
        RESTListenerConfiguration configuration = new RESTListenerConfiguration();
        doReturn(Collections.singletonList(openApiHandler)).when(delegate).handlers();

        // When
        OpenApiServerDecorator decorator = new OpenApiServerDecorator(configuration, delegate);

        // Then
        assertThat(decorator.hasEmptyRoutes()).isTrue();
    }

    @Test
    void shouldAddRouteToDelegate() {
        // Given
        RESTListenerConfiguration configuration = new RESTListenerConfiguration();
        OpenApiServerDecorator decorator = new OpenApiServerDecorator(configuration, delegate);

        // When
        decorator.addRoute("/",
                RestMethod.POST,
                null,
                null,
                null,
                requestHandler);

        // Then
        verify(delegate).addRoute("/",
                RestMethod.POST,
                null,
                null,
                null,
                requestHandler);
    }

    @Test
    void shouldRemoveRouteFromDelegate() {
        // Given
        RESTListenerConfiguration configuration = new RESTListenerConfiguration();
        OpenApiServerDecorator decorator = new OpenApiServerDecorator(configuration, delegate);

        // When
        decorator.removeRoute("/", RestMethod.POST);

        // Then
        verify(delegate).removeRoute("/", RestMethod.POST);
    }

    @Test
    void shouldStopRemoveOpenApiRouteFromDelegateAndStop() {
        // Given
        RESTListenerConfiguration configuration = new RESTListenerConfiguration();
        OpenApiServerDecorator decorator = new OpenApiServerDecorator(configuration, delegate);

        // When
        decorator.stop();

        // Then
        verify(delegate).removeRoute("/openapi.json", GET);
        verify(delegate).stop();
    }*/
}

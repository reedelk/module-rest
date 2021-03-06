package de.codecentric.reedelk.rest.component.listener.openapi.v3;

/**
 * An interface used to map an object to OpenAPI Model.
 * @param <T> the mapped OpenAPI model type.
 */
public interface OpenAPIModel<T> {

    T map(OpenApiSerializableContext context);

}

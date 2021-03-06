package de.codecentric.reedelk.rest.internal.script;

import de.codecentric.reedelk.runtime.api.annotation.Type;
import de.codecentric.reedelk.runtime.api.annotation.TypeFunction;

@Type(global = true,
        description = "The HttpPartBuilder creates new HttpPart objects.")
public class HttpPartBuilder {

    @TypeFunction(signature = "create()",
            example = "HttpPartBuilder.create()" +
                    ".attribute('filename','my_image.png')" +
                    ".binary(message.payload(), 'image/png')" +
                    ".build()",
            description = "Creates a new HttpPart object.")
    public HttpPart create() {
        return new HttpPart();
    }
}

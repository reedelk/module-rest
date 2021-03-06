package de.codecentric.reedelk.rest.component.listener.openapi.v3;

import de.codecentric.reedelk.runtime.api.annotation.*;
import de.codecentric.reedelk.runtime.api.commons.StreamUtils;
import de.codecentric.reedelk.runtime.api.component.Implementor;
import de.codecentric.reedelk.runtime.api.resource.ResourceText;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

@Component(service = ExampleObject.class, scope = ServiceScope.PROTOTYPE)
public class ExampleObject implements Implementor, OpenAPIModel<de.codecentric.reedelk.openapi.v3.model.ExampleObject> {

    @Property("Inline Example")
    @InitValue("true")
    @DefaultValue("false")
    @Example("true")
    @Description("If true, the example is in-lined in the final OpenAPI document instead " +
            "of referencing the example from the Components object.")
    private Boolean inlineExample;

    @Property("Summary")
    @Hint("My example")
    @Example("A foo example")
    @When(propertyName = "inlineExample", propertyValue = "true")
    @Description("Short description for the example.")
    private String summary;

    @Property("Description")
    @Hint("My description")
    @Example("A foo description")
    @When(propertyName = "inlineExample", propertyValue = "true")
    @Description("Long description for the example. CommonMark syntax MAY be used for rich text representation.")
    private String description;

    @Property("External Value")
    @Hint("http://example.org/examples/address-example.xml")
    @Example("http://example.org/examples/address-example.xml")
    @When(propertyName = "inlineExample", propertyValue = "true")
    @Description("A URL that points to the literal example. This provides the capability to reference examples " +
            "that cannot easily be included in JSON or YAML documents. The value field and externalValue " +
            "field are mutually exclusive.")
    private String externalValue;

    @Property("Example")
    @WidthAuto
    @Hint("assets/get-orders-example.json")
    @Example("assets/get-orders-example.json")
    @HintBrowseFile("Select Example File ...")
    @Description("The path and name of the example to be read from the project's resources folder.")
    private ResourceText value;

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExternalValue() {
        return externalValue;
    }

    public void setExternalValue(String externalValue) {
        this.externalValue = externalValue;
    }

    public ResourceText getValue() {
        return value;
    }

    public void setValue(ResourceText value) {
        this.value = value;
    }

    public Boolean getInlineExample() {
        return inlineExample;
    }

    public void setInlineExample(Boolean inlineExample) {
        this.inlineExample = inlineExample;
    }

    @Override
    public de.codecentric.reedelk.openapi.v3.model.ExampleObject map(OpenApiSerializableContext context) {
        if (inlineExample != null && inlineExample) {
            de.codecentric.reedelk.openapi.v3.model.ExampleObject mapped =
                    new de.codecentric.reedelk.openapi.v3.model.ExampleObject();
            mapped.setSummary(summary);
            mapped.setDescription(description);
            mapped.setExternalValue(externalValue);
            if (value != null) {
                String exampleData = StreamUtils.FromString.consume(value.data());
                mapped.setValue(exampleData);
            }
            return mapped;

        } else {
            return value != null ? context.getExampleObject(value) : null;
        }
    }
}

package de.codecentric.reedelk.rest.component.listener.openapi.v3;

import de.codecentric.reedelk.runtime.api.annotation.*;
import de.codecentric.reedelk.runtime.api.component.Implementor;
import de.codecentric.reedelk.runtime.api.resource.ResourceText;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

@Component(service = HeaderObject.class, scope = ServiceScope.PROTOTYPE)
public class HeaderObject implements Implementor, OpenAPIModel<de.codecentric.reedelk.openapi.v3.model.HeaderObject> {

    @Property("Description")
    @Hint("My header description")
    @Example("My header description")
    @Description("A brief description of the header. This could contain examples of use.")
    private String description;

    @Property("Style")
    @Example("form")
    @InitValue("simple")
    @DefaultValue("simple")
    @Description("Describes how the parameter value will be serialized depending on the type of the parameter value. " +
            "Default values (based on value of in): for query - form; for path - simple; for header - simple; for cookie - form.")
    private ParameterStyle style = ParameterStyle.simple;

    @Property("Schema")
    @InitValue("STRING")
    @DefaultValue("STRING")
    private PredefinedSchema predefinedSchema = PredefinedSchema.STRING;

    @Property("Schema File")
    @WidthAuto
    @HintBrowseFile("Select Schema File ...")
    @When(propertyName = "predefinedSchema", propertyValue = "NONE")
    @When(propertyName = "predefinedSchema", propertyValue = When.NULL)
    private ResourceText schema;

    @Property("Inline Schema")
    @DefaultValue("false")
    @Example("true")
    @When(propertyName = "schema", propertyValue = When.NOT_BLANK)
    @Description("If true, the schema is in-lined in the final OpenAPI document instead " +
            "of referencing the schema from the Components object.")
    private Boolean inlineSchema;

    @Property("Example")
    @Hint("myParamValue")
    @Example("myParamValue")
    @Description("Example of the parameter's potential value. " +
            "The example SHOULD match the specified schema and encoding properties if present. ")
    private String example;

    @Property("Explode")
    @DefaultValue("false")
    @Description("When this is true, parameter values of type array or object generate separate parameters " +
            "for each value of the array or key-value pair of the map. For other types of parameters this property " +
            "has no effect. When style is form, the default value is true. " +
            "For all other styles, the default value is false.")
    private Boolean explode;

    @Property("Deprecated")
    @Description("Specifies that a parameter is deprecated and SHOULD be transitioned out of usage.")
    private Boolean deprecated;

    @Property("Allow Reserved")
    @Description("Determines whether the parameter value SHOULD allow reserved characters, " +
            "as defined by RFC3986 :/?#[]@!$&'()*+,;= to be included without percent-encoding. " +
            "This property only applies to parameters with an in value of query.")
    private Boolean allowReserved;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ParameterStyle getStyle() {
        return style;
    }

    public void setStyle(ParameterStyle style) {
        this.style = style;
    }

    public PredefinedSchema getPredefinedSchema() {
        return predefinedSchema;
    }

    public void setPredefinedSchema(PredefinedSchema predefinedSchema) {
        this.predefinedSchema = predefinedSchema;
    }

    public ResourceText getSchema() {
        return schema;
    }

    public void setSchema(ResourceText schema) {
        this.schema = schema;
    }

    public void setInlineSchema(Boolean inlineSchema) {
        this.inlineSchema = inlineSchema;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }

    public Boolean getExplode() {
        return explode;
    }

    public void setExplode(Boolean explode) {
        this.explode = explode;
    }

    public Boolean getDeprecated() {
        return deprecated;
    }

    public void setDeprecated(Boolean deprecated) {
        this.deprecated = deprecated;
    }

    public Boolean getAllowReserved() {
        return allowReserved;
    }

    public void setAllowReserved(Boolean allowReserved) {
        this.allowReserved = allowReserved;
    }

    @Override
    public de.codecentric.reedelk.openapi.v3.model.HeaderObject map(OpenApiSerializableContext context) {
        de.codecentric.reedelk.openapi.v3.model.HeaderObject mappedHeader =
                new de.codecentric.reedelk.openapi.v3.model.HeaderObject();
        mappedHeader.setDescription(description);
        if (style != null) {
            mappedHeader.setStyle(de.codecentric.reedelk.openapi.v3.model.ParameterStyle.valueOf(style.name()));
        }
        mappedHeader.setSchema(context.getSchema(predefinedSchema, schema, inlineSchema));
        mappedHeader.setExample(example);
        mappedHeader.setExplode(explode);
        mappedHeader.setDeprecated(deprecated);
        mappedHeader.setAllowReserved(allowReserved);
        return mappedHeader;
    }
}

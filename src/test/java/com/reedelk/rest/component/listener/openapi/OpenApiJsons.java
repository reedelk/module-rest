package com.reedelk.rest.component.listener.openapi;

import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

import static java.nio.charset.StandardCharsets.UTF_8;

public class OpenApiJsons {

    public enum InfoObject implements Provider {

        WithAllProperties() {
            @Override
            public String path() {
                return "info_object_with_all_properties.json";
            }
        },

        WithDefaultProperties() {
            @Override
            public String path() {
                return "info_object_with_default_properties.json";
            }
        }
    }

    public enum LicenseObject implements Provider {

        WithAllProperties() {
            @Override
            public String path() {
                return "license_object_with_all_properties.json";
            }
        },

        WithDefaultProperties() {
            @Override
            public String path() {
                return "license_object_with_default_properties.json";
            }
        }
    }

    public enum ContactObject implements Provider {

        WithAllProperties() {
            @Override
            public String path() {
                return "contact_object_with_all_properties.json";
            }
        },

        WithDefaultProperties() {
            @Override
            public String path() {
                return "contact_object_with_default_properties.json";
            }
        }
    }

    public enum ServerObject implements Provider {

        WithAllProperties() {
            @Override
            public String path() {
                return "server_object_with_all_properties.json";
            }
        },

        WithDefaultProperties() {
            @Override
            public String path() {
                return "server_object_with_default_properties.json";
            }
        },
    }

    public enum ServerVariableObject implements Provider {

        WithAllProperties() {
            @Override
            public String path() {
                return "server_variable_object_with_all_properties.json";
            }
        },

        WithDefaultProperties() {
            @Override
            public String path() {
                return "server_variable_object_with_default_properties.json";
            }
        },
    }

    public enum OpenApi implements Provider {

        WithDefaultInfoAndServersAndPaths() {
            @Override
            public String path() {
                return "open_api_object_with_default_info_and_servers_and_paths.json";
            }
        };
    }

    public enum PathsObject implements Provider {

        WithDefaultPaths() {
            @Override
            public String path() {
                return "paths_object_with_default.json";
            }
        },

        WithDefaultOperation() {
            @Override
            public String path() {
                return "paths_object_with_default_operation.json";
            }
        },

        WithOperation() {
            @Override
            public String path() {
                return "paths_object_with_operation.json";
            }
        },

        WithOperationWithNullPath() {
            @Override
            public String path() {
                return "paths_object_with_operation_null_path.json";
            }
        }
    }

    public enum ComponentsObject implements Provider {

        WithNoSchemas() {
            @Override
            public String path() {
                return "components_object_with_no_schemas.json";
            }
        },

        WithSampleSchemas() {
            @Override
            public String path() {
                return "components_object_with_sample_schemas.json";
            }
        }
    }

    interface Provider {

        String path();

        default URL url() {
            return OpenApiJsons.class.getResource(path());
        }

        default String string() {
            try (Scanner scanner = new Scanner(url().openStream(), UTF_8.toString())) {
                scanner.useDelimiter("\\A");
                return scanner.hasNext() ? scanner.next() : "";
            } catch (IOException e) {
                throw new RuntimeException("String from URI could not be read.", e);
            }
        }
    }
}

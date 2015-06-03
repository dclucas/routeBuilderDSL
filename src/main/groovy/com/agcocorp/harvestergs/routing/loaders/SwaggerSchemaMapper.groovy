package com.agcocorp.harvestergs.routing.loaders

class SwaggerSchemaMapper {
    private setInnerProp(obj, prop, value) {
        def current = obj
        def props = prop.tokenize('.')
        def last = props.size() - 1

        props.eachWithIndex {it, idx ->
            if (idx == last) {
                current[it] = value
            }
            else {
                if (current[it] == null) {
                    current[it] = [:]
                }
                current = current[it]
            }
        }
    }

    private setIfNotNull(obj, prop, value) {
        if (value) {
            setInnerProp obj, prop, value
        }
    }

    private setNotNull(obj, prop, value) {
        if (!value) {
            // todo: throw proper exception here
            throw new RuntimeException()
        }
        setInnerProp obj, prop, value
    }

    private mapToSwagger(parent, level = 0) {
        def swagger = [:]

        //todo: switch this to parent.properties.each once the DSL objects start being used
        parent.each {
            switch (it.key) {
                case 'attributes':
                    def attr
                    if (level == 0) {
                        setNotNull swagger, 'properties.attributes', [properties:[:]]
                        attr = swagger.properties.attributes
                    }
                    else {
                        swagger['properties'] = [:]
                        attr = swagger
                    }
                    //attr.type = 'object'
                    parent.attributes.each {
                        attr.properties[it.key] = mapToSwagger(it.value, level + 1)
                    }
                    break;
                default:
                    if (level > 0) {
                        setIfNotNull swagger, "${it.key}", it.value
                    }
                    else {
                        setIfNotNull swagger, "properties.${it.key}", it.value
                    }
                    break;
            }
        }

        swagger
    }

    def map(schema) {
        def swagger = [
            properties: [
                data: mapToSwagger(schema)
            ]
        ]
        return swagger
    }
}

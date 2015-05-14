import cucumber.api.PendingException
import static cucumber.api.groovy.EN.*

def sut
def input
def target

def createSut(entity) {
    switch (entity) {
        case "schema" : return new Definition()
        case "path": return new Path()
        case "resource": return new Resource()
        default: throw new PendingException()
    }
}

Given(~/^a valid (\w+) definition$/) { entity ->
    sut = createSut(entity)
    target = entity
}

def defineSchema(builder) {
    builder.Comment {
        properties {
            id {
                type 'Integer'
                description 'The comment id'
            }
            name {
                type 'String'
                description 'The comment name'
            }
        }
        required 'id', 'name'
    }
}

def definePath(builder) {
    builder."/comments" {
        get { req, res ->
            "comments.GET"
        }

        post { req, res ->
            "comments.POST"
        }.document { docs ->
            docs.description = "Description for comments.POST"
            docs
        }
        .skipAuth
        .skipValidation

        "/:id" {
            get    {req, res -> "comments/:id.GET"}
            patch  {req, res -> "comments/:id.PATCH"}
                    .document { docs -> docs.operationId = "commentUpdate"; docs }
            delete {req, res -> "comments/:id.DELETE"}
        }
    }
}

When(~/^it is fully defined/) { ->
    switch (target) {
        case "schema":
            input = defineSchema(sut)
            break
        case "path":
            input = definePath(sut)
            break
        case "resource":
            input = new Resource()
            input.definition = defineSchema(sut.definition)
            input.path = definePath(sut.path)
            break
        default:
            throw new PendingException()
    }
}

def checkSchema(schema) {
    assert schema.Comment.properties.size() == 2
    assert schema.Comment.properties.id.type == 'Integer'
}

def checkPath(path) {
    assert path."/comments"
    assert path."/comments".get.run(null, null) == "comments.GET"
    assert path."/comments".post.run(null, null) == "comments.POST"
    assert path."/comments".post.document.run([ summary: "Summary for comments.POST"]) ==
            [ summary: "Summary for comments.POST", description: "Description for comments.POST"]

    assert path."/comments".children."/:id".get.run(null, null) == "comments/:id.GET"
    assert path."/comments".children."/:id".patch.document.run([:]) == [ operationId: "commentUpdate" ]

    assert path."/comments".post.flags.contains('skipAuth')
    assert path."/comments".post.flags.contains('skipValidation')
}

Then(~/it correctly maps into a set of objects/) { ->
    switch (target) {
        case "schema":
            checkSchema(input)
            break
        case "path":
            checkPath(input)
            break
        case "resource":
            checkSchema(input.definition.schemas)
            checkPath(input.path.paths)
            break
        default:
            throw new PendingException()
    }
}

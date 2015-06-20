package com.agcocorp.harvestergs.routing

class PostResourceBuilder {
    private getAll
    private getById

    PostResourceBuilder(Closure getAll, getById) {
        this.getAll = getAll
        this.getById = getById
    }

    def build() {
        def resource = new APIResource('post')
            .attributes {
                title string.required
                body string.description('Post contents').required
                tags arrayOf(string)
            }
            .paths {
                "/posts" {
                    get { req, res ->
                        //return getAll()
                        return []
                    }

                    post { req, res ->
                        //return req.data
                        return [:]
                    }

                    "/:id" {
                        get { req, res -> return getById(req.params(':id')) }
                        patch { req, res -> return req.data }
                        delete { req, res -> return null }
                    }
                }
            }

        return resource
    }
}

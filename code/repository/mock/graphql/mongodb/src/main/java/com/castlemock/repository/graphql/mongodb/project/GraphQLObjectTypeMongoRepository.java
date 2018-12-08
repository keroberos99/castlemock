/*
 * Copyright 2018 Karl Dahlgren
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.castlemock.repository.graphql.mongodb.project;

import com.castlemock.core.basis.model.SearchQuery;
import com.castlemock.core.basis.model.SearchResult;
import com.castlemock.core.mock.graphql.model.project.domain.GraphQLObjectType;
import com.castlemock.repository.Profiles;
import com.castlemock.repository.graphql.project.GraphQLObjectTypeRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

/**
 * @author Mohammad Hewedy
 * @since 1.35
 */
@Repository
@Profile(Profiles.MONGODB)
public class GraphQLObjectTypeMongoRepository extends AbstractGraphQLTypeMongoRepository<GraphQLObjectTypeMongoRepository.GraphQLObjectTypeDocument, GraphQLObjectType> implements GraphQLObjectTypeRepository {

    /**
     * The method is responsible for controller that the type that is about the be saved to mongodb is valid.
     * The method should check if the type contains all the necessary values and that the values are valid. This method
     * will always be called before a type is about to be saved. The main reason for why this is vital and done before
     * saving is to make sure that the type can be correctly saved to mongodb, but also loaded from
     * mongodb upon application startup. The method will throw an exception in case of the type not being acceptable.
     *
     * @param type The instance of the type that will be checked and controlled before it is allowed to be saved on
     *             the file system.
     * @see #save
     */
    @Override
    protected void checkType(GraphQLObjectTypeDocument type) {

    }

    /**
     * The method provides the functionality to search in the repository with a {@link SearchQuery}
     *
     * @param query The search query
     * @return A <code>list</code> of {@link SearchResult} that matches the provided {@link SearchQuery}
     */
    @Override
    public List<GraphQLObjectType> search(SearchQuery query) {
        Query nameQuery = getSearchQuery("name", query);
        List<GraphQLObjectTypeDocument> resources =
                mongoOperations.find(nameQuery, GraphQLObjectTypeDocument.class);
        return toDtoList(resources, GraphQLObjectType.class);
    }

    @Override
    public List<GraphQLObjectType> findWithApplicationId(final String applicationId) {
        List<GraphQLObjectTypeDocument> resources =
                mongoOperations.find(getApplicationIdQuery(applicationId), GraphQLObjectTypeDocument.class);
        return toDtoList(resources, GraphQLObjectType.class);
    }

    @Override
    public void deleteWithApplicationId(final String applicationId) {
        mongoOperations.remove(getApplicationIdQuery(applicationId), GraphQLObjectTypeDocument.class);
    }

    @Document(collection = "graphQLObjectType")
    protected static class GraphQLObjectTypeDocument extends AbstractGraphQLTypeMongoRepository.GraphQLTypeDocument {

    }

    private Query getApplicationIdQuery(String applicationId) {
        return query(getApplicationIdCriteria(applicationId));
    }

    private Criteria getApplicationIdCriteria(String applicationId) {
        return where("applicationId").is(applicationId);
    }

}

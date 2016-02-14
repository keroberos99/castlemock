/*
 * Copyright 2015 Karl Dahlgren
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

package com.fortmocks.web.basis.model;

import com.fortmocks.core.basis.model.Repository;
import com.fortmocks.core.basis.model.Saveable;
import com.fortmocks.web.basis.support.FileRepositorySupport;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.lang.reflect.ParameterizedType;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The abstract repository provides functionality to interact with the file system in order to manage a specific type.
 * The abstract repository is responsible for retrieving and managing instances for a specific type. All the communication
 * that involves interacting with the file system, such as saving and loading instances, are done through the file repository.
 * @author Karl Dahlgren
 * @since 1.0
 * @param <T> The type that is being managed by the file repository. This file will both be saved and loaded from the
 *              file system.
 * @param <I> The id is used as an identifier for the type.
 * @see Saveable
 */
@org.springframework.stereotype.Repository
public abstract class RepositoryImpl<T extends Saveable<I>, I extends Serializable> implements Repository<T, I> {

    protected Class<T> entityClass;

    protected Class<I> idClass;

    protected Map<I, T> collection = new ConcurrentHashMap<I, T>();

    @Autowired
    private FileRepositorySupport fileRepositorySupport;

    private static final Logger LOGGER = Logger.getLogger(RepositoryImpl.class);

    /**
     * The default constructor for the AbstractRepositoryImpl class. The constructor will extract class instances of the
     * generic types (TYPE and ID). These instances could later be used to identify the types for when interacting
     * with the file system.
     */
    public RepositoryImpl() {
        final ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
        this.entityClass = (Class<T>) genericSuperclass.getActualTypeArguments()[0];
        this.idClass = (Class<I>) genericSuperclass.getActualTypeArguments()[1];
    }


    /**
     * The initialize method is responsible for initiating the file repository. This procedure involves loading
     * the types (TYPE) from the file system and store them in the collection.
     * @see #loadFiles()
     * @see #postInitiate()
     */
    @Override
    public void initialize(){
        LOGGER.debug("Start the initialize phase for the type " + entityClass.getSimpleName());
        final Collection<T> loadedFiles = loadFiles();
        for(T type : loadedFiles){
            collection.put(type.getId(), type);
        }

        postInitiate();
    }

    /**
     * The method provides the functionality to find a specific instance that matches the provided id
     * @param id The id that an instance has to match in order to be retrieved
     * @return Returns an instance that matches the provided id
     */
    @Override
    public T findOne(final I id) {
        Preconditions.checkNotNull(id, "The provided id cannot be null");
        LOGGER.debug("Retrieving " + entityClass.getSimpleName() + " with id " + id);
        return collection.get(id);
    }

    /**
     * Retrieves a list of all the instances of the specific type
     * @return A list that contains all the instances of the type that is managed by the operation
     */
    @Override
    public List<T> findAll() {
        LOGGER.debug("Retrieving all instances for " + entityClass.getSimpleName());
        return new LinkedList<>(collection.values());
    }

    /**
     * The save method provides the functionality to save an instance to the file system.
     * @param type The type that will be saved to the file system.
     * @return The type that was saved to the file system. The main reason for it is being returned is because
     *         there could be modifications of the object during the save process. For example, if the type does not
     *         have an identifier, then the method will generate a new identifier for the type.
     */
    @Override
    public T save(final T type) {
        I id = type.getId();

        if(id == null){
            id = (I)generateId();
            type.setId(id);
        }
        checkType(type);
        final String filename = getFilename(id);
        fileRepositorySupport.save(type, filename);
        collection.put(id, type);
        return type;
    }

    /**
     * Count all the stored entities for the repository
     * @return The count of entities
     */
    @Override
    public Integer count(){
        return collection.size();
    }

    /**
     * Delete an instance that match the provided id
     * @param id The instance that matches the provided id will be deleted in the database
     */
    @Override
    public void delete(final I id) {
        Preconditions.checkNotNull(id, "The provided id cannot be null");
        final String filename = getFilename(id);
        LOGGER.debug("Start the deletion of " + entityClass.getSimpleName() + " with id " + id);
        fileRepositorySupport.delete(filename);
        collection.remove(id);
        LOGGER.debug("Deletion of " + entityClass.getSimpleName() + " with id " + id + " was successfully completed");
    }

    private String getFilename(I id){
        final String directory = getFileDirectory();
        final String postfix = getFileExtension();
        return directory + File.separator + id + postfix;
    }

    /**
     * The post initialize method can be used to run functionality for a specific service. The method is called when
     * the method {@link #initialize} has finished successful. The method does not contain any functionality and the
     * whole idea is the it should be overridden by subclasses, but only if certain functionality is required to
     * run after the {@link #initialize} method has completed.
     * @see #initialize
     */
    protected void postInitiate(){
        LOGGER.debug("Post initialize method not implemented for " + entityClass.getSimpleName());
    }

    /**
     * The method returns the directory for the specific file repository. The directory will be used to indicate
     * where files should be saved and loaded from. The method is abstract and every subclass is responsible for
     * overriding the method and provided the directory for their corresponding file type.
     * @return The file directory where the files for the specific file repository could be saved and loaded from.
     */
    protected abstract String getFileDirectory();

    /**
     * The method returns the postfix for the file that the file repository is responsible for managing.
     * The method is abstract and every subclass is responsible for overriding the method and provided the postfix
     * for their corresponding file type.
     * @return The file extension for the file type that the repository is responsible for managing .
     */
    protected abstract String getFileExtension();

    /**
     * The method is responsible for controller that the type that is about the be saved to the file system is valid.
     * The method should check if the type contains all the necessary values and that the values are valid. This method
     * will always be called before a type is about to be saved. The main reason for why this is vital and done before
     * saving is to make sure that the type can be correctly saved to the file system, but also loaded from the
     * file system upon application startup. The method will throw an exception in case of the type not being acceptable.
     * @param type The instance of the type that will be checked and controlled before it is allowed to be saved on
     *             the file system.
     * @see #save
     */
    protected abstract void checkType(T type);


    /**
     * The method is responsible for loading specific files from the file system. The files are identified by the
     * file name extension and from which directory that are stored in.
     * @return A collection with the loaded types retrieved from the file system
     * @throws IllegalStateException The FileException is thrown in case of creation of the
     *                                                       folder fails (If it does not exist) or if the directory
     *                                                       is not a folder but a file instead.
     *
     * @see #getFileDirectory()
     * @see #getFileExtension()
     */
    protected Collection<T> loadFiles(){
        final String directory = getFileDirectory();
        final String postfix = getFileExtension();
        final Collection<T> loadedTypes = fileRepositorySupport.load(entityClass, directory, postfix);
        return loadedTypes;
    }

    protected String generateId(){
        return RandomStringUtils.random(6, true, true);
    }
}

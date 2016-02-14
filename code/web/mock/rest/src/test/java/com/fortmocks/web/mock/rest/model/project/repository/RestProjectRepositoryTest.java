package com.fortmocks.web.mock.rest.model.project.repository;


import com.fortmocks.core.mock.rest.model.project.domain.RestProject;
import com.fortmocks.web.basis.support.FileRepositorySupport;
import com.fortmocks.web.mock.rest.model.project.RestProjectDtoGenerator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Karl Dahlgren
 * @since 1.4
 */
public class RestProjectRepositoryTest {

    @Mock
    private FileRepositorySupport fileRepositorySupport;

    @InjectMocks
    private RestProjectRepositoryImpl repository;
    private static final String DIRECTORY = "/directory";
    private static final String EXTENSION = ".extension";

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(repository, "restProjectFileDirectory", DIRECTORY);
        ReflectionTestUtils.setField(repository, "restProjectFileExtension", EXTENSION);
    }

    @Test
    public void testInitialize(){
        List<RestProject> restProjects = new ArrayList<RestProject>();
        RestProject restProject = RestProjectDtoGenerator.generateFullRestProject();
        restProjects.add(restProject);
        Mockito.when(fileRepositorySupport.load(RestProject.class, DIRECTORY, EXTENSION)).thenReturn(restProjects);
        repository.initialize();
        Mockito.verify(fileRepositorySupport, Mockito.times(1)).load(RestProject.class, DIRECTORY, EXTENSION);
    }

    @Test
    public void testFindOne(){
        final RestProject restProject = save();
        final RestProject returnedRestEvent = repository.findOne(restProject.getId());
        Assert.assertEquals(restProject, returnedRestEvent);
    }

    @Test
    public void testFindAll(){
        final RestProject restProject = save();
        final List<RestProject> restProjects = repository.findAll();
        Assert.assertEquals(restProjects.size(), 1);
        Assert.assertEquals(restProjects.get(0), restProject);
    }

    @Test
    public void testSave(){
        final RestProject restProject = save();
        Mockito.verify(fileRepositorySupport, Mockito.times(1)).save(restProject, DIRECTORY + File.separator + restProject.getId() + EXTENSION);
    }

    @Test
    public void testDelete(){
        final RestProject restProject = save();
        repository.delete(restProject.getId());
        Mockito.verify(fileRepositorySupport, Mockito.times(1)).delete(DIRECTORY + File.separator + restProject.getId() + EXTENSION);
    }

    @Test
    public void testCount(){
        final RestProject restProject = save();
        final Integer count = repository.count();
        Assert.assertEquals(new Integer(1), count);
    }

    private RestProject save(){
        RestProject restProject = RestProjectDtoGenerator.generateFullRestProject();
        repository.save(restProject);
        return restProject;
    }

}

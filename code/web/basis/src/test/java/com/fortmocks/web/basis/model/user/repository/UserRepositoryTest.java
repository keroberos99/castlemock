package com.fortmocks.web.basis.model.user.repository;


import com.fortmocks.core.basis.model.user.domain.User;
import com.fortmocks.web.basis.model.user.dto.UserDtoGenerator;
import com.fortmocks.web.basis.support.FileRepositorySupport;
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
public class UserRepositoryTest {

    @Mock
    private FileRepositorySupport fileRepositorySupport;

    @InjectMocks
    private UserRepositoryImpl repository;
    private static final String DIRECTORY = "/directory";
    private static final String EXTENSION = ".extension";

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(repository, "userFileDirectory", DIRECTORY);
        ReflectionTestUtils.setField(repository, "userFileExtension", EXTENSION);
    }

    @Test
    public void testInitialize(){
        List<User> users = new ArrayList<User>();
        User user = UserDtoGenerator.generateUser();
        users.add(user);
        Mockito.when(fileRepositorySupport.load(User.class, DIRECTORY, EXTENSION)).thenReturn(users);
        repository.initialize();
        Mockito.verify(fileRepositorySupport, Mockito.times(1)).load(User.class, DIRECTORY, EXTENSION);
    }

    @Test
    public void testFindOne(){
        final User user = save();
        final User returnedUser = repository.findOne(user.getId());
        Assert.assertEquals(user, returnedUser);
    }

    @Test
    public void testFindAll(){
        final User user = save();
        final List<User> users = repository.findAll();
        Assert.assertEquals(users.size(), 1);
        Assert.assertEquals(users.get(0), user);
    }

    @Test
    public void testSave(){
        final User user = save();
        Mockito.verify(fileRepositorySupport, Mockito.times(1)).save(user, DIRECTORY + File.separator + user.getId() + EXTENSION);
    }

    @Test
    public void testDelete(){
        final User user = save();
        repository.delete(user.getId());
        Mockito.verify(fileRepositorySupport, Mockito.times(1)).delete(DIRECTORY + File.separator + user.getId() + EXTENSION);
    }

    @Test
    public void testCount(){
        final User user = save();
        final Integer count = repository.count();
        Assert.assertEquals(new Integer(1), count);
    }

    private User save(){
        final User user = UserDtoGenerator.generateUser();
        repository.save(user);
        return user;
    }

}

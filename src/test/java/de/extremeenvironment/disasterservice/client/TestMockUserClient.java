package de.extremeenvironment.disasterservice.client;

import de.extremeenvironment.disasterservice.domain.User;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Created by on 10.07.16.
 *
 * @author David Steiman
 */
@Component
public class TestMockUserClient implements UserClient {

    private long userIdCounter = 1;

    @Override
    public User getUserById(Long id) {
        return new User(id);
    }

    @Override
    public User getUserByName(@PathVariable("name") String name) {
        return new User(userIdCounter++);
    }
}

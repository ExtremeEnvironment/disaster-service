package de.extremeenvironment.disasterservice.client;

import de.extremeenvironment.disasterservice.domain.User;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by on 10.07.16.
 *
 * @author David Steiman
 */
@Component
public class TestMockUserClient implements UserClient {

    private long userIdCounter = 1;

    private Map<String, User> users = new HashMap<>();

    @Override
    public User getUserById(Long id) {
        return new User(id);
    }

    @Override
    public User getUserByName(String name) {
        User user;
        if ((user = users.get(name)) != null) {
            return user;
        } else {
            user = new User(userIdCounter++);
            users.put(name, user);
            return user;
        }
    }
}

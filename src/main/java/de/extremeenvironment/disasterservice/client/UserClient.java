package de.extremeenvironment.disasterservice.client;

import de.extremeenvironment.disasterservice.domain.User;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by on 10.07.16.
 *
 * @author David Steiman
 */
@FeignClient("userservice")
public interface UserClient {
    @RequestMapping(value = "/api/users/id:{id}", method = RequestMethod.GET)
    User getUserById(@PathVariable("id") Long id);

    @RequestMapping(value = "/api/users/{name}", method = RequestMethod.GET)
    User getUserByName(@PathVariable("name") String name);
}

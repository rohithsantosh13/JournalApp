package net.rohith.jatla.journalApp.service;

import net.rohith.jatla.journalApp.entity.User;
import net.rohith.jatla.journalApp.repository.UserRepository;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserServiceTests {

    @Autowired
    UserRepository userRepository;

    @ParameterizedTest
    @CsvSource({"rohith","admin"})
    public void findByUserName(String username){
        User user = userRepository.findByUserName(username);
        assertTrue(user.getUserName().equals(username));
    }
}

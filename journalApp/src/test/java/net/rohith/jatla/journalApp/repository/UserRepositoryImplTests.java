package net.rohith.jatla.journalApp.repository;

import net.rohith.jatla.journalApp.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class UserRepositoryImplTests {

    @Autowired
    private UserRepositoryImpl userRepositoryImpl;

    @Test
    public void getUsersForSA(){
        List<User> usersFroSA = userRepositoryImpl.getUsersForSA();
       assertNotNull(usersFroSA);
    }
}

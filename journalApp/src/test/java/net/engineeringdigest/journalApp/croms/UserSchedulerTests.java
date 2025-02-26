package net.engineeringdigest.journalApp.croms;

import net.engineeringdigest.journalApp.scheduler.UserScheduler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class UserSchedulerTests {

    @Autowired
    private UserScheduler userScheduler;

    @Test
    public void fetchUsersAndSendSaMailTest(){
       userScheduler.fetchUsersAndSendSaMail();
    }

}

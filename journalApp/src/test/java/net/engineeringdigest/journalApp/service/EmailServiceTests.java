package net.engineeringdigest.journalApp.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EmailServiceTests {

    @Autowired
    private EmailService emailService;
    @Test
    public void SendEmailTest(){
        emailService.sendEmail("rohithj095@gmail.com","Testing Java Mail Service","Hi Rohith Jatla this is Rohith Jatla");

    }
}

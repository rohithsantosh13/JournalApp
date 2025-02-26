package net.engineeringdigest.journalApp;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableScheduling
@EnableTransactionManagement
public class JournalApplication {

    public static void main(String[] args) {
        ApplicationContext context =  SpringApplication.run(JournalApplication.class, args);
        System.out.println(context.getEnvironment().getActiveProfiles()[0]);
    }

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }


}
package net.engineeringdigest.journalApp.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import net.engineeringdigest.journalApp.api.response.WeatherResponse;
import net.engineeringdigest.journalApp.entity.User;
import net.engineeringdigest.journalApp.repository.UserRepository;
import net.engineeringdigest.journalApp.service.UserService;
import net.engineeringdigest.journalApp.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/user")
@Tag(name="User APIs",description = "Create, Update and Delete")
public class UserController {

    @Autowired // fetches the bean from IOC container which is nothing but the application context.
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WeatherService weatherService;

    @PutMapping
    public ResponseEntity<?> updateUser(@RequestBody User user){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User userInDb = userService.findByUserName(username);
        if(userInDb !=null){
            if (user.getUserName() != null && !user.getUserName().isEmpty()) {
                userInDb.setUserName(user.getUserName());
            }
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                userInDb.setPassword(user.getPassword());
            }
            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                userInDb.setEmail(user.getEmail());
            }
            // Only update sentimentAnalysis if present and different
            if (user.isSentimentAnalysis() != userInDb.isSentimentAnalysis()) {
                userInDb.setSentimentAnalysis(user.isSentimentAnalysis());
            }
            userService.saveUser(userInDb);
            return new ResponseEntity<>(userInDb, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping
    public ResponseEntity<?> deleteUserById(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        userRepository.deleteByUserName(authentication.getName());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/greetings")
    public  ResponseEntity<?> greetings(){
        String greeting = "";
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        WeatherResponse weatherResponse = weatherService.getWeather("Dayton");
        if(weatherResponse!=null){
            greeting = "weather today in Dayton is "+weatherResponse.getCurrent().getTemperature();
        }
        return new ResponseEntity<>("Hi "+authentication.getName()+" "+ greeting + "°C",HttpStatus.OK);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findByUserName(username);
        if (user != null) {
            // Optionally, you can create a DTO to avoid exposing sensitive fields
            return new ResponseEntity<>(user, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}

package net.rohith.jatla.journalApp.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import net.rohith.jatla.journalApp.api.response.WeatherResponse;
import net.rohith.jatla.journalApp.entity.User;
import net.rohith.jatla.journalApp.repository.UserRepository;
import net.rohith.jatla.journalApp.service.UserService;
import net.rohith.jatla.journalApp.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.stream.Collectors;


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

    private String capitalize(String input) {
        return Arrays.stream(input.split(" "))
                .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1))
                .collect(Collectors.joining(" "));
    }


    @GetMapping("/greetings")
    public ResponseEntity<String> greetings(@RequestParam(required = false) String city) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : "User";

        // Normalize city input
        if (city == null || city.trim().isEmpty()) {
            city = "Dayton";
        } else {
            city = city.trim().toLowerCase();
        }

        StringBuilder message = new StringBuilder("");

        try {
            WeatherResponse weatherResponse = weatherService.getWeather(city);
            if (weatherResponse != null && weatherResponse.getCurrent() != null) {
                message.append("Hello ")
                        .append(capitalize(username))
                        .append("! It’s currently ")
                        .append(weatherResponse.getCurrent().getTemperature())
                        .append("°C in ")
                        .append(capitalize(city))
                        .append(". Have a great day!");
            } else {
                message.append("Hi ")
                        .append(capitalize(username))
                        .append("! Sorry, weather data for ")
                        .append(capitalize(city))
                        .append(" is currently unavailable.");
            }
            return ResponseEntity.ok(message.toString());
        } catch (Exception e) {
            // Optional: log the error
            // logger.error("Error fetching weather for city: " + city, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Hi " + username + "! Unable to fetch weather at the moment. Please try again later.");
        }
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

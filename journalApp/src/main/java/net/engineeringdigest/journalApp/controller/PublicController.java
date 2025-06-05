package net.engineeringdigest.journalApp.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import net.engineeringdigest.journalApp.entity.User;
import net.engineeringdigest.journalApp.service.UserDetailsServiceImpl;
import net.engineeringdigest.journalApp.service.UserService;
import lombok.extern.slf4j.Slf4j;
import net.engineeringdigest.journalApp.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/public")
@Tag(name="Public APIs")
public class PublicController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtils jwtUtils;

    @GetMapping("/health-check")
    public String healthCheck(){
        return "ok";
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody User user) {
        if (user.getUserName() == null || user.getUserName().trim().isEmpty() ||
            user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username and password are required.");
        }
        try {
            userService.saveNewUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body("Signup successful");
        } catch (org.springframework.dao.DuplicateKeyException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred. Please try again.");
        }
    }


    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User user){
        System.out.println("Login attempt: userName=" + user.getUserName() + ", password=" + user.getPassword());
        try{
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUserName(),user.getPassword()));
            UserDetails userDetails =  userDetailsService.loadUserByUsername(user.getUserName());
            String token = jwtUtils.generateToken(userDetails.getUsername());
            return new ResponseEntity<>(token,HttpStatus.OK);
        }
        catch (Exception e){
            log.error("Exception Occurred",e);
            return new ResponseEntity<>("Incorrect Username or password",HttpStatus.BAD_REQUEST);
        }
    }
}

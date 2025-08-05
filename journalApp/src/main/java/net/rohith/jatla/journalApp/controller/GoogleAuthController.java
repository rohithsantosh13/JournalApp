package net.rohith.jatla.journalApp.controller;

import lombok.extern.slf4j.Slf4j;
import net.rohith.jatla.journalApp.repository.UserRepository;
import net.rohith.jatla.journalApp.service.UserDetailsServiceImpl;
import net.rohith.jatla.journalApp.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import net.rohith.jatla.journalApp.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("auth/google")
@Slf4j
public class GoogleAuthController {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtil;

    @GetMapping("/login")
    public ResponseEntity<?> initiateGoogleLogin() {
        String authUrl = "https://accounts.google.com/o/oauth2/auth?" +
                "client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&scope=openid%20email%20profile" +
                "&response_type=code" +
                "&access_type=offline";
        
        return ResponseEntity.ok(Collections.singletonMap("authUrl", authUrl));
    }

    @GetMapping("/callback")
    public ResponseEntity<?> handleGoogleCallback(@RequestParam String code) {
        try {
            log.info("Received authorization code: {}", code);
            
            // Exchange authorization code for tokens
            String tokenEndpoint = "https://oauth2.googleapis.com/token";
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("code", code);
            params.add("client_id", clientId);
            params.add("client_secret", clientSecret);
            params.add("redirect_uri", redirectUri);
            params.add("grant_type", "authorization_code");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenEndpoint, request, Map.class);
            
            if (tokenResponse.getStatusCode() != HttpStatus.OK || tokenResponse.getBody() == null) {
                log.error("Failed to exchange code for tokens");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to authenticate with Google");
            }

            String idToken = (String) tokenResponse.getBody().get("id_token");
            if (idToken == null) {
                log.error("No ID token received from Google");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No ID token received");
            }

            // Get user information from Google
            String userInfoUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
            ResponseEntity<Map> userInfoResponse = restTemplate.getForEntity(userInfoUrl, Map.class);
            
            if (userInfoResponse.getStatusCode() == HttpStatus.OK && userInfoResponse.getBody() != null) {
                Map<String, Object> userInfo = userInfoResponse.getBody();
                String email = (String) userInfo.get("email");
                String name = (String) userInfo.get("name");
                String picture = (String) userInfo.get("picture");
                
                log.info("Google user info - Email: {}, Name: {}", email, name);

                // Defensive: check for multiple users with the same email
                List<User> usersWithEmail = ((List<User>) userRepository.findAll())
                    .stream().filter(u -> email.equals(u.getEmail())).collect(Collectors.toList());
                if (usersWithEmail.size() > 1) {
                    log.error("Multiple users found with the same email: {}. Please resolve duplicates in the database.", email);
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("Multiple users found with the same email. Please contact support.");
                }
                User user = usersWithEmail.isEmpty() ? null : usersWithEmail.get(0);
                if (user == null) {
                    // User doesn't exist, create new user
                    log.info("Creating new user for email: {}", email);
                    user = User.builder()
                            .email(email)
                            .userName(email)
                            .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                            .roles(Arrays.asList("USER"))
                            .sentimentAnalysis(false)
                            .journalEntries(new ArrayList<>())
                            .build();
                    user = userRepository.save(user);
                    log.info("New user created with ID: {}", user.getId());
                }

                String jwtToken = jwtUtil.generateToken(email);
                
                Map<String, Object> response = new HashMap<>();
                response.put("token", jwtToken);
                response.put("user", user);
                response.put("message", "Authentication successful");
                
                return ResponseEntity.ok(response);
            }
            
            log.error("Failed to get user info from Google");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to get user information");
            
        } catch (Exception e) {
            log.error("Exception occurred while handling Google callback", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred during authentication");
        }
    }
}

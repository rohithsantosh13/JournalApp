package net.rohith.jatla.journalApp.config;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI myCustomConfig(){
        return new OpenAPI().info(new Info().title("Journal APIs").description("Rohith Jatla"))
                .servers(
                        Arrays.asList(
                        new Server().url("http://localhost:8080").description("local"),
                        new Server().url("http://localhost:8082").description("live")
                )
        )
                .tags(Arrays.asList(new Tag().name("Public APIs"),
                        new Tag().name("User APIs"),
                        new Tag().name("Journal APIs"),
                        new Tag().name("Admin APIs")
                        ));
    }
}

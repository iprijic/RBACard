package rbacore;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix="spring.datasource")
@Configuration
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Configurations {

    private String username;
    private String password;
    private String platform;
}

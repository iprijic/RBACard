package rbacore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class PreInitConfig {
    public PreInitConfig(Environment env) {

    }

   @Bean
    public DataSourceBuilder GetDataSourceBuilder(){
        return new DataSourceBuilder(null);
    }
}

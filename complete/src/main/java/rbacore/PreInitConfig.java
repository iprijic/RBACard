package rbacore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class PreInitConfig {

    @Autowired
    private Environment env;

    public PreInitConfig() {

    }

    @Bean
    public DataSourceBuilder GetDataSourceBuilder(){
        return new DataSourceBuilder(null);
    }
}

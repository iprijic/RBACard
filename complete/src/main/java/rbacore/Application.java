package rbacore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import java.sql.SQLException;


@SpringBootApplication
public class Application {

    @Autowired
    private Environment env;
    @Bean
    public DataSourceBuilder BuildDS(){
        return new DataSourceBuilder(env);
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        ConfigurableListableBeanFactory bf = context.getBeanFactory();
     }
}

package rbacore;

import jakarta.persistence.metamodel.EntityType;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.metamodel.model.domain.internal.MappingMetamodelImpl;
import org.postgresql.ds.PGSimpleDataSource;
import org.postgresql.jdbc.PgResultSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

//@org.springframework.context.annotation.Configuration
@Component
public class DataSourceBuilder {
    private final Environment env;

    private SessionFactory sessionFactory;

    public DataSourceBuilder(Environment env) {

        if(env != null) {
            this.env = env;

            if (createDataSource()) {
                buildSessionFactory();
            } else
                this.sessionFactory = null;
        }
        else
            this.env = null;
    }

    private Boolean createDataSource() {

        final PGSimpleDataSource dataSource = new PGSimpleDataSource();

        final String databaseName = this.env.getProperty("spring.datasource.databasename");

        dataSource.setServerNames(new String[] { this.env.getProperty("spring.datasource.servername") });
        final String portnumber = this.env.getProperty("spring.datasource.portnumber");
        dataSource.setPortNumbers(new int[] { Integer.parseInt(portnumber == null ? "8080" : portnumber) });
//        dataSource.setDatabaseName(this.env.getProperty("spring.datasource.databasename"));
        dataSource.setUser(this.env.getProperty("spring.datasource.username"));
        dataSource.setPassword(this.env.getProperty("spring.datasource.password"));

        try
        {
            Connection conn = dataSource.getConnection();

            PreparedStatement stmt = conn
                    .prepareStatement(String.format("select exists(SELECT datname FROM pg_catalog.pg_database WHERE datname = '%s')",databaseName));
            ResultSet rs = stmt.executeQuery();
            Boolean dbExist = false;

            while (rs.next()){
                dbExist = ((PgResultSet) rs).getBoolean("exists");
                if(dbExist) {
                    break;
                }
            }

            if(dbExist == false) {
                stmt = conn.prepareStatement(String.format("CREATE DATABASE \"%s\"", databaseName));
                stmt.executeUpdate();

                dataSource.setDatabaseName(databaseName);
                conn = dataSource.getConnection();

                String sqlCreate =
                        "CREATE TABLE public.\"Card\" (" +
                        "\"ID\" int8 NOT NULL GENERATED ALWAYS AS IDENTITY," +
                        "\"FirstName\" varchar NOT NULL," +
                        "\"LastName\" varchar NOT NULL," +
                        "\"Identifier\" varchar NOT NULL," +
                        "\"Status\" int4 NOT NULL);";

                stmt = conn.prepareStatement(sqlCreate);
                stmt.executeUpdate();
            }
            else {
                dataSource.setDatabaseName(databaseName);
                conn = dataSource.getConnection();
            }

        }
        catch (Exception ex)
        {
            System.out.println("Invalid database connection. Please verify your username,password,database name,port number or server name");
            return false;
        }
        return true;
    }

    private void buildSessionFactory() {

        Configuration conf = new Configuration();

        Properties props = new Properties();
        final String url = String.format("jdbc:postgresql://%s:%s/%s",
                this.env.getProperty("spring.datasource.servername"),
                this.env.getProperty("spring.datasource.portnumber"),
                this.env.getProperty("spring.datasource.databasename")
        );

        props.put("hibernate.connection.driver_class", "org.postgresql.Driver");
        props.put("hibernate.connection.url", url);
        props.put("hibernate.connection.username", this.env.getProperty("spring.datasource.username"));
        props.put("hibernate.connection.password", this.env.getProperty("spring.datasource.password"));
        props.put("hibernate.current_session_context_class", "thread");

        Class<?> cls = TypeFromModel();

        sessionFactory = conf
                .setProperties(props)
                .addAnnotatedClass(cls)
                .buildSessionFactory();
        }

    @Bean
    public SessionFactory getSessionFactory()
    {
        return  sessionFactory;
    }

    private Class<?> TypeFromModel() {
        return model.Card.class;
    }

    public static Class<?> GetPersistType(SessionFactory sessionFactory,Class<?> modelType) {
        MappingMetamodelImpl metaModel = (MappingMetamodelImpl) sessionFactory.getMetamodel();
        EntityType<?> ent = metaModel.entity(modelType.getName());
        return ent.getJavaType();
    }

    public static Boolean CreatePersistObject(Session session, Class<?> c, Class[] propTypes, Object[] propValues) {

        // Ne radi new NekiObjektizModela. Možda nešto ne valja sa transient/detached/persist objektima.
        // Mora se ovako.

        try {
            session.getTransaction().begin();
            Object inst = c.getConstructor().newInstance();
            Object attached = inst
                    .getClass()
                    .getMethod("attach",propTypes)
                    .invoke(inst , propValues);

            session.persist(attached);
            session.getTransaction().commit();
            //session.flush();

            return true;
        }
        catch (Exception ee)
        {
            return false;
        }
    }
}

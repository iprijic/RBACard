package rbacore;

import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;

import org.hibernate.*;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.internal.SessionImpl;
import org.hibernate.loader.access.IdentifierLoadAccessImpl;
import org.hibernate.loader.access.LoadAccessContext;
import org.hibernate.metamodel.model.domain.EntityDomainType;
import org.hibernate.metamodel.model.domain.JpaMetamodel;
import org.hibernate.metamodel.model.domain.internal.JpaMetamodelImpl;
import org.hibernate.metamodel.model.domain.internal.MappingMetamodelImpl;
import org.hibernate.metamodel.spi.MappingMetamodelImplementor;
import org.hibernate.metamodel.spi.MetamodelImplementor;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.query.Query;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaCriteriaUpdate;
import org.hibernate.query.sqm.internal.SqmCriteriaNodeBuilder;
import org.hibernate.query.sqm.tree.expression.SqmExpression;
import org.hibernate.query.sqm.tree.expression.SqmParameter;
import org.hibernate.query.sqm.tree.from.SqmRoot;
import org.hibernate.query.sqm.tree.select.SqmSelectStatement;
import org.hibernate.query.sqm.tree.update.SqmUpdateStatement;
import org.hibernate.service.ServiceRegistry;
import org.postgresql.ds.PGSimpleDataSource;
import org.postgresql.jdbc.PgResultSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

@Component
@Scope("singleton")
public class DataSourceBuilder {

    /*
    @Value("${spring.application.name}")
    String applicationName;


    @Autowired
    private Environment env;

    @Autowired
    private Configurations config;
    */

    private final Environment env;

    private SessionFactory sessionFactory;

    public DataSourceBuilder(Environment env) {
        this.env = env;

        if(createDataSource()) {
           buildSessionFactory();
        }
        else
            this.sessionFactory = null;
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



        //Class<?> cls = user1.getClass();
        Class<?> cls = User123.class;

        sessionFactory = conf
                .setProperties(props)
                .addAnnotatedClass(cls)
                .buildSessionFactory();


        MappingMetamodelImpl metaModel = (MappingMetamodelImpl) sessionFactory.getMetamodel();
        EntityType<?> ent = metaModel.entity(cls.getName());
        Class<?> c = ent.getJavaType();

        Session session = sessionFactory.openSession();



        User123 user1 = new User123();
        user1.setId(2000L);
        user1.setFirstName("Diana");

        //Map<String, Object> p1 = session.getProperties();

        //  session.save((Object)user1);


        // session.persist(user1);
        // session.getTransaction().commit();




        SqmSelectStatement cq = (SqmSelectStatement) session.getCriteriaBuilder().createQuery(c);
        SqmRoot rootEntry = (SqmRoot) cq.from(ent);
        CriteriaQuery<?> all = cq.select(rootEntry);
        TypedQuery<?> query = session.createQuery(all);
        List<?> selected = query.getResultList();

        //-------------

        SqmUpdateStatement update = (SqmUpdateStatement) session.getCriteriaBuilder().createCriteriaUpdate(c);

        SqmRoot e = (SqmRoot) update.from((Class<User123>) c);
        update.set("firstName", "Marko Stanić");
        update.where(session.getCriteriaBuilder().equal(e.get("id"), 4L));

        session.getTransaction().begin();
        int nRows = session.createQuery(update).executeUpdate();
        session.getTransaction().commit();

        //-------------


//        User user2 = new User(3500,"Rose" );
//        User user3 = new User(2500,"Denise" );
//        User user4 = new User(4000,"Mike" );
//        User user5 = new User(4500,"Linda" );

//        SqmExpression<User> ev1 = session.getCriteriaBuilder().value(user1);


        try {
            session.getTransaction().begin();
            Object inst = c.getConstructor().newInstance();
            Object attached = inst
                    .getClass()
                    .getMethod("attach",new Class[] { Long.class,String.class })
                    .invoke(inst , new Object[]{ null,"Diana" });

            session.persist(attached);
            session.getTransaction().commit();
            session.flush();
        }
        catch (Exception ee)
        {
        }

        int z = 0;
        z++;


}

    public SessionFactory getSessionFactory()
    {
        return  sessionFactory;
    }
}

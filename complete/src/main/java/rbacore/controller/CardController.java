package rbacore.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.TypedQuery;
import model.Card;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.sqm.internal.SqmCriteriaNodeBuilder;
import org.hibernate.query.sqm.tree.delete.SqmDeleteStatement;
import org.hibernate.query.sqm.tree.from.SqmRoot;
import org.hibernate.query.sqm.tree.select.SqmSelectStatement;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import rbacore.DataSourceBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

//@Controller

@RestController
public class CardController {

    private final Environment env;

    public CardController(Environment env)
    {
        this.env = env;
    }

    private String getCardAssetsPath() {
        return new File( Paths.get(".").toAbsolutePath().normalize().toString(),env.getProperty("spring.content.cardassets")).toString();
    }

    private String getCardTemplateFilename(String prefix) {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        Date resultDate;

        try {
            resultDate = sdf.parse(Instant.now().toString());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        return String.join("#",prefix, sdf.format(resultDate).toString().replace(':','-'));
    }

    private Class<?> typeFromModel() {
        return model.Card.class;
    }

    private File createCardTemplateFile(String id) {

        String path = getCardAssetsPath();
        String fullPath;
        File directory = new File(path);
        if (directory.exists() == false){
            directory.mkdir();
        }

        if(path.endsWith("\\")) {
            fullPath = path + getCardTemplateFilename(id).toString() + ".txt";
        }
        else {
            fullPath = path + "\\" + getCardTemplateFilename(id).toString() + ".txt";
        }

        File newFile = new File(fullPath);
        boolean success;
        try {
            success = newFile.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if(success)
            return newFile;
        else
            return  null;
    }

    @Autowired
    private SessionFactory sessionFactory;

    @GetMapping("/card")
    public String card() {

        return "greeting";
    }

    @GetMapping(value = "/person")
    public  ResponseEntity person(@RequestParam(value="oib", required=true) String id) {
        Session session = sessionFactory.openSession();
        session.getTransaction().begin();
        SqmCriteriaNodeBuilder cb = (SqmCriteriaNodeBuilder) sessionFactory.openSession().getCriteriaBuilder();
        Class<?> entityType = DataSourceBuilder.GetPersistType(sessionFactory, typeFromModel());
        SqmSelectStatement<?> cr = cb.createQuery(entityType);
        SqmRoot entity = cr.from(entityType);
        SqmSelectStatement queryFinal = cr.select(entity).where(cb.equal(entity.get("identifier"), id));
        TypedQuery<?> query = session.createQuery(queryFinal);
        List<?> selected = query.getResultList();
        session.getTransaction().commit();

        if(selected.size() == 0) {
            return new ResponseEntity("This person does not exist.", HttpStatus.NOT_FOUND);

        }
        else if(selected.size() > 1) {
            return new ResponseEntity("Too many persons contains same identifier (OIB).", HttpStatus.BAD_REQUEST);
        }

        String jsonItem;
        ObjectMapper mapper = new ObjectMapper();
        Object item = selected.get(0);

        try {
            jsonItem = mapper.writeValueAsString(selected.get(0));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        File resultnewFile = createCardTemplateFile(id);
        if(resultnewFile != null) {
            try {
                FileWriter myWriter = new FileWriter(resultnewFile);
                myWriter.write(jsonItem);
                myWriter.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return new ResponseEntity("The person was found in the database.The text card will be created.", HttpStatus.OK);
    }

    @PostMapping("/person")
    public ResponseEntity person(@ModelAttribute("person")Card person) {
        Class<?> type = DataSourceBuilder.GetPersistType(sessionFactory, typeFromModel());
        Boolean persistResult = DataSourceBuilder.CreatePersistObject(sessionFactory.openSession(),type,
        new Class[] { Long.class,String.class,String.class,String.class,int.class },
        new Object[] { null, person.getFirstName(), person.getLastName(), person.getIdentifier(),person.getStatus() });

        if(persistResult){
            return new ResponseEntity("New person is created successfully.", HttpStatus.OK);

            //return "New person is created successfully.";

        }

        return new ResponseEntity("ERROR: New person is not recorded into database properly.", HttpStatus.INTERNAL_SERVER_ERROR);
        //return "ERROR: New person is not recorded into database properly.";
     }


     @DeleteMapping(value = "/person")
    public  ResponseEntity delete(@RequestParam(value="oib", required=true) String id) {

         Session session = sessionFactory.openSession();
         session.getTransaction().begin();
         SqmCriteriaNodeBuilder cb = (SqmCriteriaNodeBuilder) sessionFactory.openSession().getCriteriaBuilder();
         Class<?> entityType = DataSourceBuilder.GetPersistType(sessionFactory, typeFromModel());
         SqmDeleteStatement delete = cb.createCriteriaDelete(entityType);
         SqmRoot entity = (SqmRoot) delete.from(entityType);
         delete.where(cb.equal(entity.get("identifier"), id));
         int result = session.createMutationQuery(delete).executeUpdate();
         session.getTransaction().commit();
         if(result > 0) {
             return new ResponseEntity("Person is deleted successfully.", HttpStatus.OK);
         }
         else {
             return new ResponseEntity("Failed delete operation.This person does not exist.", HttpStatus.NOT_FOUND);
         }
     }
}

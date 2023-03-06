package rbacore.controller;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import model.Card;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.BootstrapServiceRegistry;
import org.hibernate.query.sqm.internal.SqmCriteriaNodeBuilder;
import org.hibernate.query.sqm.tree.delete.SqmDeleteStatement;
import org.hibernate.query.sqm.tree.from.SqmRoot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import rbacore.DataSourceBuilder;

//@Controller

@RestController
public class CardController {

    public CardController()
    {

    }

    private Class<?> TypeFromModel() {
        return model.Card.class;
    }

    @Autowired
    private SessionFactory sessionFactory;

    @GetMapping("/card")
    public String card() {

        return "greeting";
    }

    @PostMapping("/person")
    public ResponseEntity person(@ModelAttribute("person")Card person) {
        Class<?> type = DataSourceBuilder.GetPersistType(sessionFactory,TypeFromModel());
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
    public  ResponseEntity delete(@RequestParam(value="id", required=true) long id) {

         Session session = sessionFactory.openSession();
         session.getTransaction().begin();
         SqmCriteriaNodeBuilder cb = (SqmCriteriaNodeBuilder) sessionFactory.openSession().getCriteriaBuilder();
         Class<?> entityType = DataSourceBuilder.GetPersistType(sessionFactory,TypeFromModel());
         SqmDeleteStatement delete = cb.createCriteriaDelete(entityType);
         SqmRoot entity = (SqmRoot) delete.from(entityType);
         delete.where(cb.equal(entity.get("id"), id));
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

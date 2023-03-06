package rbacore.controller;

import model.Card;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.BootstrapServiceRegistry;
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
}

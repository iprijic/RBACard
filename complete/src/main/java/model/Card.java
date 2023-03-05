package model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import java.io.Serializable;

@Entity
@Table(name = "\"Card\"", schema = "public")
public class Card {

    public Card()
    {

    }
    public Card attach(Long id,String firstName,String lastName,String identifier,String status)
    {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.identifier = identifier;
        this.status = status;

        return null;
    }

    @Id
    @Column(name = "\"ID\"")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "\"FirstName\"")
    private String firstName;  // Ime

    @Column(name = "\"LastName\"")
    private String lastName;  // Prezime

    @Column(name = "\"Identifier\"")
    private String identifier;  // OIB

    @Column(name = "\"Status\"")
    private String status;  // Mogao bi biti i Boolean

    public Long  getId()
    {
        return id;
    }
    public void setId(Long  id)
    {
        this.id = id;
    }
    public String getFirstName()
    {
        return firstName;
    }
    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }
    public String getIdentifier()
    {
        return identifier;
    }
    public void setIdentifier(String firstName)
    {
        this.identifier = identifier;
    }

}

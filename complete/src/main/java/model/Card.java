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
    public Card attach(Long id,String firstName,String lastName,String identifier,int status)
    {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.identifier = identifier;
        this.status = status;

        return this;
    }

    @Id
    @Column(name = "\"ID\"", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "\"FirstName\"", nullable = false)
    private String firstName;  // Ime

    @Column(name = "\"LastName\"", nullable = false)
    private String lastName;  // Prezime

    @Column(name = "\"Identifier\"", nullable = false)
    private String identifier;  // OIB

    @Column(name = "\"Status\"", nullable = false)
    private int status;  // Mogao bi biti i Boolean

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
    public String getLastName()
    {
        return lastName;
    }
    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }
    public String getIdentifier()
    {
        return identifier;
    }
    public void setIdentifier(String identifier)
    {
        this.identifier = identifier;
    }
    public int getStatus()
    {
        return status;
    }
    public void setStatus(int status)
    {
        this.status = status;
    }

}

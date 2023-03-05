package rbacore;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import java.io.Serializable;

@Entity
@Table(name = "\"User\"", schema = "public")
public class User123 implements Serializable {

    public User123()
    {

    }

    public User123 attach(Long id,String firstName)
    {
        this.id = id;
        this.firstName = firstName;
        return this;
    }

    public void attach1()
    {

    }

    /*
    public User123(Long id,String firstName)
    {
        this.id = id;
        this.firstName = firstName;

    }
*/

    @Id
    @Column(name = "\"ID\"")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long   id;



    @Column(name = "\"FullName\"")
   private String firstName;




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



}

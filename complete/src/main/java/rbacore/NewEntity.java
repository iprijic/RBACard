package rbacore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "new_entity")
public class NewEntity {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    private String firstName; // Ime
    private String lastName; // Prezime
    private String identifier; // OIB
    private int status; // Može i Boolean



}
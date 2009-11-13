package com.doxla;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Entity;

@Entity
public class Domain {

    @Id
    @GeneratedValue
    private Long id;

    private String directoryNumber;

    private Domain() { /** Hibernate **/}

    public Domain(String directoryNumber) {
        this.directoryNumber = directoryNumber;
    }

    public Long getId() {
        return id;
    }

    public String getDirectoryNumber() {
        return directoryNumber;
    }
}

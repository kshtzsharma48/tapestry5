package org.example.app1.entities;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class User2
{
    @Id
    private Long id;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ee.idu0020.entity;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Dmitri
 */
@Entity
@Table(name = "water")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Water.findAll", query = "SELECT w FROM Water w"),
    @NamedQuery(name = "Water.findById", query = "SELECT w FROM Water w WHERE w.id = :id"),
    @NamedQuery(name = "Water.findByName", query = "SELECT w FROM Water w WHERE w.name = :name"),
    @NamedQuery(name = "Water.findByMineralisation", query = "SELECT w FROM Water w WHERE w.mineralisation = :mineralisation"),
    @NamedQuery(name = "Water.findByContent", query = "SELECT w FROM Water w WHERE w.content = :content")})
public class Water implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Column(name = "name")
    private String name;
    @Column(name = "mineralisation")
    private Integer mineralisation;
    @Column(name = "content")
    private String content;

    public Water() {
    }

    public Water(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getMineralisation() {
        return mineralisation;
    }

    public void setMineralisation(Integer mineralisation) {
        this.mineralisation = mineralisation;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Water)) {
            return false;
        }
        Water other = (Water) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ee.idu0020.entity.Water[ id=" + id + " ]";
    }
    
}

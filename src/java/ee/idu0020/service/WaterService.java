/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ee.idu0020.service;

import ee.idu0020.entity.Water;
import java.util.List;

/**
 *
 * @author Dmitri
 */
public interface WaterService {

    public List<Water> findAll();

    public Water findById(int id);

    public void update(Water shirt);
    
    public void delete(Water shirt);
}

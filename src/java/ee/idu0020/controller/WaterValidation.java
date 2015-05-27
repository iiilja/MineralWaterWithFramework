/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ee.idu0020.controller;

import java.util.HashMap;
import java.util.Map;


/**
 *
 * @author iiilja
 */
public class WaterValidation {
  
    public static WaterError validate(String id, String name, String mineralisation){
        WaterError error = new WaterError();
        boolean ok = true;
        try {
            Integer.parseInt(mineralisation);
        } catch (Exception e) {
            error.setMineralisation("Only numbers");
            ok = false;
        }

        try {
            Integer.parseInt(id);
        } catch (Exception e) {
            //errorList.put("id", "Dont touch id !");
        }
        if (name.length() == 0) {
            error.setName("Can not be empty");
            ok = false;
        }
        if (name.length() > 10) {
            error.setName("Max 10 cahracters");
            ok = false;
        }
        return ok ? null : error;
    }
}
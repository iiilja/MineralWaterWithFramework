/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ee.idu0020.controller;

import ee.idu0020.entity.Water;
import ee.idu0020.service.WaterService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author Dmitri
 */
@Controller
public class WaterController {

    public WaterController() {
        System.out.println("\n\nAA\n\n");
    }
    
    
    
    @Autowired(required=true)
    private WaterService waterService;
    
    
    @RequestMapping(value = "s", method = RequestMethod.GET)
    public ModelAndView getAllWater(
            @RequestParam(required = false) Integer id,
            HttpServletRequest request,
            HttpServletResponse response) throws JSONException{
        if (id != null) {
            Map<String, Object> model = new HashMap<>();
            model.put("water", waterService.findById(id));
            return new ModelAndView("water", model);//getWithID(id);
        }
        List<Water> water = waterService.findAll();
        Map<String, Object> model = new HashMap<>();

        model.put("waters", water);
        return new ModelAndView("waters", model);
    }
    
    @RequestMapping(value = "s", method = RequestMethod.POST)
    public ModelAndView doSomeAction(
            @RequestParam String action,
            @RequestParam String name,
            @RequestParam String content,
            @RequestParam String mineralisation,
            @RequestParam String id,
            HttpServletRequest request,
            HttpServletResponse response) throws JSONException{
        
        if (action != null) {
            switch (action){
                case "save":
                    
                    WaterError error = WaterValidation.validate(id, name, mineralisation);
                    Map<String, Object> model = new HashMap<>();
                    Water water = waterService.findById(Integer.parseInt(id));
                    if (error == null) {
                        water.setName(name);
                        water.setContent(content);
                        water.setMineralisation(Integer.parseInt(mineralisation));
                        waterService.update(water);
                        
                    } else {
                        model.put("formError", error);
                    }
                    model.put("water", water);
                    return new ModelAndView("water",model);
                case "create":
                    
                    break;
            }
            return null;
        }
        return null;
    }

    
    @ExceptionHandler(Exception.class)
    public void handleAllException(Exception ex) {
        System.err.println(ex.getMessage());
        ex.printStackTrace();
    }
}

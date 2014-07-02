/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ee.promobox.controller;

import ee.promobox.entity.Users;
import ee.promobox.service.UserService;
import java.io.Writer;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author Maxim
 */
@Controller
public class UserController {

    private final static Logger log = LoggerFactory.getLogger(
            UserController.class);

    @Autowired
    private UserService userService;


    @RequestMapping("/index")
    public ModelAndView indexHandler(
            HttpServletRequest request,
            HttpServletResponse response) throws Exception{
        
   
        List<Users> list = userService.findAllUsers();
        
        for (Users u : list) {
           
            response.getOutputStream().print(u.getFirstname());

        }

        return null;

    }

    private ModelAndView printResult(String text, HttpServletResponse response) throws Exception {

        response.setContentType("text/html; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        Writer writer = response.getWriter();

        writer.write(text);

        writer.flush();

        return null;
    }
}

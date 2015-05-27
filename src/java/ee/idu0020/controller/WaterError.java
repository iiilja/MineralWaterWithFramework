/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ee.idu0020.controller;


/**
 *
 * @author iiilja
 */
public class WaterError {

    private String name;
    private String mineralisation;
    private String content;

    public WaterError() {
    }

    public WaterError(String name, String mineralisation, String content) {
        this.name = name;
        this.mineralisation = mineralisation;
        this.content = content;
    }
    
    

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMineralisation() {
        return mineralisation;
    }

    public void setMineralisation(String mineralisation) {
        this.mineralisation = mineralisation;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

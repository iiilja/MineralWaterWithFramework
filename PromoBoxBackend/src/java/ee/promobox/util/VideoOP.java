/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ee.promobox.util;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author vitalispiridonov
 */
public class VideoOP {
    
    private List<String> args = new ArrayList<String>();
    
    public VideoOP(String path) {
        args.add(path);
    }
    
    public VideoOP input(File file) {
        args.add("-i " + file.getAbsolutePath());
        
        return this;
    }
    
    public VideoOP codecVideo(String videoCodec) {
        args.add("-c:v " + videoCodec);
        
        return this;
    }
    
    public VideoOP codecAudio(String audioCodec) {
        args.add("-c:a " + audioCodec);
        
        return this;
    }
    
    public VideoOP preset(String preset) {
        args.add("-preset " + preset);
        
        return this;
    }
    
    public VideoOP crf(int crf) {
        args.add("-crf " + crf);
        
        return this;
    }
    
    public VideoOP bitrateAudio(String bitrateAudio) {
        args.add("-c:a " + bitrateAudio);
        
        return this;
    }
    
    public boolean processToFile(File outputFile) {
        String out = outputFile.getAbsolutePath();

        
        List<String> arguments = new ArrayList<String>(args);
        arguments.add(out);
        
        System.out.println(arguments);
        try {
            Process process = new ProcessBuilder(arguments).start();
            
            InputStream errorStream = null;
            String error = null;
            try {
                errorStream = process.getErrorStream();
                error = IOUtils.toString(errorStream);
            } finally {
                IOUtils.closeQuietly(errorStream);
            }
            
            if (process.waitFor() != 0) {
            	System.err.print(error);
            } else {
                return true;
            }
        } catch (Exception ex) {
        	System.err.print(ex);
        	ex.printStackTrace();
        }
        
        return false;
    }
}

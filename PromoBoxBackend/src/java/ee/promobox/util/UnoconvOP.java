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
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author vitalispiridonov
 */
public class UnoconvOP {
    
    private final static Logger log = LoggerFactory.getLogger(
            UnoconvOP.class);

    private List<String> args = new ArrayList<String>();
    
    public UnoconvOP(String path) {
        args.add(path);
    }
    
    public UnoconvOP output(File file) {
        args.add("-o");
        args.add(file.getAbsolutePath());

        return this;
    }
    
    public UnoconvOP format(String format) {
        args.add("-f");
        args.add(format);
        
        return this;
    }
    
    public UnoconvOP input(File file) {
        args.add(file.getAbsolutePath());

        return this;
    }
    
    public boolean processToFile() {
        List<String> arguments = new ArrayList<String>(args);

        log.info(toString());

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
                log.error(error);
            } else {
                return true;
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }

        return false;
    }
    
    @Override
    public String toString() {
        return String.format("UnoconvOP[%s]", StringUtils.join(args.iterator(), " "));
    }
}

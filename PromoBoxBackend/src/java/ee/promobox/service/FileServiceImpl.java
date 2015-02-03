/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ee.promobox.service;

import ee.promobox.KioskConfig;
import ee.promobox.controller.DevicesController;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author vitalispiridonov
 */
@Service
public class FileServiceImpl implements FileService{
    
    @Autowired
    KioskConfig config;
    
    private final static Logger log = LoggerFactory.getLogger(FileServiceImpl.class);

    @Override
    public File getClientFolder(int clientId) {
        return new File(config.getDataDir() + clientId + File.separator);
    }

    @Override
    public File getArchiveClientFolder(int clientId) {
        return new File(config.getArchiveDir() + clientId + File.separator);
    }
    
    @Override
    public File getRawFile(int clientId, int fileId) {
        return new File(getClientFolder(clientId), fileId + "");
    }

    @Override
    public File getOutputFile(int clientId, int fileId, Integer page) {
    	try {
	    	String filename = getClientFolder(clientId).getCanonicalPath() + "/" + fileId + "_output";
	    	
	    	if (page != null) {
	    		filename += "-" + page;
	    	}
	    	
	        return new File(filename);
    	} catch (Exception ex) {
    		log.error(ex.getMessage(), ex);
    	}
    	
    	return null;
    }
    
    @Override
    public File getThumbFile(int clientId, int fileId, Integer page) {
    	try {
	    	String filename = getClientFolder(clientId).getCanonicalPath() + "/" + fileId + "_thumb";
	    	
	    	if (page != null) {
	    		filename += "-" + page;
	    	}
	    	
	        return new File(filename);
    	} catch (Exception ex) {
    		log.error(ex.getMessage(), ex);
    	}
    	
    	return null;
    }

	@Override
	public File getOutputMp4File(int clientId, int fileId) {
		
		return new File(getClientFolder(clientId), fileId + "_mp4");
	}
    
    
    
}

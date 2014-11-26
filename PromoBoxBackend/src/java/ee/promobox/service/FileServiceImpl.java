/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ee.promobox.service;

import ee.promobox.KioskConfig;
import java.io.File;
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
    public File getOtputFile(int clientId, int fileId) {
        return new File(getClientFolder(clientId), fileId + "_output");
    }

    @Override
    public File getOtputPortFile(int clientId, int fileId) {
        return new File(getClientFolder(clientId), fileId + "_output_port");
    }
    
    @Override
    public File getThumbFile(int clientId, int fileId) {
        return new File(getClientFolder(clientId), fileId + "_thumb");
    }
    
    
    
}

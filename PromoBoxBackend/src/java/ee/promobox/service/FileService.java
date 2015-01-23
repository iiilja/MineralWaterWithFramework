/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ee.promobox.service;

import java.io.File;

/**
 *
 * @author vitalispiridonov
 */
public interface FileService {
    public File getClientFolder(int clientId);
    public File getArchiveClientFolder(int clientId);
    public File getRawFile(int clientId, int fileId);
    public File getOutputFile(int clientId, int fileId, Integer page);
    public File getOutputMp4File(int clientId, int fileId);
    public File getThumbFile(int clientId, int fileId, Integer page);
}

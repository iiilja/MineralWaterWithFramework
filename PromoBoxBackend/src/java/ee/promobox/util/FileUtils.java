/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ee.promobox.util;

import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author Dan
 */
public class FileUtils {
    
    public static final int FILE_TYPE_JPG = 1;
    public static final int FILE_TYPE_PNG = 2;
    public static final int FILE_TYPE_MP3 = 3;
    public static final int FILE_TYPE_MP4 = 4;
    public static final int FILE_TYPE_AAC = 5;
    public static final int FILE_TYPE_AVI = 6;
    public static final int FILE_TYPE_MOV = 7;
    public static final int FILE_TYPE_PDF = 8;
   
    public static String renameFile(String filename) {
        
        return filename;
    }
    
    public static int determineFileTypeNumber(String filetype) {
        switch (filetype.toUpperCase()) {
            case "JPG":  return FILE_TYPE_JPG;
            case "JPEG": return FILE_TYPE_JPG;
            case "PNG": return FILE_TYPE_PNG;
            case "MP3": return FILE_TYPE_MP3;
            case "MP4": return FILE_TYPE_MP4;
            case "AAC": return FILE_TYPE_AAC;
            case "AVI": return FILE_TYPE_AVI;
            case "MOV": return FILE_TYPE_MOV;
            case "PDF": return FILE_TYPE_PDF;
            default: return 0;
        }
    }
    
}

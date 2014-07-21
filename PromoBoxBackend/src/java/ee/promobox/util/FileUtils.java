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
   
    public static String renameFile(String filename) {
        
        return filename;
    }
    
    public static int determineFileTypeNumber(String filetype) {
        switch (filetype.toUpperCase()) {
            case "JPG":  return 1;
            case "JPEG": return 2;
            case "PNG": return 3;
            case "MP3": return 4;
            case "MP4": return 5;
            case "AAC": return 6;
            case "AVI": return 7;
            case "MOV": return 8;
            case "PDF": return 9;
            default: return 0;
        }
    }
    
}

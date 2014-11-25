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
public class VideoOP {

    private final static Logger log = LoggerFactory.getLogger(
            VideoOP.class);

    private List<String> args = new ArrayList<String>();

    public VideoOP(String path) {
        args.add(path);
    }

    public VideoOP input(File file) {
        args.add("-i");
        args.add(file.getAbsolutePath());

        return this;
    }

    public VideoOP codecVideo(String videoCodec) {
        args.add("-c:v");
        args.add(videoCodec);

        return this;
    }

    public VideoOP codecAudio(String audioCodec) {
        args.add("-c:a");
        args.add(audioCodec);

        return this;
    }

    public VideoOP preset(String preset) {
        args.add("-preset");
        args.add(preset);

        return this;
    }

    public VideoOP crf(int crf) {
        args.add("-crf");
        args.add("" + crf);

        return this;
    }

    public VideoOP bitrateAudio(String bitrateAudio) {
        args.add("-b:a");
        args.add(bitrateAudio);

        return this;
    }
    
    public VideoOP bitrateVideo(String bitrateVideo) {
        args.add("-b:v");
        args.add(bitrateVideo);

        return this;
    }
    
    public VideoOP maxrate(String maxrate) {
        args.add("-maxrate");
        args.add(maxrate);

        return this;
    }

    public VideoOP scale(String scale) {
        args.add("-vf");
        args.add("scale=" + scale);

        return this;
    }
    
    public VideoOP flip(String flip) {
        args.add("-vf");
        args.add(flip);

        return this;
    }
    
    public VideoOP format(String format) {
        args.add("-f");
        args.add(format);

        return this;
    }
    
     public VideoOP thumbnail() {
        args.add("-ss");
        args.add(String.valueOf(2));
        args.add("-vframes");
        args.add(String.valueOf(1));

        return this;
    }

    public boolean processToFile(File outputFile) {
        String out = outputFile.getAbsolutePath();

        List<String> arguments = new ArrayList<String>(args);
        arguments.add(out);

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
        return String.format("VideoOp[%s]", StringUtils.join(args.iterator(), " "));
    }
}

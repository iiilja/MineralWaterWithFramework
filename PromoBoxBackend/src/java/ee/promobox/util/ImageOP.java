package ee.promobox.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageOP {
    //private static final Log log = LogFactory.getLog(ImageOP.class);

    private final static Logger log = LoggerFactory.getLogger(
            ImageOP.class);

    private List<String> args = new ArrayList<String>();
    private byte[] inputData;

    private String outputFormat;

    public ImageOP(String path) {
        args.add(path);
    }

    public ImageOP input(byte[] data) {
        inputData = data;
        args.add("-[0]");

        return this;
    }

    public ImageOP input(File file) {
        args.add(file.getAbsolutePath());

        return this;
    }
    
    public ImageOP page(int page) {
        args.set(args.size()-1, args.get(args.size()-1) + "[" + page + "]");
        return this;
    }

    public ImageOP strip() {
        args.add("-strip");

        return this;
    }

    public ImageOP size(int width, int height) {
        args.add("-size");
        args.add(String.format("%dx%d", width, height));

        return this;
    }

    public ImageOP width(int width) {
        args.add("-size");
        args.add(String.format("%dx", width));

        return this;
    }

    public ImageOP swap(int index1, int index2) {
        args.add("-swap");
        args.add(String.format("%d,%d", index1, index2));

        return this;
    }

    public ImageOP swapLastTwo() {
        args.add("+swap");

        return this;
    }

    public ImageOP composite() {
        args.add("-composite");

        return this;
    }

    public ImageOP quality(int quality) {
        quality = Math.max(quality, 0);
        quality = Math.min(quality, 100);

        args.add("-quality");
        args.add(Integer.toString(quality, 10));

        return this;
    }

    private static String geometry(Integer width, Integer height, Integer offsetX, Integer offsetY) {
        StringBuilder builder = new StringBuilder();

        if (width != null) {
            builder.append(width);
        }
        if (height != null) {
            builder.append("x");
            builder.append(height);
        }
        if (offsetX != null) {
            if (offsetX > -1) {
                builder.append("+");
            }
            builder.append(offsetX);
        }
        if (offsetY != null) {
            if (offsetY > -1) {
                builder.append("+");
            }
            builder.append(offsetY);
        }

        return builder.toString();
    }

    public ImageOP rotate(int angleInDegrees) {
        args.add("-rotate");
        args.add(Integer.toString(angleInDegrees));

        return this;
    }

    public ImageOP font(String font) {
        args.add("-font");
        args.add(font);

        return this;
    }

    public ImageOP scale(Integer width, Integer height) {
        args.add("-scale");

        StringBuilder builder = new StringBuilder();
        if (width != null) {
            builder.append(width);
        }
        if (height != null) {
            builder.append("x");
            builder.append(height);
        }
        args.add(builder.toString());

        return this;
    }

    public ImageOP resize(Integer width, Integer height) {
        return resize(width, height, false);
    }

    public ImageOP resize(Integer width, Integer height, boolean percent) {
        return resize(width, height, percent, false, false);
    }

    public ImageOP resize(Integer width, Integer height, boolean percent, boolean ifExceed, boolean smallestFittingDimension) {
        StringBuilder builder = new StringBuilder();

        if (width != null) {
            builder.append(width);

            if (percent) {
                builder.append('%');
            }
        }
        if (height != null) {
            builder.append('x');
            builder.append(height);

            if (percent) {
                builder.append('%');
            }
        }

        if (ifExceed) {
            builder.append('>');
        }

        if (smallestFittingDimension) {
            builder.append('^');
        }

        args.add("-resize");
        args.add(builder.toString());

        return this;
    }

    public ImageOP repage(boolean full) {
        if (full) {
            args.add("+repage");
        } else {
            args.add("-repage");
        }

        return this;
    }

    public ImageOP repage(boolean full, Integer width, Integer height, Integer offsetX, Integer offsetY) {
        this.repage(full);
        args.add(geometry(width, height, offsetX, offsetY));

        return this;
    }

    public ImageOP negate() {
        args.add("-negate");

        return this;
    }

    public ImageOP startGroup() {
        args.add("(");

        return this;
    }

    public ImageOP endGroup() {
        args.add(")");

        return this;
    }

    public ImageOP blur(int radius, Integer sigma) {
        String value = Integer.toString(radius);

        if (sigma != null) {
            value += "x" + sigma;
        }

        args.add("-blur");
        args.add(value);

        return this;
    }

    public ImageOP gamma(float... gammaPerChannel) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < gammaPerChannel.length; ++i) {
            if (i != 0) {
                builder.append(',');
            }

            builder.append(gammaPerChannel[i]);
        }

        args.add("-gamma");
        args.add(builder.toString());

        return this;
    }

    public ImageOP threshold(int value, boolean percent) {
        String val = Integer.toString(value);
        if (percent) {
            val += "%";
        }

        args.add("-threshold");
        args.add(val);

        return this;
    }

    public ImageOP monochrome() {
        args.add("-monochrome");

        return this;
    }

    public ImageOP depth(int depth) {
        args.add("-depth");
        args.add(Integer.toString(depth));

        return this;
    }

    public ImageOP background(String background) {
        args.add("-background");
        args.add(background);

        return this;
    }

    public ImageOP gravity(String gravity) {
        args.add("-gravity");
        args.add(gravity);

        return this;
    }


    public ImageOP extent(String extent) {
        args.add("-extent");
        args.add(extent);

        return this;
    }

    public ImageOP pointsize(int pointsize) {
        args.add("-pointsize");
        args.add(Integer.toString(pointsize));

        return this;
    }

    public ImageOP fill(String fill) {
        args.add("-fill");
        args.add(fill);

        return this;
    }
    

    public ImageOP density(int density) {
        args.add("-density");
        args.add(Integer.toString(density));

        return this;
    }
    
    public ImageOP geometry(int geometry) {
        args.add("-geometry");
        args.add(Integer.toString(geometry));

        return this;
    }

    public ImageOP label(String label) {
        args.add("label:" + label);

        return this;
    }

    public ImageOP caption(String caption) {
        args.add("caption:" + caption);

        return this;
    }

    public ImageOP outputFormat(String format) {
        this.outputFormat = format;

        return this;
    }

    public byte[] processToByteArray() {
        String out = (outputFormat != null ? outputFormat + ":-" : "-");

        List<String> arguments = new ArrayList<String>(args);
        arguments.add(out);

        try {
            Process process = new ProcessBuilder(arguments).start();

            if (inputData != null) {
                OutputStream output = null;
                try {
                    output = new BufferedOutputStream(process.getOutputStream());
                    output.write(inputData);
                } finally {
                    IOUtils.closeQuietly(output);
                }
            }

            InputStream inputStream = null;
            byte[] data = null;
            try {
                inputStream = process.getInputStream();
                data = IOUtils.toByteArray(inputStream);
            } finally {
                IOUtils.closeQuietly(inputStream);
            }

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
                return data;
            }
        } catch (Exception ex) {
            System.err.print(ex);
        }

        return null;
    }

    public boolean processToFile(File outputFile) {
        String out = null;
        if (outputFormat != null) {
            out = outputFormat + ":" + outputFile.getAbsolutePath();
        } else {
            out = outputFile.getAbsolutePath();
        }

        List<String> arguments = new ArrayList<String>(args);
        arguments.add(out);

        log.info(toString());

        try {
            Process process = new ProcessBuilder(arguments).start();

            if (inputData != null) {
                OutputStream output = null;
                try {
                    output = new BufferedOutputStream(process.getOutputStream());
                    output.write(inputData);
                } finally {
                    IOUtils.closeQuietly(output);
                }
            }

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
        return String.format("ImageOp[%s]", StringUtils.join(args.iterator(), " "));
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ee.promobox;

import static com.sun.javafx.geom.Curve.next;
import java.nio.ByteBuffer;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import uk.co.caprica.vlcj.component.DirectMediaPlayerComponent;
import uk.co.caprica.vlcj.player.direct.BufferFormat;
import uk.co.caprica.vlcj.player.direct.BufferFormatCallback;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.format.RV32BufferFormat;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import static java.util.concurrent.ThreadLocalRandom.current;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import static javax.swing.Spring.height;
import static javax.swing.Spring.width;
import org.json.JSONObject;
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

/**
 * Example showing how to render video to a JavaFX Canvas component.
 * <p>
 * The target is to render full HD video (1920x1080) at a reasonable frame rate
 * (>25fps).
 * <p>
 * This test can render the video at a fixed size, or it can take the size from
 * the video itself.
 * <p>
 * -Dprism.verbose=true
 * <p>
 * You may need to set -Djna.library.path=[path-to-libvlc] on the command-line.
 * <p>
 * Based on an example contributed by John Hendrikx.
 */
public final class PromoboxApplication extends Application {

    private static final String VIDEO_FILE = "c:\\test\\test.mp4";

    private static final boolean useSourceSize = true;

    private static final int WIDTH = 1920;

    private static final int HEIGHT = 1080;

    private final Canvas canvas;

    private final PixelWriter pixelWriter;

    private final WritablePixelFormat<ByteBuffer> pixelFormat;

    private final BorderPane borderPane;

    private final DirectMediaPlayerComponent mediaPlayerComponent;

    private Stage stage;

    private Scene scene;

    private final AtomicInteger frameNumber = new AtomicInteger();

    private JSONObject data;

    /**
     *
     */
    public PromoboxApplication() throws Exception {
        NativeLibrary.addSearchPath(
                RuntimeUtil.getLibVlcLibraryName(), "C:\\Program Files (x86)\\VideoLAN\\VLC"
        );

        Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);

        canvas = new Canvas();

        pixelWriter = canvas.getGraphicsContext2D().getPixelWriter();
        pixelFormat = PixelFormat.getByteBgraInstance();

        borderPane = new BorderPane();
        borderPane.setCenter(canvas);

        mediaPlayerComponent = new TestMediaPlayerComponent();

        data = new JSONObject(new String(Files.readAllBytes(Paths.get("data.json"))));

    }
    
    
//     public void startImage(Image image) {
//        ObservableList<Node> c = root.getChildren();
//
//        if (current != null)
//            c.remove(current);
//
//        current = next;
//        next = null;
//
//        // Create fade-in for new image.
//        next = new ImageView(image);
//
//        next.setFitHeight(height);
//        next.setFitHeight(width);
//        next.setPreserveRatio(true);
//        next.setOpacity(0);
//
//        c.add(next);
//
//        FadeTransition fadein = new FadeTransition(Duration.seconds(1), next);
//
//        fadein.setFromValue(0);
//        fadein.setToValue(1);
//
//        ScaleTransition dropout = new ScaleTransition(Duration.seconds(1), current);
//        dropout.setInterpolator(Interpolator.EASE_OUT);
//        dropout.setFromX(1);
//        dropout.setFromY(1);
//        dropout.setToX(0.75);
//        dropout.setToY(0.75);
//
//        PauseTransition delay = new PauseTransition(Duration.seconds(1));
//
//        SequentialTransition st = new SequentialTransition(
//            new ParallelTransition(fadein, dropout), delay);
//
//        st.setOnFinished(new EventHandler<ActionEvent>() {
//            @Override
//            public void handle(ActionEvent t) {
//                Image image = getNextImage();
//
//                if (image != null)
//                    startImage(image);
//            }
//        });
//
//        st.playFromStart();
//    }


    @Override
    public void start(Stage primaryStage) throws Exception {
        Group root = new Group();

        this.stage = primaryStage;

        stage.setTitle("vlcj JavaFX Direct Rendering Test");

        scene = new Scene(root, WIDTH, HEIGHT, Color.BLACK);

        root.getChildren().add(borderPane);

        File file = new File(data.getJSONArray("files").getJSONObject(0).getString("path"));
        
        Image image = new Image(file.toURI().toString());

        // simple displays ImageView the image as is
        ImageView iv1 = new ImageView();

        iv1.setImage(image);
        iv1.setPreserveRatio(true);
        iv1.setSmooth(true);

        root.getChildren().add(iv1);

        iv1.setFitWidth(300);

        primaryStage.setScene(scene);
        primaryStage.show();
        
        mediaPlayerComponent.getMediaPlayer().addMediaPlayerEventListener( new MediaPlayerEventAdapter() {

            @Override
            public void finished(MediaPlayer mp) {
                System.out.println("Finished");
            }

            @Override
            public void error(MediaPlayer mp) {
            }

        });
        
        mediaPlayerComponent.getMediaPlayer().playMedia(VIDEO_FILE);
    }

    @Override
    public void stop() throws Exception {
        mediaPlayerComponent.getMediaPlayer().stop();
        mediaPlayerComponent.getMediaPlayer().release();
    }


    private class TestMediaPlayerComponent extends DirectMediaPlayerComponent {

        AtomicReference<ByteBuffer> currentByteBuffer = new AtomicReference<>();

        @Override
        public void display(DirectMediaPlayer mediaPlayer, Memory[] nativeBuffers, final BufferFormat bufferFormat) {

            final int renderFrameNumber = frameNumber.incrementAndGet();

            currentByteBuffer.set(nativeBuffers[0].getByteBuffer(0, nativeBuffers[0].size()));

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    ByteBuffer byteBuffer = currentByteBuffer.get();

                    int actualFrameNumber = frameNumber.get();

                    if (renderFrameNumber == actualFrameNumber) {
                        pixelWriter.setPixels(0, 0, bufferFormat.getWidth(), bufferFormat.getHeight(), pixelFormat, byteBuffer, bufferFormat.getPitches()[0]);
                    } else {
                        System.out.println("[FINE] " + " - Skipped late frame " + renderFrameNumber + " (actual = " + actualFrameNumber + ")");
                    }
                }
            });

        }

        public TestMediaPlayerComponent() {
            super(new TestBufferFormatCallback());
        }
    }

    /**
     * Callback to get the buffer format to use for video playback.
     */
    private class TestBufferFormatCallback implements BufferFormatCallback {

        @Override
        public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
            final int width;
            final int height;

            if (useSourceSize) {
                width = sourceWidth;
                height = sourceHeight;
            } else {
                width = WIDTH;
                height = HEIGHT;
            }

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    canvas.setWidth(width);
                    canvas.setHeight(height);

                    stage.setWidth(width);
                    stage.setHeight(height);
                }
            });

            return new RV32BufferFormat(width, height);
        }
    }

    /**
     * Application entry point.
     *
     * @param args command-line arguments
     */
    public static void main(final String[] args) {
        Application.launch(args);
    }
}

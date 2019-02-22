package sample;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

// Serializable means that objects of this class can be read/written over ObjectStreams
public class Message implements Serializable {
    // Message includes both sender ID and Image being sent
    private String sender;
    private String type;
    // Image is transient means that we have to provide our own code to read/write object
    private String text;
    private transient Image data2;
    // private transient Media data3;

    Message(String who, String quote, Image what) {
        sender = who;
        text = quote;
        data2 = what;
    }

    String sender() {
        return sender;
    }

    Image data2() {
        return data2;
    }

    public String toString() {
        return "\"" + data2 + "\" from: " + sender;
    }

    private void readObject(ObjectInputStream inStream) throws IOException, ClassNotFoundException {
        // this reads sender String with default code
        inStream.defaultReadObject();
        // this reads data Image using this custom code
        data2 = SwingFXUtils.toFXImage(ImageIO.read(inStream), null);
    }

    private void writeObject(ObjectOutputStream outStream) throws IOException {
        // this writes sender String with default code
        outStream.defaultWriteObject();
        // this writes data Image using this custom code
        ImageIO.write(SwingFXUtils.fromFXImage(data2, null), "png", outStream);
    }

}

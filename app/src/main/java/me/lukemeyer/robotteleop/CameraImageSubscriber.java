package me.lukemeyer.robotteleop;

import android.graphics.Bitmap;
import android.util.Log;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;
import android.graphics.BitmapFactory;
import org.jboss.netty.buffer.ChannelBuffer;

import sensor_msgs.CompressedImage;

public class CameraImageSubscriber extends AbstractNodeMain {
    private Subscriber<CompressedImage> subscriber = null;
    private final static java.lang.String TAG = "CameraImageSubscriber";
    private boolean newImageReady = false;
    private Bitmap newestImage;

    public CameraImageSubscriber() {
    }

    public GraphName getDefaultNodeName() {
        return GraphName.of("androidApp/CameraImageSubscriber");
    }

    public void onStart(ConnectedNode connectedNode) {
        subscriber = connectedNode.newSubscriber("raspicam_node/image/compressed", "sensor_msgs/CompressedImage");
        subscriber.addMessageListener(new MessageListener<CompressedImage>() {
            @Override
            public void onNewMessage(CompressedImage compressedImage) {
                Log.i(TAG, "newMessage");
                ChannelBuffer buffer = compressedImage.getData();
                byte[] data = buffer.array();
                newestImage = BitmapFactory.decodeByteArray(data, buffer.arrayOffset(), buffer.readableBytes());
                newImageReady = true;
            }
        });
    }

    public Boolean isNewImageReady() {
        return newImageReady;
    }

    public Bitmap getNewImage() {
        newImageReady = false;
        return newestImage;
    }
}

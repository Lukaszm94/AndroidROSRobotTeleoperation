package me.lukemeyer.robotteleop;

import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;

import geometry_msgs.Twist;

// subscribes to PV velocity

public class VelocitySubscriber extends AbstractNodeMain {
    private final static String TAG = "VelocitySubscriber";
    private Subscriber<Twist> subscriber = null;
    private double linearVelocityPV = 0.0;
    private double angularVelocityPV = 0.0;

    public VelocitySubscriber() {

    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("androidApp/VelocitySubscriber");
    }

    public void onStart(ConnectedNode connectedNode) {
        subscriber = connectedNode.newSubscriber("/lfc/velocity_PV", "geometry_msgs/Twist");
        subscriber.addMessageListener(new MessageListener<Twist>() {
            @Override
            public void onNewMessage(Twist twist) {
                linearVelocityPV = twist.getLinear().getX();
                angularVelocityPV = twist.getAngular().getZ();
            }
        });
    }

    public double getLinearVelocity() {
        return linearVelocityPV;
    }

    public double getAngularVelocity() {
        return angularVelocityPV;
    }
}

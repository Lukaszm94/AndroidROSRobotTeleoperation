package me.lukemeyer.robotteleop;

import android.content.Context;
import android.util.Log;

import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;

import geometry_msgs.Twist;

public class VelocitySetpointPublisher extends AbstractNodeMain {
    private final String TAG = "VelocitySetpointPub";
    private String topic_name;
    private Publisher<Twist> publisher = null;

    public VelocitySetpointPublisher() {
        this.topic_name = "lfc/velocity_SP";
    }

    public VelocitySetpointPublisher(String topic) {
        this.topic_name = topic;
    }

    public GraphName getDefaultNodeName() {
        return GraphName.of("androidApp/VelocitySetpointPublisher");
    }

    public void onStart(ConnectedNode connectedNode) {
        publisher = connectedNode.newPublisher(this.topic_name, "geometry_msgs/Twist");
        Log.i(TAG, "onStart");
    }

    public void publishSetpoint(double linearVelocity, double angularVelocity) {
        if(publisher == null) {
            Log.e(TAG, "publishSetpoint: publisher is null, returning");
            return;
        }
        geometry_msgs.Twist msg = publisher.newMessage();
        geometry_msgs.Vector3 vectorLinear = msg.getLinear();
        vectorLinear.setX(linearVelocity);
        vectorLinear.setY(0.0); vectorLinear.setZ(0.0);
        geometry_msgs.Vector3 vectorAngular = msg.getAngular();
        vectorAngular.setZ(angularVelocity);
        vectorAngular.setX(0.0); vectorAngular.setY(0.0);
        publisher.publish(msg);
    }
}

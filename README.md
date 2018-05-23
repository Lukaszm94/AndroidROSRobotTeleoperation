#Overview
This Android application enables you to teleoperate (control remotely) a mobile robot that is based on ROS (Robot Operating System)[Link](http://www.ros.org/).

Main features include:

* setting robots speed setpoint (linear and angular) - via virtual joystick or tilting your phone
* displaying live camera image from the robot
* viewing linear and angular velocity plots (setpoint (SP) and process variable (PV))

# ROS topics
Application subscribes to topics:

* live video frames
	* topic :`raspicam_node/image/compressed`
	* message type: `sensor_msgs/ImageCompressed`
* velocity process variable (current value)
	* topic: `lfc/velocity_PV`
	* message type: `geometry_msgs/Twist`

Application publishes to topic:

* velocity setpoint
	* topic: `lfc/velocity_SP`
	* message type: `geometry_msgs/Twist`
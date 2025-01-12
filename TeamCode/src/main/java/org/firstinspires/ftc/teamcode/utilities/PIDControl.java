package org.firstinspires.ftc.teamcode.utilities;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

public class PIDControl{
    double intergralSum =0;
    double Kp = 0.04;
    double Ki= 0.0;
    double Kd =0.001;

    ElapsedTime timer = new ElapsedTime();
    private double lastError = 0;




    public double PIDValue(double reference, double state){
        double error = reference-state;
        while (error > 180)  error -= 360;
        while (error <= -180) error += 360;
        intergralSum +=error *timer.seconds();
        double derivative = (error-lastError) / timer.seconds();
        lastError = error;


        timer.reset();

        double gain2 = Math.sqrt(Math.abs(error)/180.0);
        double output =  (error*Kp) + (derivative*Kd) + (intergralSum*Ki);
        //double output =  ((error*Kp) + (derivative*Kd) + (intergralSum*Ki))*gain2;
        return output;
    }


}
/*
 * CRUX Open Source Ventilator
  https://www.cruxbd.com 
  Version: Demo 4.0
  Team Member:
  1) Syed Razwanul Haque(Nabil)
     https://www.nabilbd.com/ventilator
   Team Lead, Research, Coding
  2) Maruf Hossain
   Research, Mechanical & 3D Design
  Licensing Under Processing
  3) Shafi
     UI Software
  4) Hasan
     3D Design, Assembling
  5) Shovon S Saha, shovonsaha3401@gmail.com
     General Research, Documentation, Scientific Paper
 */  
 /*
  * SIMV Mode works with PRVC mode (ToDo: SIMV with Pressure Control Mode)
  * SIMV with Support mode not incorporated
  * 
  */
#include <Arduino.h>
#include <Servo.h>
#include <Wire.h>
#include "config.h"
#include "gasblending.h"
#include "callibration.h"
#include "volume.h"
#include "pressure.h"
#include "datafilter.h"
#include "sensor.h"
#include "showdata.h"
#include "alarm.h"
#include "SFM3000CORE.h"

SFM3000CORE sensirionFlow(64); //Sensirion Flow Sensor Object
//Left to Right- VDD,SCL(A5),GND,SDA(A4)
// To Do: Replace minimum variable size needed. e.g. uint8_t insteed of int where applicable
// To Do: Make the code object orienteed. Create Separate Method to Write Motor Speed
//////////Data Frame Variable//////////
String getVentFrame; // Data String Received from Android Tab as a Frame. e.g.  getVentFrame = "v061502.0042090050025150702";
String mode;
uint8_t bpm;
int Tv;
int Ti;
int Te;
float nonSyncTime;  // for SIMV
float syncTime;
float peep;
int pip;
int breathCycleTime;
float targetFlowRate;
/// Variables for Alarm
int TvAlarmHigh;
int TvAlarmLow;
int peepAlarmHigh;
int peepAlarmLow;
int pipAlarmHigh;
int pipAlarmLow;
int simvTriggerFlow;
int simvOnOff;

//----------------------------------------------------------
void setup() {
  Serial.begin(115200);
  Serial.println("Started");
  Wire.begin(); // For Flow Sensor
  delay(1000); // Just for Stability
  pinMode(flowSensorOnOff, OUTPUT); // 5V Supply for Sensirion SFM3300 Flow Sensor. Since it require maximum 10ma so we are providing power from mcu pin 
  digitalWrite(flowSensorOnOff, HIGH); // SFM3300 Power Supply Started
  delay(100);
  sensirionFlow.init(); // Software Initialization of the SFM3300 Flow Sensor
  delay(1000);
 
#ifdef dcMotor //Actually Current Turbine from Aliexpress Although its a BLDC motor technically. Previously we used this pin to control a DC motor to provide PWM and write now providing PWM to BLDC Turbine
  pinMode(turbineMotorPin, OUTPUT);
  #endif
////////////////// Indicator LED //////////////////////////////
  pinMode(indicatorLed, OUTPUT);  //Green Big LED
////////////////// Servo Motor Initialization //////////////////
  servoOne.attach(servoOnePin); // Pressure Release Servo at Pin 5
  servoTwo.attach(servoTwoPin); // Not implemented
  oxygenServo.attach(oxygenServoPin);//In this version it has no real effects
  airServo.attach(airServoPin);//In this version it has no real effects
  peepServo.attach(40); // In this version it has no real effects
///////////////////////////////////////////////////////////////  
  oxygenServo.write(30);//In this version it has no real effects
  airServo.write(95);//In this version it has no real effects
  sensorCallibration(); // Pressure Senosr will be callibrated
  getVentFrame = "v061502.0042090050025150702"; //for testing purpose. In actual use please comment it out
 // getVentFrame = "p105001.5041090050025150702";
 // getVentFrame = "s";
    mode = getVentFrame.substring(0, 1); //e.g. v or p . v=PRVC mode p=PCV mode 
    bpm = getVentFrame.substring(1, 3).toInt(); //BPM=breath per minute . Two digit. e.g 06
    Tv = getVentFrame.substring(3, 6).toInt(); //Tidal Volume in ml , 3 digit. highest 999
    Ti = getVentFrame.substring(6, 9).toFloat() * 1000; //in millisecond
    peep = getVentFrame.substring(9, 11).toInt();
    pip = getVentFrame.substring(11, 13).toInt();
    TvAlarmHigh = getVentFrame.substring(13, 16).toInt(); // 3 digit, since highest 999
    TvAlarmLow = getVentFrame.substring(16,19).toInt();// 3 digit
    pipAlarmHigh = getVentFrame.substring(19,21).toInt(); //2 digit
    pipAlarmLow = getVentFrame.substring(21,23).toInt(); //2 digit
    peepAlarmHigh = getVentFrame.substring(23,25).toInt(); // 2 digit
    peepAlarmLow = getVentFrame.substring(25,27).toInt(); // 2 digit
  
//  mode = getVentFrame.substring(0, 1);
//  bpm = getVentFrame.substring(1, 3).toInt(); //BPM=breath per minute
//  Tv = getVentFrame.substring(3, 6).toInt(); //Tidal Volume in ml
//  Ti = getVentFrame.substring(6, 9).toFloat() * 1000; //in millisecond
//  peep = getVentFrame.substring(9, 11).toFloat();
//  pip = getVentFrame.substring(11, 13).toInt();
//  motorSpeed = getVentFrame.substring(13, 16).toInt();
  breathCycleTime = int((60 / float(bpm)) * 1000); //in millisecond. e.g. if BPM=10 , breath Cycle Time = 6000ms (Inpiration Time + Expiration Time)
  nonSyncTime = breathCycleTime * 0.9; //for SIMV Mode. to detect Sync Time. e.g. if breath cycle =10 then 9 is non sync period 
  Te = (breathCycleTime - Ti); //in millisecond, Te means Expiration Time during Exhale
  syncTime = nonSyncTime - Ti; //(in millisecond)in SIMV if trigger happens after this time period of Expiration then SIMV will give breath early
  
  targetFlowRate = Tv / ((float(Ti) / float(1000))); // in ml , not needed right now
 
  Serial.println("End of Setup");
}

void loop() {
  digitalWrite(indicatorLed, HIGH); // Green LED ON to indicate that device is operational
  if (Serial.available() > 0) {

    getVentFrame = Serial.readString(); 
    mode = getVentFrame.substring(0, 1);
    bpm = getVentFrame.substring(1, 3).toInt(); //BPM=breath per minute
    Tv = getVentFrame.substring(3, 6).toInt(); //Tidal Volume in ml
    Ti = getVentFrame.substring(6, 9).toFloat() * 1000; //in millisecond
    peep = getVentFrame.substring(9, 11).toInt();
    pip = getVentFrame.substring(11, 13).toInt();
    TvAlarmHigh = getVentFrame.substring(13, 16).toInt(); // 3 digits, since highest 999
    TvAlarmLow = getVentFrame.substring(16,19).toInt();// 3 digits
    pipAlarmHigh = getVentFrame.substring(19,21).toInt(); //2 digits
    pipAlarmLow = getVentFrame.substring(21,23).toInt(); //2 digits
    peepAlarmHigh = getVentFrame.substring(23,25).toInt(); // 2 digits
    peepAlarmLow = getVentFrame.substring(25,27).toInt(); // 2 digits
    simvTriggerFlow = getVentFrame.substring(27,29).toInt();//2 digits ,flow triggers ranging from 0.7 to 2.0 L/min - 11 ml/second to 50 ml/second
    simvOnOff = getVentFrame.substring(29,30).toInt();//1 digit, 1= SIMV ON , 0= SIMV OFF 

    breathCycleTime = int((60 / float(bpm)) * 1000); //in millisecond
    Te = (breathCycleTime - Ti); //in millisecond
    targetFlowRate = Tv / ((float(Ti) / float(1000))); // in ml
    nonSyncTime = breathCycleTime * 0.9; //for SIMV Mode. to detect Sync Time. e.g. if breath cycle =10 then 9 is non sync period 
    Te = (breathCycleTime - Ti); //in millisecond
    syncTime = nonSyncTime - Ti; //(in millisecond)in SIMV if trigger happens in this time period SIMV will give breath early
    //breathCycleTime = 20; Ti = 1000; Te = 2000;
  }
  //vent input=M,BPM,Tv(3 digit only),Ti,PEEP,PIP,TvAlarmHigh,TvAlarmLow,pipAlarmHigh,pipAlarmLow,peepAlarmHigh,peepAlarmLow
  //           V,20,999,1.5,05,20,900,500,25,15,07,02      //p155001.5052090050025150702

/// Restart Mechanism if Sensor Error Occured 
flowRate = sensirionFlow.getvalue();
   if((flowRate < -4500.0)||(flowRate > 4500.00)){ // if this condition satisfy that means error.
      Serial.println(flowRate);
      digitalWrite(flowSensorOnOff, LOW); // Sensor 5V Power OFF
      delay(1000);
      digitalWrite(flowSensorOnOff, HIGH); // Sensor 5V Power ON, for Hard Reset 
      delay(500);
      sensirionFlow.init();
      delay(50);
      sensirionFlow.softReset(); //Software Reset
      sensirionFlow.init();
      delay(1000);
      motorSpeed = 30; //Just to Run Slowly
     //To Do = Create Flow Sensor Error Alarm
    }

  ///Volume Control Mode//////
  if (mode == "v") {
   
    volumeControl(); //PRVC
    //inspiratoryLoop();
  }
  
  if (mode == "p") { //PCV = Pressure Control Ventilation
    //stabilizingCounter = 0;
    pressureControl();
  }
  
  if (mode == "s"){ //Debugging Mode. Its not actually a mode. Its for Testing. It will just execute below procedure. 
    analogWrite(turbineMotorPin,0);
    
    //servoControl("close");
    float flowResult = sensirionFlow.getvalue();
    Serial.print(" Pressure=");
    Serial.print(pressureSenseMpx2010(pressureSenseMpx2010Pin));
    Serial.print(" SensirionML/S=");
    Serial.print(flowResult);
    Serial.print(" SensirionSLM=");
    Serial.println((flowResult*60)/1000);
    delay(100);
    servoControl("open");
    flowResult = sensirionFlow.getvalue();
    Serial.print(" Pressure=");
    Serial.print(pressureSenseMpx2010(pressureSenseMpx2010Pin));
    Serial.print(" SensirionML/S=");
    Serial.print(flowResult);
    Serial.print(" SensirionSLM=");
    Serial.println((flowResult*60)/1000);
    
    delay(100);

    //peepServo.write(1);
  
    
    
    //servoControl("open");
    //analogWrite(turbineMotorPin,0);
    //Serial.print(" Pressure=");
    //Serial.print(pressureSenseMpx2010(pressureSenseMpx2010Pin));
   
    //delay(3000);
    //delay(40);

    //     bldc.write(15);
//    servoControl("open");
//    delay(5000);
//    servoControl("close");
//    delay(5000);
    
//    float arrayData[] = {4.81,4.81,5.02,5.02,5.02,5.02,5.02,5.23,5.02,5.23,5.44,5.23,5.44,5.02,5.44,5.44,5.44};
//    Serial.println(dataFilter(arrayData,sizeof(arrayData)/sizeof(float)));
//    servoControl("open");
//    delay(1000);
//    bldc.write(15);
//    serialDataOutVolumeControl("Stop");
//float testData[] = {10.1,2.0 , 6.0, 2.0 , 1.0, 7.0, 7.6,8.9,7.6};
//Serial.println(highestPressureCalculator(testData, sizeof(testData)/sizeof(float)));
  }
 
  
  

}

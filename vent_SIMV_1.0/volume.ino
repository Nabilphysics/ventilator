
//v102501.5042090050025150702
int volumeError;
void volumeControl() {

  /////////////////////inspiratory Phase///////////////
  inspirationStart:
  
  volume = 0;
  //inspiratoryLoop();
    //for (timeSumming = 0; timeSumming < Ti; timeSumming = timeSumming + (timeDifference/1000)) {
  for (timeSumming = 0; timeSumming < Ti; timeSumming = timeSumming + timeDifference) {

    servoControl("close"); //Pressure Release Mechanism OFF to Apply pressure through Patient Circuit.
    //timex = micros();
    timex = millis();
    analogWrite(turbineMotorPin, motorSpeed);
    flowRate = sensirionFlow.getvalue();
    //Serial.println(flowRate);
    //delayMicroseconds(550);
    delay(2);
    pressureMeasured = pressureSenseMpx2010(pressureSenseMpx2010Pin); //from MPX2010DP Pressure Sensor

    //volume = flowRate * (timeDifference/1000000) + volume;     // Tidal Volume in ml
    volume = flowRate * (timeDifference / 1000) + volume;   // Tidal Volume in ml

    continuousVolumeChangeAllModes = volume; 
    //timeTrack = timeTrack + (timeDifference/1000);
    timeTrack = timeTrack + timeDifference;
    //timeTrackVolumeUpdate = timeTrackVolumeUpdate + timeDifference;

    //timey = micros();
    timey = millis();
    timeDifference = timey - timex;

    if (timeTrack > timeAfterDataShow) {
      Serial.println("Inspiration");
      motorSpeedAllModes = motorSpeed;
      disconnectFlagAllModes = disconnectFlag;  // to make it common for all modes and keep the individual data as well for debugging
      flowRawArray[numberOfTime] = flowRate;
      pressureRawArrayVolume[numberOfTime] = pressureMeasured;
      volumeRawArray[numberOfTime] = volume;
      pressureAllModes = pressureMeasured;
      flowRateAllModes = flowRate;
      continuousVolumeChangeAllModes = volume;
      serialDataOut("I");// For Debugging, I=During Inspiratory or B=Between Inspiratory & Expiratory or E=During Expiratory or L= End of Expiratory
      numberOfTime = numberOfTime + 1;
      timeTrack = 0;
    }
  }
  //float filteredFlowRate = dataFilter(flowRawArray, numberOfTime);
  float highestPressure = highestPressureCalculator(pressureRawArrayVolume, numberOfTime);//pressureRawArrayVolume means Pressure During PRVC mode
  int maxVolume = highestVolumeCalculator(volumeRawArray, numberOfTime);
  volume = maxVolume;

  ////////Alarm///////////////////////
  alarmCheckPip(highestPressure);// pipAlarm = "HPP"; or "LPP" or "NAA" // High Peak Pressure
  alarmCheckVolume(volume); // volumeAlarm = "HV" or "LV" or "NA";

  ///////// PID (P only) //////////////
  volumeError = Tv - maxVolume; //int volumeError = Tv - volume;
  //////////Pressure Limit for Safety
  if (highestPressure > 35.0) { // motor speed will not increase further to limit pressure 35.0 cmH2O
    volumeError = -50; // motor speed will decrease as system will find negative error. This is for patient safety
    Serial.println("Pressure Limit Reached");
  }
 /////////////////// Patient Circuit Disconnect Detection Flag ///////////////////
 if (highestPressure < 5.0) {
    disconnectFlag = true;
    //servoControl("close");
    disconnectFlagAllModes = disconnectFlag;
    motorSpeed = 30;
    motorPEEPSpeed = 30;
    volumeError = 0;
    delay(100);
    Serial.println("Disconnected");
  }
  else {
    disconnectFlag = false;
  }
  ///////////////////End Patient Circuit Disconnect Detection Flag ///////////////////

  if ( (volumeError >= -1) && ( 1 > volumeError)) { //Error threshold value to reduce Jitters when volume has reached very close to targetted volume.  
    motorSpeed = motorSpeed + 0;
  }
  else {
    motorSpeed = motorSpeed + volumeError * 0.07; //Kp = 0.07 , Proportional Constant
    if (volumeError > 215.0) { // 215 * 0.07 = 15, This will prevent the system from sudden motor speed change
      volumeError = 215.0;
    }
    if (motorSpeed > motorSpeedMax) {
      motorSpeed = motorSpeedMax;
    }
    if (motorSpeed < motorSpeedMin) {
      motorSpeed = motorSpeedMin;
    }
  }
  motorSpeedAllModes = motorSpeed;
  pressureAllModes = highestPressure;
  flowRateAllModes = flowRate;
  continuousVolumeChangeAllModes = volume;
  serialDataOut("B");// I=During Inspiratory or B=Between Inspiratory & Expiratory or E=During Expiratory or L= End of Exiratory


  ///////////////////Expiratory Loop/////////////////

  numberOfTime = 0; //reset numberOfTime from inpiratory phase
  //expiratoryLoop();
    for (timeSumming = 0; timeSumming < Te; timeSumming = timeSumming + (timeDifference / 1000)) {
    servoControl("open");// Pressure Release Mechanism (Servo is pushed downward)

    timex = micros();
    analogWrite(turbineMotorPin, motorPEEPSpeed);

    flowRate = sensirionFlow.getvalue();
    delayMicroseconds(500);
    volume = flowRate * (timeDifference / 1000000) + volume;     // Tidal Volume in ml
    continuousVolumeChangeAllModes = volume;
    timeTrack = timeTrack + (timeDifference / 1000);
/*
    Serial.print("SyncTime="); 
    Serial.print(syncTime);
    Serial.print(" TimeSumming=");
    Serial.println(timeSumming);
  */  
//SIMV Trigger (pressure trigger of 1.0 cm H2O, flow triggers ranging from 0.7 to 2.0 L/min - 11 ml/second to 50 ml/second)
// syncTime is 90% of Breath Cycle Time    
    if((timeSumming > syncTime)&& (flowRate > 20) ){  //20ml/Second is hard coded. You can change it and rather than hard coded you can receive from Android App. 
      Serial.println("Trigger");
     
      goto inspirationStart;// using goto is not a good practice.
    }
   
   
    //How Often Serial Data will Show
    if (timeTrack > timeAfterDataShow) {
      motorSpeedAllModes = motorPEEPSpeed;
      pressureAllModes = pressureSenseMpx2010(pressureSenseMpx2010Pin);
      flowRateAllModes = flowRate;
      continuousVolumeChangeAllModes = volume;
      serialDataOut("E");// I=During Inspiratory or B=Between Inspiratory & Expiratory or E=During Expiratory or L= End of Exiratory

      timeTrack = 0;
    }
    timey = micros();
    timeDifference = timey - timex;

  }
  pressureMeasured = pressureSenseMpx2010(pressureSenseMpx2010Pin);
  peepError = peep - pressureMeasured;
  ///////////// Alarm PEEP //////////////
  alarmCheckPeep(pressureMeasured); //peepAlarm = "HP" or "LP" or "NA" //High PEEP or Low PEEP or No Alarm


  if ( (peepError >= -0.50) && ( 0.50 > peepError)) {  //Error Stabilization , ignore if PEEP error is within  +-50cmH2O
    motorPEEPSpeed = motorPEEPSpeed + 0;
    //Serial.println("PEEP Constrain");
  }
  else {
    motorPEEPSpeed = motorPEEPSpeed + peepError * 1.5; //1.5 = Proportional Constant 
    if (motorPEEPSpeed  > motorPEEPSpeedMax) {
      motorPEEPSpeed  = motorPEEPSpeedMax;
    }
    if (motorPEEPSpeed  < motorPEEPSpeedMin) {
      motorPEEPSpeed  = motorPEEPSpeedMin;
    }
  }
  pressureAllModes = pressureSenseMpx2010(pressureSenseMpx2010Pin);
  serialDataOut("L");// I=During Inspiratory or B=Between Inspiratory & Expiratory or E=During Expiratory or L= End of Exiratory
}

//void inspiratoryLoop(){
//  
//}
//
//void expiratoryLoop(){
//  
//}

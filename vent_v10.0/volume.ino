
//v102501.5042090050025150702
int volumeError;
void volumeControl() {

  /////////////////////inspiratory Phase///////////////
  volume = 0;
  inspiratoryLoop();

  //float filteredFlowRate = dataFilter(flowRawArray, numberOfTime);
  float highestPressure = highestPressureCalculator(pressureRawArrayVolume, numberOfTime);
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

  if ( (volumeError >= -1) && ( 1 > volumeError)) {
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
  expiratoryLoop();
  
  pressureMeasured = pressureSenseMpx2010(pressureSenseMpx2010Pin);
  peepError = peep - pressureMeasured;
  ///////////// Alarm PEEP //////////////
  alarmCheckPeep(pressureMeasured); //peepAlarm = "HP" or "LP" or "NA" //High PEEP or Low PEEP or No Alarm


  if ( (peepError >= -0.50) && ( 0.50 > peepError)) {
    motorPEEPSpeed = motorPEEPSpeed + 0;
    //Serial.println("PEEP Constrain");
  }
  else {
    motorPEEPSpeed = motorPEEPSpeed + peepError * 1.5; //1.0
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

void inspiratoryLoop(){
    //for (timeSumming = 0; timeSumming < Ti; timeSumming = timeSumming + (timeDifference/1000)) {
  for (timeSumming = 0; timeSumming < Ti; timeSumming = timeSumming + timeDifference) {

    servoControl("close");
    //timex = micros();
    timex = millis();
    analogWrite(turbineMotorPin, motorSpeed);
    flowRate = sensirionFlow.getvalue();
    //Serial.println(flowRate);
    //delayMicroseconds(550);
    delay(2);
    pressureMeasured = pressureSenseMpx2010(pressureSenseMpx2010Pin);

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
      motorSpeedAllModes = motorSpeed;
      disconnectFlagAllModes = disconnectFlag;  // to make it common for all modes and keep the individual data as well for debugging
      flowRawArray[numberOfTime] = flowRate;
      pressureRawArrayVolume[numberOfTime] = pressureMeasured;
      volumeRawArray[numberOfTime] = volume;
      pressureAllModes = pressureMeasured;
      flowRateAllModes = flowRate;
      continuousVolumeChangeAllModes = volume;
      serialDataOut("I");// I=During Inspiratory or B=Between Inspiratory & Expiratory or E=During Expiratory or L= End of Expiratory
      numberOfTime = numberOfTime + 1;
      timeTrack = 0;
    }
  }
}

void expiratoryLoop(){
    for (timeSumming = 0; timeSumming < Te; timeSumming = timeSumming + (timeDifference / 1000)) {
    servoControl("open");

    timex = micros();
    analogWrite(turbineMotorPin, motorPEEPSpeed);

    flowRate = sensirionFlow.getvalue();
    delayMicroseconds(500);
    volume = flowRate * (timeDifference / 1000000) + volume;     // Tidal Volume in ml
    continuousVolumeChangeAllModes = volume;
    timeTrack = timeTrack + (timeDifference / 1000);

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
}

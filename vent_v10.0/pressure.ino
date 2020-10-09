



void pressureControl() {

  /////////////////////inspiratory Phase///////////////
  volume = 0;
  //pressureTi = Ti;
  //pressureTe = Te;
  targetPressure = pip;
  targetPEEPPressure = peep;
  for (timeSumming = 0; timeSumming < Ti ; timeSumming = timeSumming + timeDiff) {
    int time1 = millis();
    servoControl("close");
    if (disconnectFlagPressureMode == true) {
#ifdef bldcMotor
      bldc.write(initialPressureMotorSpeed);
#endif
#ifdef dcMotor
      analogWrite(turbineMotorPin, initialPressureMotorSpeed);
#endif

    }
    else {
#ifdef bldcMotor
      bldc.write(pressureMotorSpeed);
#endif
#ifdef dcMotor
      analogWrite(turbineMotorPin, pressureMotorSpeed);
#endif
    }
    //pressureControlModeflowRate = flowRateMpx7002Sensor(flowSenseMpx7002Pin);
    pressureControlModeflowRate = sensirionFlow.getvalue();
    delay(3);
    volume = pressureControlModeflowRate * (timeDiff / 1000) + volume;     // Tidal Volume in ml
    timeTrack = timeTrack + timeDiff;

    flowRateAllModes = pressureControlModeflowRate;
    //continuousVolumeChangeAllModes = volumePressureMode;
    motorSpeedAllModes = pressureMotorSpeed;

    int time2 = millis();
    timeDiff = time2 - time1;

    if (timeTrack > timeAfterDataShow) {
      disconnectFlagAllModes = disconnectFlagPressureMode;
      realPressure = pressureSenseMpx2010(pressureSenseMpx2010Pin);
      pressureAllModes = realPressure;
      pError = targetPressure - realPressure;

      pressureMotorSpeed = initialPressureMotorSpeed + pError * 3;
      if (disconnectFlagPressureMode == true) {
        pressureMotorSpeed = initialPressureMotorSpeed;
      }
      if (pressureMotorSpeed < (initialPressureMotorSpeed - 20)) { //Lets say init speed = 50, so High=70, LOW=35
        pressureMotorSpeed = (initialPressureMotorSpeed - 20);
      }
      if (pressureMotorSpeed > (initialPressureMotorSpeed + 30)) {
        pressureMotorSpeed = (initialPressureMotorSpeed + 30);
      }



      ema = EMA_fun(0.15, realPressure, ema);
      ema_ema = EMA_fun(0.15, ema, ema_ema);

      float DEMA = 2 * ema - ema_ema;
      //Serial.print(disconnectFlagPressureMode);
      //Serial.print("inExpPressure= ");
      //Serial.print(realPressure);
      //Serial.print( "flow= ");
      //Serial.print(pressureControlModeflowRate);
      // Serial.print( "volume= ");
      // Serial.print(volumePressureMode);
      //      Serial.print(" DEMA= ");
      //      Serial.println(DEMA);
      //Serial.print(" Insp MotorSpeed=");
      //Serial.print(pressureMotorSpeed);
      //Serial.print(" Base Motor MotorSpeed=");
      //Serial.println(initialPressureMotorSpeed);


      if (timeSumming >  (Ti / 2)) {
        pressureRawArray[numberOfTime] = realPressure;
        numberOfTime = numberOfTime + 1;
        //         Serial.print("Real Pressure=");
        //         Serial.print(realPressure);
        //         Serial.print("Number of Time=");
        //         Serial.println(numberOfTime);
      }
      ////////////// Data Out ///////////////////
      serialDataOut("I");//I= Inhale

      timeTrack = 0;
    }
  }
  ///////////////inspiratory end ////////////////////////

  float filteredPressure = dataFilter(pressureRawArray, numberOfTime);
  pressureAllModes = filteredPressure;
  ///////////////Alarm Volume and PIP ///////////
  alarmCheckPip(filteredPressure);// pipAlarm = "HPP"; or "LPP" or "NAA" // High Peak Pressure
  alarmCheckVolume(volume); // volumeAlarm = "HV" or "LV" or "NA";

  initialPressureError = targetPressure - filteredPressure;
  if (initialPressureError > 7) { // To Avoid Sudden Motor Speed Increase
    initialPressureError = 7;
  }
  initialPressureMotorSpeed = initialPressureMotorSpeed + initialPressureError * 1.0 + 0.5;
#ifdef bldcMotor
  if (initialPressureMotorSpeed < lowestMotorSpeed) {
    initialPressureMotorSpeed = lowestMotorSpeed;
  }
  if (initialPressureMotorSpeed > highestMotorSpeed) {
    initialPressureMotorSpeed = highestMotorSpeed;
  }
#endif

#ifdef dcMotor
  if (initialPressureMotorSpeed < lowestMotorSpeed) {
    initialPressureMotorSpeed = lowestMotorSpeed;
  }
  if (initialPressureMotorSpeed > highestMotorSpeed) {
    initialPressureMotorSpeed = highestMotorSpeed;
    //Serial.println("highest Motor Speed Achieved");
  }
#endif


  if (disconnectFlagPressureMode == true) {
    initialPressureMotorSpeed = 30;
  }
  /////////////////// Patient Circuit Disconnect & Reconnect Detection Flag ///////////////////
  if (filteredPressure  < 1.0) {
    disconnectLoop = disconnectLoop + 1;
    if (disconnectLoop >= 1) {
      disconnectFlagPressureMode = true;
      disconnectFlagAllModes = disconnectFlagPressureMode;
      //Serial.print("Disconnected");
      //Serial.print("Disconnected, Flow & Pressure =");
      //Serial.println(realPressure);
    }
  }

  if (filteredPressure  > 1.0) {  //if connect then false
    disconnectLoop = 0;
    disconnectFlagPressureMode = false;
    disconnectFlagAllModes = disconnectFlagPressureMode;
    //Serial.print("Normal");
    //Serial.println(realPressure);

  }

  /////////////////// End of Patient Circuit Disconnect Detection Flag ///////////////////
  serialDataOut("B");//B = Between Inspirator and Expiratory

  ///////////////////Expiratory Loop/////////////////

  numberOfTime = 0; //reset numberOfTime from inpiratory phase
  for (timeSumming = 0; timeSumming < Te; timeSumming = timeSumming + timeDiff) {
    int timex = millis();
    servoControl("open");
    if (disconnectFlagPressureMode == true) {
#ifdef bldcMotor
      bldc.write(30);
#endif
#ifdef dcMotor
      analogWrite(turbineMotorPin, 30);
#endif
    }
    else {
#ifdef bldcMotor
      bldc.write(peepMotorSpeed);
#endif
#ifdef dcMotor
      analogWrite(turbineMotorPin, peepMotorSpeed);
#endif
    }

    //pressureControlModeflowRate = -flowRateMpx7002Sensor(flowSenseMpx7002Pin) * 1.2;
    pressureControlModeflowRate = sensirionFlow.getvalue();
    delay(3);
    volume = pressureControlModeflowRate * (timeDiff / 1000) + volume;     // Tidal Volume in ml
    flowRateAllModes = pressureControlModeflowRate;
    //continuousVolumeChangeAllModes = volumePressureMode;

    timeTrack = timeTrack + timeDiff;
    //How Often Serial Data will Show
    if (timeTrack > timeAfterDataShow) {
      realPressure = pressureSenseMpx2010(pressureSenseMpx2010Pin);
      pressureAllModes = realPressure;
      motorSpeedAllModes = peepMotorSpeed;

      ema = EMA_fun(0.20, realPressure, ema);
      ema_ema = EMA_fun(0.20, ema, ema_ema);

      float DEMA = 2 * ema - ema_ema;
      //Serial.print(disconnectFlagPressureMode);
      //Serial.print("inExpPressure= ");
      //Serial.print(realPressure);
      //Serial.print( "flow= ");
      //Serial.print(pressureControlModeflowRate);
      // Serial.print( "volume= ");
      // Serial.print(volumePressureMode);
      //Serial.print(" PeepMotorSpeed= ");
      //Serial.println(peepMotorSpeed);
      //      Serial.print(" PeepErrorPressureMode= ");
      //      Serial.print(peepErrorPressureMode);
      //      Serial.print(" filter= ");
      //      Serial.println(DEMA);
      motorSpeedAllModes = peepMotorSpeed;
      serialDataOut("E"); //E = Expiratory
      timeTrack = 0;
    }


    int timey = millis();
    timeDiff = timey - timex;
  }
  ////////////// PEEP Alarm ///////////////
  pressureAllModes = realPressure;
  alarmCheckPeep(realPressure); //peepAlarm = "HP" or "LP" or "NA" //High PEEP or Low PEEP or No Alarm
  serialDataOut("L"); // L = Last Moment of Expiratory
  //////////////////PID Loop for PEEP(only P) /////////////////
  peepErrorPressureMode = targetPEEPPressure - realPressure;
  if (peepErrorPressureMode > 5) {
    peepErrorPressureMode = 1;
  }
  peepMotorSpeed = peepMotorSpeed + peepErrorPressureMode * 1.5 + 0.6; //

  if (peepMotorSpeed  > peepMotorSpeedMax) {
    peepMotorSpeed  = peepMotorSpeedMax;
  }
  if (motorPEEPSpeed  < peepMotorSpeedMin) {
    peepMotorSpeed  = peepMotorSpeedMin;
  }

}

float EMA_fun(float alpha, float latest, float stored) {
  return (alpha * latest) + ((1 - alpha) * stored);
}

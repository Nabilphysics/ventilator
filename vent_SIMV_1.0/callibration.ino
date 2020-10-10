
void sensorCallibration() {
  delay(1000);
  uint8_t i;
  for (i = 1; i <= 10; i++) {
    mpx7002Raw = analogRead(flowSenseMpx7002Pin);
    //Serial.println(mpx7002Raw);
    delay(10);
    sumMpx7002 = sumMpx7002 + mpx7002Raw;
  }

  mpx7002AnalogOffset = sumMpx7002 / (i - 1);
  //Serial.print("flowSensorMpx7002Offset= "); Serial.println(mpx7002AnalogOffset);

  for (i = 1; i <= 10; i++) {
    mpx2010Raw = analogRead(pressureSenseMpx2010Pin);
    //Serial.println(mpx2010Raw);
    delay(10);
    sumMpx2010 = sumMpx2010 + mpx2010Raw;
  }
  //Serial.println(sumMpx2010);
  mpx2010AnalogOffset = sumMpx2010 / (i - 1);
  mpx7002AnalogOffset = sumMpx7002 / (i - 1);
  //Serial.print("PressureSensorMpx2010Offset= "); Serial.println(mpx2010AnalogOffset);
  //  for(i=1;i<=10;i++){
  //    dmvRaw = analogRead(dmvAnalogPin);
  //   // Serial.println(mpx2010Raw);
  //    delay(10);
  //    sumDmv= sumDmv + dmvRaw;
  //  }
  //  //Serial.println(sumMpx2010);
  //  dmvAnalogOffset = sumDmv/(i-1);


}

#ifdef bldcMotor
void bldcCallibration() {
  digitalWrite(bldcPowerRealyPin, LOW);
  delay(100);
  bldc.write(180);
  delay(100);
  digitalWrite(bldcPowerRealyPin, HIGH);
  delay(5000);
  bldc.write(5);
  delay(5000);
}
#endif

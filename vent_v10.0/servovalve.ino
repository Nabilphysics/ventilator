#ifdef bldcMotor
void servoControl(String CloseOpen) {
  if (CloseOpen == "close") {
    releaseServo.write(servoCloseValue);
  }
  if (CloseOpen == "open") {
    releaseServo.write(servoOpenValue);
  }
}
#endif

#ifdef dcMotor
void servoControl(String CloseOpen) {
  if (CloseOpen == "close") {
    servoOne.write(servoCloseValue);
    servoTwo.write(servoCloseValue);
  }
  if (CloseOpen == "open") {
    servoOne.write(servoOpenValue);
    servoTwo.write(servoOpenValue);
  }
}
#endif

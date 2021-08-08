
float pressureSenseMpx2010(int mpxAnalogPin) {
  int mpxAnalog = analogRead(mpxAnalogPin);
  float mpxCm = ((mpxAnalog - mpx2010AnalogOffset) / 6.5) * 1.35951;
  return mpxCm;
}


 

float flowRateMpx7002Sensor(int analogPin) {
  int  flowSensorAnalog = analogRead(analogPin);
  float flowSensorCmH2o = (flowSensorAnalog - mpx7002AnalogOffset) / 20.03; 
  //double  P = flowSensorCmH2o * 98.0665;
  //analog difference and flowrate :0=0,1=146,2=207,3=254 ,4=  ,5=  ,6=  ,7=  ,8=   ,9=   ,10=
                            
  double  Q = (66.29 * sqrt(abs(flowSensorCmH2o * 98.0665)));
 // if((-255.0 < Q) && (Q <= 255.0)){
 // Q = 0;
// }
  return Q;
}

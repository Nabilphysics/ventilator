void serialDataOut(String dataEvent) {
    continuousVolumeChangeAllModes = pressureSenseMpx2010(pressureSenseMpx2010Pin);
    getVentFrame.remove(27);  
    //Serial.println(pressureSenseMpx2010(pressureSenseMpx2010Pin));
    if(volume < 0){
      volume = 0;
    }
      dataOut =
      "@" +
      dataEvent +
      "," +
      String(disconnectFlagAllModes) + 
      "," +
      getVentFrame +
      "," +
      volumeAlarm +
      "," +
      peepAlarm +
      "," +
      pipAlarm +
      "," +
      String(flowRateAllModes) +
      "," +
      String(volume) +
      "," +
      String(pressureAllModes) +
      "," +
      String(motorSpeedAllModes) +
      "#";
      //" FR=" + String(flowRate) +
    
      //" Vcon= " + String(volume) +
      //" MpxP= " + String(pressureSenseMpx2010(pressureSenseMpx2010Pin)) +
      //" mpxAnalog= "+String(analogRead(pressureSenseMpx2010Pin))+
      //" t= " + String(numberOfTime) +
      //" MotSp= "+String(motorSpeed)+
      //" peep= "+ String(peep)+
      //" peepError= "+String(peepError)+
      //" PEEPSpeed= "+String(motorPEEPSpeed)+
    
     Serial.println(dataOut);

      String dataOut2 =
      "@" +
      dataEvent +
      " Dis= " +
      String(disconnectFlagAllModes) + 
      ">" +
      getVentFrame +
      " VolAlarm= " +
      volumeAlarm +
      " peepAlarm =" +
      peepAlarm +
      " pipAlarm= " +
      pipAlarm +
      " FlowRate= " +
      //String(flowRateAllModes) +
      String(sensirionFlow.getvalue())+
      " Vol= " +
      String(volume) +
      " Time= " +
      String(timeDifference) +
      " Prsr= " +
      String(pressureAllModes) +
      " Motor= " +
      String(motorSpeedAllModes) +
      " #";
      
    // Serial.println(dataOut2);
//      Serial.print("pressure= ");
//      Serial.print(pressureSenseMpx2010(pressureSenseMpx2010Pin));
  //    Serial.print("volume= ");
//      Serial.print(volume);
//   
//      Serial.print(" ");
//      Serial.print(sensirionFlow.getvalue());
//      Serial.print(" ");
//      Serial.println(pressureSenseMpx2010(pressureSenseMpx2010Pin));
      
      Serial.flush();
//////////////// Tidal Volume Graph //////////////////////////
//if(volume < 0){
//  volume = 0;
// // Serial.println(volume);
//}
//else{
// // Serial.println(volume);
//}

      //Serial.println(peepError);
      //Serial.println(pressureMeasured);
      //Serial.println(flowRate);
 
  //float  sensor_value = pressureSenseMpx2010(pressureSenseMpx2010Pin);
 
  float sensor_value = flowRate;
  //Serial.print("Raw= ");
  //Serial.print(sensor_value);
  ema = EMA_function(ema_a, sensor_value, ema);
  ema_ema = EMA_function(ema_a, ema, ema_ema);
   
  float DEMA = 2*ema - ema_ema;    
///////////Filtered Flow Rate Output /////////////
  //Serial.print(" DEMA= ");
  //Serial.println(DEMA);
  
  //Serial.print(" , Raw= ");
  //Serial.println(sensor_value);
 
  
      
}

float EMA_function(float alpha, float latest, float stored){
  return (alpha*latest) + ((1-alpha)*stored);
}

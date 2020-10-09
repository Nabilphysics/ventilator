//int TvAlarmHigh;
//int TvAlarmLow;
//int peepAlarmHigh;
//int peepAlarmLow;
//int pipAlarmHigh;
//int pipAlarmLow;

void alarmCheckVolume(int volumeAnyMode){
  if(volumeAnyMode > TvAlarmHigh){
    volumeAlarm = "HV"; //High Volume
  }
  else if(volumeAnyMode < TvAlarmLow ){
    volumeAlarm = "LV"; //Low Volume
  }
  else{
    volumeAlarm = "NA"; //No Alarm
  }
}

void alarmCheckPeep(float peepAnyMode){
    if(peepAnyMode > peepAlarmHigh){
    peepAlarm = "HP"; // High PEEP
  }
  else if(peepAnyMode < peepAlarmLow ){
    peepAlarm = "LP"; //Low PEEP 
  }
  else{
    peepAlarm = "NA";
  }
}

void alarmCheckPip(float pipAnyMode){
   if(pipAnyMode > pipAlarmHigh){
    pipAlarm = "HPP"; // High Peak Pressure
  }
  else if(pipAnyMode < pipAlarmLow ){
    pipAlarm = "LPP"; //Low Peak Pressure 
  }
  else{
    pipAlarm = "NAA"; //No Alarm NAA insteed of NA to maintain length
  }
}
void alarmDisconnect(boolean disconnectAnyMode){
    //ToDo: Hardware Beep or Other Thing e.g. if(disconnectAnyMode == true){//do something}
}

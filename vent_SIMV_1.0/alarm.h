#ifndef ALARM_H
#define ALARM_H

void alarmCheckVolume(int volumeAnyMode);
void alarmCheckPeep(float peepAnyMode);
void alarmCheckPip(float pipAnyMode);
void alarmDisconnect(boolean disconnectAnyMode);

String alarmReason;

String volumeAlarm; //NM = no alarm
String peepAlarm;
String pipAlarm;

#endif

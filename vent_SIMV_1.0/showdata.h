#ifndef SHOWDATA_H
#define SHOWDATA_H

//serialDataOut("B");// I=During Inspiratory or B=Between Inspiratory & Expiratory or E=During Expiratory or L= End of Exiratory 
//>LV>NA>NAA>508>5>19>47#
// @I>0>p155001.5052090050025150702>LV>NA>NAA>486>5>18>44#
// @ = Begining > I=Data State > 0 = Disconnect Flag >p155001.5052090050025150702 > volume Alarm(LV,HV,NA) > peep ALarm(LP,HP,NA) > pip Alarm(HPP,LPP,NAA) > Flow Rate(Graph) > Volume Change(Graph) > Pressure(Graph) > Motor Speed 
//// I=During Inspiratory or B=Between Inspiratory & Expiratory or E=During Expiratory or L= End of Exiratory
// mode p=pressure support, v=volume support
// 
 
 //p155001.5052090050025150702
//Mode,BPM(2digit),Tv(3 digit),Ti(1.5),PEEP(2),PIP(2),TvAlarmHigh(3),TvAlarmLow(3),pipAlarmHigh(2),pipAlarmLow(2),peepAlarmHigh(2),peepAlarmLow(2)
  //           V,20,999,1.5,05,20,900,500,25,15,07,02     

//#include "config.h"
//#include "servovalve.h"
#include "volume.h"
#include "sensor.h"
#include "SFM3000CORE.h"
void serialDataOut(String dataEvent);
int EMA_function(float alpha, int latest, int stored);
float ema_a = 0.11; //0.27= pressure
float ema_ema = 0;
float ema = 0;
#endif

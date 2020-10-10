#ifndef CALLIBRATION_H
#define CALLIBRATION_H

#include "config.h"
void sensorCallibration();
#ifdef bldcMotor
void bldcCallibration();
#endif

int mpx7002AnalogOffset;
int mpx2010AnalogOffset;
int dmvAnalogOffset;

int mpx7002Raw = 0;
int mpx2010Raw = 0;
int dmvRaw =0;

int sumMpx7002 = 0;
int sumMpx2010 = 0;
int sumDmv = 0;


#endif

#ifndef SIMV_H
#define SIMV_H

#include "config.h"
#include "servovalve.h"
#include "showdata.h"
#include "datafilter.h"
#include "alarm.h"
void simv();

//uint8_t simvPressure;
//uint8_t simvPeepPressure;
//uint8_t simvPeepPressureError;
//uint8_t simvTriggerPressure;
//
//uint8_t simvInpirationMotorSpeed;
//uint8_t simvPeepMotorSpeed; 
//
//uint8_t simvHighestMotorSpeed;
//uint8_t simvLowestMotorSpeed;
//
//float targetFlow;
//float flowError;

//volume
////int volume; redefinition of int volume
//float pressureControlModeflowRate;
//#ifdef bldcMotor
//uint8_t highestMotorSpeed = 160;//Dont increase from 225 because during ins highest motor speed 225+30=255. If increase Error Will off motor
//uint8_t lowestMotorSpeed = 15;
//uint8_t initialPressureMotorSpeed = 30;
//uint8_t peepMotorSpeed = 30;
//uint8_t peepMotorSpeedMax = 80;
//uint8_t peepMotorSpeedMin = 20;
//uint8_t peepErrorPressureMode;
//uint8_t bldcSpeed;
//#endif
//
//#ifdef dcMotor
//uint8_t highestMotorSpeed = 225; //Dont increase from 225 because during ins highest motor speed 225+30=255. If increase Error Will off motor
//uint8_t lowestMotorSpeed = 5;
//uint8_t initialPressureMotorSpeed = 30;
//uint8_t peepMotorSpeed = 30;
//uint8_t peepMotorSpeedMax = 80;
//uint8_t peepMotorSpeedMin = 20;
//uint8_t peepErrorPressureMode;
#endif


float pError;
float initialPressureError;
float realPressure;
float pressureRawArray[200];



boolean disconnectFlagPressureMode = false;
uint8_t disconnectLoop = 0;

#endif

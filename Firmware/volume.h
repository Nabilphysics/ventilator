#ifndef VOLUME_H
#define VOLUME_H

#include "servovalve.h"
#include "config.h"
#include "showdata.h"
#include "alarm.h"
void volumeControl();
String dataOut;
float timeDiff;

float flowRate;
float flowRawArray[100];
float pressureRawArrayVolume[250];
int volumeRawArray[250];
int timeTrack = 0;
int timeTrackVolumeUpdate = 0;
int timeSumming;
float volume;
int numberOfTime = 0;

float peepError;
float pressureMeasured;
boolean disconnectFlag = false;
uint8_t disconnectEvent = 0;
#endif

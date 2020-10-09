#ifndef DATAFILTER_H
#define DATAFILTER_H

float dataFilter(float dataArray[], int arraySize);
float highestPressureCalculator(float dataArray[], int arraySize);
int highestVolumeCalculator(int volumeDataArray[], int arraySize);
//
float shrinkPercent = 0.40; //20% low from Standard Deviation Range(lower > less filter)

float sum = 0;
float sumSd = 0;
float stdDev = 0;
float stdShrink = 0;
float filterAdd = 0;
float filterDataAvg = 0;
int dataIndex;
int k;
float mean =0;


#endif

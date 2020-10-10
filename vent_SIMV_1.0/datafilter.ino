//float highestPressure = highestPressureCalculator(pressureArray, numberOfTime);


float highestPressureCalculator(float dataArray[], int arraySize){
  float highestVal = 0;
  for(dataIndex = 0; dataIndex < arraySize; dataIndex++){
    float val = dataArray[dataIndex];
    if(val > highestVal){
      highestVal = val;
    }
  }
  return highestVal;
}

int highestVolumeCalculator(int volumeDataArray[], int arraySize){
  int highestVolume = 0;
  for(int volDataIndex = 0; volDataIndex < arraySize; volDataIndex ++){
    int volumeVal = volumeDataArray[volDataIndex];
    if(volumeVal > highestVolume){
      highestVolume = volumeVal;
    }
  }
  return highestVolume;
}

float dataFilter(float dataArray[], int arraySize) {
 filterDataAvg =0;
 mean =0;
 sum =0;
 sumSd =0;
 filterAdd = 0;
 stdDev = 0;
 float filterArray[arraySize];
  k = 0;
  for (dataIndex = 0; dataIndex < arraySize; dataIndex++) {
    sum = sum + dataArray[dataIndex];
  }
  mean = sum / dataIndex;
//  Serial.print("mean= ");
//  Serial.println(mean);
  for (dataIndex = 0; dataIndex < arraySize; dataIndex++) {
    sumSd = sumSd + sq((dataArray[dataIndex] - mean));
  }

  stdDev = sqrt(sumSd / dataIndex);
  if (stdDev < 1)
  {
    //Serial.println("direct mean");
    return mean;
  }
  else {
    //Serial.println("else");
    stdShrink = stdDev - (stdDev * shrinkPercent);
    //Serial.print("stdShrink= ");
    //Serial.println(stdShrink);
    for (dataIndex = 0; dataIndex < arraySize; dataIndex++) {
      if ((dataArray[dataIndex] < (mean + stdShrink)) && (dataArray[dataIndex] > (mean - stdShrink))) {
        dataArray[dataIndex] = dataArray[dataIndex];
        //Serial.print("dataArray[i]= ");Serial.println(dataArray[dataIndex]);
        k = k + 1;
        filterAdd = filterAdd + dataArray[dataIndex];
        //    Serial.print("k= ");Serial.print(k);Serial.print(" DataArray[i");Serial.print(i);Serial.print("]= ");
        //    Serial.println(dataArray[i]);
      }

    }
    //Serial.print("k= ");
    //Serial.println(k);
    filterDataAvg = filterAdd / k;
    if (k == 0)return mean;
    else return filterDataAvg;

  }
  
}

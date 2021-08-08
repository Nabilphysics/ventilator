/*
  Modified From: https://www.14core.com/wiring-sfm3000-air-gas-flow-meter/

*/

#include "SFM3000CORE.h"
#include <Wire.h>

 
//SFM3000CORE::SFM3000CORE(uint8_t i2cAddress)
SFM3000CORE::SFM3000CORE(int i2cAddress)
{
  //: mI2cAddress(i2cAddress)
  mI2cAddress = i2cAddress;
}

void SFM3000CORE::init()
{
  int a = 0;
  int b = 0;
  int c = 0; 
  
 
  Wire.begin();
  //Serial.begin(9600);
  delay(1000);
  Wire.beginTransmission(byte(mI2cAddress)); // transmit to device with I2C mI2cAddress
  Wire.beginTransmission(byte(mI2cAddress)); // transmit to device with I2C mI2cAddress
  Wire.write(byte(0x10));      //
  Wire.write(byte(0x00));      //
  Wire.endTransmission();
  delay(5);
  
  Wire.requestFrom(mI2cAddress, 3); //
  a = Wire.read(); // received first byte stored here
  b = Wire.read(); // received second byte stored here
  c = Wire.read(); // received third byte stored here

  Wire.endTransmission();
  //Serial.print(a);
  //Serial.print(b);
  //Serial.println(c);
  
  delay(5);
  
  Wire.requestFrom(mI2cAddress, 3); //Wire.requestFrom(mI2cAddress, 3); 
  a = Wire.read(); // received  first byte stored here
  b = Wire.read(); // received second byte stored here
  c = Wire.read(); // received third byte stored here
  Wire.endTransmission();
  //Serial.print(a);
  //Serial.print(b);
  //Serial.println(c);
  
  delay(5);
  
}

void SFM3000CORE::softReset()
{ 
    Wire.beginTransmission(byte(64));
    Wire.write(0x20);
    Wire.write(0x00);
    delay(500);
    Serial.println("SofT Reset");
    
//    int ret;
//    do {
//    // Soft reset the sensor
//    Wire.beginTransmission(byte(64));
//    Wire.write(0x20);
//    Wire.write(0x00);
//    delay(500);
//    ret = Wire.endTransmission();
//    if (ret != 0) {
//      Serial.println("Error while sending soft reset command, retrying...");
//      delay(500); // wait long enough for chip reset to complete
//    }
//  } while (ret != 0);
//  if(ret == 0){
//    Serial.println("Soft Reset Done");
//  }
}
 
float SFM3000CORE::getvalue()
{
    Wire.requestFrom(mI2cAddress, 3); // set read 3 bytes from device with address 0x40
  uint16_t a = Wire.read(); // received first byte stored here. The variable "uint16_t" can hold 2 bytes, this will be relevant later
  uint8_t b = Wire.read(); // second received byte stored here
  uint8_t crc = Wire.read(); // crc value stored here
  uint8_t mycrc = 0xFF; // initialize crc variable
  mycrc = crc8(a, mycrc); // let first byte through CRC calculation
  mycrc = crc8(b, mycrc); // and the second byte too
  if (mycrc != crc) { // check if the calculated and the received CRC byte matches
    //Serial.println("Error: wrong CRC");
  }
  a = (a << 8) | b; // combine the two received bytes to a 16bit integer value
  // a >>= 2; // remove the two least significant bits
  //float Flow = (float)a;
  unsigned int Flow=a;
  float flowSlm = ((float)Flow - sensirion3300Offset) / scaleFactor; //Flow in slm
  float milliLiterPerSecond = flowSlm * 16.66; // (flowSLM/60)*1000 to convert milli Liter Per Second , Here 1000/60=16.66 
  return milliLiterPerSecond - flowSensorError;
  //return flowSlm;
}

uint8_t SFM3000CORE::crc8(const uint8_t data, uint8_t crc)
{
     crc ^= data;
     for ( uint8_t i = 8; i; --i ) {
       crc = ( crc & 0x80 )
       ? (crc << 1) ^ 0x31
       : (crc << 1);
    }
  return crc;
}

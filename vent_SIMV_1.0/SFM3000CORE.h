/*
  Credit:
  Modified From: https://www.14core.com/wiring-sfm3000-air-gas-flow-meter/
 
 */

#ifndef SFM3000CORE_h
#define SFM3000CORE_h
 
class SFM3000CORE {
  public:
    SFM3000CORE(int i2cAddress);
    void init();
    float getvalue();
    void softReset();
 
  private:
  
  int mI2cAddress;
  long int sensirion3300Offset = 32768; // Offset for Sensirion 3300 Flow Sensor
  float scaleFactor = 120.0; //For Air
  uint8_t crc8(const uint8_t data, uint8_t crc);
};

 
#endif

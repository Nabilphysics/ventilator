float simvPressure;
float simvTargetPeepPressure = 5.0;
float simvPeepPressureError;
float simvTriggerPressure;

uint8_t simvInpirationMotorSpeed = 5;
uint8_t simvPeepMotorSpeed = 30; 
uint8_t simvPeepMotorSpeedMax = 255;
uint8_t simvPeepMotorSpeedMin = 5;
uint8_t simvInitialMotorSpeed = 10;
uint8_t simvHighestMotorSpeed;
uint8_t simvLowestMotorSpeed;

float simvTargetFlow; //ml
float simvFlowError;
float simvFlowRate;

float timeDiffsimv;
void simv(){
  
  inspiration();
  //Serial.println("Mid without Function");

  mid1();
  expiration();
  
  
  }
  /////////////////////inspiratory Phase///////////////
 void inspiration(){
  Serial.println("1");
 }
 void expiration(){
  Serial.println("3");
  mid1();
  
 }

void mid1(){
  Serial.println("2");
 }

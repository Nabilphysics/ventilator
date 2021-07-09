#ifndef CONFIG_H
#define CONFIG_H

//////////Ventilator Hardware Pin//////////////
//Hardware Connection: 

//Servo 1 > Servo One(Pressure Release Servo) > Pin 5
//pin:9 > turbine motor(bldc or dc)
//A0: > Pressure Sensor, 
//D17: > FlowSensorPower(5V)
// Indicator LED > A5
//Left to Right- VDD,SCL(A5),GND,SDA(A4)
//Sensirion Flow Sensor: Left to Right- VDD,SCL(Arduino - A5),GND,SDA(Arduino - A4)
//Unused in this version
//Pin:11 > servo or relay (for BLDC Version)
//Servo 2 > ArduinoMega Pin 52

//////// Select Motor Type /////////////
//#define bldcMotor
#define dcMotor
//////////// I/O Pin Defination ////////////////////
const byte flowSensorOnOff = 17;
const byte turbineMotorPin = 9;
const byte flowSenseMpx7002Pin = 14;//A14
const byte pressureSenseMpx2010Pin = 0; //A0
const byte indicatorLed = A5;

//////// Universal Variable  ////////
boolean disconnectFlagAllModes;
float flowSensorError = 0;//in ml
int flowRateAllModes;
float continuousVolumeChangeAllModes;
uint8_t motorSpeedAllModes;
float pressureAllModes;
uint8_t timeAfterDataShow = 40; //ms
uint8_t stabilizingCounter = 0;

float timex;
float timey;
float timeDifference;






#ifdef dcMotor
//////////// Servo Pin ///////////////
Servo servoOne;
Servo servoTwo;
Servo oxygenServo;
Servo airServo;
Servo peepServo;
const byte servoOnePin = 5; // pressure release servo
const byte servoTwoPin = 52;
const byte oxygenServoPin = 46;
const byte airServoPin = 48;

uint8_t servoCloseValue = 56; //High Pressure
uint8_t servoOpenValue = 90; // Low Pressure
uint8_t oxygenServoFullOpenValue = 170;
//uint8_t oxygenServo;

uint8_t motorSpeed = 30; //initial inspiratory motor speed 
uint8_t motorSpeedMin = 20;
uint8_t motorSpeedMax = 140;
float motorPEEPSpeed = 30;
float motorPEEPSpeedMax = 150;
float motorPEEPSpeedMin = 20;
#endif

//////////Start CPAP Mode Settings //////////////
#ifdef dcMotor
int motorBaseSpeedCpap = 10;
int motorSpeedMaxCpap = 150;
int motorSpeedMinCpap = 0;
float cpapPressureMax = 20.0;
float cpapRampTime;
float cpapPidProportionalConstant;

#endif




//////////End CPAP Mode Settings//////////////

#endif

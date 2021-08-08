#ifndef SENSOR_H
#define SENSOR_H

float pressureSenseMpx2010(int mpxAnalogPin);
float flowRateMpx7002Sensor(int analogPin);

// Dimentional & Environmental Constants
//double Di = 0.018;     // Inlet Diameter(m)
//double Do = 0.010;     // Orifice Diameter(m)
//double B;              // Diameter ratio beta, Do/Di
//double Ai;             // Inlet crossectional Area(m2)
//double Ao;             // Orifice crosssectional Area(m2)
//double Fd = 1.16;      // Fluid densitty (kg/m3)
//double Cf = 0.65;      // Flow coefficient
//double Cd;             // Discharge Coefficient, Ao/Ai (area ratio)
//double dt = 0.001;     // Time derivative

#endif

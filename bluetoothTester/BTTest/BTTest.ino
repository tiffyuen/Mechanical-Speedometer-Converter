#include <EEPROM.h>

unsigned long inRatio;    // input ratio, Also happens to be dt for 0.1 SPH [dt > inRatio => SPHr = 0]
unsigned long outRatio;   // output ratio * 10,000,000 (to compensate for float)
unsigned short maxSpeed;  // max speed our speedometer can show

bool SCalMode = false;
byte SRecMode = 0;
unsigned long targetRPM;

/*
 * Calculate inRatio via stored values.
 * finalDrive should be 1 if not being used
 * wheelCirc will have units of meter or milliMiles (miles * 1000) [odd units but necessary for easy calcs]
 */
void updateInputRatio(char numMag, float wheelCirc, float finalDrive) {
  // Hard coded number = 3,600,000,000 [microseconds/hr] / 1000 [compensate for milliMiles/meters]
  //                     * 10 [compensate for SPHr/and targetRPM units]
  // Complicated I know, but floats on arduino are very Very VERY slow
  inRatio = (long) (finalDrive * wheelCirc * 36000000 / (float) numMag);
}

void setup() {
  
  Serial.begin(9600);
}

void loop() {

  if(checkBT()){
    delay(10);
  }
}

bool checkBT() {
  static char data[26];
  bool updated = false;
  float temp;

  // check and recieve data
  switch(recvData(data)) {
    Serial.print(recvData(data));
    case 'U': // Store Units
      EEPROM.update(0, data[0] - '0');
      if(EEPROM.read(0) == 0){
        Serial.println("MPH");
      } else if (EEPROM.read(0) == 1)  {
        Serial.println("KPH");
      }
      updated = true;
      break;

    case 'M': // Store Max Speed
      maxSpeed = (short) atol(data);
      EEPROM.put(1, maxSpeed);
      Serial.print(EEPROM.get(1, maxSpeed));
      updated = true;
      break;
      
    case 'N': // Store Number of Magnets
      EEPROM.update(3, data[0] - '0');
      //Serial.print(EEPROM.get(3, data[0] - '0'));
      updateInputRatio(EEPROM.read(3), EEPROM.get(12, temp), EEPROM.get(4, temp));
      if(EEPROM.read(3) == 1){
        Serial.println("1");
      } else if(EEPROM.read(3) == 2){
        Serial.println("2");
      } else if(EEPROM.read(3) == 4) {
        Serial.println("4");
      }
      updated = true;
      break;

    case 'F': // Store Final Drive Ratio
      EEPROM.put(4, ((float) atol(data)) / 1000000);
      updateInputRatio(EEPROM.read(3), EEPROM.get(12, temp), EEPROM.get(4, temp));
      Serial.print(EEPROM.get(4, temp), 9);
      updated = true;
      break;

    case 'S': // Store Speedometer Ratio
      outRatio = atol(data);
      EEPROM.put(8, outRatio);
      Serial.print(EEPROM.get(8, outRatio));
      updated = true;
      break;

    case 'W': // Store Tire Size & Circumference
      char* temps;
      byte i = 0

      EEPROM.put(12, ((float) atol(strtok(data, ":"))) / 1000000);

      temps = strtok(NULL, ":");
      for(; i < 14 && temps[i] != '\0'; i++) {
        EEPROM.update(16 + i, temps[i]);
      }
      EEPROM.update(16 + i, '\0')

      updateInputRatio(EEPROM.read(3), EEPROM.get(12, temp), EEPROM.get(4, temp));
      Serial.print(EEPROM.get(12, temp), 9);
      updated = true;
      break;

    case 'P':
      SCalMode = data[0] - '0';
      Serial.print(data[0]);
      break;

    case 'T':
      if(SCalMode) {
        targetRPM = atol(data);
        Serial.print(targetRPM);
      }
      break;

    case 'D':
      byte tempb = atol(data)
      Serial.print(tempb);

      if(tempb) {
        SRecMode = tempb;
        // set all recording data to 0
        // start recording data

      } else {
        // Do calculations, update ratios and send value off
        /*
          EEPROM.put(4, ((float) atol(data)) / 1000000);
          updateInputRatio(EEPROM.read(3), EEPROM.get(12, temp), EEPROM.get(4, temp));
          Serial.print(EEPROM.get(4, temp), 9);
          updated = true;
        */
        Serial.print("F:10000000");
        SRecMode = 0;
      }

      break;

    case '\0':
    default:
      break;
  }

  if(updated) {
    digitalWrite(LED_BUILTIN, HIGH);
    for(byte i = 0; data[i] != '\0' && i < 13; i++) {
      Serial.print(data[i]);
    }
    Serial.println("");
  }
}

char recvData(char* data) {
  char type = '\0';
  byte itr;

  byte aval = Serial.available();

/*if(aval != 0){
  Serial.print("numData: ");
  Serial.println(aval);
}*/
  if(Serial.available() > 1) {
    type = Serial.read();

    if(!isupper(type)) {
      digitalWrite(LED_BUILTIN, HIGH);
      return '\0';
    }

    data[0] = Serial.read();

    if(data[0] != ':') {
      digitalWrite(LED_BUILTIN, HIGH);
      return '\0';
    }

    for(itr = 0; itr < 26 && (Serial.available() > 0); itr++) {
      data[itr] = Serial.read();
      delay(1);
    }

    if(itr == 26 && data[23] != '\0') {
      digitalWrite(LED_BUILTIN, HIGH);
      return '\0';
    }

    digitalWrite(LED_BUILTIN, LOW);
  }
  
  return type;
}
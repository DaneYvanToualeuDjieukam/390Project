/*
 * @@@@@@@@@@@@@@@@ LITTLE READ ME @@@@@@@@@@@@@@@@@@@@@
 * doc["button"] == "reset_pressed" - > ONE pressed the wps button
 * doc["button"] == "alarm_off"   -> teh user pressed the button to turn off the alarm
 * doc["type"] == "request" -> send a request to uno to get the sensors states
 * doc["type"] == "sleep_mode" -> tell the hardware to stop for about 5min -8min (to define by programmer)
 * doc["type"] = "result"   -> answer the esp32's "request"
 * doc["type"] == "setup"   -> no wps in eeprom, wait for the user to press the wps button
 * doc["error_ring"] == "two_rings" -> to define a successfull wps connection
 * doc["error_ring"] == "one_ring" -> to define an unsucessfull wps connection
 * doc["state"] == "ON"  -> fire/smoke detected
 * doc["state"] == "OFF" -> fire/smoke not detected
 * 
 * Sleep in arduino has to be lower than the one on esp, as the esp is the master..
 */

//Include the needed library, we will use softer serial communication with the ESP8266
#include <ArduinoJson.h>
#include <LowPower.h>
#include <avr/wdt.h>
#include <SoftwareSerial.h>

// Send a JSON-formatted request with key "type" and value "request"
// then parse the JSON-formatted response with keys "gas" and "distance"
DynamicJsonDocument doc(1024);

// initialize the serial port that will be used
#define RX 0    //INPUT
#define TX 1    //OUTPUT

//Led and buzzer
#define ledPin 12  
#define buzzer 11 //define buzzer pin

//button related
#define alarmbtn_Pin 10   //to stop the ringing of the device
boolean buttonState = LOW;  
boolean button_Still_Pressed = false;
int pressed=0;

//for flame sensor
#define flame_digital 13 // define the flame sensor pin

//for smoke sensor
#define smoke_analog A0
#define sensorThres 300

//serial port related
String message = "";
bool messageReady = false;


/*
 * Loop until the serial document from uno was received
 * true if doc was received
 */
 /*
boolean loop_until_serialisation(){
  boolean keeplooping = true;
  String message = "";
  while(keeplooping) // loop until a decument from uno is received
  { 
    if(Serial.available())
    {
      message = Serial.readString(); // get the value passed on the serial port
      //attempt to desertilize the message
      DeserializationError error = deserializeJson(doc,message);
      if(error)
      { 
        keeplooping = true;
      } 
      else
      {
        return true;   //exit the loop as a value is inputed on the serial port
      }
    }
  }
}
*/

/*
 * Delay the uno for 5s (turn off the device power output
 */
void quick_sleep_until_next_phase(){
  //delay(6000); //delay the program for 10s 
}

/*
 * 
//determine if a fire or smoke was detected
 *
 */
void smoke_or_fire_detected()
{  
  bool smoke_fire_detected = false;
  int analogSensor = analogRead(smoke_analog);
  int value = digitalRead (flame_digital) ;// read flamedigital value and assigne it to val variable
  int button_current_state = digitalRead(alarmbtn_Pin);

  if ((analogSensor > sensorThres ||value == LOW)){   //if value = 0, fire detected
    smoke_fire_detected = true;
  }
  else
  {
    digitalWrite(ledPin,LOW);
    doc["state"] = "OFF";
    return;   //nothing else to do in the loop my G
  }

  if(button_current_state == LOW && smoke_fire_detected){   //Button is pressed
    digitalWrite(ledPin,LOW);
    doc["button"] = "alarm_off";  // -> the user pressed the button to turn off the alarm
    quick_sleep_until_next_phase();                //go in deep sleep ----------------------- edit
    smoke_fire_detected = false;
  }
  else if(smoke_fire_detected)     //Button not pressed even is there is a smoke/fire
  {
    digitalWrite(ledPin,HIGH);
    doc["state"] = "ON";
    make_it_ring("twice");
  }
}

/*
 * 
//Buzz time!
 *
 */
void make_it_ring(String times)
{
  if(times == "once")
  {
    tone(buzzer, 650, 2000); //the buzzer emit sound at 650 MHz for 500 millis
  }
  else if(times == "twice")
  {
    tone(buzzer, 650, 500); //the buzzer emit sound at 650 MHz for 500 millis
    delay(1000); //wait 500 millis
    tone(buzzer, 650, 500); //the buzzer emit sound at 650 MHz for 500 millis
  }
}

/*
 * As the name says
 */
void softwareReset( uint8_t prescaller) {
  // start watchdog with the provided prescaller
  wdt_enable(prescaller);
  // wait for the prescaller time to expire
  // without sending the reset signal by using
  // the wdt_reset() method
  while(1) {}
}

/*
 *
//arduino uno pins input/output setup
 *
 */
void setup()
{
  Serial.begin(4800);    
  Serial.setTimeout(1000);    //set a value of 1s to read the serial
  pinMode(ledPin, OUTPUT);       
  pinMode (buzzer, OUTPUT); //output interface defines the buzzer
  pinMode(smoke_analog, INPUT);
  pinMode (flame_digital, INPUT) ; // input interface defines the flame sensor 
  pinMode(alarmbtn_Pin, INPUT_PULLUP);    //INPUT_PULLUP since it's connected to a resistor to 5v (left one)
  pinMode(RX,INPUT);    //INPUT FOR SERIAL PORT
  pinMode(TX,OUTPUT);   //OUPUT FOR SERIAL PORT
}

/*
 * see https://www.baldengineer.com/arduino-internal-pull-up-resistor-tutorial.html for pullup resistor
 */
void loop() 
{
  /*
  String message = "";
  
  if(Serial.available()){

      make_it_ring("twice");
      message = Serial.readString(); // get the value passed on the serial port

        Serial.println("The message is...");
        Serial.println(message);
        
      //attempt to desertilize the message
      DeserializationError error = deserializeJson(doc,message);
      if(error)
      {
        Serial.print(F("deserializeJson() failed: "));
        Serial.println(error.c_str());
      } 
      else
      {
              if(doc["type"] == "sleep_mode")
              {
                make_it_ring("once");
                delay(2000);
              }
      }
    }
    else
    {
    }
  */

  //loop_until_serialisation(); //Loop until a doc is output on the serial port
  boolean keeplooping = true;
  String message = "";
  while(keeplooping) // loop until a decument from uno is received
  { 
    if(Serial.available())
    {
      message = Serial.readString(); // get the value passed on the serial port
        
      //attempt to desertilize the message
      DeserializationError error = deserializeJson(doc,message);
      if(error)     //if an error, then the right file wasn't sent; keep looping
      { 
        keeplooping = true;
      } 
      else      //if there is no error, then get the doc - file
      {    
        if(doc["type"] == "reset_pressed")
        {
          make_it_ring("twice");
         // softwareReset(WDTO_60MS); //restart in 10ms
        }
        else if(doc["type"] == "turn_on")   //turn the device on
        {
          // device is on... do nothing
        }
        else if(doc["type"] == "quick_ring")
        {
          tone(buzzer, 500, 500); //the buzzer emit sound at 400 MHz for 500 millis
        }
        else if(doc["type"] == "turn_off")  //turn the device on
        {
         quick_sleep_until_next_phase(); 
        }
        else if(doc["type"] == "request")
        {   
          smoke_or_fire_detected();
          doc["type"] = "result";
          serializeJson(doc,Serial);  //send document to serial port
          //delay(2000);
        }
        else if(doc["type"] == "sleep_mode")
        {
          delay(6000); //delay the program for 10s 
        }
        else if(doc["error_ring"] == "two_rings") // to define a successfull wps connection
        {
          make_it_ring("twice");
        }
        else if(doc["error_ring"] == "one_ring") //to define an unsucessfull wps connection
        {
          make_it_ring("once");
        }
      }
    }
  }
}

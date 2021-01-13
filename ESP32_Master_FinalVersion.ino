/*
 * @@@@@@@@@@@@@@@@ LITTLE READ ME @@@@@@@@@@@@@@@@@@@@@
 * doc["button"] == "reset_pressed" - > ONE pressed the wps button
 * doc["button"] == "alarm_off"   -> teh user pressed the button to turn off the alarm
 * doc["type"] == "request" -> send a request to uno to get the sensors states
 * doc["type"] == "sleep_mode" -> tell the hardware to stop for about 5min -8min (to define by programmer)
 * doc["type"] = "result"   -> answer the esp32's "request"
 * doc["error_ring"] == "two_rings" -> to define a successfull wps connection
 * doc["error_ring"] == "one_ring" -> to define an unsucessfull wps connection
 * doc["state"] == "ON"  -> fire/smoke detected
 * doc["state"] == "OFF" -> fire/smoke not detected
 */

// Include the needed library, we will use softer serial communication with the ESP8266
#include <ArduinoJson.h>
#include <WiFi.h>
#include <EEPROM.h>
#include <Arduino.h>  
#include <esp_wps.h>
#include <SoftwareSerial.h>

// ESP Configuration
#define ESP_WPS_MODE      WPS_TYPE_PBC
#define ESP_MANUFACTURER  "ESPRESSIF"
#define ESP_MODEL_NUMBER  "ESP32"
#define ESP_MODEL_NAME    "ESPRESSIF IOT"
#define ESP_DEVICE_NAME   "ESP STATION"
#define EEPROM_SIZE 250

// initialize the serial port that will be used - Esp32NodeMCU
#define RX 3    //INPUT
#define TX 1    //OUPUT

// Device's industrial info 
String DEVICE_ID = "Kul78vB";
String DEVICE_PASSWORD = "HUJ";

//UserUID on firebase
String UserID = "";
String device_newName = "";

// WPS and Wifi connection Variables
static esp_wps_config_t config;
boolean wps_connected = false;
boolean defaultWifi_connected = false;  
boolean start_DefaultWiFi_connected = false; // defines if the default wifi configuration should be done (assuming a previous wps configuration was performed)
int counter_restart = 0;    //value saved in EEPROM. Defines the number of time one tried to connect to internet without much success. (max of 10times)   
 
// Firebase settings
// see - https://github.com/mobizt/Firebase-ESP32 - for the different push/pull methods, etc.
#include <FB_HTTPClient32.h>
#include <FirebaseESP32.h>
#include <FirebaseJson.h>
#define FIREBASE_HOST "project-1b3d6.firebaseio.com"                         //--> URL address of your Firebase Realtime Database.
#define FIREBASE_AUTH "7nVgVdQONJKkN6HpnyBUXxIC899C5HzSdYWtwSVR"                 //--> Your firebase database secret key.
FirebaseData firebaseData;

//to reset the device if needed
const int reset_button = 13;

// Send a JSON-formatted request with key "type" and value "request"
// then parse the JSON-formatted response with keys "gas" and "distance"
DynamicJsonDocument doc(1024);

/*
 * Write the wifi name in EEPROM (100bytes) and wifi password (100bytes)
 * pass the adddress offset(contains will containt the lenght of the string) and the string to pass.
 * password shouldn't be longer than 200bytes... a wifi name  or passwords that long is just unsusual...
 * see - https://roboticsbackend.com/arduino-write-string-in-eeprom/#Write_the_String
*/
void writeString_ToEEPROM(int addrOffset, String strToWrite){
  byte len = strToWrite.length();  //len = length
  //EEPROM.write(addrOffset, len);
  for (int i = 0; i < len; i++)
  {
    EEPROM.write(addrOffset + i, strToWrite[i]);
  }
  EEPROM.write(addrOffset + len,'\0');   //Add termination null character
  EEPROM.commit();
 //-- delay(500);
}

/*
 * Read the wifi name in EEPROM
 * addrOffset = 0 for wifi ssid
 * addrOffset = 100 for wifi password
 * use *by reference to return the value
 * see - https://roboticsbackend.com/arduino-write-string-in-eeprom/#Write_the_String
*/
void readString_FromEEPROM(int addrOffset, String *strToRead){
  char data[100]; //Max 100 Bytes
  int i=0;
  unsigned char k;
  //k = EEPROM.read(addrOffset);
  
  while(k != '\0' && i < 100)   //Read until null character
  {
    k = EEPROM.read(addrOffset + i);
    data[i] = k;
    i++;
  }
  
  data[i]='\0';
  *strToRead = String(data);    //return the full string
}

void wpsInitConfig(){
  config.crypto_funcs = &g_wifi_default_wps_crypto_funcs;
  config.wps_type = ESP_WPS_MODE;

  strcpy(config.factory_info.manufacturer, ESP_MANUFACTURER);
  strcpy(config.factory_info.model_number, ESP_MODEL_NUMBER);
  strcpy(config.factory_info.model_name, ESP_MODEL_NAME);
  strcpy(config.factory_info.device_name, ESP_DEVICE_NAME);
}

void WiFiEvent(WiFiEvent_t event, system_event_info_t info){
  switch(event)
  {
    case SYSTEM_EVENT_STA_START:
      break;
    case SYSTEM_EVENT_STA_GOT_IP:
      wps_connected = true;
      break;
    case SYSTEM_EVENT_STA_DISCONNECTED:
      WiFi.reconnect();
      ++counter_restart;
      break;
    case SYSTEM_EVENT_STA_WPS_ER_SUCCESS:
      esp_wifi_wps_disable();
      delay(10);
      WiFi.begin();
      break;
    case SYSTEM_EVENT_STA_WPS_ER_FAILED:
      esp_wifi_wps_disable();
      esp_wifi_wps_enable(&config);
      esp_wifi_wps_start(0);
      wps_connected = false;
      break;
    case SYSTEM_EVENT_STA_WPS_ER_TIMEOUT:
      esp_wifi_wps_disable();
      esp_wifi_wps_enable(&config);
      esp_wifi_wps_start(0);
      wps_connected = false;
      break;
    case SYSTEM_EVENT_STA_WPS_ER_PIN:
      break;
    default:
      break;
  }
}

/*
 * connect via the wps which automatically save the wifi data info in case of a successfull connection
 * if the wps connection does't work if it's either
 *  1.WPS button not pressed
 *  2.timer delay -> empty ssid string
 * 
 * return true if the connection was successsful
 *  
 */
boolean start_WPSPBC(){
  WiFi.onEvent(WiFiEvent);
  WiFi.mode(WIFI_MODE_STA);
  
  wpsInitConfig();    //set up the esp
  esp_wifi_wps_enable(&config);   //ready the system for connection
  esp_wifi_wps_start(0);  //start the wps connection

  int timerStart = millis(); // the time the delay started
  
  while((millis() - timerStart) <= 100000)//allocate a delay of 100s
  {    
    if(wps_connected == true)//meawhile, if the wps connection was performed, continue
    {        
        String dummy = "";
        writeString_ToEEPROM(0, String(WiFi.SSID()));   //write the wifi name
        delay(2000);
        
        writeString_ToEEPROM(100, String(WiFi.psk()));  //write the wifi password
        delay(2000);
        return true;
     }
  }
  return false;
}

/*
 * Connect to saved wifi 
 * Restart the esp32 if not able to connect to the wifi
 * Will go over the start_WPSBC aagain
 */
boolean start_DefaultWIFI()
{
    String wifi_ssid = "";
    String wifi_password = "";
    readString_FromEEPROM(0, &wifi_ssid);
    readString_FromEEPROM(100, &wifi_password);
    
    esp_wifi_wps_disable();
    WiFi.mode(WIFI_STA);  //esp32 direct wifi connection mode
    WiFi.begin(wifi_ssid.c_str(), wifi_password.c_str()); // start the wifi connection
    
    int counter = 0;
    int timerStart = millis();    //starting time of the clock
   //restart the esp32 if not connected after 10 iteration (5seconds)
    while (WiFi.status() != WL_CONNECTED)
    {
      if(counter == 9)
      {
        //ESP.restart();    //restart
        WiFi.reconnect(); //8second passed, reconnect the wifi
        counter = -1;  //since if we put it to 0, it will only loop 8 times        
      }
      
      delay(500);
      ++counter;
  
      if(counter_restart == 10) //if there was an attempt to case SYSTEM_EVENT_STA_DISCONNECTED: for a total of 10times... couldn't connect to internet
      {
        counter_restart = 0;  //reset the wifi.restart() counter
        return false;   //exit the funtion
      }
  
      if((millis() - timerStart) >= 100000)        //allocate a delay of 100s   to connect to internet 
      {
        return false; //exit the funtion
      }
      
    }   
    return true;
}


/*
 * Buzzzz Time
 * Ring for 3s to define if the internet connection was unsuccessful
 */
void alarm(){
  //tone(buzzer, 500, 500); //the buzzer emit sound at 400 MHz for 500 millis
  //tone(buzzer, 650, 500); //the buzzer emit sound at 650 MHz for 500 millis
  //delay(500); //wait 500 millis
}

/*
 * Clear the EEPROM addresses
 * Enter a 0 to adresses and skip if there is already one.
 * see - https://www.norwegiancreations.com/2017/02/using-eeprom-to-store-data-on-the-arduino/
*/
void clear_EEPROM(){

  for (int i = 0 ; i < 250 ; i++)
  {
    if(EEPROM.read(i) != 0) //skip already "empty" addresses
    {                   
      EEPROM.write(i, 0);    //write 0 to address i
    }
  }
  EEPROM.commit();
}

/*
 * Define if a wifi name is already in the EEPROM Memory
 * true - EEPROM.read(0) > 0 -> name exist
 * false -  EEPROM.read(0) <= 0 -> name does not exist
 */
boolean wifi_exist_in_EERPOM(){
    
  String dummy = "";
  readString_FromEEPROM(0,&dummy);  
  delay(500);
  if(dummy.length() > 0)    //if the SSID exists, then return true
  {
    return true;    //there is a wifi SSID in the eeprom
  }
  else
  {
    return false;   //the eeprom is empry...
  }
}

/*
 * Loop until the serial document from uno was received
 */
void loop_until_serialisation(){
  boolean keeplooping = true;
  while(keeplooping) // loop until a decument from uno is received
  { 
    if(Serial.available())
    {
      keeplooping = false;
      return;   //exit the loop as a value is inputed on the serial port
    }
  }
}

/*
 * Sent request command to uno 
 */
void send_command_doc(String str_type, String cmd){

  if(str_type != "button")
  {
     doc["button"] = "none";
  }
  
  if(str_type != "type")
  {
    doc["type"] = "none";
  }
  
  if(str_type != "error_ring")
  {
    doc["error_ring"] = "none";
  }
  
  if(str_type != "state")
  {
    doc["state"] = "none";
  }

  doc[str_type] = cmd;      // Define the type of command to send to uno
  serializeJson(doc, Serial);    // send the command via serial port
  delay(50); //small delay to send the command
}

  //  Retrieve the firebase device status
  //    1. if OFF, then deactivate the sensors  -> false
  //        --- no need to check for the ring state if the power is off, as it will done set to off by default
  //    2. if ON, then activate the sensors -> true
  //        -- check for the ring state 
  //    see - https://arduino.stackexchange.com/questions/18434/is-there-any-way-i-can-turn-off-the-5v-pin
boolean device_on(){
  String power = "";
  //retrive the power sare of the device in arduino
  if (Firebase.getString(firebaseData, "user/" + UserID + "/Devices/" + device_newName + "/Power"))  // get the Power string from arduino
  {
    if (firebaseData.dataType() == "string") // confirm the data type
    {
      power = firebaseData.stringData();  //one can switch the device off or on as they wish via the app
      if(power == "OFF")
      {
        send_command_doc("type","turn_off");
        //--delay(8000);
        return false;
      }
      else
      {
        send_command_doc("type","turn_on");
        //--delay(100);
        return true;
      }  
    }
  } 
  else 
  {
    //do nothing
  }
 return false;    //either somehing if wrong with the device state in firebase, or the device is off
}

/*
 * Send the sensors's state to firebase
 *  1. Retrurn true if there is a smoke or a fire detected
 *  2. otherwise, return false
 */
boolean send_sensors_State_To_Firebase(){
  String message = "";    //get eh error message from the deserialization
  send_command_doc("type","request");  //send a request to uno get the sensor's state
  delay(2000);  //wait forr 2
    
    // Reading the response
    boolean messageReady = false;
    while(messageReady == false) { // blocking but that's ok
      if(Serial.available()){
        messageReady = true;   
        message = Serial.readString(); 
     }
    }
      //Serial.println("The message is...");
     // delay(2000);
      //Serial.println(message);
      //delay(2000);
      
      // Attempt to deserialize the JSON-formatted message
      DeserializationError error = deserializeJson(doc,message);
      if(error) 
      {
        Serial.print(F("deserializeJson() failed: "));
         delay(2000);
         
        Serial.println(error.c_str());
         delay(2000);
      }
            
      if(doc["type"] == "result")
      { 
        //determine the sensors status
        if(doc["state"] == "ON")
        {
          Firebase.setString(firebaseData, "user/" + UserID + "/Devices/" + device_newName + "/Status", "ON");
          delay(500);
          return true;
        }
        else
        {
          Firebase.setString(firebaseData, "user/" + UserID + "/Devices/" + device_newName + "/Status", "OFF");
          delay(500);
          Firebase.setInt(firebaseData, "user/" + UserID + "/Devices/" + device_newName + "/ring_state", 0);   //Reset the command
          delay(500);
          return false; //No smoke or fire were detected.
        }
      }
    delay(1000);
  return false;
}

/*
 * Return the device (edited) used under the user
 * normally, should return a value since the user is connected to it, to be able to run this aprt of the code
 */
String getDeviceNewName(){
  String dummyName = "";
  if (Firebase.getString(firebaseData, "/Devices/" + DEVICE_ID + "/EditedName"))  // get the Power string from arduino
  {
    if (firebaseData.dataType() == "string") // confirm the data type
    {
      dummyName = firebaseData.stringData();  //one can switch the device off or on as they wish via the app
      if(dummyName != "NEW")
      {
       return dummyName;
      }
    }
  } 
  else 
  {
  }
 return "NEW";    // No user attached to the device
}

/*
 * Is a user connected to the device or not
 *  1. if yes, then work normaly
 *  2. if no, turn off the device
 */
boolean user_connected_to_device()
{
  String user_ = "";
  //get the user ID
  if (Firebase.getString(firebaseData, "Devices/" + DEVICE_ID + "/UserID"))  // get the Power string from arduino
  {
    if (firebaseData.dataType() == "string") // confirm the data type
    {
      user_ = firebaseData.stringData();  //one can switch the device off or on as they wish via the app
      if(user_ != "NEW")
      {
        UserID = user_;   //retrieve the userid connected to the device
        device_newName = getDeviceNewName();    //retrieve the device name!

        if(device_newName != "NEW")   //get the right user name.(There might be a delay in "Android to firebase"
        {
          return true;
        }
      }
      return false;
    }
  } 
  else 
  {
    return false;
  }
}

/*
 * define if the user set the device "OFF" via android
 *  1.  "1" if the user turned the ringing "off" from the app -> return true
 *          - "enter 0 everytime you pass data (if applicable)
 *  2. return false if the user didn't turn if off via the application
 */
boolean software_Ring_State(){
  int ring_State = 0;
  if (Firebase.getInt(firebaseData, "user/" + UserID + "/Devices/" + device_newName + "/ring_state"))  // get the Power string from arduino
  { 
    if (firebaseData.dataType() == "int") // confirm the data type
    {
      ring_State = firebaseData.intData();  //one can switch the device off or on as they wish via the app
      if(ring_State == 1)    //THE user turn offf the ringing via the app
      {
        Firebase.setInt(firebaseData, "user/" + UserID + "/Devices/" + device_newName + "/ring_state", 0);   //acknowledge the command sent from firebase
        return true;      //the device is connected to a user
      }
    }
  } 
  else 
  {
    return false;
  }
 return false;    // somehing if wrong with the user id in firebase
}

/*
 * determine if the device is still connected to internet
 *    1. if not the case, try to connect with the internal wifi settings on eeprom
 *    2. in case of failure, gg my friend
 */
boolean device_connected_to_internet()
{
  delay(1000);
  
  //first, always define if you are still conencted to the wifi
  if(wifi_exist_in_EERPOM())    //if not connected...try to set it up!
  {
     //Serial.println("exist");
     delay(1000);
    //  there should already be an wifi in eeprom cause of the wps... 
    //  but since I already implemented the wifi_exist_in_EERPOM function, lets use it lol
    //  if there is a value in EEPROM, then it's possible to perform a direct wifi connection
    //    1. connect directly to wifi
    //    2. do your thing in firebase
    if(WiFi.status()  != WL_CONNECTED)      
    {
      if(start_DefaultWIFI())   //two rings for a "wifi connected!" true alarm.
      {
        Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);    // connect to firebase
        return true;
      }
      return false;  //was not able to connect via direct link
    }
    else
    {
      Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);    // connect to firebase
      return true;
    }
  }
  else
  {
    // Serial.println("does not exist");
     delay(100);
    if(do_the_initial_settting())    //one must connect via wps!!!!
    {
    return true;
    }
  }
  return false;
}


/*
 * Delay the uno for 10s (turn off the device power output
 */
void quick_sleep_until_next_phase(){
    //send_command_doc("type","turn_off");
   //-- delay(8000); //delay the program for 10s 
}

/*
 * Wait until the user press the wps button (right one)
 * should press the button for at least 5s
 */
boolean wait_for_wpsbtn_pressed()
{
  boolean keepLooking = true;
  int buttonState = 0;         // current state of the button, HIGH as we have active low with INPUT_PULLUP
  int lastButtonState = 0;     // previous state of the button
  int lastTime = 0;   //sleep timer comparaison point

/*
        String wifi_ssid = "";
    String wifi_password = "";
    readString_FromEEPROM(0, &wifi_ssid);
    readString_FromEEPROM(100, &wifi_password);


    Serial.println("ssid: " + wifi_ssid);
    delay(2000);
    Serial.println("pass: " + wifi_password);
    delay(2000);

    Serial.println("button state =  " + String(digitalRead(reset_button)));
    delay(2000);
    */
  while(keepLooking) // loop until a document from uno is received
  {  
    //put uno in sleep while it's waiting for the user to press the reset button on the esp (10s)
    if(millis() - lastTime >= 5000) {
      lastTime = millis();
      send_command_doc("type", "quick_ring");   //make a ring, then delay for 5s, so teh user knows you are waiting for him
      serializeJson(doc,Serial);    // send the command via serial port
      delay(2000);    //delay two second for uno to perform the action
    }
    
     buttonState = digitalRead(reset_button);
    // compare the buttonState to its previous state
    if (buttonState != lastButtonState) {
  
      if (buttonState == LOW) {  // if the current state is pressed as we use INPUT_PULLUP
        // went from HIgh to Low to on:
      }else {
        
        // if the current state is HIGH then the button
        // wend from on to off:

        //loop until the user release the button
        while(digitalRead(reset_button) == HIGH)
        {
          //do nothing
        }
    
        return true;
      }
      // Delay a little bit to avoid bouncing
      //delay(50);
    }
    else
    {
      keepLooking = true;
    }
    // save the current state as the last state,
    //for next time through the loop
    lastButtonState = buttonState;
  }
}

/*
 * Wait for the user to press the wps button
 */
boolean do_the_initial_settting(){
  
  boolean keepLooping = true;
  String message = "";
  int lastTime_timer = millis();

  delay(100);
  wait_for_wpsbtn_pressed();
  delay(100);
  
  //button was pressed, perform a long ring to advise the user
   send_command_doc("error_ring", "two_rings");
   serializeJson(doc,Serial);    // send the command via serial port
   delay(2000);    //wait for uno

  //one must perform a wps connetion
  //    1. wait for wps button pressed in serial.port
  //    2. upon press, perform the connection
  //    3. do your thing in firebase  
  //    4. if no connection applied.....make a long ring
  //    5. on a connection, make 2 ring with a 500ms delay
  if(start_WPSPBC()) //CONNECT TO WPS - 
  {
    Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);    // connect to firebase
    send_command_doc("error_ring","two_rings");
    serializeJson(doc,Serial);    // send the command via serial port
    delay(2000);    //wait for uno
    return true;
  }
  else
  {
   send_command_doc("error_ring", "one_ring");
    serializeJson(doc,Serial);    // send the command via serial port
    delay(2000);    //wait for uno
    ESP.restart();
    return false;
  }
}

//is the user trying to reset the esp32?
void resetButtonPressed()
{
  /*
  boolean btn_pressed = false;
  int timer_initial = 0;
  int timer_final = 0;
  if(digitalRead(reset_button) == HIGH)
  {
     timer_initial = millis();  //initial time
     btn_pressed = true;
  }
 
  while(btn_pressed) // loop until a decument from uno is received
  { 
    if(digitalRead(reset_button) == HIGH)    // if the button is pressed
    {
      btn_pressed = true;
    }
    else
    {
      if((millis() - timer_initial) >= 10000)     //if the user have been pressing the button for 10s... restart both devices
       {
        btn_pressed = false;
        clear_EEPROM();
        ESP.restart();
        return;   //user complete the verification, return
       }
       btn_pressed = false;
    }
  } */

   boolean keepLooking = true;
  int buttonState = 0;         // current state of the button, HIGH as we have active low with INPUT_PULLUP
  int lastButtonState = 0;     // previous state of the button
  int lastTime = 0;   //sleep timer comparaison point
     //Serial.println("button: " + String(digitalRead(reset_button)));
     delay(1000);
  while(keepLooking) // loop until a document from uno is received
  {  
     //Serial.println("looping");
     //delay(1000);
     
     buttonState = digitalRead(reset_button);
    // compare the buttonState to its previous state
    if (buttonState != lastButtonState) {
  
      if (buttonState == LOW) {  // if the current state is pressed as we use INPUT_PULLUP
        // went from HIgh to Low to on:
      }
      else // if the user pressed the button
      {
        lastTime = millis();    //starting time
        
        // if the current state is HIGH then the button
        // went from on to off:
        //loop until the user release the button
        while(digitalRead(reset_button) == HIGH)
        {
          //put uno in sleep while it's waiting for the user to press the reset button on the esp (10s)
          if(millis() - lastTime >= 5000) {
            send_command_doc("error_ring", "two_rings");   //make a ring, then delay for 5s, so teh user knows you are waiting for him
            serializeJson(doc,Serial);    // send the command via serial port
            delay(2000);    //delay two second for uno to perform the action
            clear_EEPROM();
            //delay(1000);   //just a lil delay to be sure the eeprom is cleared before it restart
            ESP.restart();
            return;   //user complete the verification, return
          }
        }
        return ;
      }
      // Delay a little bit to avoid bouncing
      //delay(50);
    }
    else
    {
      keepLooking = false;
    }
     //delay(1000);
     
    // save the current state as the last state,
    //for next time through the loop
    lastButtonState = buttonState;
  }
}

/*
 * Wifi and Firebase connection setup
 */
void setup() {
  Serial.begin(4800);  
  delay(10);
  pinMode(RX,INPUT);    //INPUT FOR SERIAL COMMUNICATION
  pinMode(TX,OUTPUT);   //OUTPUT FOR SERIAL COMMUNICATION
  Serial.setTimeout(1000); //set 1 sec to read the value
  EEPROM.begin(EEPROM_SIZE);    // internal memory must be initialized, with how many addresses(locations) you want to use
  delay(10);
  //writeString_ToEEPROM(0, "HypeHome");
  //writeString_ToEEPROM(100, "Yamamoto");
  //clear_EEPROM();             // as the name says   ----------------------------  don't forget to remove the actually connected wifi link 
  WiFi.disconnect(true);
  pinMode(reset_button, INPUT_PULLDOWN);    //INPUT_PULLUP since it's connected to a resistor to 5v (left one)
  delay(2000);    //delay so that uno is available before.  
}

/*
 * Determine if the device is in forced off state,
 *  1. if yes - send the "do noting" command to the arduino
 *  2.if nop - pass the sensor state to firebase
 */
void loop()
{ 
/*
    int i = 10;
    send_command_doc("type","sleep_mode"); //send a command to uno to shut the system for at least 5min.
    delay(10000);
*/

  boolean detected_something = true;
  //is the user trying to reset the esp32?
  resetButtonPressed();

  // is the device still connected to internet?
  //gotta see if the user pressed the "reset button" at some point
  if(device_connected_to_internet())
  {   
    //  Is the device assigned to a user?
    if(user_connected_to_device())
    {
      delay(1000);

      Firebase.setInt(firebaseData, "Devices/" + DEVICE_ID + "/Time", 1);   //device is working, tell the user!!!! AYYYEEEE
        delay(2000);
     //* 
      //  Retrieve the firebase device status
      //    1. if OFF, then desactivate the sensors
      //    2. if ON, then activate the sensors and send the sensor value
      //    see - https://arduino.stackexchange.com/questions/18434/is-there-any-way-i-can-turn-off-the-5v-pin
      if(device_on())
      {
        delay(2000);
      
        //send the sensor state to firebase - true if there is a smoke or a fire
        //keep looping until the user either turn the ring off, or press the button (in case of a fire)
        while(detected_something) 
        {
          
          detected_something = send_sensors_State_To_Firebase();    
          delay(2000);
        
         if(detected_something)   // if something is detected, the user can either force the sleep mode or just wait until teh fire/smoke finish...
         {
            if(software_Ring_State())     //it's ringing and the user turned it off
            {
              Firebase.setString(firebaseData, "user/" + UserID + "/Devices/" + device_newName + "/Status", "OFF");
              send_command_doc("type","sleep_mode"); //send a command to uno to shut the system for at least 5min.
              detected_something = false;
              delay(10000); //sleep for 5min
              break;
            }
            
           
            if(doc["button"] == "alarm_off")      //user want to stop the device from working via the hard press button
            {
                Firebase.setString(firebaseData, "user/" + UserID + "/Devices/" + device_newName + "/Status", "OFF");
                detected_something = false;
                send_command_doc("type","sleep_mode"); //send a command to uno to shut the system for at least 5min.
                detected_something = false;
                delay(10000); //sleep for 5min
                break;
            }
          //delay(1000); //loop every seconds
          }
          else
          {
           quick_sleep_until_next_phase();  //nothing is detected!!
           break;  //exit teh while loop - nothing is detected
          }
          //--delay(5000); //loop every seconds //if nothing is detected, just do a quick sleep 
         }
         
        }
        else    //the no fire/smoke... you are same bro!
        {
          quick_sleep_until_next_phase();
        }     
      
      }
      else  //if device is off, sleep for 10s
      {
        quick_sleep_until_next_phase();
      }
    }
    else
    {
   //   Serial.println("no connected");
     // delay(1000);
      quick_sleep_until_next_phase();
    }
}

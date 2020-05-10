<table style="width:100%">
  <tr>
    <td style="width: 45%;">
      <img src="https://raw.githubusercontent.com/MAFFPT/Hubitat/master/Nuki%20Smart%20Lock%202.0/icons/Hubitat-logo.jpg" alt="Hubitat" width="200"/>
    </td>
    <td style="width: 10%;"><p style="font-size: x-large;">+</p></td>
    <td style="width: 45%;">
      <img src="https://raw.githubusercontent.com/MAFFPT/Hubitat/master/Nuki%20Smart%20Lock%202.0/icons/Nuki-logo.png" alt="Nuki" width="200"/>
    </td>
  </tr>
</table>

# Nuki Smart Lock 2.0<small><sup>&copy;</sup></small> Integration for Hubitat Elevation<small><sup>&copy;</sup></small>


## Introduction

These app and drivers were developed to overcome the lack of native support for the Nuki Smart Lock 2.0<sup>&copy;</sup> lock (https://nuki.io/en/) in the Hubitat Elevation<sup>&copy;</sup> (HE) (https://hubitat.com) home automation (domotics) system.

It is used an “Integration” app that shields the user from the cumbersome task of configuring all the devices – all the user has to do is to download the app and all drivers to his/hers Hubitat Elevation hub, run the app and *voilá* – the magic happens!

Well, that’s what at least I hope will happen …

To keep this documentation short and its maintenance at a minimum level, the "on app" documentation is prioritized. Here you will not find **screenshots**. Not that I do not think they are important - I believe they are. However, as I said, I have prioritized providing the instructions on the app itself. This way I believe I can react to problems and/or user´s needs faster.


## Organization

The app/drivers are stored in two subfolders of the **Apps & drivers** folder: '**production**' and '**debug**', with slightly but important differences between them.

Let's talk about it ...

### 'debug' subfolder

This folder contains the app and drivers with a lot - I mean, a lot ... a **massive** lot! - of debugging code to help **you** help **me** to gather information when a problem is detected and needs my intervention.

The debugging information will be logged - in your Hubitat Elevatiion hub system log - only when you toogle the "debug info logging" option at the main page of the apps/drivers. If you do not activate the "debug info logging" option, no debugging information will be logged whatsoever.

You may use this code as your production code. However, since there is a lot - did I mention a **massive** lot before? - of debugging code imbebbed into the app/drivers code, it not only increases the execution time and may pontentially impacts negatively the performance of your HE hub, but also increases the total code size.

### 'production' subfolder

This folder contains all apps and drivers striped off all the debugging code and even the "debug info logging" option - why keep it if it would be meaningless and produces nothing at all?

As I said before, these versions of app and drivers is meant to decrease possible overhead of the debugging code over your HE hub, even when the "debug logging info" option is not enabled. I know that probably the overhead is minimum, but, if you add a lot of minimuns, it would not be a "minimum" anymore ...

But I will tell you one more reason to have a production and debug versions: the debug version code is ugly ... and I like my code to look nice from inside out. My therapist says that I should keep doing this as an occupational therapy to help me not to be sent back to the mental institution ... 🤪

### Wich app & drivers versions should I use?

I *strongly* suggest you to use the production versions and install the debugging versions only when a problem is detected.

However, it is your choice wich one you want to use.

## Supported devices

So far, the following Nuki products are supported by the app/drivers:

* Nuki bridge (firmware version ??? and above)
* Nuki Smart Lock 2.0 (firmware version ??? and above)

#### To be supported soon ... stay tuned!

* Nuki Keypad
* Nuki Openner

## Installation

### Install app & drivers

First of all you'll need to install the following app and drivers in your HE hub.

**Suggestion:** Open these links in a new browser tab so it will be easier for you to follow the instructions.

* **App**

  * Nuki Smart Lock 2.0 Integration app
  
     * [see app's code raw content - DEBUG version](https://raw.githubusercontent.com/MAFFPT/Hubitat/master/Nuki%20Smart%20Lock%202.0/Apps%20%26%20drivers/debug/apps/Nuki%20Smart%20Lock%202.0%20Integration)
     * Note: PRODUCTION version not available yet
  
  Follow these Hubitat´s instructions to install the app
  
   * [Hubitat: How to install custom apps](https://docs.hubitat.com/index.php?title=How_to_Install_Custom_Apps)
  
* **Drivers**

  * Nuki Bridge driver
    * [see driver's code raw content - DEBUG version](https://raw.githubusercontent.com/MAFFPT/Hubitat/master/Nuki%20Smart%20Lock%202.0/Apps%20%26%20drivers/debug/drivers/Nuki%20Bridge)
     * Note: PRODUCTION version not available yet
     
  * Nuki Smart Lock driver
     * [see driver's code raw content - DEBUG version](https://raw.githubusercontent.com/MAFFPT/Hubitat/master/Nuki%20Smart%20Lock%202.0/Apps%20%26%20drivers/debug/drivers/Nuki%20Smart%20Lock%202.0)
     * Note: PRODUCTION version not available yet
    
  Follow these instructions to install the drivers
  
   * [Hubitat: How to install custom drivers](https://docs.hubitat.com/index.php?title=How_to_Install_Custom_Drivers)   

### Integration app operation

Once you have installed the app and pertinent drivers, the installation of your Nuki brige(s) and paired devices to it is straightforward: just open the app and follow the steps and, very important, _read carefully_ all the information contained on each page presented to you _before_ requesting any action to the app - meaning, do not be a "fast clicker"! However, the app is - at least I hope it is ... - carefully crafted to alert you of any potential damaging operation **before** you act. So, _please read the all the information on each and every page before acting_ !!!

As much as I prioritized the "on app" documentation, I would like to discuss about some app/drivers features an general information about them.

### Nuki bridge(s) and paired device(s) discovery and installation

The Integration app is able to install in a single run 'n' bridges and respective paired device(s). It is not necessary to run it multiple times for each Nuki bridge that you eventually have.

After the Integration app discovers all Nuki bridges installed on your network, it provides to you the possibility to select which one(s) you want to install/reinstall.

### Installation notes
 
- The Integration app is able to discover/detect **only** the Nuki bridges that are installed on the same network as you HE hub where you run the Integration app
- It is not possible to discover bridges intalled on different networks
- If you have different neworks for you household and/or home automation equipments, you will need to reconfigure your setup accordingly
- This is not an app limitation: it is Nuki´s behavior, since it is necessary contact first Nuki´s servers to discover the Nuki bridges installed on your network
- Since it is necessary to contact Nuki´s servers to do the bridges discovery, an Internet connection must be active to properly run the Integration app - no surprise here, right?
- The app automatically installs all bridge´s paired devices for a given bridge
- When requested to install an already installed bridge (reinstall), that bridge and all its paired devices will be automatically deleted
- **IMPORTANT:** When a bridge is deleted (and by consequence its paired devices), all Rule Machines (RM) rules that reference for the deleted devices are affected, resting useless; this happens because HE uses an internal device id instead of their names and this is a good practice to do so


## Future developments

My signus is Aquarius - the guys that like horoscope use to say that people from Aquarius is always thinking about the future. I do not know it is true, but in my case it is ...

So, here are some enhancements that I plan to implement in app & drivers future versions.

### Version 1.1

- Integrate app/drivers to Alexa

**NOTE**: No plan to support Google devices altogether: after a lot of frustating contacts with Google regarding support for various device configurations, I simply gave up.

### Version 1.2

- Nuki Key Fob and Nuki Keypad support
- If it is possible to detect wich Key Fob and/or Keypad has been used to unlock the dorr, generate an HE event accondingly

### Version 2.0

- Implement the option of only install new devices connected to a Nuki bridge, not touching the bridge itself and others paired devices

### Version 3.0

- Detection of all Nuki bridges without contacting Nuki's web servers (Internet free version)

## Licence

Check this [license]() for License information.

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

# Nuki Smart Lock 2.0<sup>&copy;</sup> Integration for Hubitat Elevation<sup>&copy;</sup>


## Introduction

These app and drivers were developed to overcome the lack of native support for the Nuki Smart Lock 2.0<sup>&copy;</sup> lock (https://nuki.io/en/) in the Hubitat Elevation<sup>&copy;</sup> (HE) home automation (domotics) system.

It is used an “Integration” app that shields the user from the cumbersome task of configuring all the devices – all the user has to do is to download the app and all drivers to his/hers Hubitat Elevation hub, run the app and **voilá** – the magic happens!

Well, that’s what at least I hope will happen …

To keep this documentation short and its maintenance at a minimum level, the "on app" documentation is prioritized. Here you will not find *screenshots*. Not that I do not think they are important - I believe they are. However, as I said, I have prioritized providing the instructions on the app itself. This way I believe I can react to problems and/or user´s needs faster.


## Organization

The material (app/drivers) is stored in two subfolders of **Apps & drivers** folder: '**production**' and '**debug**', with slightly but important differences between them.

Let's see them ...

### 'debug' folder

This folder contains all apps and drivers with a lot - I mean, a lot, a **massive** lot - of debugging code to help **you** help **me** to gather information when a problem is detected.

The debugging information will be logged - in your Hubitat Elevatiion hub system log - only when you toogle the "debug info logging" option at the main page of the apps/drivers. If you do not activate the "debug info logging" option, no debugging information will be logged whatsoever.

You may use this code as your production code. However, since there is a lot - did I mention a **massive** lot before? - of debugging code imbebbed into the apps/drivers code, it not only increases the execution time and may impact negatively the performance of your HE hub, but also increases the total code size.

### 'production' folder

This folder contains all apps and drivers striped off all the debugging code and even the "debug info logging" option - why keep it if it would be meaningless and produces nothing at all?

As I said before, this version of apps and drivers is meant to decrease possible overhead of the debugging code over your HE hub, even when the "debug info" is not enabled. I know that the overhead is minimum, but, if you add a lot of minimuns, it would not be a "minimum" anymore ...

But I will tell you one more reason to have a production and debug versions: the debug version code is ugly ... and I like my code to look nice from inside out. My therapist says that I should keep doing this as an occupational therapy to help me not to be sent back to the mental institution ... 🤪

### Wich app & drivers versions should I use?

I *strongly* suggest you to use the production versions and install the debugging versions only when a problem is detected.

However, it is your choice wich one you want to use.


## Installation

First of all you'll need to install the following app and drivers to your HE hub.

**Suggestion:** Open these links in a new browser tab so it will be easier for you to follow the instructions

* App

  * Nuki Smart Lock 2.0 Integration app
  
     * [see app's code raw content - DEBUG version](https://raw.githubusercontent.com/MAFFPT/Hubitat/master/Nuki%20Smart%20Lock%202.0/Apps%20%26%20drivers/debug/apps/Nuki%20Smart%20Lock%202.0%20Integration)
     * Note: PRODUCTION version not available yet
  
  Follow these instructions to install the app:
  
   * [Hubitat: How to install custom apps](https://docs.hubitat.com/index.php?title=How_to_Install_Custom_Apps)
  
  
* Drivers

  * Nuki Bridge driver
    * [see driver's code raw content - DEBUG version](https://raw.githubusercontent.com/MAFFPT/Hubitat/master/Nuki%20Smart%20Lock%202.0/Apps%20%26%20drivers/debug/drivers/Nuki%20Bridge)
     * Note: PRODUCTION version not available yet
     
  * Nuki Smart Lock driver
     * [see driver's code raw content - DEBUG version](https://raw.githubusercontent.com/MAFFPT/Hubitat/master/Nuki%20Smart%20Lock%202.0/Apps%20%26%20drivers/debug/drivers/Nuki%20Smart%20Lock%202.0)
     * Note: PRODUCTION version not available yet
    
  Follow these instructions to install the drivers:
  
   * [Hubitat: How to install custom drivers](https://docs.hubitat.com/index.php?title=How_to_Install_Custom_Drivers)   


## Supported devices

So far, the following Nuki products are supported by the app/drivers:

* Nuki bridge (firmware version ??? and above)
* Nuki Smart Lock 2.0 (firmware version ??? and above)

### To be supported soon ... stay tuned!

* Nuki Keypad
* Nuki Openner


## Operation

Once you have installed the app and pertinent drivers, the installation of your Nuki brige(s) and paired devices is straightforward: just open the app and follow the steps and, very important, _read carefully_ all the information contained on each page presented before requesting any action to the app - meaning, do not be a "fast clicker"! However, the app is - at least I hope it is ... - carefully crafted to alert you of any potential damaging operation *before* you act. So, _please read the all the information on each and every page before acting_ !!!






## Licence

Check this [license]() for License information.


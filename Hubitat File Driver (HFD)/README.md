# Hubitat<small><sup>&copy;</sup></small> File Driver (HFD)

# Introduction

This driver was developed to allow other developers to read/write/append files at the brand new (BETA) and powerfull local storage in the Hubitat<small><sup>&copy;</sup></small> Elevation hub.

It is intended to be a "provisory" solution, while Hubitat moves ahead with the BETA and awards us with a full API to access files from apps and drivers.

**NOTE:** This solution itself is a BETA version - users input is expected and very welcome!


# HFD - The Good, the Bad and the Ugly

## Components

This solution uses two components:

- A custom driver [Hubitat File Driver (HFD)](https://github.com/MAFFPT/Hubitat/blob/master/Hubitat%20File%20Driver%20(HFD)/driver/Hubitat%20File%20Driver%20(HFD).groovy)

- A model Rule Machine rule [HFD base rulez9(https://github.com/MAFFPT/Hubitat/tree/master/Hubitat%20File%20Driver%20(HFD)/rule)

## Installation

- **The custom driver above must be used to create a Virtual Device (VD), specific to access a particular file**

  ### What? A Virtual Device for EACH file that I want to access?

  This is **"The Ugly"** part: I am afraid that it is the way it will work ... at least for now.

  I am aware that creating a VD for each file may be cumbersome. However, so far, it is the only way I have found to implement this solution. The problem that caused this is the fact that I could not find a way of referencing a file name as a variable, since a file reference is implemented as variable itself.
  
  And there is more ...
  
  **The Ugly"** goes on: this device must be created as a **child device** of the user app or driver, otherwise it will not be possible to access it.

- **A Rule Machine rule - and guess what? Also specific for each file**

  **The Bad** part ...
  
  And, please, do not shoot me !
  
  The user needs to create a rule for each file he/she wants to access, using the model Rule Machine provided.
  
  When creating the rule the user needs to replace the string "TestFile5" found all around the model rule for the name of the file that will be handled.
  
- **Suggested naming convention for drivers and rules**

  First of all, I must stress that this is only a suggested naming convention. The user, obviously, can use the naming convention that best suit his/hers needs or preferences.
  
  - Driver: **File Driver [** *filename* **]**
  
    Example: for the file **myAppLogFile**, the suggested driver name would be **File Driver [myAppLogFile]**
    
  - Rule: **File Driver [** *filename* **]**
  
    Example: for the same file, the suggested driver name would be **File Driver [myAppLogFile]**
  
## Using HFD

Finally **The Good** part!

- Reading from a file

  Call the **read** method of the specific VD created for the file
  
  - Example: *myAppLogFileDevice*.read ()
  
    Being 
     
     - ***myAppLogFileDevice*** the object that references the VD you have created 
  
  Then, read the **fileContents** attribute of the device
  
  - Example: *myAppLogFileDevice*.currentValue ("fileContents", true)
  
    Being 
    
     - ***myAppLogFileDevice*** the object that references the VD you have created
     - **"fileContents"** the attribute where you will find the contents of the file
     - **true** argument is to force the reading of the attribute skipping the cache, reading the last information from the database
     
   **NOTE** 
   
   It has been observed some delay between the execution of the **read** command and the availability of the file contents at the "fileContents" attribute.
   
   To avoid data inconsistency, it has been added a delay (pause) at the end of the execution of the **read** command to give time to the HE to update the attribute. This delay, expressed in miliseconds, for now, is stored in a internal driver variable:
   
    >
    > @Field static _afterCommandDelay = 250
    >


After creating the

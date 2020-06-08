# Hubitat<small><sup>&copy;</sup></small> File Driver (HFD)

## Introduction

This solution was developed to allow you to read/write/append files at the brand new (BETA) and powerfull local storage in the Hubitat<small><sup>&copy;</sup></small> Elevation hub.

This solution intended to be a "provisory" one, while Hubitat moves ahead with the BETA and awards us with a full API to access files from apps and drivers.

**NOTE:** This solution itself is a BETA version - users input is expected and very welcome!


## HFD - The Good, the Bad and the Ugly

### Components

This solution has two components:

- A custom driver [Hubitat File Driver (HFD)](https://github.com/MAFFPT/Hubitat/blob/master/Hubitat%20File%20Driver%20(HFD)/driver/Hubitat%20File%20Driver%20(HFD).groovy)

- A model Rule Machine rule [HFD model rule](https://github.com/MAFFPT/Hubitat/tree/master/Hubitat%20File%20Driver%20(HFD)/rule)

### Installation

- **The custom driver above must be used to create a Virtual Device (VD), specific to access a particular file**

  ### What? A Virtual Device for EACH file that I want to access?

  This is **"The Ugly"** part: I am afraid that it is the way it will work ... at least for now.

  I am aware that creating a VD for each file may be cumbersome. However, so far, it is the only way I have found to implement this solution. The problem that caused this is the fact that I could not find a way of referencing a file name as a variable, since a file reference is implemented as variable itself.
  
  And there is more ...
  
  **The Ugly"** goes on: this device must be created as a **child device** of the user app or driver, otherwise it will not be possible to use it.

- **A Rule Machine rule**

  **The Bad** part ... also specific for each file
  
  And, please, do not shoot me !
  
  You need to create a rule for each file you want to access, using the model Rule Machine provided.
  
  When creating the rule you need to replace the string **"TestFile5"** found all around the model rule for the name of the file that will be handled.
  
- **Suggested naming convention for drivers and rules**

  First of all, I must stress that this is only a suggested naming convention. You, obviously, can use the naming convention that best suits ypur needs or preferences.
  
  - Driver: **File Driver [** *filename* **]**
  
    Example: for the file **myAppLogFile**, the suggested driver name would be **File Driver [myAppLogFile]**
    
  - Rule: **File Driver [** *filename* **]**
  
    Example: for the same file, the suggested driver name would be **File Driver [myAppLogFile]**
  
## Using HFD

Finally **The Good** part!

### Reading from a file

Call the **read** method of the specific VD created for the file
  
>
> *myAppLogFileDevice*.read ()
>
  
  where 
     
  - ***myAppLogFileDevice*** is the object that references the VD you have created 
  
Then, read the **fileContents** attribute of the device
  
>
> *myStringVariable* = *myAppLogFileDevice*.currentValue ("fileContents", true)
>
  
  where 
    
   - ***myStringVariable*** is the String variable to receive the contents of the fil
   - ***myAppLogFileDevice*** is the object that references the VD you have created
   - **"fileContents"** is the attribute where you will find the contents of the file
   - **true** argument is to force the reading of the attribute skipping the cache, reading the last information from the database
     
**NOTE** 
   
 It has been observed some delay between the execution of the **read** command and the availability of the file contents at the **"fileContents"** attribute.
   
 To avoid data inconsistency, it has been added a delay (pause) at the end of the execution of the **read** command to give time to the HE to update the attribute. This delay, expressed in miliseconds, is stored, for now, in a internal driver constant:
   
 >
 > @Field static _afterCommandDelay = 250
 >
    
 If this value is deemed sufficient, it will stay that way. However, in case if you need to increase this value to fit your particular HE hub enviroment, please report it to me. If necessary, I can create a driver's preference variable to store that value and made easier to change it.

### Writing to a file

Call the **write** method of the specific VD created for the file
  
>
> *myAppLogFileDevice*.write (*content to be written*)
>
  
  where 
     
  - ***myAppLogFileDevice*** is the object that references the VD you have created
  - ***content to be written*** is - guess what ... - is the string to be written to the file! It can be a literal string, a variable, etc.
  
**NOTE**

  When ask for a write operation, two things can happen:
  
  - If the file does not exist yet, it will be created and the **"content to be written"** will be stored in it
  - If the file already exists, it will be **overwritten**, and the ***content to be written*** will be stored in it
  
  So, be careful when requesting a **write** operation!

### Appending to a file

Call the **append** method of the specific VD created for the file
  
>
> *myAppLogFileDevice*.append (*content to be appended*)
>
  
  where 
     
  - ***myAppLogFileDevice*** is the object that references the VD you have created
  - ***content to be appended*** is the string to be appended to the end of the file

## Final words

I hope that everything works as expected ... 


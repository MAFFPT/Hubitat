/*
    Copyright 2020 Marco Felicio (maffpt@gmail.com)

    Licensed under the Creative Commons (CC) Attribution-NonCommercial 4.0 International (CC BY-NC 4.0) - the "License";
    you may not use this file except in compliance with the License.
    
    You may obtain a copy of the License at

        https://creativecommons.org/licenses/by-nc-sa/4.0/

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    
    See the License for the specific language governing permissions and
    limitations under the License.
*/

import groovy.transform.Field

@Field static _nukiNamespace = "maffpt.nuki"             // All apps and drivers must be at the same namespace
@Field static _nukiOpenerDriverVersion = "0.5.3"         // Current version of this driver

@Field static Map _openerDeviceModes = [2: "Door mode", 3: "Continuous mode"]

@Field static _nukiDriverNameOpener = "Nuki Opener"      // Nuki Smart Lock 2.0's device driver name

@Field static String _nukiDeviceTypeOpener = "2"

metadata 
{
    definition (name: "Nuki Opener", namespace: "maffpt.nuki", author: "Marco Felicio") 
    {
        capability "Battery"
        
        capability "DoorControl"
        command "open"
        command "close"
        command "activateCM"
        command "activateRTO"
        command "deactivateCM"
        command "deactivateRTO"
        command "electricStrikeActuation"
        
        //capability "Actuator"
        
        command "status"
    }
/*    
    tiles 
    {
        standardTile ("activate continuous mode", "activatecontinuousmode", inactiveLabel: false, decoration: "flat", width: 3, height: 2) 
        {
            state "default", label:'activate continuous mode', action:"activateContinuousMode", icon: "st.locks.lock.locked"
        }
        
        standardTile ("activate rto", "activaterto", inactiveLabel: false, decoration: "flat", width: 3, height: 2) 
        {
            state "default", label:'activate rto', action:"activateRto", icon: "st.locks.lock.locked"
        }
        
        standardTile ("deactivate continuous mode", "deactivatecontinuousmode", inactiveLabel: false, decoration: "flat", width: 3, height: 2) 
        {
            state "default", label:'deactivate continuous mode', action:"deactivateContinuousMode", icon: "st.locks.lock.locked"
        }
        
        standardTile ("deactivate rto", "activaterto", inactiveLabel: false, decoration: "flat", width: 3, height: 2) 
        {
            state "default", label:'deactivate rto', action:"deactivateRto", icon: "st.locks.lock.locked"
        }
        
        standardTile ("electric strike actuation", "electricstrikeactuation", inactiveLabel: false, decoration: "flat", width: 3, height: 2) 
        {
            state "default", label:'electric strike actuation', action:"electricStrikeActuation", icon: "st.locks.lock.locked"
        }
    }
*/
}


def initialize () 
{
  	logDebug "initialize: IN"
    logDebug "initialize: device.capabilities = ${device.capabilities}"
    logDebug "initialize: OUT"
}


def installed ()
{
  	logDebug "installed: IN"

    initialize()

    logDebug "installed: installed device ${device.data}"
    logDebug "installed: OUT"
}


def parse (Map jsonMap)
{
    logDebug "parse: IN"
    logDebug "parse: received jsonMap = ${jsonMap}"
    logDebug "parse: device.capabilities = ${device.capabilities}"
    
    // Let's first be sure that the NukiIds of this device and the parsed one are the same
    if (jsonMap.nukiId.toString () != device.data.nukiInfo.DeviceId.toString ())
    {
        trow new Exception ("${device.data.label}: Inconsistent data - events from device with Nuki ID '${jsonMap.nukiId.toString ()}' cannot be handled by device hander for device '${device.data.nukiInfo.DeviceId.toString ()}'")
    }
    
    logInfo "${device.data.label}: Status changed on this device to ${jsonMap.stateName.toUpperCase ()}. Battery status is ${jsonMap.batteryCritical ? "CRITICAL" : "NORMAL"}."

    sendOpenerEvent (jsonMap.stateName)
    parent.sendBatteryEvent (device, jsonMap.batteryCritical)

    def openerState = _openerStates.find { it.stateName.toUpperCase () == jsonMap.stateName.toUpperCase ()}
    logDebug "parse: openerState = ${openerState}"
    parent.sendProgressEvent (device, openerState?.progressText)

    logDebug "parse: OUT"
}


//
// This is here just in case
// Theoretically all callbacks from the Nuki bridge are handled by the Nuki bridge's driver
//
def parse (String description)
{
    logDebug "parse: IN"
    
    def parsedDescription = parseLanMessage (description)
    logDebug "parse: parsed description = ${parsedDescription}"
    
    logDebug "parse: OUT"
}


//
// Actually nothing is done here ...
//
def uninstalled ()
{
    logDebug "uninstalled: IN"

    logInfo "${device.label}: Nuki device '${device.label}' successfully uninstalled"
       
    logDebug "uninstalled: OUT"
}


//
// Nor here ...
//
def updated () 
{
    logDebug "updated: IN"

    logDebug "updated: OUT"
}


// ======================================
// Device specific methods
// ======================================


//
// Activates continuous mode
//
def activateCM ()
{
   	logDebug "activateCM: IN"

    sendCommandToNuki (_lockActions [4],       // action = "activate continuous mode"
                       false)                  // waitCompletition

  	logDebug "activateCM: OUT"   
}


//
// Activates ring to open (rto)
//
def activateRTO ()
{
   	logDebug "activateRTO: IN"

    sendCommandToNuki (_lockActions [1],       // action = "activate rto"
                       false)                  // waitCompletition

  	logDebug "activateRTO: OUT"   
}


//
// Close: deactivate RTO and CM modes
//
def close ()
{
   	logDebug "close: IN"
    
    deactivateCM ()
    
    deactivateRTO ()
    
    parent.sendProgressEvent (device, "Close requested")
    
    logDebug "close: OUT"   
}


//
// Deactivates continuous mode
//
def deactivateCM ()
{
   	logDebug "deactivateCM: IN"

    sendCommandToNuki (_lockActions [5],       // action = "deactivate continuous mode"
                       false)                  // waitCompletition

  	logDebug "deactivateCM: OUT"   
}


//
// Deactivates ring to open (rto)
//
def deactivateRTO ()
{
   	logDebug "deactivateRTO: IN"

    sendCommandToNuki (_lockActions [2],       // action = "deactivate rto"
                       false)                  // waitCompletition

  	logDebug "deactivateRTO: OUT"   
}


//
// Electric strike actuation
//
def electricStrikeActuation ()
{
   	logDebug "electricStrikeActuation: IN"

    sendCommandToNuki (_lockActions [3],       // action = "electric strike actuation"
                       false)                  // waitCompletition

  	logDebug "electricStrikeActuation: OUT"   
}


//
// Open (calls for the door's electric unlatching)
//
def open ()
{
   	logDebug "open: IN"

    sendCommandToNuki (_lockActions [3],       // action = "electric strike actuation"
                       false)                  // waitCompletition

  	logDebug "open: OUT"   
}


//
// Recover device's current status
//
def status ()
{
    logDebug "status: IN"
    logDebug "status: device.data = ${parent.device.data}"
    
    def deviceInfo = parent.getDeviceInfo (device.data.nukiInfo, parent.data)
    logDebug "status: deviceInfo = ${deviceInfo}"
    
    def deviceMode = _openerDeviceModes.find { it.key == deviceInfo?.mode }
    logDebug "status: deviceMode = ${deviceMode.value}"

    def deviceStatus = "Device mode: ${deviceMode.value} //\n" +
                       "State: ${deviceInfo?.stateName} //\n" +
                       "Battery: ${deviceInfo?.batteryCritical ? "CRITICAL" : "NORMAL"} //\n\n"+
                       "<b>NOTE</b>: avoid requesting this status frequently since it may drain your opener's batteries too fast."
    
    sendOpenerEvent (deviceInfo?.stateName)
    parent.sendProgressEvent (device, deviceStatus, deviceInfo)
    
    logDebug "status: OUT"
}


//=========================================
//    Support stuff
//=========================================


//
// Format and send a command to the Nuki device, handling the result
//
def sendCommandToNuki (Map action, boolean waitCompletition) 
{
    logDebug "sendCommandToNuki: IN"
    logDebug "sendCommandToNuki: Processing action specs = ${action}"

    def returnData
        
    def deviceStatus
    def actionNameUCase = action.actionName.toUpperCase ()
    def actionNameLCase = action.actionName.toLowerCase ()
    def endLoop = false
    def sendCommand = false
    def errorMessage = ""
    
    parent.sendProgressEvent (device, action.actionInProgress)

    for (i in 1..3) 
    {
        deviceStatus = parent.getNukiDeviceStatus (device.data.nukiInfo)
        logDebug "sendCommandToNuki: deviceStatus = ${deviceStatus}"
        
        logDebug "sendCommandToNuki: stateName = ${deviceStatus.lastKnownState.stateName.toUpperCase ()}"
        
        switch (deviceStatus.lastKnownState.stateName.toUpperCase ())
        {
            case "UNTRAINED":
                logDebug "sendCommandToNuki: falling through case 'UNTRAINED'"
                errorMessage = "Nuki device not trained - use Nuki smartphone app to train it"
                sendCommand = false
                endLoop = true
                break
            case "ONLINE":
                logDebug "sendCommandToNuki: falling through case 'ONLINE'"
                switch (actionNameUCase)
                {
                    case "ACTIVATE RTO":
                    case "DEACTIVATE RTO":
                    case "ELECTRIC STRIKE ACTUATION":
                    case "ACTIVAVE CONTINUOUS MODE":
                    case "DEACTIVATE CONTINUOUS MODE":                    
                        logDebug "sendCommandToNuki: falling through case 'ONLINE', subcase 'ACTIVATE RTO'/'DEACTIVATE RTO'/'ELECTRIC STRIKE ACTUATION'/'ACTIVAVE CONTINUOUS MODE'/'DEACTIVATE CONTINUOUS MODE'"
                        sendCommand = true
                        endLoop = true
                        break
                    default:
                        logDebug "sendCommandToNuki: falling through case 'ONLINE', subcase 'default'"
                        break
                }
                break
            case "RTO ACTIVE":
                logDebug "sendCommandToNuki: falling through case 'RTO ACTIVE'"
                switch (actionNameUCase)
                {
                    case "ACTIVATE RTO":
                        logDebug "sendCommandToNuki: falling through case 'RTO ACTIVE', subcase 'ACTIVATE RTO'"
                        sendCommand = false
                        endLoop = true
                        break
                    case "DEACTIVATE RTO":
                    case "ELECTRIC STRIKE ACTUATION":
                    case "ACTIVAVE CONTINUOUS MODE":
                    case "DEACTIVATE CONTINUOUS MODE":                    
                        logDebug "sendCommandToNuki: falling through case 'RTO ACTIVE', subcase 'DEACTIVATE RTO'/'ELECTRIC STRIKE ACTUATION'/'ACTIVAVE CONTINUOUS MODE'/'DEACTIVATE CONTINUOUS MODE'"
                        sendCommand = true
                        endLoop = true
                        break
                    default:
                        logDebug "sendCommandToNuki: falling through case 'RTO ACTIVE', subcase 'default'"
                        break
                }
                break
            case "OPENING":
            case "BOOT RUN":
                logDebug "sendCommandToNuki: falling through case 'OPENING'/'BOOT RUN'"
                // Let's give some time to the opener to complete the current operation
                break
            case "UNDEFINED":
                logDebug "sendCommandToNuki: falling through case 'UNDEFINED'"
                errorMessage = "Nuki device has an UNDEFINED status - please try again latter"
                sendCommand = false
                endLoop = true
                break
        }
        if (endLoop)
        {
            break
        }
        logInfo "sendCommandToNuki: pausing for 1.5 seconds"
        pauseExecution (1500)
    }

    if (sendCommand)
    {
        try
        {
            def httpRequest = "${parent.buildBridgeURL (parent.data)}/lockAction?${parent.buildNukiActionCommand (device.data.nukiInfo, _nukiDeviceTypeOpener, action.actionCode, waitCompletition)}"
            logDebug "sendCommandToNuki: httpRequest = ${httpRequest})"
    	    httpGet (httpRequest)
            {
                resp -> 
                    returnData = resp?.data
            }
            if (returnData?.success)
            {
                //logDebug "sendCommandToNuki: event name = lock - value = ${eventSuccessValue}"
                //sendLockEvent (eventSuccessValue)
            }
            else
            {
                logWarn "${_nukiDriverNameOpener}: sending of command '${action.actionName}' to Nuki lock unsuccessful"
            }
        }
        catch (err)
        {
            logDebug ("sendCommandToNuki: Error on httpPost = ${err}")
        }
    }
    
    if (errorMessage == "")
    {
        parent.sendProgressEvent (device, action.actionSuccess)
    }
    else
    {
        parent.sendProgressEvent (device, "${action.actionFailure} - see device events for more information by clicking on the 'Events' button at the top of this page", errorMessage)
        logWarn "${_nukiDriverNameOpener}: ${errorMessage}"
        returnData = false
    }
    logDebug "sendCommandToNuki: OUT"
    
    return returnData
}


def handleHttpError (errorCode)
{
    logDebug "handleHttpError: IN"
    logDebug "handleHttpError: errorCode = ${errorCode}"
    switch (errorCode)
    {
        case 401:
            sendEvent (name: "errorCode", value: "Invalid token")
            break
        case 403:
            sendEvent (name: "errorCode", value: "Authentication disabled")
            break
        case 404:
            sendEvent (name: "errorCode", value: "Nuki device unknown")
            break
        case 503:
            sendEvent (name: "errorCode", value: "Nuki device is offline")
            break
        default:
            sendEvent (name: "errorCode", value: "Unexpected error (${errorCode})")
            break
    }
    
    logDebug "handleHttpError: OUT"
}


def sendOpenerEvent (openerStatus)
{
    logDebug "sendOpenerEvent: IN"
    logDebug "sendOpenerEvent: openerStatus = ${openerStatus}"

    sendEvent (name: "doorControl", value: openerStatus) 

    logDebug "sendOpenerEvent: OUT"
}


// Logging stuff
def appDebugLogging () { return parent.appDebugLogging () }

def logDebug (message) { if (appDebugLogging ()) log.debug (message) }
def logInfo  (message) { log.info (message) }
def logWarn  (message) { log.warn (message) }


// Static globals
@Field static Map _lockActions = [1: [actionCode:       1,
                                      actionName:       "activate rto",
                                      eventName:        "unlock", 
                                      actionInProgress: "unlocking (waiting for Nuki bridge to finish operation)",
                                      actionSuccess:    "unlock command successfully sent (waiting for Nuki bridge confirmation)",
                                      actionFailure:    "unlocking failed"], 
                                  
                                  2: [actionCode:       2,
                                      actionName:       "deactivate rto",
                                      eventName:        "lock",
                                      actionInProgress: "locking (waiting for Nuki bridge to finish operation)",
                                      actionSuccess:    "lock command successfully sent (waiting for Nuki bridge confirmation)",
                                      actionFailure:    "locking failed"],
                                  
                                  3: [actionCode:       3,
                                      actionName:       "electric strike actuation",
                                      eventName:        "unlatch",
                                      actionInProgress: "unlatching (waiting for Nuki bridge to finish operation)",
                                      actionSuccess:    "unlatch command successfully sent (waiting for Nuki bridge confirmation)",
                                      actionFailure:    "unlatching failed"],
                                  
                                  4: [actionCode:       4,
                                      actionName:       "activate continuous mode",
                                      eventName:        "lock",
                                      actionInProgress: "pausing 20 seconds before locking (lock 'n' go)",
                                      actionSuccess:    "'lock 'n' go' command successfully sent (waiting for Nuki bridge confirmation)",
                                      actionFailure:    "lock 'n' go failed"],
                                 
                                  5: [actionCode:       5,
                                      actionName:       "deactivate continuous mode",
                                      eventName:        "lock",
                                      actionInProgress: "locking & unlatching (waiting for Nuki bridge to finish operation)",
                                      actionSuccess:    "locking & unlatching command successfully sent (waiting for Nuki bridge confirmation)",
                                      actionFailure:    "locking & unlatching failed"] \
                                  ]
@Field static List _openerStates = [[stateId: 0,
                                   stateName: "untrained",
                                   progressText: "Opener needs calibration - check Nuki smartphone app"],
                                  
                                  [stateId: 1,
                                   stateName: "online",
                                   progressText: "Opener"],
                                 
                                  [stateId: 3,
                                   stateName: "rto active", 
                                   progressText: "Ring to open option active"],

                                  [stateId: 5,
                                   stateName: "open",
                                   progressText: "Opened"],
                                 
                                  [stateId: 7,
                                   stateName: "opening",
                                   progressText: "Opening in progress"],
                                 
                                  [stateId: 253,
                                   stateName: "booting",
                                   progressText: "Nuki opener booting - please wait"],
                                 
                                  [stateId: 255,
                                   stateName: "undefined",
                                   progressText: "I don't know what to do ..."]
                                 ]
//@Field static Map lockActions2 = [0: "NO_ACTION", 1: "UNLOCK", 2: "LOCK", 3: "UNLATCH", 4: "LOCK_N_GO", 5: "LOCK_N_GO_WITH_UNLATCH"]
//@Field static Map lockDoorStatus = [0: "UNAVAILABLE", 1: "DEACTIVATED", 2: "DOOR_CLOSED", 3: "DOOR_OPENED", 4: "DOOR_STATE_UNKNOWN", 5: "CALIBRATING"]
//@Field static Map lockButtonActions = [0: "NO_ACTION", 1: "INTELLIGENT", 2: "UNLOCK", 3: "LOCK", 4: "UNLATCH", 5: "LOCK_N_GO", 6: "SHOW_STATUS"]


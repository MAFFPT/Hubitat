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

@Field static Map _lockActions = [1: [actionCode:       1,
                                      actionName:       "unlock",
                                      eventName:        "unlock",
                                      transientState:   "unlocking",
                                      actionInProgress: "unlocking (waiting for Nuki bridge to finish operation)",
                                      actionSuccess:    "unlock command successfully sent (waiting for Nuki Bridge confirmation)",
                                      actionFailure:    "unlocking failed"], 
                                  
                                  2: [actionCode:       2,
                                      actionName:       "lock",
                                      eventName:        "lock",
                                      transientState:   "locking",
                                      actionInProgress: "locking (waiting for Nuki bridge to finish operation)",
                                      actionSuccess:    "lock command successfully sent (waiting for Nuki Bridge confirmation)",
                                      actionFailure:    "locking failed"],
                                  
                                  3: [actionCode:       3,
                                      actionName:       "unlatch",
                                      eventName:        "unlatch",
                                      transientState:   "unlatching",
                                      actionInProgress: "unlatching (waiting for Nuki bridge to finish operation)",
                                      actionSuccess:    "unlatch command successfully sent (waiting for Nuki Bridge confirmation)",
                                      actionFailure:    "unlatching failed"],
                                  
                                  4: [actionCode:       4,
                                      actionName:       "lock 'n' go",
                                      eventName:        "lock",
                                      transientState:   "locking",
                                      actionInProgress: "pausing 20 seconds before locking (lock 'n' go)",
                                      actionSuccess:    "'lock 'n' go' command successfully sent (waiting for Nuki Bridge confirmation)",
                                      actionFailure:    "lock 'n' go failed"],
                                 
                                  5: [actionCode:       5,
                                      actionName:       "lock 'n' go with unlatch",
                                      eventName:        "lock",
                                      transientState:   "locking",
                                      actionInProgress: "locking & unlatching (waiting for Nuki bridge to finish operation)",
                                      actionSuccess:    "locking & unlatching command successfully sent (waiting for Nuki Bridge confirmation)",
                                      actionFailure:    "locking & unlatching failed"] \
                                  ]
@Field static List _lockStates = [[stateId: 0,
                                   stateName: "uncalibrated",
                                   progressText: "lock needs calibration"],
                                  
                                  [stateId: 1,
                                   stateName: "locked",
                                   progressText: "locking successful"],
                                 
                                  [stateId: 2,
                                   stateName: "unlocking",
                                   progressText: "unlocking in progress"],
                                 
                                  [stateId: 3,
                                   stateName: "unlocked", 
                                   progressText: "unlocking successful"],
                                 
                                  [stateId: 4,
                                   stateName: "locking", 
                                   progressText: "locking in progress"],
                                 
                                  [stateId: 5,
                                   stateName: "unlatched",
                                   progressText: "unlatch successful"],
                                 
                                  [stateId: 6,
                                   stateName: "unlocked lockngo",
                                   progressText: "unlocking 'n' go successful"],
                                 
                                  [stateId: 7,
                                   stateName: "unlatching",
                                   progressText: "unlatching in progress"],
                                 
                                  [stateId: 253,
                                   stateName: "booting",
                                   progressText: "Nuki bridge booting - please wait"],
                                 
                                  [stateId: 254,
                                   stateName: "motor blocked",
                                   progressText: "motor blocked - please check your lock"],
                                 
                                  [stateId: 255,
                                   stateName: "undefined",
                                   progressText: "I don't know what to do ..."]
                                 ]
//@Field static Map lockActions2 = [0: "NO_ACTION", 1: "UNLOCK", 2: "LOCK", 3: "UNLATCH", 4: "LOCK_N_GO", 5: "LOCK_N_GO_WITH_UNLATCH"]
//@Field static Map lockDoorStatus = [0: "UNAVAILABLE", 1: "DEACTIVATED", 2: "DOOR_CLOSED", 3: "DOOR_OPENED", 4: "DOOR_STATE_UNKNOWN", 5: "CALIBRATING"]
//@Field static Map lockButtonActions = [0: "NO_ACTION", 1: "INTELLIGENT", 2: "UNLOCK", 3: "LOCK", 4: "UNLATCH", 5: "LOCK_N_GO", 6: "SHOW_STATUS"]

@Field static _nukiNamespace = "maffpt"                  // All apps and drivers must be at the same namespace
@Field static _nukiLockDriverVersion = "0.2"             // Current version of this driver

@Field static Map _lockDeviceModes = [2: "Door mode"]

@Field static String _nukiDeviceTypeLock = "0"
@Field static String _nukiDriverNameLock = "Nuki Smart Lock 2.0"    // Nuki Smart Lock 2.0's device driver name

metadata 
{
    definition (name: "Nuki Smart Lock 2.0", namespace: "maffpt", author: "Marco Felicio") 
    {
        capability "Battery"
        
        capability "ContactSensor"

        capability "DoorControl"
        command "open"
        
        capability "Lock"
        command "lock"
        command "lockNGo"
        command "unlock"
        command "unlatch"
        
        command "status"
    }
/*
    preferences 
    {
        input ("debugLogging",
               "bool",
               defaultValue: false,
               required: false,
               submitOnChange: true,
               title: "Enable debug logging\n<b>CAUTION:</b> a lot of log entries will be recorded!")
    }
*/    
    tiles 
    {
        standardTile ("device.label", "device.lock", inactiveLabel: false, decoration: "flat", width: 3, height: 2) 
        {
            state "locked", label:'Locked', action:"unlock", icon: "st.doors.garage.garage-open", backgroundcolor: "#00ff00", nextState: "unlocking"
            state "unlocking", label: "Unlocking", icon: "st.doors.garage.garage-opening", backgroundcolor: "#0000ff", nextState: "unlocked"
            
            state "unlocked", label:'Unlocked', action:"lock", icon: "st.doors.garage.garage-closed", backgroundcolor: "#ff0000", nextState: "locking"
            state "locking", label:'Locking', icon: "st.doors.garage.garage-closing", backgroundcolor: "#0000ff", nextState: "locked"
        }
        
        valueTile ("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 3, height: 2)
        {
            state "battery", label: "Battery", icon: "st.batteries.battery.full", unit: "%", backgroundColors:[[value: 100, color: "#00ff00"],
                                                                                                               [value: 20,  color: "#ff0000"]]
        }
/*
        standardTile ("lockNGo", "lockNGo", inactiveLabel: false, decoration: "flat", width: 3, height: 2) 
        {
            state "default", label:'lock&Go', action:"lockNGo", icon: "st.locks.lock.locked"
        }
*/
    }
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
    //logInfo "XXXXXXXXXXXXXXXXXXXXXXXXXXXX"
    //addGrandchildDevice ()

    logDebug "installed: installed device ${device.data}"
    logDebug "installed: OUT"
}


//
// Install a device paired to a bridge
//
def addGrandchildDevice ()
{
    logDebug "addGrandchildDevice: IN"
    
    def deviceData = [:]
    deviceData.DeviceType = "Switch"
    deviceData.DeviceTypeName = "Virtual Switch"
    deviceData.DeviceId = "${device.data.nukiInfo.DeviceId}-2"
    deviceData.Name = "${device.name} latch switch"
    deviceData.DebugLoggingRequired = debugLogging    // if this device is running with debugLogging, the child device will run with it too

    def deviceProperties = [:]
    deviceProperties.label = deviceData.Name
    deviceProperties.name = deviceData.DeviceTypeName
    deviceProperties.data = deviceData
    deviceProperties.isComponent = true               // the child device will be "attached" to this device

    try 
    {
        //logDebug "addGrandchildDevice: trying to install device with nukiId = ${nukiDevice.nukiId}"
        def deviceDNI = "${device.dni}-Latch"
        def childDevice = addChildDevice ("hubitat",           // namespace - must be the same for this app and driver
                                          "Virtual Switch",    // typeName = driver of the child device - must have been previously loaded into this HE hub
                                          "anyDniWillDo",      // deviceNetworkId
                                          deviceProperties)
        // if we pass through here, it means that the device was correcly added. Let's flag it!
        logDebug "addGrandchildDevice: device with deviceId = '${deviceData.DeviceId}' and deviceDNI = '${deviceDNI}' successfully added"
        
        //nukiDevice.successfullyInstalled = true
    }
    catch (com.hubitat.app.exception.UnknownDeviceTypeException e)
    {
        logWarn "${deviceData.DeviceTypeName}: Failed to install device with nukiId = ${deviceData.DeviceId}. Driver (${deviceData.DeviceTypeName}) not installed on this Hubitat Elevation hub; install it before attempting to run this app again."
        //nukiDevice.successfullyInstalled = false
    }
    catch (error) 
    {
        logWarn "${deviceData.DeviceTypeName}: Failed to install device with nukiId = ${deviceData.DeviceId}. Error = ${error}"
        //nukiDevice.successfullyInstalled = false
    }
    
    logDebug "addGrandchildDevice: OUT"
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

    sendLockEvent (jsonMap.stateName)
    parent.sendBatteryEvent (device, jsonMap.batteryCritical)

    def lockState = _lockStates.find { it.stateName.toUpperCase () == jsonMap.stateName.toUpperCase ()}
    logDebug "parse: lockState = ${lockState}"
    parent.sendProgressEvent (device, lockState?.progressText)

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
// Handles locks requests
//
def lock (Map cmds) 
{
    logDebug "lock: IN"
    logDebug "lock: cmds = ${cmds}"

    sendCommandToNuki (_lockActions [2],       // action = "lock"
                       true)                   // waitCompletition

    logDebug "lock: OUT"
}


def lockNGo ()
{
    logDebug "lockNGo: IN"

    sendCommandToNuki (_lockActions [4],       // action = "lock 'n' go"
                       false)                  // waitCompletition

    logDebug "lockNGo: OUT"
}


//
// Unlatches the door
//
def open ()
{
   	logDebug "open: IN"

    sendCommandToNuki (_lockActions [3],       // action = "unlatch"
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
    
    def deviceMode = _lockDeviceModes.find { it.key == deviceInfo?.mode }
    logDebug "status: deviceMode = ${deviceMode.value}"

    def deviceStatus = "Device mode: ${deviceMode.value} //\n" +
                       "State: ${deviceInfo?.stateName} //\n" +
                       "Battery: ${deviceInfo?.batteryCritical ? "CRITICAL" : "NORMAL"} //\n\n"+
                       "<b>NOTE</b>: avoid requesting this status frequently since it may drain your lock's batteries too fast."
    
    sendLockEvent (deviceInfo?.stateName)
    parent.sendProgressEvent (device, deviceStatus, deviceInfo)
    
    logDebug "status: OUT"
}


def unlatch ()
{
    logDebug "unlatch: IN"
    
    sendCommandToNuki (_lockActions [3],       // action = "unlatch"
                       true)                   // waitCompletition
    
    logDebug "unlatch: OUT"
}

def unlock (Map cmds) 
{
    logDebug "unlock: IN"
    logDebug "unlock: cmds = ${cmds}"
    
    sendCommandToNuki (_lockActions [1],       // action = "unlock"
                       true)                   // waitCompletition

    logDebug "unlock: OUT"
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
            case "UNCALIBRATED":
                logDebug "sendCommandToNuki: falling through case 'UNCALIBRATED'"
                errorMessage = "Nuki device not calibrated - use Nuki smartphone app to calibrate it"
                sendCommand = false
                endLoop = true
                break
            case "LOCKED":
                logDebug "sendCommandToNuki: falling through case 'LOCKED'"
                switch (actionNameUCase)
                {
                    case "LOCK":
                    case "LOCK 'N' GO":
                    case "LOCK 'N' GO WITH UNLATCH":
                        logDebug "sendCommandToNuki: falling through case 'LOCKED', subcase 'LOCK'/'LOCK 'N' GO'/'LOCK 'N' GO WITH UNLATCH'"
                        errorMessage = "Nuki device already locked - ${actionNameUCase} command ignored"
                        sendCommand = false
                        endLoop = true
                        break
                    case "UNLATCH":
                    case "UNLOCK":
                        logDebug "sendCommandToNuki: falling through case 'LOCKED', subcase 'UNLATCH'/'UNLOCK'"
                        sendCommand = true
                        endLoop = true
                        break
                    default:
                        logDebug "sendCommandToNuki: falling through case 'LOCKED', subcase 'default'"
                        break
                }
                break
            case "LOCKING":
            case "UNLATCHING":
            case "UNLOCKING":
                logDebug "sendCommandToNuki: falling through case 'LOCKING'/'UNLATCHING'/'UNLOCKING'"
                // Let's give some time to the lock to complete the current operation
                break
            case "UNLATCHED":
                logDebug "sendCommandToNuki: falling through case 'UNLATCHED'"
                switch (actionNameUCase)
                {
                    case "LOCK":
                    case "LOCK 'N' GO":
                    case "LOCK 'N' GO WITH UNLATCH":
                        logDebug "sendCommandToNuki: falling through case 'UNLATCHED', subcase 'LOCK'/'LOCK 'N' GO'/'LOCK 'N' GO WITH UNLATCH'"
                        sendCommand = true
                        endLoop = true
                        break
                    case "UNLATCH":
                        logDebug "sendCommandToNuki: falling through case 'UNLATCHED', subcase 'UNLATCH'"
                        errorMessage = "Nuki device already unlatched - ${actionNameUCase} command ignored"
                        sendCommand = false
                        endLoop = true
                        break
                    case "UNLOCK":
                        logDebug "sendCommandToNuki: falling through case 'UNLATCHED', subcase 'UNLOCK'"
                        errorMessage = "Nuki device already unlocked & unlatched - ${actionNameUCase} command ignored"
                        sendCommand = false
                        endLoop = true
                        break
                    default:
                        logDebug "sendCommandToNuki: falling through case 'UNLATCHED', subcase 'default'"
                        break
                }
                break
            case "UNLOCKED":
            case "UNLOCKED (LOCK 'N' GO)":
                logDebug "sendCommandToNuki: falling through case 'UNLATCHED'/'UNLOCKED'/'UNLOCKED (LOCK 'N' GO)'"
                switch (actionNameUCase)
                {
                    case "LOCK":
                    case "LOCK 'N' GO":
                    case "LOCK 'N' GO WITH UNLATCH":
                        logDebug "sendCommandToNuki: falling through case 'UNLOCKED'/'UNLOCKED (LOCK 'N' GO)', subcase 'LOCK'/'LOCK 'N' GO'/'LOCK 'N' GO WITH UNLATCH'"
                        sendCommand = true
                        endLoop = true
                        break
                    case "UNLATCH":
                        logDebug "sendCommandToNuki: falling through case 'UNLOCKED'/'UNLOCKED (LOCK 'N' GO)', subcase 'UNLATCH'"
                        sendCommand = true
                        endLoop = true
                        break
                    case "UNLOCK":
                        logDebug "sendCommandToNuki: falling through case 'UNLOCKED'/'UNLOCKED (LOCK 'N' GO)', subcase 'UNLOCK'"
                        errorMessage = "Nuki device already unlocked - ${actionNameUCase} command ignored"
                        sendCommand = false
                        endLoop = true
                        break
                    default:
                        logDebug "sendCommandToNuki: falling through case 'UNLOCKED'/'UNLOCKED (LOCK 'N' GO)', subcase 'default'"
                        break
                }
                break
            case "MOTOR BLOCKED":
                logDebug "sendCommandToNuki: falling through case 'MOTOR BLOCKED'"
                errorMessage = "Nuki lock motor blocked - calibrate the lock using the Nuki smartphone app; please check system log for more information on how to proceed"
                logWarn "${device.data.label}: 9. When the calibration is finhised, re-execute the previously failed operation"
                logWarn "${device.data.label}: 8. Follow the instructions provided by the Nuki app"
                logWarn "${device.data.label}: 7. Select the 'Calibrate Smart Lock' option"
                logWarn "${device.data.label}: 6. Select the 'Manage Smart Lock' option"
                logWarn "${device.data.label}: 5. Choose this smart lock (${device.data.label})"
                logWarn "${device.data.label}: 4. Select the 'Smart Lock' option"
                logWarn "${device.data.label}: 3. Select the 'Manage my devices' menu item"
                logWarn "${device.data.label}: 2. Open the Nuki menu (touch the tree bars icon on the top left part of the screen)"
                logWarn "${device.data.label}: 1. Access the Nuki app on your smartphone"
                logWarn "${device.data.label}: To unblock it, follow these instructions:"
                logWarn "${device.data.label}: The motor in this Nuki smart lock is blocked"
                sendCommand = false
                endLoop = true
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
        sendLockEvent (action.transientState)
        try
        {
            def httpRequest = "${parent.buildBridgeURL (parent.data)}/lockAction?${parent.buildNukiActionCommand (device.data.nukiInfo, _nukiDeviceTypeLock, action.actionCode, waitCompletition)}"
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
                logWarn "${_nukiDriverNameLock}: sending of command '${action.actionName}' to Nuki lock unsuccessful"
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
        logWarn "${_nukiDriverNameLock}: ${errorMessage}"
        returnData = false
    }
    logDebug "sendCommandToNuki: OUT"
    
    return returnData
}


def xxxbuildNukiLockActionCommand (actionCode, waitCompletition)
{
    logDebug "buildNukiLockActionCommand: IN"
    logDebug "buildNukiLockActionCommand: actionCode = ${actionCode}, waitCompletition = ${waitCompletition}"
    
    def httpBody
    
    if (actionCode != null)
    {
        logDebug "buildNukiLockActionCommand: device.data.nukiInfo = ${device.data.nukiInfo}"
        httpBody = "nukiId=${device.data.nukiInfo.DeviceId}" +
                   "&deviceType=${_nukiDeviceTypeLock}" +
                   "&action=${actionCode}" +
                   "&token=${parent.data.Token}" +
                   "&nowait=${waitCompletition ? 0 : 1}"
    }
    else
    {
        log.debug "buildNukiLockActionCommand: OUT with exception"
        throw new Exception ("Invalid action description (${actionDescription})")
    }
    logDebug "buildNukiLockActionCommand: httpBody = ${httpBody}"
    logDebug "buildNukiLockActionCommand: OUT"

    return httpBody
}


def xxsendRequestToNuki (String request)
{
    logDebug "sendRequestToNuki: IN"
    logDebug "Processing ${request} request"
    
    def returnData
    
    try
    {
        def requestToSend = parent.buildNukiRequest (request)
    	httpPost ( requestToSend )
        {
            resp ->
                returnData = resp.data
        }
    }
    catch (err)
    {
        handleHttpError (err)
    }

    logDebug "sendRequestToNuki: OUT"
    
    return returnData
}


def xxxbuildNukiLockRequest (request)
{
    logDebug "buildNukiLockRequest: IN"
    def requestToSend = "http://${state.ip}/${request}?token=${state.token}"
    
    logDebug "buildNukiLockRequest: requestToSend = ${requestToSend}"
    logDebug "buildNukiLockRequest: OUT"
    
    return requestToSend
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


def sendLockEvent (lockStatus)
{
    logDebug "sendLockEvent: IN"
    logDebug "sendLockEvent: lockStatus = ${lockStatus}"

    sendEvent (name: "lock", value: lockStatus) 

    logDebug "sendLockEvent: OUT"
}


// Logging stuff
def appDebugLogging () { return parent.appDebugLogging () }

def logDebug (message) { if (appDebugLogging ()) log.debug (message) }
def logInfo  (message) { log.info (message) }
def logWarn  (message) { log.warn (message) }

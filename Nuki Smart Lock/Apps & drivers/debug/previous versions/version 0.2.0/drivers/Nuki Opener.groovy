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
@Field static List _lockStates = [[stateId: 0,
                                   stateName: "untrained",
                                   progressText: "opener needs calibration???"],
                                  
                                  [stateId: 1,
                                   stateName: "online",
                                   progressText: "locking successful???"],
                                 
                                  [stateId: 3,
                                   stateName: "rto active", 
                                   progressText: "unlock successful???"],

                                  [stateId: 5,
                                   stateName: "open",
                                   progressText: "unlatch successful???"],
                                 
                                  [stateId: 7,
                                   stateName: "opening",
                                   progressText: "opening in progress"],
                                 
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

@Field static _nukiNamespace = "maffpt"                  // All apps and drivers must be at the same namespace
@Field static _nukiOpenerDriverVersion = "0.2"           // Current version of this driver

@Field static Map _deviceModes = [2: "Door mode"]

@Field static _nukiDriverNameOpener = "Nuki Opener"    // Nuki Smart Lock 2.0's device driver name

@Field static String _nukiDeviceTypeOpener = "2"

metadata 
{
    definition (name: "Nuki Opener", namespace: "maffpt", author: "Marco Felicio") 
    {
        capability "DoorControl"
        command "open"
    }

    preferences 
    {
        input ("debugLogging",
               "bool",
               defaultValue: false,
               required: false,
               submitOnChange: true,
               title: "Enable debug logging\n<b>CAUTION:</b> a lot of log entries will be recorded!")
    }
    
    tiles 
    {
        standardTile ("lockNGo", "lockNGo", inactiveLabel: false, decoration: "flat", width: 3, height: 2) 
        {
            state "default", label:'lock&Go', action:"lockNGo", icon: "st.locks.lock.locked"
        }
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
    // if the parent device was running with debugLogging, it will automatically be passed to this device
    if (device.data.DebugLoggingRequired)
    {
        debugLogging = true
    }

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
    if (jsonMap.nukiId.toString () != device.data.data.DeviceId.toString ())
    {
        trow new Exception ("${device.data.label}: Inconsistent data - events from device with Nuki ID '${jsonMap.nukiId.toString ()}' cannot be handled by device hander for device '${device.data.data.DeviceId.toString ()}'")
    }
    
    logInfo "${device.data.label}: Status changed on this device to ${jsonMap.stateName.toUpperCase ()}. Battery status is ${jsonMap.batteryCritical ? "CRITICAL" : "NORMAL"}."

    sendOpenerEvent (jsonMap.stateName)

    def lockState = _lockStates.find { it.stateName.toUpperCase () == jsonMap.stateName.toUpperCase ()}
    logDebug "parse: lockState = ${lockState}"
    sendProgressEvent (lockState?.progressText)

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
// Activates the Opener
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
    
    def deviceInfo = getDeviceInfo (device.data.data, parent.data)
    logDebug "status: deviceInfo = ${deviceInfo}"
    
    def deviceMode = _deviceModes.find { it.key == deviceInfo?.mode }
    logDebug "status: deviceMode = ${deviceMode.value}"

    def deviceStatus = "Device mode: ${deviceMode.value} //\n" +
                       "State: ${deviceInfo?.stateName} //\n" +
                       "Battery: ${deviceInfo?.batteryCritical ? "CRITICAL" : "NORMAL"} //\n\n"+
                       "<b>NOTE</b>: avoid requesting this status frequently since it may drain your lock's batteries too fast."
    
    sendLockEvent (deviceInfo?.stateName)
    sendProgressEvent (deviceStatus, deviceInfo)
    
    logDebug "status: OUT"
}


//=========================================
//    Support stuff
//=========================================


//
// Retrieve information about a bridge
//
def getDeviceInfo (deviceData, parentData)
{
    logDebug "getDeviceInfo: IN"
    logDebug "getDeviceInfo: deviceData = ${deviceData}"
    logDebug "getDeviceInfo: parentData = ${parentData}"

    def httpRequest = "${parent.buildBridgeURL (parentData)}/lockState?nukiId=${deviceData.DeviceId}&deviceType=${deviceData.DeviceType}&token=${parentData.Token}"
    logDebug "getBridgeInfo: httpRequest = ${httpRequest}"

    def deviceInfo

    try
    {
	    httpGet (httpRequest)
		{
            resp ->           
                logDebug "refresh: resp data: ${resp.data}"
                deviceInfo = resp.data
        }
    }
    catch (e)
    {
        throw new Exception ("${parent.device.data.Label}: method 'getBridgeInfo' - Fatal error = ${error}")
    }     

    logDebug "getBridgeInfo: OUT"
    
    return deviceInfo
}


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
    
    sendProgressEvent (action.actionInProgress)

    for (i in 1..3) 
    {
        deviceStatus = parent.getNukiDeviceStatus (device.data.data)
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
            def httpRequest = "${parent.buildBridgeURL (parent.data)}/lockAction?${buildNukiOpenerActionCommand (action.actionCode, waitCompletition)}"
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
        sendProgressEvent (action.actionSuccess)
    }
    else
    {
        sendProgressEvent ("${action.actionFailure} - see device events for more information by clicking on the 'Events' button at the top of this page", errorMessage)
        logWarn "${_nukiDriverNameOpener}: ${errorMessage}"
        returnData = false
    }
    logDebug "sendCommandToNuki: OUT"
    
    return returnData
}


def buildNukiOpenerActionCommand (actionCode, waitCompletition)
{
    logDebug "buildNukiOpenerActionCommand: IN"
    logDebug "buildNukiOpenerActionCommand: actionCode = ${actionCode}, waitCompletition = ${waitCompletition}"
    
    def httpBody
    
    if (actionCode != null)
    {
        logDebug "buildNukiOpenerActionCommand: device.data.data = ${device.data.data}"
        httpBody = "nukiId=${device.data.data.DeviceId}" +
                   "&deviceType=${_deviceTypeOpener}" +
                   "&action=${actionCode}" +
                   "&token=${parent.data.Token}" +
                   "&nowait=${waitCompletition ? 0 : 1}"
    }
    else
    {
        log.debug "buildNukiOpenerActionCommand: OUT with exception"
        throw new Exception ("Invalid action description (${actionDescription})")
    }
    logDebug "buildNukiOpenerActionCommand: httpBody = ${httpBody}"
    logDebug "buildNukiOpenerActionCommand: OUT"

    return httpBody
}


def sendRequestToNuki (String request)
{
    logDebug "sendRequestToNuki: IN"
    logDebug "Processing ${request} request"
    
    def returnData
    
    try
    {
        def requestToSend = buildNukiOpenerRequest (request)
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


def buildNukiOpenerRequest (request)
{
    logDebug "buildNukiOpenerRequest: IN"
    def requestToSend = "http://${state.ip}/${request}?token=${state.token}"
    
    logDebug "buildNukiOpenerRequest: requestToSend = ${requestToSend}"
    logDebug "buildNukiOpenerRequest: OUT"
    
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


def sendBatteryEvent (batteryCritical)  
{
    logDebug "sendBatteryEvent: IN"

    sendEvent (name: "battery", value: (batteryCritital ? 20 : 100), unit: "%")          

    logDebug "sendBatteryEvent: OUT"
}


def sendErrorEvent (errorMessage, errorDescription = "")
{
    logDebug "sendErrorEvent: IN"

    sendEvent (name: "error", value: errorMessage, descriptionText: errorDescription)          

    logDebug "sendErrorEvent: OUT"
}


def sendLockEvent (lockStatus)
{
    logDebug "sendLockEvent: IN"
    logDebug "sendLockEvent: lockStatus = ${lockStatus}"

    sendEvent (name: "lock", value: lockStatus) 

    logDebug "sendLockEvent: OUT"
}


def sendProgressEvent (status, statusMessage = "")
{
    logDebug "sendProgressEvent: IN"
    logDebug "sendProgressEvent: status = ${status} / statusMessage = ${statusMessage}"

    sendEvent (name: "progress", value: status, descriptionText: statusMessage)          

    logDebug "sendProgressEvent: OUT"
}


// Logging stuff
def logDebug (message) { if (debugLogging) log.debug (message) }
def logInfo  (message) { log.info (message) }
def logWarn  (message) { log.warn (message) }

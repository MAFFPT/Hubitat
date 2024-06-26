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

@Field static _nukiNamespace = "maffpt.nuki"            // All apps and drivers must be at the same namespace
@Field static _nukiLockDriverVersion = "0.9.0"          // Current version of this driver

@Field static Map _lockDeviceModes = [2: "Door mode"]

@Field static String _nukiDeviceTypeLock = "0"
@Field static String _nukiDriverNameLock = "Nuki Smart Lock"    
                                                        // Nuki Smart Lock device driver name

@Field static String _bridgeFirmwareDoorSensorSupport = "2.6.0"

//@Field static String _previousBatteryState = "100"
//@Field static String _previousDoorsensorStateName = "CLOSED"
//@Field static String _previousLockState = "LOCKED"


metadata 
{
    definition (name: "Nuki Smart Lock", namespace: "maffpt.nuki", author: "Marco Felicio") 
    {
        capability "Battery"
        
        capability "ContactSensor"

        capability "Lock"
        command "lock"
        command "lockNGo"
        command "unlock"
        command "unlatch"
        
        command "getKeypadBatteryStatus"
        
        command "status"
        
        attribute "commandRejectionReason", "string"

        attribute "previousBatteryState", "string"
        attribute "previousContactStateName", "string"
        attribute "previousLockStateName", "string"
        
        attribute "keypadPresent", "boolean"
        attribute "keypadDeviceId", "string"
    }

    preferences 
    {
        input ("unlatchWhenUnlock",
               "bool",
               defaultValue: false,
               required: false,
               submitOnChange: true,
               title: "Unlock & unlatch the door when the UNLOCK command is received",
               description: "<br/><b>NOTES:</b>" +
                            "<ul>" +
                               "<li>Once this option is set, the UNLOCK command will also perform the UNLATCH operation, thus allowing a faster door opening</li>" +
                            "</ul>"
              )
        
        input ("ignoreLockCommandWhenOpened",
               "bool",
               defaultValue: false,
               required: false,
               submitOnChange: true,
               title: "Ignores LOCK command when the door is open/ajar",
               description: "<br/><b>NOTES:</b>" +
                            "<ul>" +
                               "<li>Once this option is set, the LOCK / LOCK&GO commands will be ignored (rejected) if the door is not closed</li>" +
                               "<li>Also, the <b><i>commandRejectionReason</i></b> custom attribute will be set with the <b><i>doorOpened</i></b> value in order to allow the rejection to be handled at Rule Machine; also, a log entry ('info' type) will be generated</li>" +
                               "<li>This option will be innefective if no door sensor is installed or detected and a log entry ('info' type) will be gererated to reflect it</li>" +
                            "</ul>"
              )
    }

    tiles 
    {//decoration: "flat", 
        standardTile ("device.label", "device.lock", inactiveLabel: false, width: 3, height: 2) 
        {
            state "locked",    label: "Locked",     action:"unlock", icon: "st.doors.garage.garage-open",    backgroundcolor: "#00ff00" //, nextState: "unlocking"
            state "unlocking", label: "Unlocking",                   icon: "st.doors.garage.garage-opening", backgroundcolor: "#0000ff" //, nextState: "unlocked"
            
            state "unlocked",  label: "Unlocked",   action:"lock",   icon: "st.doors.garage.garage-closed",  backgroundcolor: "#ff0000" //, nextState: "locking"
            state "locking",   label: "Locking",                     icon: "st.doors.garage.garage-closing", backgroundcolor: "#0000ff" //, nextState: "locked"
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
    def errorOnHttpGet = false
    def bridgeInfo = [:]
    
    def wrkDoorEvent
    
    logDebug "parse: IN"
    logDebug "parse: received jsonMap = ${jsonMap}"
    logDebug "parse: device.capabilities = ${device.capabilities}"
    
    // Let's first be sure that the NukiIds of this device and the parsed one are the same
    if (jsonMap.nukiId.toString () != device.data.nukiInfo.DeviceId.toString ())
    {
        trow new Exception ("${device.data.label}: Inconsistent data - events from device with Nuki ID '${jsonMap.nukiId.toString ()}' cannot be handled by device handler for device '${device.data.nukiInfo.DeviceId.toString ()}'")
    }
    
    // starting at bridge version 2.6.0 - support for doorSensor
    def doorSensorMessageText = ""
    def doorSensorState = [:]
    
    try
    {
        bridgeInfo = parent.getBridgeInfo (parent.data)
    }
    catch (e)
    {
        errorOnHttpGet = true
    }
    
    if (! errorOnHttpGet)
    {
        if (doCurrentBridgeFirmwareSupportsDoorSensor(bridgeInfo.versions.firmwareVersion))
        {
            logDebug ("parse: Door sensor status IS recognizable by this firmware version (${bridgeInfo.versions.firmwareVersion})")
            doorSensorState = _doorSensorStates.find { it.stateId == jsonMap.doorsensorState }
            if (doorSensorState != null) // Meaning ... is there any door sensor installed?
            {
                doorSensorMessageText = " Door sensor state = ${jsonMap.doorsensorStateName?.toUpperCase ()}."
 
                if (jsonMap.doorsensorStateName != previousContactStateName)
                {
                    switch (jsonMap.doorsensorStateName)
                    {
                        case "door opened":
                            wrkDoorEvent = "open"
                            break
                        case "door closed":
                            wrkDoorEvent = "closed"
                            break
                        default:
                            wrkDoorEvent = jsonMap.doorsensorStateName
                            break
                     }
                    logDebug "parse: wrkDoorEvent = ${wrkDoorEvent}"
                    sendContactEvent (wrkDoorEvent)
                }
                previousContactStateName = jsonMap.doorsensorStateName
            }
            else
            {
                doorSensorMessageText = " Door sensor is not installed/detected/properly configured."
            }
        }
        else
        {
            logDebug ("parse: Door sensor status IS NOT recognizable by this firmware version (${bridgeInfo.versions.firmwareVersion})")
        }
    
        //logInfo "${device.data.label}: Status changed on this device to ${jsonMap.stateName.toUpperCase ()}.${doorSensorMessageText} Battery status is ${jsonMap.batteryCritical ? "CRITICAL" : "NORMAL"}."
        logInfo "${device.data.label}: Status changed on this device to ${jsonMap.stateName.toUpperCase ()}. ${doorSensorMessageText} Battery level is ${jsonMap.batteryChargeState}%."
        
        // end
        
    
        if (jsonMap.stateName != previousLockStateName)
        {
            sendLockEvent (jsonMap.stateName)
            previousLockStateName = jsonMap.stateName
        }    
        
        //parent.sendBatteryEvent (device, jsonMap.batteryCritical)
        if (jsonMap.batteryChargeState != previousBatteryState)
        {
            parent.sendBatteryEvent (device, jsonMap.batteryChargeState)
            previousBatteryState = jsonMap.batteryChargeState
        }
        
        
        def lockState = _lockStates.find { it.stateName.toUpperCase () == jsonMap.stateName.toUpperCase ()}
        logDebug "parse: lockState = ${lockState}"
    
        parent.sendProgressEvent (device, lockState?.progressText)
    
        //// starting at bridge version 2.6.0 - support for doorSensor
        //if (doorSensorState?.sendEvent)
        //{
        //    sendDoorEvent (doorSensorState.eventText)
        //}
        //// end
    }
    else
    {
        logInfo "Bridge information request failed. Contact developer."
        parent.sendProgressEvent "Bridge information request failed. Contact developer."
    }
    
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
    
    if (canExecuteLock (_lockActions [2]))
    {
        sendCommandToNuki (_lockActions [2],       // action = "lock"
                           true)                   // waitCompletition
    }
    
    logDebug "lock: OUT"
}


def lockNGo ()
{
    logDebug "lockNGo: IN"

    if (canExecuteLock (_lockActions [4]))
    {
        sendCommandToNuki (_lockActions [4],       // action = "lock 'n' go"
                           false)                  // waitCompletition
    }
    
    logDebug "lockNGo: OUT"
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
    
    // starting at bridge version 2.6.0 - support for doorSensor
    def doorSensorMessageText = ""
    def doorSensorState = [:]
    
    def bridgeInfo = parent.getBridgeInfo (parent.data)
    logDebug "status: bridgeInfo = ${bridgeInfo}"

//    if (bridgeInfo.versions.firmwareVersion >= _bridgeFirmwareDoorSensorSupport)
    if (doCurrentBridgeFirmwareSupportsDoorSensor(bridgeInfo.versions.firmwareVersion))
    {
        logDebug ("status: Door sensor status is recognizable by this firmware version (${bridgeInfo.versions.firmwareVersion})")
        doorSensorState = _doorSensorStates.find { it.stateId == deviceInfo.doorsensorState }
        doorSensorMessageText = "Door sensor state = ${deviceInfo.doorsensorStateName.toUpperCase ()}"
    }
    else
    {
        logDebug ("status: Door sensor status IS NOT recognizable by this firmware version (${bridgeInfo.versions.firmwareVersion})")
    }
    
    
    resetRejectionEvent ()
    
    logInfo "${device.data.label}: Status changed on this device to ${deviceInfo.stateName.toUpperCase ()}. ${doorSensorMessageText}. Battery level is ${deviceInfo.batteryChargeState} %."

    // end

    def deviceStatus = "<br/>Device mode: ${deviceMode.value.toUpperCase ()}" +
                       "<br/>State: ${deviceInfo?.stateName.toUpperCase ()}" +
                       "<br/>" + doorSensorMessageText +
                       "<br/>Battery: ${deviceInfo?.batteryChargeState} %"+
                       "<br/>NOTE: avoid requesting this status frequently since it may drain your lock's batteries too fast"
    
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

    if (unlatchWhenUnlock)
    {
        unlatch ()
    }
    else
    {
        sendCommandToNuki (_lockActions [1],       // action = "unlock"
                           true)                   // waitCompletition
    }
    
    logDebug "unlock: OUT"
}

//=========================================
//
//=========================================
def getKeypadBatteryStatus ()
{
    def retn = "false"
    
    // is Keypad present?
    if (getvalue ("keypadPresent", skipCache = true))
    {
        
    }

    return retn
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
            logWarn ("sendCommandToNuki: Error on httpPost = ${err}")
        }
        
        // resetting commandRejectionReason attribute
        resetRejectionEvent ()
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


//
// version 0.5.0 - handling the ignoreLockCommandWhenOpened option
//
def canExecuteLock (Map action)
{
    logDebug "canExecuteLock: IN"

    def executeLock = true
    
    def deviceInfo = parent.getDeviceInfo (device.data.nukiInfo, parent.data)
    logDebug "status: deviceInfo = ${deviceInfo}"

    if (ignoreLockCommandWhenOpened)
    {
        switch (deviceInfo.doorsensorState)
        {
            case 3: // door opened
                sendRejectionEvent ("doorOpened")
                logInfo "${device.data.label}: Locking command (${action.actionName.toUpperCase ()}) ignored because the door is open"
                executeLock = false
                break
            case 0: // unavailabe
                logInfo "${device.data.label}: Ignore lock command option innefective: door sensor not detected"
                break
            default:
                break
        }
    }
    
    logDebug "canExecuteLock: OUT"
    
    return executeLock
}


//
//
//
def resetRejectionEvent ()
{
    logDebug "resetRejectionEvent: IN"

    sendRejectionEvent ("noRejection")

    logDebug "resetRejectionEvent: OUT"
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


//
//
//
def sendContactEvent (contactEvent)
{
    logDebug "sendContactEvent: IN"
    logDebug "sendContactEvent: contactEvent = ${contactEvent}"
    
    def sendIt = false
    
    sendEvent (name: "contact", value: contactEvent) 

    logDebug "sendContactEvent: OUT"
}


//
//
//
def sendLockEvent (lockStatus)
{
    logDebug "sendLockEvent: IN"
    logDebug "sendLockEvent: lockStatus = ${lockStatus}"

    sendEvent (name: "lock", value: lockStatus) 

    logDebug "sendLockEvent: OUT"
}


//
//
//
def sendRejectionEvent (rejectionReason)
{
    logDebug "sendRejectionEvent: IN"
    logDebug "sendRejectionEvent: rejectionReason = ${rejectionReason}"

    sendEvent (name: "commandRejectionReason", value: rejectionReason) 

    logDebug "sendRejectionEvent: OUT"
}


//
// Compare the current Bridge Firmware version with the Firmware version that supports door sensor 
//
def doCurrentBridgeFirmwareSupportsDoorSensor (currentFirmwareVersion)
{
    logDebug ("doCurrentBridgeFirmwareSupportsDoorSensor: IN")
  
    def retn = (comparableFWVersion (currentFirmwareVersion) >= comparableFWVersion (_bridgeFirmwareDoorSensorSupport))

    logDebug ("doCurrentBridgeFirmwareSupportsDoorSensor: retn = ${retn}")
    logDebug ("doCurrentBridgeFirmwareSupportsDoorSensor: OUT")
    
    return retn
}


//
//  Changes version string x.y.z into something that can be comparabl
//        
def comparableFWVersion (fwVersion)
{
  logDebug ("comparableFWVersion: IN")
  
  def splitted = fwVersion.split("\\.")
  def comparable = ""
  
  for (part in splitted)
  {
    comparable += part.padLeft (3, "0") + "."
  }
  
  logDebug ("comparableFWVersion: comparable = ${comparable}")
  logDebug ("comparableFWVersion: OUT")

  return comparable
}


// Logging stuff
def appDebugLogging () { return parent.appDebugLogging () }

def logDebug (message) { if (appDebugLogging ()) log.debug (parent.stripToken (message)) }
def logInfo  (message) { log.info (parent.stripToken (message)) }
def logWarn  (message) { log.warn (parent.stripToken (message)) }


// Static Globals
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


@Field static List _doorSensorStates = [[stateId: 0,
                                         stateName: "unavailable",
                                         sendEvent: false,
                                         eventText: ""],
                                  
                                        [stateId: 1,
                                         stateName: "deactivated",
                                         sendEvent: false,
                                         eventText: ""],
                                 
                                        [stateId: 2,
                                         stateName: "door closed",
                                         sendEvent: true,
                                         eventText: "closed"],
                                 
                                        [stateId: 3,
                                         stateName: "door opened",
                                         sendEvent: true,
                                         eventText: "open"],
                                 
                                        [stateId: 4,
                                         stateName: "door state unknown",
                                         sendEvent: false,
                                         eventText: ""],
                                 
                                        [stateId: 5,
                                         stateName: "calibrating",
                                         sendEvent: false,
                                         eventText: ""]
                                       ]


//@Field static Map lockActions2 = [0: "NO_ACTION", 1: "UNLOCK", 2: "LOCK", 3: "UNLATCH", 4: "LOCK_N_GO", 5: "LOCK_N_GO_WITH_UNLATCH"]
//@Field static Map lockDoorStatus = [0: "UNAVAILABLE", 1: "DEACTIVATED", 2: "DOOR_CLOSED", 3: "DOOR_OPENED", 4: "DOOR_STATE_UNKNOWN", 5: "CALIBRATING"]
//@Field static Map lockButtonActions = [0: "NO_ACTION", 1: "INTELLIGENT", 2: "UNLOCK", 3: "LOCK", 4: "UNLATCH", 5: "LOCK_N_GO", 6: "SHOW_STATUS"]

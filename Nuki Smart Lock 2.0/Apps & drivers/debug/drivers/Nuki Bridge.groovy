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

@Field static _nukiNamespace = "maffpt.nuki"               // All apps and drivers must be at the same namespace
@Field static _nukiBridgeDriverVersion = "0.6.0"           // Current version of this driver

@Field static _nukiDriverNameBridge = "Nuki Bridge"        // name of the device type = driver name

@Field static _nukiDeviceTypes = [0: "Nuki Smart Lock", 2: "Nuki Opener"]

metadata 
{
    definition (name: "Nuki Bridge", namespace: "maffpt.nuki", author: "Marco Felicio") 
    {
        capability "Health Check"
        command "ping"

        capability "Refresh"
        command "refresh"
        
        command "getLog"
        command "status"
    }

    tiles 
    {
        // When clicked, retrieves the last 20 log entries of this Nuki bridge
        standardTile ("getLog", "getLog", inactiveLabel: false, decoration: "flat", width: 3, height: 2) 
        {
        	state "default", action:"getLog", icon: "st.locks.lock.locked"
    	}
        
        // When clicked, refresh information about this Nuki bridge and its paired devices
        standardTile ("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 3, height: 2) 
        {
        	state "default", action:"refresh", icon:"st.secondary.refresh"
    	}
    }
}


//
// Helper method called by 'installed' and 'updated' methods
//
def initialize () 
{
  	logDebug "initialize: IN"

    // As you can see, we are doing nothing here - at least so far ...
    
    logDebug "initialize: OUT"
}


//
// Called when the bridge is installed for the first time
//
def installed ()
{
  	logDebug "installed: IN"
    logDebug "installed: installing device ${device.data}"

    // Install all paired devices to this bridge
    addPairedDevices (device)
    
    // Setup the callbacks from the Nuki bridge to this HE bridge
    setupCallbacks (device.data)
    
    // Helper method
    initialize ()

    logInfo "${device.label}: Nuki bridge '${device.label}' successfully installed"
    
    logDebug "installed: OUT"
}


//
// Called when something is reported by the device via callback and gets to this hub 
//
def parse (description) 
{
    logDebug "parse: IN"

    // Let's make the value received from the Nuki bridge inteligible
    def parsedDescription = parseLanMessage (description)
    logDebug "parse: parsedDescription = ${parsedDescription}"

    // Let's log the information received from the Nuki bridge
    logAndPropagateEvent (device.data, parsedDescription.json)
    
    logDebug "parse: Parse returned ${result?.descriptionText}"
    logDebug "parse: OUT"
    
    return result
}


//
// Checks if the bridge is responsive
//
def ping ()
{
    logDebug "ping: IN"

    def bridgeInfo = getBridgeInfo (device.data)

    def bridgeStatus = "${bridgeInfo?.wlanConnected ? "Ping successuful - Bridge reachable." : "Ping failed - bridge unreachable"}"
    
    sendEvent (name: "ping", value: bridgeStatus)
    
    logDebug "ping: OUT"
}


//
// Collect information about the device and display it at the 'Current status' area of the device interface
//
def refresh () 
{
  	logDebug "refresh: IN"
    
    def bridgeInfo = getBridgeInfo (device.data)

    sendEvent (name: "info", value: bridgeInfo?.toMapString (), data: bridgeInfo)

    logDebug "refresh: OUT"
}


//
// Check if the device is LAN & Web connected
//
def status ()
{
    logDebug "status: IN"
    
    def bridgeInfo = getBridgeInfo (device.data)

    def bridgeStatus = "WLAN status: ${bridgeInfo?.wlanConnected ? "connected" : "not connected"}\n" +
                       "Nuki server status: ${bridgeInfo?.serverConnected ? "connected" : "not connected"}"
    
    sendEvent (name: "status", value: bridgeStatus, data: bridgeInfo)
    
    logDebug "status: OUT"
}


def updated () 
{
    logDebug "updated: IN"

    initialize ()

    logDebug "updated: OUT"
}


//
// Called when the device is uninstalled
// Remove, if any present, all child devices of this device
//
def uninstalled ()
{
    logDebug "uninstalled: IN"

    String deviceDNI
    def childDevice
    
    device.data.PairedDevices?.each
    {
        deviceDNI = parent.buildDeviceDNI (it)
        
        logDebug "uninstalled: uninstalling device = ${it} - DNI: ${deviceDNI}"
        
        childDevice = getChildDevice (deviceDNI)
        
        if (childDevice)
        {
            deleteChildDevice (deviceDNI)
        }
    }
    logInfo "${device.label}: Nuki bridge '${device.label}' successfully uninstalled"
    
    logDebug "uninstalled: OUT"
}


//=========================================
// Support stuff
//=========================================

//=========================================
// Callback support methods
//=========================================

//
// Configure callbacks in a bridge
//
def setupCallbacks (deviceData)
{
    logDebug "setupCallbacks: IN"
    logDebug "setupCallbacks: deviceData = ${deviceData}"
    
    // First, let's clear previously registered callbacks
    clearCallbacks (deviceData)

    // Now, let's register a callback to this HE hub
    def bridgeURL = buildBridgeURL (deviceData)
    def encodedCallbackURL = buildEncodedCallbackURL ()
    def httpRequest = "${bridgeURL}/callback/add?url=${encodedCallbackURL}&token=${deviceData.Token}"
    logDebug "setupCallbacks: httpRequest = ${httpRequest}"
    
    try
    {
	    httpGet (httpRequest)
		{
            resp ->           
                logDebug "clearCallbacks: resp data: ${resp?.data}"
        }
    }
    catch (e)
    {
        thow new Exception ("${device.data.Label}: Method 'setupCallbacks' - fatal error = ${e}")
    }
    
    logDebug "setupCallbacks: OUT"
}


//
// Clear all callbacks registered in a bridge
//
def clearCallbacks (deviceData)
{
    logDebug "clearCallbacks: IN"

    def httpRequest = "${buildBridgeURL (deviceData)}/callback/list?token=${deviceData.Token}"
    logDebug "clearCallbacks: httpRequest = ${httpRequest}"

    try
    {
	    httpGet (httpRequest)
		{
            resp ->           
                logDebug "clearCallbacks: resp data: ${resp?.data}"
                
                resp?.data?.callbacks?.each
                {
                    clearCallback (deviceData, it)
                }
        }
    }
    catch (e)
    {
        throw new Exception ("${device.data.Label}: method 'clearCallbacks' - Fatal error = ${error}")
    }
    
    logDebug "clearCallbacks: OUT"
}


//
// Clear a specific callback registered in a bridge
//
def clearCallback (deviceData, callback)
{
    logDebug "clearCallback: IN"
     
    def httpRequest = "${buildBridgeURL (deviceData)}/callback/remove?id=${callback.id}&token=${deviceData.Token}"
    logDebug "clearCallback: httpRequest = ${httpRequest}"

    try
    {
	    httpGet (httpRequest)
		{
            resp ->           
                logDebug "clearCallback: resp data: ${resp?.data}"
        }
    }
    catch (e)
    {
        throw new Exception ("${device.data.Label}: method 'clearCallback' - Fatal error = ${error}")
    }     
  
    logDebug "clearCallback: OUT"
}


//
// Build callback URL
//
def buildEncodedCallbackURL ()
{
    logDebug "buildEncodedCallbackURL: IN"
    
    def callbackURL = "http://${location.hubs[0].localIP}:${location.hubs[0].localSrvPortTCP}"
    logDebug "buildEncodedCallbackURL: original callback URL = ${callbackURL}"
    
    def encodedCallbackURL = java.net.URLEncoder.encode (callbackURL, "UTF-8")
    logDebug "buildEncodedCallbackURL: URLEncoded callback URL = ${encodedCallbackURL}"

    logDebug "buildEncodedCallbackURL: OUT"

    return encodedCallbackURL
}


//
// Retrieve information about a bridge
//
def getBridgeInfo (deviceData)
{
    logDebug "getBridgeInfo: IN"
    logDebug "getBridgeInfo: deviceData = ${deviceData}"
    
    def httpRequest = "${parent.buildBridgeURL (deviceData)}/info?token=${deviceData.Token}"
    logDebug "getBridgeInfo: httpRequest = ${httpRequest}"

    def bridgeInfo

    try
    {
	    httpGet (httpRequest)
		{
            resp ->           
                logDebug "refresh: resp data: ${resp.data}"
                bridgeInfo = resp.data
        }
    }
    catch (e)
    {
        throw new Exception ("${deviceData.Label}: method 'getBridgeInfo' - Fatal error = ${error}")
    }     

    logDebug "getBridgeInfo: OUT"
    
    return bridgeInfo
}


//
// Add all devices paired to a bridge
//
def addPairedDevices (bridge)
{
    logDebug "addPairedDevices: IN"
    
    bridge.data.PairedDevices.each
    {
        addPairedDevice (bridge, it)         
    }
    
    logDebug "addPairedDevices: OUT"
}


//
// Install a device paired to a bridge
//
def addPairedDevice (bridge, nukiDevice)
{
    logDebug "addPairedDevice: IN"
    logDebug "addPairedDevice: nukiDevice = ${nukiDevice}"
    
    def nukiInfo = [:]
    nukiInfo.DeviceType = nukiDevice.deviceType
    nukiInfo.DeviceTypeName = parent.getNukiDeviceDriver (nukiDevice)
    nukiInfo.DeviceId = nukiDevice.nukiId
    nukiInfo.Name = nukiDevice.name
//    nukiInfo.DebugLoggingRequired = debugLogging    // if this device is running with debugLogging, the child device will run with it too

    def deviceProperties = [:]
    deviceProperties.label = parent.buildNukiDeviceLabel (nukiDevice)
    deviceProperties.name = nukiInfo.DeviceTypeName
    deviceProperties.nukiInfo = nukiInfo
    deviceProperties.isComponent = true               // the child device will be "attached" to this device

    try 
    {
        logDebug "addPairedDevice: trying to install device with nukiId = ${nukiDevice.nukiId}"
        def deviceDNI = parent.buildDeviceDNI (nukiDevice)
        def childDevice = addChildDevice (_nukiNamespace,               // namespace - must be the same for this app and driver
                                          nukiInfo.DeviceTypeName,      // typeName = driver of the child device - must have been previously loaded into this HE hub
                                          deviceDNI,                    // deviceNetworkId
                                          deviceProperties)
        // if we pass through here, it means that the device was correcly added. Let's flag it!
        logDebug "addPairedDevice: device with deviceId = '${nukiDevice.nukiId}' and deviceDNI = '${deviceDNI}' successfully added"
        // TO BE MOVED TO DEVICE DRIVER
        logInfo "${bridge.label}: Paired device '${childDevice}' successfully installed"
        
        nukiDevice.successfullyInstalled = true
    }
    catch (com.hubitat.app.exception.UnknownDeviceTypeException e)
    {
        logWarn "${_nukiDriverNameBridge}: Failed to install device with nukiId = ${nukiDevice.nukiId}. Driver (${nukiInfo.DeviceTypeName}) not installed on this Hubitat Elevation hub; install it before attempting to run this app again."
        nukiDevice.successfullyInstalled = false
    }
    catch (error) 
    {
        logWarn "${_nukiDriverNameBridge}: Failed to install device with nukiId = ${nukiDevice.nukiId}. Error = ${error}"
        nukiDevice.successfullyInstalled = false
    }
    
    logDebug "addPairedDevice: OUT"
}


def handleHttpError (errorCode)
{
    logDebug "handleHttpError: IN"
    logDebug "handleHttpError: errorCode = ${errorCode}"
    switch (errorCode)
    {
        case 401:
            sendEvent (name: "errorCode", value: "Token invalid")
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
// Logging and propagating the information returned from the bridge callback
//
def logAndPropagateEvent (Map deviceData, Map jsonMap)
{
    logDebug "logEvent: IN"

    // Get the Nuki device data for the device where the event occurred
    def nukiDevice = deviceData.PairedDevices.find { it.nukiId == jsonMap.nukiId }
    logDebug "logEvent: nukiDevice = ${nukiDevice}"
    logDebug "logEvent: nukiId = ${nukiDevice.nukiId}"
    
    // Building the DNI for the device - we need it for recover information about it from HE
    def deviceDNI = parent.buildDeviceDNI (nukiDevice)
    logDebug "logEvent: deviceDNI = ${deviceDNI}"
    
    // Load the device object for this device
    def childDevice = getChildDevice (deviceDNI)
    logDebug "logEvent: childDevice = ${childDevice}"
    
    // Now, let's pass this event to the Nuki Device to handle it
    childDevice.parse (jsonMap)
    
    logDebug "logEvent: OUT"
}

//=========================================
// Shared code
//
// These methods are shared between various
// devices (bridge, lock, opener, etc)
//=========================================


//
// Get the bridge's URL (shared method with device drivers)
//
def buildBridgeURL (deviceData)
{
    logDebug "buildBridgeURL: IN"

    def bridgeURL = "http://${deviceData.IP}:${deviceData.Port}"

    logDebug "buildBridgeURL: bridge URL = ${bridgeURL}"
    logDebug "buildBridgeURL: OUT"
    
    return bridgeURL
}


//
// Get the current status of a given device (shared method with device drivers)
//
def getNukiDeviceStatus (targetDeviceData)
{
    logDebug "getNukiDeviceStatus: IN"
    logDebug "getNukiDeviceStatus: targetDeviceData = ${targetDeviceData}"
    
    def deviceStatus
    
    def httpRequest = "${buildBridgeURL (device.data)}/list?token=${device.data.Token}"
    logDebug "getNukiDeviceStatus: httpRequest = ${httpRequest}"

    httpGet (httpRequest) 
    { 
        resp ->
            logDebug "getNukiDeviceStatus: resp = ${resp.data}"
            deviceStatus = resp.data.find { it.nukiId.toString () == targetDeviceData.DeviceId.toString () }
            logDebug "getNukiDeviceStatus: deviceStatus = ${deviceStatus}"
        
            if ( ! deviceStatus )
            {
                throw new Exception ("${device.data.Label}: Device with Nuki ID = ${targetDeviceData.DeviceId} not properly configured at Nuki bridge '${device.data.Label}'")
            }
    }
    logDebug "getNukiDeviceStatus: OUT"
    
    return deviceStatus
}


//
// Retrieve information about a bridge (NOTE: shared method between device drivers)
//
def getDeviceInfo (bridgeData, parentData)
{
    logDebug "getDeviceInfo: IN"
    logDebug "getDeviceInfo: bridgeData = ${bridgeData}"
    logDebug "getDeviceInfo: parentData = ${parentData}"

    def httpRequest = "${parent.buildBridgeURL (parentData)}/lockState?nukiId=${bridgeData.DeviceId}&deviceType=${bridgeData.DeviceType}&token=${parentData.Token}"
    logDebug "getDeviceInfo: httpRequest = ${httpRequest}"

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
        throw new Exception ("${parentData.Label}: method 'getDeviceInfo' - Fatal error = ${error}")
    }     

    logDebug "getDeviceInfo: OUT"
    
    return deviceInfo
}


//
// Builds a Nuki action command to be sent to the bridge (NOTE: shared method between device drivers)
//
def buildNukiActionCommand (nukiInfo, deviceType, actionCode, waitCompletition)
{
    logDebug "buildNukiActionCommand: IN"
    logDebug "buildNukiActionCommand: waitCompletition = ${waitCompletition}"
    logDebug "buildNukiActionCommand: actionCode = ${actionCode}"
    logDebug "buildNukiActionCommand: deviceType = ${deviceType}"
    logDebug "buildNukiActionCommand: nukiInfo = ${nukiInfo}"
    
    def httpBody
    
    if (actionCode != null)
    {
        httpBody = "nukiId=${nukiInfo.DeviceId}" +
                   "&deviceType=${deviceType}" +
                   "&action=${actionCode}" +
                   "&token=${device.data.Token}" +
                   "&nowait=${waitCompletition ? 0 : 1}"
    }
    else
    {
        log.debug "buildNukiActionCommand: OUT with exception"
        throw new Exception ("Invalid action description (${actionDescription})")
    }
    logDebug "buildNukiActionCommand: httpBody = ${httpBody}"
    logDebug "buildNukiActionCommand: OUT"

    return httpBody
}


//
// Builds a request to be sent to the bridge (NOTE: shared method between device drivers)
//
def buildNukiRequest (request)
{
    logDebug "buildNukiRequest: IN"
    def requestToSend = "http://${state.ip}/${request}?token=${state.token}"
    
    logDebug "buildNukiRequest: requestToSend = ${requestToSend}"
    logDebug "buildNukiRequest: OUT"
    
    return requestToSend
}


//
// Event methods (NOTE: shared methods between device drivers)
//

//
// Record a battery event
//
def sendBatteryEvent (forDevice, batteryCritical)  
{
    logDebug "sendBatteryEvent: IN"

    forDevice.sendEvent (name: "battery", value: (batteryCritital ? 20 : 100), unit: "%")          

    logDebug "sendBatteryEvent: OUT"
}


//
// Record a progress event
//
def sendProgressEvent (forDevice, status, statusMessage = "")
{
    logDebug "sendProgressEvent: IN"
    logDebug "sendProgressEvent: status = ${status} / statusMessage = ${statusMessage}"

    forDevice.sendEvent (name: "progress", value: status, descriptionText: statusMessage)          

    logDebug "sendProgressEvent: OUT"
}


//
// Record an error event
//
def xxsendErrorEvent (forDevice, errorMessage, errorDescription = "")
{
    logDebug "sendErrorEvent: IN"

    forDevice.sendEvent (name: "error", value: errorMessage, descriptionText: errorDescription)          

    logDebug "sendErrorEvent: OUT"
}


//
// Logging stuff
//
def appDebugLogging () { return parent.appDebugLogging () }
//def appDebugLogging () { return true }


def logDebug (message) { if (appDebugLogging ()) log.debug (message) }
def logInfo  (message) { log.info (message) }
def logWarn  (message) { log.warn (message) }

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

@Field static _nukiNamespace = "maffpt.nuki"
@Field static _nukiDriverNameBridge = "Nuki Bridge"          // name of the device type = driver name
@Field static _nukiDriverNameLock = "Nuki Smart Lock 2.0"    // Nuki Smart Lock 2.0's device driver name
@Field static _nukiDriverNameOpener = "Nuki Opener"          // Nuki Opener's device driver name

@Field static _nukiIntegrationVersion = "0.5.2"

@Field static _nukiDiscoverBridgesURL = "https://api.nuki.io/discover/bridges"

definition (name:              "Nuki Smart Lock 2.0 Integration",
            namespace:         "maffpt.nuki",
            author:            "Marco Felicio (MAFFPT)",
            description:       "Integration app for Nuki<sup>&reg;</sup> Smart Lock 2.0 - version ${_nukiIntegrationVersion}",
            category:          "Convenience",
            singleInstance:    true,
            iconUrl:           "https://raw.githubusercontent.com/MAFFPT/Hubitat/Nuki Smart Lock 2.0/icons/nuki-logo-white.svg",
            iconX2Url:         "https://raw.githubusercontent.com/erocm123/SmartThingsPublic/master/smartapps/erocm123/sonoff-connect.src/sonoff-connect-icon-2x.png",
            iconX3Url:         "https://raw.githubusercontent.com/erocm123/SmartThingsPublic/master/smartapps/erocm123/sonoff-connect.src/sonoff-connect-icon-3x.png",
	        documentationLink: "https://github.com/MAFFPT/Hubitat/blob/master/Nuki%20Smart%20Lock%202.0/README.md")


preferences 
{
	page (name: "mainPage")
	page (name: "selectBridgesToAddPage")
    page (name: "addedBridgesPage")
}


def installed () 
{
    logDebug "installed: IN"

    initialize()
    
    logDebug "installed: OUT"
}


def updated () 
{
    logDebug "updated: IN"
    
    initialize () 
    
    logInfo "Nuki Smart Lock 2.0 Integration finished"

    logDebug "updated: OUT"
}


def initialize () 
{
	logDebug "initialize: IN"

    //unschedule ()
    
	logDebug "initialize: OUT"
}


def parse (description)
{
    throw new Exception ("Unexpected call to parse method. Received parameter: ${parseLanMessage (description)}")   
}


//
//    mainPage
//
def mainPage ()
{
	logDebug "mainPage: IN (OUT will not be shown)"
    
	initialize ()
    
    logInfo "Nuki Smart Lock 2.0 Integration started"

    return dynamicPage (name: "mainPage",
                        uninstall: true,
                        install: true) \
                        {
                            standardHeader ("Bridges discovery")
                            section () \
                            {
                                href  (page: "selectBridgesToAddPage",
                                       title: "<b>Discover Nuki<sup>&reg;</sup> Bridges</b>",
                                       description: "\nDiscover Bridges present on your local network for installation")
                                
                                input (name: "debugLogging",
                                       type: "bool",
                                       defaultValue: false,
                                       required: true,
                                       submitOnChange: true,
                                       title: "Enable <u>app</u> and <u>drivers</u> debug logging\n<b>CAUTION:</b> a lot of log entries will be recorded!")
			                    
                                paragraph "<br/><b>Please note:</b><br />"
                                paragraph "&bull; Set up your bridge(s) and lock(s) correctly using the Nuki<sup>&reg;</sup> smartphone App before using this app."
                                paragraph "&bull; It is <b>mandatory</b> the use of static IP address(es) for your bridge(s). See your router documentation on how to set static IP on DHCP settings."
		                    }
                        }
}


//
// Look for the bridges present on this network
//
def selectBridgesToAddPage ()
{
    logDebug "selectBridgesToAddPage: IN (OUT will not be shown)"

    def existingBridges = []
    existingBridges = discoverBridges ()
    logDebug "selectBridgesToAddPage: existing bridges = ${existingBridges}"
    
    def existingBridgesParam = [:]
    
    // create a list of discovered bridges, flaging if it is already installed or not
	def bridgesList = [:]
    
    existingBridges.each
    {
        logDebug "selectBridgesToAddPage: processing bridge: ${it}"
        logDebug "selectBridgesToAddPage: bridge (${it.bridgeId}) ${it.alreadyInstalled ? "is already installed" : "has not been installed yet"}"
        
        bridgesList ["${it.bridgeId}"] = "Nuki bridge (${it.bridgeId}) - ${it.alreadyInstalled ? "already installed (check warnings below)" : "new"}"
//        bridgesList ["${it.bridgeId}#"] = "Nuki bridge (${it.bridgeId}) - ${it.alreadyInstalled ? "already installed (check warnings below)" : "new"}"

        existingBridgesParam << [ (it.bridgeId): it ]
	}
    bridgesList.sort () // let's make it neat ...
    
	logDebug "selectBridgesToAddPage: bridgesList = ${bridgesList}"
	logDebug "selectBridgesToAddPage: existingBridgesParam = ${existingBridgesParam}"
	//logDebug "selectBridgesToAddPage: existingBridges = ${existingBridges}"
    
    def desc
    switch (bridgesList.size())
    {
        case 0:
            desc = "No bridges to install"
            break
        case 1:
            desc = "1 bridge found - select it to install"
            break
        default:
            desc = "${bridgesList.size()} bridges found - select bridges to install"
    }
    
	return dynamicPage (name: "selectBridgesToAddPage") \
                        {
                            standardHeader ("<b>Install Nuki<sup>&reg;</sup> Bridges and its Nuki<sup>&reg;</sup> paired device(s) to your Hubitat Elevation<sup>&reg;</sup> hub</b>")
                            section() \
                            {
                                input (name: "selectedBridgesToAdd", 
                                       required: true,
                                       type: "enum",
                                       multiple: true,
                                       options: bridgesList,
                                       title: "Use the following list to select the Bridge(s) to install. Then click on <b>'Install selected Bridge(s)'</b> box below. " +
                                              "Right after clicking on it, the led on every selected Bridge will lit up, one by one. " +
                                              "When the Bridge's led lit up, you must press the button on the Bridge to allow it to be recognized by this app.",
                                       description: desc)
                                
                                href  ("addedBridgesPage",
                                       title: "Install selected Bridge(s)",
                                       params: existingBridgesParam,
                                       description: "\nClick here to install\n\n<b>NOTICE:</b>Don't forget to press the selected Bridge(s) button when its LED lit up",
                                       state: "")
                                
                                paragraph "<b>WARNING</b>: Selecting a Bridge already installed will automatically delete it, its paired device(s) and\n" +
                                          "all references to them in this Hubitat Elevation<sup>&reg;</sup> hub (e.g. Rules in Rule Machine)." 
                                paragraph "Sometimes it gets difficult to get an answer from the Nuki<sup>&reg;</sup> bridges ...\n" +
                                          "If it happens, you must execute the most important debug action in all history of IT: power recicle!\n" +
                                          "So, unplug your Nuki<sup>&reg;</sup> Bridge(s), wait 15 seconds, plug it again and wait for the led stop flashing.\n" +
                                          "Then, restart the installation of the Bridge(s) and paired device(s)."
                             }
                        }
}


def addedBridgesPage (existingBridgesParam)
{
    logDebug "addedBridgesPage: IN (OUT will not be shown)"
    logDebug "addedBridgesPage: existingBridgesParam = ${existingBridgesParam}"
    logDebug "addedBridgesPage: selectedBridgesToAdd = ${selectedBridgesToAdd}"
    
    def bridge
    def addedBridges // = []
    def addedBridge
    def resultMessages = ""
    def pairedDevices
    def pairedDevice
    
    if (selectedBridgesToAdd)
    {
        logDebug "addedBridgesPage: before adding bridges ..."
        
        addedBridges = addBridges (selectedBridgesToAdd, existingBridgesParam)
        
        logDebug "addedBridgesPage: after adding bridges ... installed bridges = ${addedBridges}"
    }
    
    // Let's build a list of bridges and paired devices
    logDebug "addedBridgesPage: existingBridgesParam = ${existingBridgesParam}"
    logDebug "addedBridgesPage: selectedBridgesToAdd = ${selectedBridgesToAdd}"
    
    addedBridges.each
    {
        logDebug "addedBridgesPage: current bridge ID = ${it.bridgeId}"
        // currentBridgeId = it.bridgeId
        addedBridge = existingBridgesParam [it.bridgeId] 
        logDebug "addedBridgesPage: addedBridge found = ${addedBridge}"
        
        resultMessages += "&bull; Nuki bridge '${it.label}'${it.installationSuccess ? " successfully installed" : ": installation failed - please check system log for more information"}"
        
        if (it.installationSuccess)
        {
            pairedDevices = getPairedDevices (addedBridge)
            if (pairedDevices.size () != 0)
            {
                resultMessages += "\n&nbsp;&nbsp;Paired devices:\n"
                logDebug "addedBridgesPage: pairedDevices = ${pairedDevices}"
                pairedDevices.each
                {
                    logDebug "addedBridgesPage: paired device = ${it}"
                    resultMessages += "&nbsp;&nbsp;- ${getNukiDeviceDriver (it)} '${buildNukiDeviceLabel (it)}' successfully installed\n"
                }
            }
            else
            {
                resultMessages += "\n&nbsp;&nbsp;&nbsp;&nbsp;No paired devices to this bridge\n\n"
            }
        }
        resultMessages += "\n\n"
    }
    
  
	return dynamicPage (name: "addedBridgesPage",
                        title: "",
                        install: true) \
                        {
                            standardHeader ("<b>Installation of Nuki<sup>&reg;</sup> bridges to your Hubitat Elevation<sup>&reg;</sup> hub</b>")
                            section() \
                            {
                                if (addedBridges.size() != 0)
                                {
                                    paragraph resultMessages
                                }
                                else
                                {
                                    paragraph "No Nuki<sup>&reg;</sup> bridges selected to install. Click 'Done' to finish."
                                }
                            }
                        }
}


//
//    Ask nuki.io about all Nuki bridges knwon on the same
//    network where this HE hub is installed
//
def discoverBridges () 
{
    logDebug "discoverBridges: IN"
    
    def discoveredBridges = []
    def success
    
    for (i=0; i<3; i++) // I would rather use the "3.times" loop construction (elegant, isn't it?), 
                        // but with it I can't exit the loop with "break" ... so sad 
    {
        success = false
        try 
        {
            logDebug "discoverBridges: trying to discover local bridges - try # ${i}"
            httpGet (_nukiDiscoverBridgesURL) 
            { 
                resp -> 
                    if (resp.data.errorCode == 0) 
                    {
                        logDebug "discoverBridges: got response (${resp.data})"

                        discoveredBridges = getBridgesData (resp)
                        success = true
                    }
                    else
                    {
                        logDebug "discoverBridges: pausing 5 seconds"
                        pauseExecution (5000)
                    }
            }
        }
        catch (Exception e) 
        {
            logDebug "discoverBridges: failed attempt to discover bridges - error message = ${e.message}"
        }
        
        if (success)
        {
            logDebug "discoverBridges: found (${discoveredBridges.size()}) bridges ... finishing discovery loop"
            break
        }
    }
    
    logDebug "discoverBridges: discovered bridges = ${discoveredBridges}"
    logDebug "discoverBridges: OUT"

    return discoveredBridges
}


//
//    Parse the HTTP response from nuki.io,
//    extracting the data of all existing bridges 
//    on the same network of the HE
//
def getBridgesData (response)
{
    logDebug "getBridgesData: IN"
    logDebug "getBridgesData: getting bridge data from response = ${response.data}"
    
    def bridgesData = []
    def bridgeData = [:]
    def alreadyInstalled
    def bridgeDNI
    
    response?.data?.bridges.each 
    {
        bridgeDNI = buildBridgeDNI (it)
        alreadyInstalled = getChildDevice (bridgeDNI) ? true : false
        
        bridgeData = ["bridgeId": it.bridgeId.toString(),            // took me hours and hours to figure it out ... need to convert int to string!
                      "dni": bridgeDNI, 
                      "ip": it.ip,
                      //"mac": getDottedMacAddress (it.ip),
                      "port": it.port, 
                      "dateUpdated": it.dateUpdated, 
                      //"token": getBridgeToken (it),
                      "label": buildBridgeLabel (it),
                      "alreadyInstalled" : alreadyInstalled]
        bridgesData << bridgeData
        
        logDebug "getBridgesData: bridge added to list: ${bridgeData}"
    }
    
    logDebug "getBridgesData: total of ${bridgesData.size ()} bridge(s) found"
    logDebug "getBridgesData: current list of bridges = ${bridgesData}"
	logDebug "getBridgesData: OUT"
    
    return bridgesData
}


//
//    Get the bridge token asking the bridge itself
//
//    Note: User must press bridge button to allow it to provide it's token
//
def getBridgeToken (bridge)
{
    logDebug "getBridgeToken: IN"
    logDebug "getBridgeToken: getting token for bridge = ${bridge}"
    
    def bridgeToken = ""

    def httpRequest = "${buildBridgeURL (bridge)}/auth"
    logDebug "getBridgeToken: httpRequest = ${httpRequest}"
    
    try
    {
        httpGet (httpRequest) 
        { 
            resp -> 
                if (resp.data.success) 
                {
                    logDebug "getBridgeToken: got response (${resp.data})"
                    logDebug "getBridgeToken: before getting bridge token"
                    bridgeToken = resp.data.token
                    logDebug "getBridgeToken: after getting token = ${resp.data.token}"
                }
                else
                {
                    logDebug "getBridgeToken: pausing 5 seconds"
                    pauseExecution (5000)
                }
        }
    }
    catch (java.net.SocketTimeoutException e)
    {
        logWarn "Nuki Smart Lock 2.0 Integration failed"
        throw new Exception ("Button at Bridge with ID = ${bridge.bridgeId} not pressed. Installation failed.")
    }

    logDebug "getBridgeToken: bridge token = ${bridgeToken}"
    logDebug "getBridgeToken: OUT"

    return bridgeToken
}


//
// Add all selected bridges to this HE hub
//
def addBridges (bridgesToAdd, existingBridges)
{
    logDebug "addBridges: IN"
    logDebug "addBridges: bridges IDs to install = ${bridgesToAdd}"
    logDebug "addBridges: existing bridges = ${existingBridges}"

    def addedBridges = []
    def addedBridge = [:]
    
    def bridge
    
    bridgesToAdd.each 
    { 
        bridgeId ->
            //logDebug "addBridges: checking if user wants to install bridge with bridgeId = ${bridgeId}"
            bridge = existingBridges [bridgeId]
            logDebug "addBridges: let's install bridge = ${bridge}"

            addedBridge = addBridge (bridge)
            addedBridges << addedBridge
    }
    
    logDebug "addBridges: installed bridges = ${addedBridges}"
    logDebug "addBridges: OUT"
    
    return addedBridges
}


//
// Add a bridge and return the operation result
//
def addBridge (bridge)
{
    logDebug "addBridge: IN"
    logDebug "addBridge: installing bridge = ${bridge}"
    
    def bridgeAddSuccess
    def pairedDevices = [:]
    def addedBridge = [:]
    def bridgeDNI
    
    bridge.token = getBridgeToken (bridge)
    pairedDevices = getPairedDevices (bridge)
    bridgeDNI = buildBridgeDNI (bridge)
    
    // Let's check first if this bridge is already installed
    if (bridge.alreadyInstalled)
    {
        // It's flagged as already installed ... let's uninstall it and its paired devices
        def installedBridge = getChildDevice (bridgeDNI)

        logInfo "${app.name}: Bridge '${bridge.label}' was previously installed and will be uninstalled now to allow it to be installed again."          

        deleteChildDevice (bridgeDNI)        
    }

    def deviceData = [:]
    deviceData.Token = bridge.token
    deviceData.BridgeId = bridge.bridgeId
    deviceData.IP = bridge.ip
    deviceData.Mac = getDottedMacAddress (bridge.ip)
    deviceData.Port = bridge.port
    deviceData.Label = buildBridgeLabel (bridge)
    deviceData.PairedDevices = pairedDevices
//    deviceData.DebugLoggingRequired = debugLogging    // if this app is running with debugLogging, the child device will run with it too

    Map bridgeProperties = [:]
    bridgeProperties.label = deviceData.Label
    bridgeProperties.name = _nukiDriverNameBridge
    bridgeProperties.data = deviceData
    bridgeProperties.isComponent = false

    try 
    {
        logDebug "addBridge: trying to install bridge with bridgeId = ${bridge.bridgeId}"
        addChildDevice (_nukiNamespace,               // namespace - must be the same for this app and driver
                        _nukiDriverNameBridge,        // typeName = driver name of the child device - must have been previously installed into this HE hub
                        bridgeDNI,                    // Device Network Id
                        location.hubs[0].id,          // This HE hub
                        bridgeProperties)
        // if we pass through here, it means that the bridge was correcly added. Let's flag it!
        //logInfo "Nuki bridge '${deviceData.Label}' successfully installed."
        logDebug "addBridge: bridge with bridgeId = ${bridge.bridgeId} successfully installed"

        bridgeAddSuccess = true
    }
    catch (com.hubitat.app.exception.UnknownDeviceTypeException e)
    {
        throw new Exception ("Failed to install bridge with bridgeId = ${bridge.bridgeId}. Driver (${_nukiDriverNameBridge}) not installed on this Hubitat Elevation hub; install it before attempting to run this app again")
    }
    catch (error) 
    {
        logDebug "addBridge: Failed to install bridge with bridgeId = ${bridge.bridgeId}. Error = ${error}"

        throw new Exception ("Failed to install bridge with bridgeId = ${bridge.bridgeId}. Error = ${error}")
    }
 
    if (bridgeAddSuccess)
    {
        pairedDevices = getPairedDevices (bridge)
    }
    
    addedBridge = bridge.clone ()
    addedBridge.installationSuccess = bridgeAddSuccess       // new value
    addedBridge.pairedDevices = pairedDevices.clone()        // new value
   
    logDebug "addBridge: installed bridge = ${addedBridge}"
    logDebug "addBridge: OUT"

    return addedBridge
}


//
// Get all Nuki devices paired to a bridge
//
def getPairedDevices (bridge)
{
    logDebug "getPairedDevices: IN"
    logDebug "getPairedDevices: discovering all Nuki devices paired to the bridge with ID = ${bridge.bridgeId}"
    
    def pairedDevices = []
  
    // First, let's ask the Nuki bridge the devices paired to it
    def httpRequest = "${buildBridgeURL (bridge)}/list?token=${bridge.token}"
    
    logDebug "getPairedDevices: httpRequest = '${httpRequest}'"

    httpGet (httpRequest) 
    {
        resp -> 
        resp.data?.each
        {
            logDebug "getPairedDevices: Storing information about paired device to bridge '${bridge.bridgeId}': ${it}"

            pairedDevices << it
        }
    }
    
    logDebug "getPairedDevices: pairedDevices = ${pairedDevices}"
    logDebug "getPairedDevices: OUT"
    
    return pairedDevices
}


//==================================================
//    Support stuff
//==================================================

//
// Build a bridge label
//
def buildBridgeLabel (bridge)
{
    logDebug "buildBridgeLabel: IN"

    def lbl = "${_nukiDriverNameBridge} (${bridge.bridgeId})"
    
    logDebug "buildBridgeLabel: bridge label = ${lbl}"
    logDebug "buildBridgeLabel: OUT"

    return lbl
}


//
//    Build a DNI (Device Network Identifier) based on the bridge ID
//
def buildBridgeDNI (bridge)
{
    logDebug "buildBridgeDNI: IN"
    
    def dni = getMacAddress (bridge.ip)

    logDebug "buildBridgeDNI: DNI = ${dni}"
    logDebug "buildBridgeDNI: OUT"
    
    return dni
}

//
// Get mac address from ip address
//
def getMacAddress (ip)
{
    logDebug "getMacAddress: IN"
    logDebug "getMacAddres: IP = ${ip}"
	
    def macAddress = getMACFromIP (ip)
    
    logDebug "getMacAddress: macAddress = ${macAddress}"
    logDebug "getMacAddress: OUT"
    
    return macAddress
}


//
// Build a dotted notation mac address string
//
def getDottedMacAddress (ip)
{
    logDebug "getDottedMacAddress: IN"

    def macAddress = getMacAddress (ip).toLowerCase()
    def standardMacAddress = macAddress.substring ( 0, 2) + ":" + 
                             macAddress.substring ( 2, 4) + ":" + 
                             macAddress.substring ( 4, 6) + ":" + 
                             macAddress.substring ( 6, 8) + ":" + 
                             macAddress.substring ( 8,10) + ":" +
                             macAddress.substring (10)
    
    logDebug "getDottedMacAddress: dotted mac address = ${macAddress} / ${standardMacAddress}"
    logDebug "getDottedMacAddress: OUT"

    return standardMacAddress
}


//
// Get Nuki bridge url
//
def buildBridgeURL (bridge)
{
    logDebug "buildBridgeURL: IN"
    
    def bridgeURL
    
    if (bridge?.IP)
    {
        bridgeURL = "http://${bridge.IP}:${bridge.Port}"
    }
    else
    {
        bridgeURL = "http://${bridge.ip}:${bridge.port}"    
    }
    
    logDebug "buildBridgeURL: bridge URL = ${bridgeURL}"
    logDebug "buildBridgeURL: OUT"
    
    return bridgeURL
}


//
// Build a DNI (Device Network Identifier) based on the Nuki device ID
//
def buildDeviceDNI (nukiDevice)
{
    logDebug "buildDeviceDNI: IN"
    logDebug "buildDeviceDNI: building DNI for device = ${nukiDevice}"
    
    String deviceDNI = "Nuki ${nukiDevice.nukiId}"
    logDebug "buildDeviceDNI: deviceDNI = ${deviceDNI}"
    
    logDebug "buildDeviceDNI: OUT"
    
    return deviceDNI
}


//
// Build a Nuki device label
//
def buildNukiDeviceLabel (nukiDevice)
{
    logDebug "buildNukiDeviceLabel: IN"
    
    def deviceLabel
    
    switch (nukiDevice.deviceType)
    {
        case "0":
            deviceLabel = "${nukiDevice.name}"
            break
        case "2":
            deviceLabel = "${nukiDevice.name}"
            break
        default:
            throw new Exception ("Nuki Bride driver: Method 'buildNukiDeviceLabel' - Fatal error = unsupported device type (${nukiDevice.deviceType})")
            break
    }
    
    logDebug "buildNukiDeviceLabel: OUT"

    return deviceLabel
}


//
// Get the driver name for a device
//
def getNukiDeviceDriver (nukiDevice)
{
    logDebug "getNukiDeviceDriver: IN"
    logDebug "getNukiDeviceDriver: nukiDevice = ${nukiDevice}"
    
    def deviceDriver
    
    switch (nukiDevice.deviceType)
    {
        case "0":
            deviceDriver = _nukiDriverNameLock
            break
        case "2":
            deviceDriver = _nukiDriverNameOpener
            break
        default:
            throw new Exception ("Nuki Smart Lock Integration: method 'getNukiDeviceDriver' - Fatal error: unsupported device type (${nukiDevice.deviceType})")
            break
    }
    logDebug "getNukiDeviceDriver: deviceDriver = ${deviceDriver}"
    logDebug "getNukiDeviceDriver: OUT"
    
    return deviceDriver
}


//
// Formatting stuff
//
def standardHeader (subheader)
{
    def header = "<h3 style='color: white; background-color: #ff8517; text-align: center; vertical-align: bottom; height: 30px;'><b>Nuki<sup>&reg;</sup> Smart Lock 2.0 Integration version ${_nukiIntegrationVersion}</b></h3>"
    
    if (subheader != "")
    {
        header += "<h4 style='color: #29b5fb; font-size: large;'><b>${subheader}</b></h4>"
    }
    
    section (header) 
    {
        if (subheader != "")
        {
            //paragraph "<h4 style='color: #29b5fb; font-size: large;'><b>${subheader}</b></h4>"
        }
    }
}


//
// Logging stuff
//
def appDebugLogging () { return debugLogging }

def logDebug (message) { if (debugLogging) log.debug (message) }
def logInfo  (message) { log.info (message) }
def logWarn  (message) { log.warn (message) }

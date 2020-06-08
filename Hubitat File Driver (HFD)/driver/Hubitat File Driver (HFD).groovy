/*
    Copyright 2020 Marco Felicio (maffpt@gmail.com)

    Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
    in compliance with the License. You may obtain a copy of the License at:
  
        http://www.apache.org/licenses/LICENSE-2.0
  
    Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
    on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
    for the specific language governing permissions and limitations under the License.
*/

import groovy.transform.Field

@Field static _Namespace = "maffpt.HFD"
@Field static _driverVersion = "0.1.0"

@Field static Long _afterCommandDelay = 250

metadata 
{
    definition (name: "Hubitat File Driver (HFD)", namespace: "maffpt.HFD", author: "Marco Felicio") 
    {
        capability "Switch"
        command "append", [[name: "content", type: "STRING", description: "Content to be appended to the file"]]
        command "read"
        command "write",  [[name: "content", type: "STRING", description: "Content to be written to the file"]]
        
        //attribute "fileName", "string"
        attribute "content", "string"
        //attribute "callbackURL", "string"
        attribute "operation", "enum", ["append", "read", "write"]
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

    logDebug "parse: OUT"
}


//
//
//
def parse (String description)
{
    logDebug "parse: IN"
    
    def parsedDescription = parseLanMessage (description)
    logDebug "parse: parsed description = ${parsedDescription}"
    
    logDebug "parse: OUT"
}


//
//
//
def uninstalled ()
{
    logDebug "uninstalled: IN"

    logInfo "${device.label}: File Driver '${device.label}' successfully uninstalled"
       
    logDebug "uninstalled: OUT"
}


//
//
//
def updated () 
{
    logDebug "updated: IN"

    logDebug "updated: OUT"
}


//
// 
//
def off ()
{
    logDebug "off: IN"

    logDebug "off: command/method disabled"

    logDebug "off: OUT"
}


//
// 
//
def on ()
{
    logDebug "on: IN"

    logDebug "on: command/method disabled"

    logDebug "on: OUT"
}


//
// 
//
def append (String contentToAppend)
{
    logDebug "append: IN"
    logDebug "append: contentToAppend = '${contentToAppend}'"
    
    changeState ("append", contentToAppend)

    logDebug "append: OUT"
}


//
// 
//
def read ()
{
    logDebug "read: IN"
    
    changeState ("read")

    logDebug "read: OUT"
}


//
// 
//
def read (String readValue)
{
    logDebug "read with parameter: IN"
    logDebug "read with parameter: readValue = ${readValue}"
    
    sendEvent (name: "content", value: readValue)   
    
    logDebug "read with parameter: OUT"
}


//
// 
//
def write (String contentToWrite)
{
    logDebug "write: IN"
    logDebug "write: contentToWrite = '${contentToWrite}'"
    
    changeState ("write", contentToWrite)
    
    logDebug "write: OUT"
}


//
//
//
def changeState (String operation, String content = "")
{
    logDebug "changeState: IN"
    logDebug "changeState: operation = ${operation}"
    logDebug "changeState: content = ${content}"
    logDebug "changeState: current device state = ${device.currentValue ('switch', true)}"
        
    sendEvent (name: "content", value: content)
    sendEvent (name: "operation", value: operation)
    
    if (device.currentValue ("switch", true) == "on")
    {
        logDebug "changeState: changing device state to = 'off'"
        sendEvent (name: "switch", value: "off")
    }
    else
    {
        logDebug "changeState: changing device state to = 'on'"
        sendEvent (name: "switch", value: "on")
    }
    
    switch (operation)
    {
        case "append":
        case "write":
            pauseExecution (_afterCommandDelay)
            break
        default:
            break
    }
}


def logDebug (message) { if (debugLogging) log.debug (message) }
def logInfo  (message) { log.info (message) }
def logWarn  (message) { log.warn (message) }

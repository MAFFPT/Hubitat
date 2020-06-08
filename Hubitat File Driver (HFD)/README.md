# Hubitat<small><sup>&copy;</sup></small> File Driver (HFD)

# Introduction

This driver was developed to allow other developers to read/write/append files at the brand new (BETA) and powerfull local storage in the Hubitat<small><sup>&copy;</sup></small> Elevation hub.

It is intended to be a "provisory" solution, while Hubitat moves ahead with the BETA and awards us with a full API to access files from apps and drivers.

**NOTE:** This solution itself is a BETA version - users input will be expected and very welcome!


# HFD

## Components

This solution uses two components:

- A custom driver [Hubitat File Driver (HFD)](https://github.com/MAFFPT/Hubitat/blob/master/Hubitat%20File%20Driver%20(HFD)/driver/Hubitat%20File%20Driver%20(HFD).groovy)

- A Rule Machine rule [HFD base rulez9(https://github.com/MAFFPT/Hubitat/tree/master/Hubitat%20File%20Driver%20(HFD)/rule)

## Working process

- A custom driver is used to create a Virtual Device (VD), specific to access a particular file. 

  ### What? A Virtual Device for EACH file that I want to access?

I am afraid that it is the way it will work ... at least for now.

I am aware that creating a VD for each file is cumbersome. However, so far, it is the only way I have found to implement this solution. The problem that caused this is the fact that I could not find a way of referencing a file name as a variable, since a file reference is implemented as variable itself.


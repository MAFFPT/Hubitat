# Apps & drivers

## Contents

Holds all the code for Nuki Smart Lock 2.0<sup>&copy;</sup> apps and drivers.

## Organization

This folder is divided into two subfolders, **production** and **debug**, with slightly between them.

Let's see it ...

### 'debug' subfolder

This folder contains all apps and drivers with a lot - I mean, a lot, a **massive** lot - of debugging code to help **you** help **me** to gather information when a problem is found.

The debugging information will be logged - in your Hubitat Elevatiion hub system log - only when you toogle the "debug info logging" option at the main page of the apps/drivers. If you do not activate the "debug info logging" option, no debugging information will be logged whatsoever.

You may use this code as your production code. However, since there is a lot - did I mention a **massive** lot before? - of debugging code imbebbed into the apps/drivers code, it not only increases the execution time and may impact negatively the performance of your HE hub, but also increases the total code size.

### 'production' subfolder

This folder contains all apps and drivers striped off all the debugging code and even the "debug info logging" option - why keep it if it would be meaningless and produces nothing at all?

As I said before, this version of apps and drivers is meant to decrease possible overhead of the debugging code over your HE hub, even when the "debug info" is not enabled. I know that the overhead is minimum, but, if you add a lot of minimuns, it would not be a "minimum" anymore ...

But I tell you one more reason to have a production and debug version: the debug version code is ugly ... and I like my code to look nice from inside out. My therapist says that I should keep doing this as an occupational therapy so it helps me not to get back to the mental institution ... 🤪

### Wich app & drivers version should I use?

I *strongly* suggest you to use the production version and install the debugging versions only when a problem is found.

However, it is your choice wich one you want to use.

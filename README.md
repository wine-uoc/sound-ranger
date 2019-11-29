# Sound Localizer

SoundLocalizer

This work takes part on a social study about the interactions during a team meeting. It aims to analyse the distance between two people in a room while one is speaking to the other for example.
My objective was to create an Android application that calculates the distance between two smartphones.

Functionnement :
	The first smartphone sends an none audible signal to the second smartphone. The second one detects the signal and sends back to the first smartphone that detects it and records the last of this exchange. With the known speed of sound, we can calculate the distance between the two smartphones.

The detection  is made by the Fast Fourier Transform that decompose the signal and identifies the frequency that we look for.


Bibliography :
GraphView library: https://github.com/jjoe64/GraphView/wiki/Simple-graph
Fast Fourier Transform library : https://github.com/dasaki/android_fft_minim
Wave file library : http://www.edumobile.org/android/audio-recording-in-wav-format-in-android-programming/

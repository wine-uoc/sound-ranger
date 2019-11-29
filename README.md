# Sound Localizer

SoundLocalizer

This work takes part on a social study about the interactions during a team meeting. It aims to analyse the distance between two people in a room while one is speaking to the other for example.
My objective was to create an Android application that calculates the distance between two smartphones.

Functionnement :
	The first smartphone sends an none audible signal to the second smartphone. The second one detects the signal and sends back to the first smartphone that detects it and records the last of this exchange. With the known speed of sound, we can calculate the distance between the two smartphones.

The detection  is made by the Fast Fourier Transform that decompose the signal and identifies the frequency that we look for.


The problem :
- To calculate the last of the sound's way : the idea to calculate this time was to calculate the whole time of the exchange T and to multiplicate by the sound’s speed V to have the distance D. To subtract the time of treatment C, I suppose to measure the time when the two smartphones were touched and to remove this time from the total.
	D = V x (T-C) 
However, the time of treatment is changing due to the step copying of the buffer in an audio file and treat it by the Fast Fourier Transform thread. A solution would be to access the buffer in the recording function and to analyse it when it is full. However, this buffer is not compatible with my Fast Fourier Transform function. 


Bibliography :
GraphView library: https://github.com/jjoe64/GraphView/wiki/Simple-graph
Fast Fourier Transform library : https://github.com/dasaki/android_fft_minim
Wave file library : http://www.edumobile.org/android/audio-recording-in-wav-format-in-android-programming/

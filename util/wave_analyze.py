from os.path import dirname, join as pjoin
from scipy.io import wavfile
import scipy.io
import matplotlib.pyplot as plt
import numpy as np

wav_fname = "../material/mytestspeech.wav"
samplerate, data = wavfile.read(wav_fname)
length = data.shape[0] / samplerate

print(f"length = {length}s, samplerate={samplerate}")
if len(data.shape) > 1:
    print(f"number of channels = {data.shape[1]}")

time = np.linspace(0., length, data.shape[0])
plt.plot(time, data[:], label="1st channel")
plt.legend()
plt.xlabel("Time [s]")
plt.ylabel("Amplitude")
plt.show()
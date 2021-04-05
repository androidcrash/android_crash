# Code Repository
## Monitor Infrastructure
   The monitor infrastructure is implemented in [CrashCapturer.zip](https://github.com/androidcrash/android_crash/blob/main/code/CrashCapturer.zip). In practice, our CrashCapturer references an open source crash collection framework (xCrash on github)
## Root Cause Analysis Pipeline
   Our root cause analysis pipeline is implemented in [preprocess.py](https://github.com/androidcrash/android_crash/blob/main/code/preprocess.py) and [clustering.py](https://github.com/androidcrash/android_crash/blob/main/code/clustering.py). Here, our analysis algorithm references the [ReBucket](https://www.microsoft.com/en-us/research/publication/rebucket-a-method-for-clustering-duplicate-crash-reports-based-on-call-stack-similarity/) algorithm.
   
## Our Countmeasures
   Our countmeasures include a novel class in SingletonPlus.java and a routine in [SafeNewDemo.java](https://github.com/androidcrash/android_crash/blob/main/code/SafeNewDemo.java). We will release the SingletonPlus.java in couple of days.

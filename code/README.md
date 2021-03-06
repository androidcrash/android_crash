# Code Repository
## Monitor Infrastructure
   The monitor infrastructure is implemented based on [CrashCapturer](https://github.com/androidcrash/android_crash/tree/main/code/CrashCapturer), a user-space tool for capturing
app crash events in a timely and comprehensive manner. In practice, our CrashCapturer references an open source crash collection tool (xCrash on github)

   
## Root Cause Analysis Pipeline
   Our root cause analysis pipeline is implemented in [preprocess.py](https://github.com/androidcrash/android_crash/blob/main/code/preprocess.py) and [clustering.py](https://github.com/androidcrash/android_crash/blob/main/code/clustering.py). Here, our analysis algorithm references the [ReBucket](https://www.microsoft.com/en-us/research/publication/rebucket-a-method-for-clustering-duplicate-crash-reports-based-on-call-stack-similarity/) algorithm.
   
## Our Countmeasures
   Our countmeasures include a novel class SingletonPlus in [SingletonPlus.java](https://github.com/androidcrash/android_crash/blob/main/code/SingletonPlus/app/src/main/java/com/example/singletonplus/SingletonPlus.java) and a routine in [SafeNewDemo.java](https://github.com/androidcrash/android_crash/blob/main/code/SafeNewDemo.java). Moreover, we also release a test demo in the folder [SingletonPlus](https://github.com/androidcrash/android_crash/blob/main/code/SingletonPlus/).
   
## For Developers
  Our code are MIT licensed. Please adhere to the corresponding open source policy when applying modifications and commercial uses.

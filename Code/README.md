All code should be stored in this folder.

# Build instructions
[Command Line Help](Iperf3_network_tester/commandLineBuildApp.md)

# Source Code Map

## Top Level
```

Iperf3_network_tester/
├── AGENTS.md
├── app
├── build
├── build.gradle.kts
├── commandLineBuildApp.md
├── gradle
├── gradle.properties
├── gradlew
├── gradlew.bat
├── IPERF3_EXE.md
├── local.properties
├── planning.md
├── README.md
├── session-ses_2ba1.md
└── settings.gradle.kts
```
## Code Level
```
Iperf3_network_tester/app/src/main/java/
app/src/main/java
└── edu
    └── bu
        └── cs683_jabramson_project
            └── iperf3_network_tester
                ├── Constants.kt
                ├── Iperf3Application.kt
                ├── MainActivity.kt
                ├── model
                │   ├── Iperf3Parameters.kt
                │   └── Iperf3ResultsData.kt
                ├── runner
                │   └── iperf3Runner.kt
                ├── ui
                │   └── theme
                │       ├── Color.kt
                │       ├── mesloFontFamily.kt
                │       ├── Theme.kt
                │       └── Type.kt
                ├── utils
                │   ├── findIperf3Binary.kt
                │   └── MonitorIPerf3Output.java
                ├── view
                │   └── Iperf3View.kt
                └── viewmodel
                    └── Iperf3RunViewModel.kt

12 directories, 14 files
```
## Test Level
```
[ 952]  Iperf3_network_tester/app/src/test/
└── [ 856]  java
    └── [ 760]  edu
        └── [ 664]  bu
            └── [ 568]  cs683_jabramson_project
                └── [ 472]  iperf3_network_tester
                    └── [ 376]  ExampleUnitTest.kt
```
## Android Test Level
```
[1.3k]  Iperf3_network_tester/app/src/androidTest/
└── [1.2k]  java
    └── [1.1k]  edu
        └── [1.0k]  bu
            └── [ 923]  cs683_jabramson_project
                └── [ 827]  iperf3_network_tester
                    └── [ 731]  ExampleInstrumentedTest.kt
```

Iperf3_network_tester/
├── AGENTS.md
├── app
├── build
├── build.gradle.kts
├── commandLineBuildApp.md
├── gradle
├── gradle.properties
├── gradlew
├── gradlew.bat
├── IPERF3_EXE.md
├── local.properties
├── planning.md
├── README.md
├── session-ses_2ba1.md
└── settings.gradle.kts

4 directories, 12 files

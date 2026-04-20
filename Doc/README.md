All documents should be stored in this folder.

# Documentation Directory Organization

```
.
├── AI
│   ├── 01_question.md
│   ├── 02_answer.md
│   ├── 03_question.md
│   ├── android_icon_steps.json
│   ├── android_icon_steps.md
│   └── pre-deploy.md
├── android phone screen shots
│   ├── CleanShot 2026-04-19 at 18.06.55.png
│   ├── CleanShot 2026-04-19 at 18.07.29.png
│   └── motorola 2023 android phone.png
├── Attempt feedback.docx
├── BAK
│   ├── build.gradle.kts.emulator
│   ├── CS683_AbramsonJerold_FinalSubmission_iperf3_network_tester.docx
│   ├── CS683_AbramsonJerold_FinalSubmission_iperf3_network_tester.pdf
│   ├── CS683_AbramsonJerold_iperf3_network_tester.docx
│   ├── CS683_AbramsonJerold_iperf3_network_tester.pdf
│   ├── Execution 1 Screen Grab.png
│   └── Execution 2 Screen Grab.png
├── CS683 Project Supplement.docx
├── CS683 Project Supplement.pdf
├── CS683ProjectTemplate.docx
├── Emulator Screen Shots
│   ├── Screenshot_20260419_175328.png
│   ├── Screenshot_20260419_175445.png
│   ├── Screenshot_20260419_175614.png
│   ├── Screenshot_20260419_175702.png
│   ├── Screenshot_20260419_175709.png
│   ├── Screenshot_20260419_175716.png
│   ├── Screenshot_20260419_175804.png
│   ├── Screenshot_20260419_175850.png
│   ├── Screenshot_20260419_175921.png
│   ├── Screenshot_20260419_175928.png
│   ├── Screenshot_20260419_175932.png
│   ├── Screenshot_20260419_175935.png
│   └── Screenshot_20260419_175937.png
├── images
│   ├── Analti.png
│   ├── android_iperf3_architecture.svg
│   ├── appRunning.png
│   ├── img.png
│   ├── iperf3_test_execution_flow.svg
│   ├── logcatoutput.png
│   ├── Uncle-Tools-iperf3-grab2.png
│   ├── Uncle-Tools-iperf3-grab3.png
│   └── Uncle-Tools-iperf3.png
├── iperf3Bundling
│   ├── AGENTS.md
│   ├── gradleTips.md
│   └── modelCard.txt
├── logCatOutput.md
└── README.md

7 directories, 47 files
```
# Project Code
* [iPerf3 Network Performance Measurement Tool](../Code/README.md)

## References
In order to utilize this application, a suitable version of the iperf3 binary executable for Android is required.

* I have bundled the iperf3 binaries along with the application and confirmed that they are properly put in the application private sandbox during application installation.

* There is a broader architectural concern related to running arbitrary binaries on Android due to SELinux.

* Some details on this are in the project report.

* A more comprehensive discussion is included in the videos.

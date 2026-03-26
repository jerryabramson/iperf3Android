# iPerf3 Network Performance Measurement Tool

 - **Jerold (Jerry) Abramson**

# Project Assignment Submission
* [Jerry Abramson's GitHub Link Project Code](../Code/README.md)


## Overview 

The application being developed will utilize the de-facto network protocol tool for testing network speeds between a client and a server.

* **Note**: The application will only be implementing the client-side functionality of iPerf3, it is assumed that an iPerf3 server is running on another accessible via the Android client.


## More details

* For my project I am implementing a network performance monitor based on the iPerf3 protocol.
* Although there are a couple of Android apps that already partially implement this, one of them is terribly complex, quite expensive,  (Analti), more focused on WiFi and internet performance monitoring.
* There is also a very old version that is nothing more then the text based version of the existing 'iperf3' application that runs in a console terminal window.
* There is a fairly nice version that runs on iOS, but that has not been ported to Android.
- *For some context on iPerf3 is available* → [here](https://iperf.fr/iperf-doc.php)

### Motivation
* I utilize network testing tools as part of my "home-labb'ing" hobby.
* One of the tools I am constantly utilizing is based on the iPerf3 protocol, which is defined here:

### Purpose
* Provide the ability to test WiFi signal strength using an Android phone.
### Potential Users
* iPerf3 is a well-known tool for networking professionals.
* My goal is to launch this application on the Android Play Store over the summer for a one-time purchase price of: $0.99
*(This section should be completed in __iteration 0__ as part of your proposal. It can be modified in later iterations. )

### Related Work
* I have developed a Java text-based console program that runs the iPerf3 native executable in the background, and provides performance details in a asyncronous manner.

* Here are a few samples:
#### Startup
```
 newTestBandWidth.sh pve -R

[Sun Mar 15 21:33:48 EDT 2026]               Executing:
iperf3 command-line:
 ==> '/opt/local/bin/iperf3' '--forceflush' '--connect-timeout' '3000' '-c' 'pve' '-O 2' '-P 8' '-t' '10' '-R' ''
Running: [      *

```

#### Completion
```
[Sun Mar 15 21:33:48 EDT 2026]               Executing:
iperf3 command-line:
==> '/opt/local/bin/iperf3' '--forceflush' '--connect-timeout' '3000' '-c' 'pve' '-O 2' '-P 8' '-t' '10' '-R' ''
Running: [          *
Connecting to host pve, port 5201
Reverse mode, remote host pve is sending
Local Host/IP: 192.168.1.10 remote Host/IP: 192.168.1.7 Remote Port: 5201
Results:   0.00-10.00      *          ]      2.33  Gbits/sec  sender
        => 0.00-10.00                        2.33  Gbits/sec  receiver
[Avg]             2.33  Gbits/sec
[Min]             1.16  Gbits/sec
[Max]             2.33  Gbits/sec
──────────────────────────────────────────────────────────────────────────────
Return Code: 000 [Avg=2.33 Gbits/sec]
```


### Similar Applications
There are a few implementations of this tool on the Android play store.

#### These are listed below:
| App | Description                  | Play Store Link                                                                              |
|---|------------------------------|----------------------------------------------------------------------------------------------|
| Analti | *analiti Networking Experts* | [link](https://play.google.com/store/apps/details?id=com.analiti.fastest.android) 
| iperf3 | *Uncle Tools*                | [link](https://play.google.com/store/search?q=iperf3&c=apps)                      |

#### There is an iOS application that has some similarities to my project deliverable, but it has not been ported to Android.
* I will be using this opportunity to develop a Mobile Android application with similar options but a cleaner<br> user interface with some of the options from my<br> 
*`     newTestBandWidth.sh`*<br> application.

---
## Requirement Analysis and Testing
### Requirement 1

| Title                                                                                 | prototype of remote IP connection (**ESSENTIAL**)                                                                           |
|---------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------|
| Description                                                                           | A hard-coded remote IP address will be used as the remote iPerf3 server                                                     |
| Acceptance Tests                                                                      | runs a fixed loop of 10 iperf3 intervals over TCP/IP                                                                        |
| Test Results                                                                          | `[Avg]             2.33  Gbits/sec`                                                                                         |
| Mockup                                                                                | Text based output ──→ `iperf3 -c 172.236.101.251: local 192.168.1.10 connected to 172.236.101.251 ─→ Result: 940 Mbits/sec` |
| Status                                                                                | Iteration 1 - Perform a simple ping to remote host<br> Iteration 2 (or possibly 3) - perform a single iperf3 test (count=1) |
---
### Requirement 2
|Title<br> | Client side host/ip address entry (*ESSENTIAL*)                                                                                              |
|---|----------------------------------------------------------------------------------------------------------------------------------------------|
|Description| The user shall be able to type in a host/ip address                                                                                          |
|Mockups| [png](images/Uncle-Tools-iperf3-grab2.png)                                                                                                          |
|Acceptance Tests| host-name/IP Address validation                                                                                                              |
|Test Results| Positive Test: "`google.com`" **Test runs**<br>Negative Test: "`abcd.baddomain`" *Validation error*                                          |
|Status| Iteration 1 - Allow entry of any text<br>Iteration 2 - Validate remote host<br>Iteration 3 - perform iperf3 test with hard-coded-count of 10 |
---
### Requirement 3
| Title  | Input desired count of iperf3 loops (*ESSENTIAL*)                                                                           |
|------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------|
| Description                                    | The user shall be able enter a test count (i.e. 10 seconds)                                                                 |
| Mockups                                        | [png](images/Uncle-Tools-iperf3-grab2.png)                                                                                         |
| Acceptance Tests                               | Test count entry validation                                                                                                 |
| Test Results                                   | Postive: Enter `  10` - test runs for 10 iterations, Negative: Enter `  -1` - input validation fails with **error message** |                                                                                                                            |
| Status                                         | Iteration 1 - User Interface allows manual keyboard entry of count<br>Iteration 3 - iperf3 runs for specified count         |
---
### Requirement 4
|Title | Unit of measurement                                                                                                                    |
|---|----------------------------------------------------------------------------------------------------------------------------------------|
|Description| The user shall be able to specify the unit of measurement for output bandwidths                                                        |
|Mockups| Entry of `MB`, Entry of `gb`, Entry of `bytes`                                                                                         |
|Acceptance Tests| Validate rationale unit text                                                                                                           |
|Test Results| Positive: ` MB`, Negative ` foobar`                                                                                                    |
|Status| Iteration 1 - Allow keyboard entry of unit<br>Iteration 2 - validate unit of measurement<br>Normalize output results to specified unit |
---
### Requirement 5
|Title<br>(Essential/Desirable/Optional) |  |
|---|---|
|Description|  |
|Mockups| |
|Acceptance Tests| |
|Test Results| |
|Status| |


## Design and Implementation

I utilized the new Claude.ai capability for render of diagrams without the need for Canvas, PlantUML, or other tools.

### Layered architecture — the app is organized into four tiers from UI down to the native iperf3 binary:
* [svg](images/android_iperf3_architecture.svg)

### Test execution flow — what happens when the user taps "Run test":
* [svg](images/iperf3_test_execution_flow.svg)

*(This section should describe the basic architecture (e.g. MVC, or MVVM) and your detailed design and implementation.  This section may contain the following aspects:
- Basic architecture
- UI design and implementation
-- Activities, fragments, special widgets, etc
- Other android features 
-- Service, sensors, animations, etc
- Third party APIs
- Data Design and implementation 
-- Database schema, data storage 
- Algorithms
…
    
…
*(In iteration 0, you can provide an overview or simply  list some basic implementation features. 
In later iterations, this section should be updated to provide detailed explanation on how you implement your requirements. You shall provide some explanation as well as supporting evidence, such as sample code snippets (or the file name and line numbers of the code you try to explain). In particular, if you used any features that are not discussed in the class, provide a detailed explanation here.)*

## Project Structure
*(Please provide a screenshot(s) of your current project structure, which should show all the packages, kotlin/java files and resource files in your project. You should also highlight any files/packages you have changed, added/deleted in this iteration compared with the previous iteration. __This is not needed for iteration 0__)*
    
## Timeline

*(Please provide  a summary of the requirements implemented and Android/third party components used in the past and current iterations, and the plan in the future iteration. __This is needed for every iteration including iteration 0.__ In iteration 0, you shall give a plan for __all future iterations__. In later iterations, you shall update it according to your progress such as describe what you have implemented in current iteration and modify the future iteration plan accordingly. The last two columns on the right are only needed if your project is a group project. )*

|Iteration | Application Requirements (Eseential/Desirable/Optional) | Android Components and Features| member 1 contribution/tasks| member 2 contribution/tasks|
|---|---|---|---|---|
|1| | | | |
|2| | | | |
|3| | | | |

## AI Usage Log

|Tool | Task | Evaulation| Links or Prompt History|
|---|---|---|---|
|| | | |
|| | | |
|| | | |
## Future Work (Optional)
*(This section can describe possible future works. Particularly the requirements you planned but didn’t get time to implement, and possible Android components or features to implement them. 
This section is optional, and you can include this section in the final iteration if you want.)*

    
##Project Demo Links
*(For on campus students, we will have project presentations in class. __For online students, you are required to submit a video of your project presentation which includes a demo of your app and explanation of your implementation.__ You can use Kaltura to make the video and then submit it on blackboard. Please check the following link for the details of using Kaltura to make and submit videos on blackboard. You can also use other video tools and upload your video to youtube if you like: https://onlinecampus.bu.edu/bbcswebdav/courses/00cwr_odeelements/metcs/cs_Kaltura.htm  )*


## References
In order to utilize this application, a suitable version of the iperf3 binary executable for Android is required.

I have provided a small script for this purpose, but the best approach is to follow the instructions here:
https://github.com/davidBar-On/android-iperf3/

*(Please list all your references here)*

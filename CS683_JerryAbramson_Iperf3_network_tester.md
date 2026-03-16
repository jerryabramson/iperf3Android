# iPerf3 Network Performance Measurement Tool

 - **Jerold (Jerry) Abramson**

## Overview 

The application being developed will utilize the de-facto network protocol tool for testing network speeds between a client and a server.

The application will only be implementing the client-side functionality of iPerf3, it is assumed that an iPerf3 server is running on another accessible via the Android client.


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

* Startup
```
 newTestBandWidth.sh pve -R

[Sun Mar 15 21:33:48 EDT 2026]               Executing:
iperf3 command-line:
 ==> '/opt/local/bin/iperf3' '--forceflush' '--connect-timeout' '3000' '-c' 'pve' '-O 2' '-P 8' '-t' '10' '-R' ''
Running: [      *

```

* Completion
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
These are listed below:
1. Analti
2. iperf3NamedSomething


## Requirement Analysis and Testing 

*(This section should clearly describe all features/requirements that you __plan to implement or have implemented__ for your application. You should separate them into three categories: essential, desirable and optional.
 
|Title<br>(Essential/Desirable/Optional) |  |
|---|---|
|Description|  |
|Mockups| |
|Acceptance Tests| |
|Test Results| |
|Status| |

For example: 

|Title(Essential/Desirable/Optional)<br> |View project details (Essential)|
|---|---|
|Description|As a user, I want to view the details of a project so that I can have a better idea about that project.  |
|Mockups|You can put one or more mockups here. |
|Acceptance Tests|Given a project list is shown on the screen, <br> When the user clicks on one project on the list, <br>Then the project details will be displayed on the screen, including project title, brief description, implementation stack, authors, keywords, project links, etc.|
|Test Results| You shall provide some screenshots of the execution result.|
|Status|Iteration 1: implemented the project detail UI page <br> Iteration 2: Implemented User click event<br> Iteration 3: Implemented project database. Completed. |)* 


*(In Iteration 0 (project planning phase), this section should contain most essential features, some desirable features and possibly a few optional features if you want. Each feature listed in this section should have a title and a brief description, preferably using the user story template “As (a role)… I want (some feature), so that (value)...” . Each essential feature should also have at least one acceptance test, and one or multiple mockups if applicable.)*

*(In later iterations (iteration 1 to 3), this section should be updated to reflect your progress. In particular, make sure to __update the status row__ of each requirement. __Highlight each feature/requirement that you work on in the current iteration__, you should also provide some test results if it is completed or partially completed.)*


## Design and Implementation

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

*(Please list all your references here)*

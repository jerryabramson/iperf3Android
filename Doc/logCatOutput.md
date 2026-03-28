# Sample logcat output showing asyncronous ProcessBuilder Execution:
```
2026-03-28 14:01:27.810   324-459   System.out              edu...project.iperf3_network_tester  I  iperf3 binary: /bin/iperf3
2026-03-28 14:01:28.823   324-512   Iperf3Runner:           edu...project.iperf3_network_tester  D  stdout: Connecting to host 192.168.1.28, port 5201
2026-03-28 14:01:28.823   324-512   Iperf3Runner:           edu...project.iperf3_network_tester  D  stdout: Reverse mode, remote host 192.168.1.28 is sending
2026-03-28 14:01:28.823   324-512   Iperf3Runner:           edu...project.iperf3_network_tester  D  stdout: [  5] local 10.0.2.16 port 60104 connected to 192.168.1.28 port 5201
2026-03-28 14:01:28.823   324-512   Iperf3Runner:           edu...project.iperf3_network_tester  D  stdout: [ ID] Interval           Transfer     Bitrate
2026-03-28 14:01:28.823   324-512   Iperf3Runner:           edu...project.iperf3_network_tester  D  stdout: [  5]   0.00-1.00   sec  46.9 MBytes   393 Mbits/sec                  
2026-03-28 14:01:29.823   324-512   Iperf3Runner:           edu...project.iperf3_network_tester  D  stdout: [  5]   1.00-2.00   sec  48.1 MBytes   403 Mbits/sec                  
2026-03-28 14:01:30.823   324-512   Iperf3Runner:           edu...project.iperf3_network_tester  D  stdout: [  5]   2.00-3.00   sec  48.2 MBytes   405 Mbits/sec                  
2026-03-28 14:01:31.823   324-512   Iperf3Runner:           edu...project.iperf3_network_tester  D  stdout: [  5]   3.00-4.00   sec  47.5 MBytes   399 Mbits/sec                  
2026-03-28 14:01:32.823   324-512   Iperf3Runner:           edu...project.iperf3_network_tester  D  stdout: [  5]   4.00-5.00   sec  48.2 MBytes   404 Mbits/sec                  
2026-03-28 14:01:33.823   324-512   Iperf3Runner:           edu...project.iperf3_network_tester  D  stdout: [  5]   5.00-6.00   sec  47.2 MBytes   396 Mbits/sec                  
2026-03-28 14:01:34.823   324-512   Iperf3Runner:           edu...project.iperf3_network_tester  D  stdout: [  5]   6.00-7.00   sec  48.3 MBytes   406 Mbits/sec                  
2026-03-28 14:01:35.823   324-512   Iperf3Runner:           edu...project.iperf3_network_tester  D  stdout: [  5]   7.00-8.00   sec  48.5 MBytes   407 Mbits/sec                  
2026-03-28 14:01:36.823   324-512   Iperf3Runner:           edu...project.iperf3_network_tester  D  stdout: [  5]   8.00-9.00   sec  49.5 MBytes   415 Mbits/sec                  
2026-03-28 14:01:37.826   324-512   Iperf3Runner:           edu...project.iperf3_network_tester  D  stdout: [  5]   9.00-10.00  sec  49.2 MBytes   413 Mbits/sec                  
2026-03-28 14:01:37.827   324-512   Iperf3Runner:           edu...project.iperf3_network_tester  D  stdout: - - - - - - - - - - - - - - - - - - - - - - - - -
2026-03-28 14:01:37.827   324-512   Iperf3Runner:           edu...project.iperf3_network_tester  D  stdout: [ ID] Interval           Transfer     Bitrate         Retr
2026-03-28 14:01:37.827   324-512   Iperf3Runner:           edu...project.iperf3_network_tester  D  stdout: [  5]   0.00-10.00  sec   482 MBytes   404 Mbits/sec    3             sender
2026-03-28 14:01:37.827   324-512   Iperf3Runner:           edu...project.iperf3_network_tester  D  stdout: [  5]   0.00-10.00  sec   482 MBytes   404 Mbits/sec                  receiver
2026-03-28 14:01:37.827   324-512   Iperf3Runner:           edu...project.iperf3_network_tester  D  stdout: 
2026-03-28 14:01:37.827   324-512   Iperf3Runner:           edu...project.iperf3_network_tester  D  stdout: iperf Done.
```

# Screen Grab during execution (inside the `@Composable`)
[app running](appRunning.png)

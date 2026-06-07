# Remove Latent iperf3 Binary Code
**All tasks completed on `aireview3` branch**
* iperf3 now runs via JNI (`libcellularlib.so`). 
* The old subprocess-based code path that depends on the bundled iperf3 binary is dead code. 
1.  Assets (delete) - *Done*
2. Utility file (delete) *Done*
3. Dead subprocess runner (delete) *Done*
4. Build config (edit) *Done*
5. MainActivity (edit) *Done*
6. Iperf3View (edit) *Done*
7. Iperf3RunViewModel (edit) *Done* 
8. Iperf3Parameters model (edit) *Done*
9. Active Path (untouched)

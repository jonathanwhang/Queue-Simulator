# Queue-Simulator

Here we implement a discrete event simulator with multiple servers, simulating the arrival and execution of requests.

The following is the workflow of the system. First, requests arrive with rate λ and enter at server S0 which is
a single-processor system with average service time Ts0. From here, the request goes to either S1 or S2 with
probability p0,1 and p0,2, respectively. 

S1 has a single infinite queue and two processors, each with average service time Ts1. S2 has a single processor and K2 maximum queue size (this includes the request being currently served). The processor can serve a request in Ts2 time. Any request that completes processing at S1 or S2 always goes to S3. 

This is a single-processor system with service time following a distribution whose PMF is given via 6 parameters t1, p1, t2, p2, t3, p3 where ti is the time it takes to process a request, and pi is the probability that it will take ti time for a request to be processed at S3. After a request completes at S3, it is released from the system with probability p3,out, it goes back to S1 with probability p3,1, or it goes back to S2 with probability p3,2. Assume all service times for S0 − S2, as well as inter-arrival times of requests from the outside at S0 are exponentially distributed.

The main(...) function accepts 17 parameters from the calling environment (in the following order):
1. length of simulation time in milliseconds. This should be passed directly as the time parameter to the simulate(...) function.
2. average arrival rate of requests at the system λ;
3. average service time Ts0 at S0;
4. average service time Ts1 at S1;
5. average service time Ts2 at S2;
6. service time t1 at S3;
7. probability p1 of service time t1 at S3;
8. service time t2 at S3;
9. probability p2 of service time t2 at S3;
10. service time t3 at S3;
11. probability p3 of service time t3 at S3;
12. K2 maximum length of the queue expressed in number of requests at S2;
13. routing probability p0,1 that a request will go from S0 to S1;
14. routing probability p0,2 that a request will go from S0 to S2;
15. routing probability p3,out that a request will exit the system from S3;
16. routing probability p3,1 that a request will go from S3 back to S1;
17. routing probability p3,2 that a request will go from S3 back to S2;

public class Event implements Comparable<Event> {
	public double time;
	public String type;
	public String server;
	
	public Event(double ti, String ty, String serv)
	{
		this.time = ti;
		this.type = ty;
		this.server = serv;
	}

	public int compareTo(Event event)
    {
        if (this.time > event.time)
        {
            return 1;
        }
        else if (this.time < event.time)
        {
            return -1;
        }
        else
        {
            return 0;
        }
    }
	
	public void function (State sys, double time, double max_t, double avg_arr_rate, 
						  double avg_serv_s0, double avg_serv_s1, double avg_serv_s2, 
						  double[] s3_times, double[] s3_probs, int K2, double p01, 
						  double p02, double p3out, double p31, double p32)
	{
		Server0 s0 = sys.S0; Server1 s1 = sys.S1; Server2 s2 = sys.S2; Server3 s3 = sys.S3;

		if (time > max_t)
			return;

		if (this.type.equals("monitor"))
		{
			s0.tot_queue_len += s0.ReqQueue.size();
			s1.tot_queue_len += s1.ReqQueue.size();
			s2.tot_queue_len += s2.ReqQueue.size();
			s3.tot_queue_len += s3.ReqQueue.size();

			sys.monitors++;

			double timeToNextMon = Exp.getExp(avg_arr_rate);
			Event new_mon = new Event(time + timeToNextMon, "monitor", "n/a");
			Simulator.Schedule.add(new_mon);
		}

		switch(this.server)
		{
			case "0":
				s0_function(sys, time, max_t, avg_arr_rate, avg_serv_s0, avg_serv_s1, avg_serv_s2, p01, p02, K2);
				break;

			case "1":
				s1_function(sys, time, max_t, avg_arr_rate, avg_serv_s1, s3_times, s3_probs);
				break;

			case "2":
				s2_function(sys, time, max_t, avg_arr_rate, avg_serv_s2, K2, s3_times, s3_probs);
				break;

			case "3":
				s3_function(sys, time, max_t, avg_arr_rate, avg_serv_s1, avg_serv_s2, s3_times, s3_probs, p3out, p31, p32, K2);
				break;
		}
	}

	public void s0_function (State sys, double time, double max_t, double avg_arr_rate, double avg_serv_s0, 
							 double avg_serv_s1, double avg_serv_s2, double p01, double p02, int K2)
	{
		Server0 s0 = sys.S0; Server1 s1 = sys.S1; Server2 s2 = sys.S2; Server3 s3 = sys.S3;

		switch (this.type)
		{
			case "birth":
				String reqID = "R" + Integer.toString((int) sys.tot_count);
				Request req = new Request(reqID, time, 0,0,0,0);
				sys.tot_count++;

				s0.ReqQueue.add(req);
				req.arr_t = time;
				req.first_arr_t = time;

				System.out.println(req.id + " ARR: " + time);

				if (s0.ReqQueue.size() == 1)	// check to see if req can be processed right away
				{
					double process_t = Exp.getExp(1/avg_serv_s0);
					Event new_death = new Event(time + process_t, "death", "0");
					Simulator.Schedule.add(new_death);

					req.start_t = time;

					System.out.println(req.id + " START S0: " + time);
				}

				double intarr_t = Exp.getExp(avg_arr_rate);
				Event new_birth = new Event(time + intarr_t, "birth", "0");
				Simulator.Schedule.add(new_birth);
				break;

			case "death":
				Request done = s0.ReqQueue.removeFirst();
				done.finish_t = time;
				done.response_t = done.finish_t - done.arr_t;

				System.out.println(done.id + " DONE S0: " + time);

				s0.tot_response_t += done.response_t;
				s0.completed++;

				s0.util_t += done.finish_t - done.start_t;

				// sending the done request to the next server
				double nextServer = Custom.getCustom(new double[]{1.0, 2.0}, new double[]{p01, p02});
				
				// done goes to s1
				if (nextServer == 1.0)
				{
					// sending done to s1
					s1.ReqQueue.add(done);
					System.out.println(done.id + " FROM S0 TO S1: " + time);
					done.arr_t = time;

					// scheduling done's death in s1 right away if we can (1)
					if (s1.ReqQueue.size() == 1) // done is the only request in s1's queue
					{
						done.start_t = time;
					
						// deciding which processor done goes to
						double whichPro = Custom.getCustom(new double[] {1.0, 2.0}, new double[] {0.5,0.5});
						if (whichPro == 1.0)
						{
							s1.pro1 = true; done.pro = 1;
							double process_t = Exp.getExp(1/avg_serv_s1);
							Event new_death = new Event(time + process_t, "death", "1");
							Simulator.Schedule.add(new_death);

							System.out.println(done.id + " START S1,1: " + time);
						}
						else
						{
							s1.pro2 = true; done.pro = 2;
							double process_t = Exp.getExp(1/avg_serv_s1);
							Event new_death = new Event(time + process_t, "death", "1");
							Simulator.Schedule.add(new_death);

							System.out.println(done.id + " START S1,2: " + time);
						}
					}

					// scheduling done's death in s1 right away if we can (2)
					else if (s1.ReqQueue.size() == 2)	// includes done; done is the second req, but not yet assigned to a processor
					{									// this means only ONE of the processors is busy and done can still be 
														// processed right away

						if (s1.pro1 == false)
						{
							done.pro = 1;
							double process_t = Exp.getExp(1/avg_serv_s1);
							Event new_death = new Event(time + process_t, "death", "1");
							Simulator.Schedule.add(new_death);
							s1.pro1 = true;

							done.start_t = time;

							System.out.println(done.id + " START S1,1: " + time);
						}
						else
						{
							done.pro = 2;
							double process_t = Exp.getExp(1/avg_serv_s1);
							Event new_death = new Event(time + process_t, "death", "1");
							Simulator.Schedule.add(new_death);
							s1.pro2 = true;

							done.start_t = time;

							System.out.println(done.id + " START S1,2: " + time);
						}

					}
				}
				// done goes to s2
				else 
				{
					// sending done to s2
					if (s2.ReqQueue.size() >= K2)
					{
						s2.dropped++;
						System.out.println(done.id + " DROP S2: " + time);
					}
					else
					{
						s2.ReqQueue.add(done);
						System.out.println(done.id + " FROM S0 TO S2: " + time);
						done.arr_t = time;
					}
					
					// scheduling done's death in s2 right away if we can
					if (s2.ReqQueue.size() == 1)		
					{
						done.start_t = time;
	
						double process_t = Exp.getExp(1/avg_serv_s2);
						Event new_death = new Event(time + process_t, "death", "2");
						Simulator.Schedule.add(new_death);

						System.out.println(done.id + " START S2: " + time);
					}
				}

				// scheduling the next death for s0
				if (s0.ReqQueue.size() > 0)		// if something in the queue, start the next process
				{
					Request new_req = s0.ReqQueue.peek();
					new_req.start_t = time;

					System.out.println(new_req.id + " START S0: " + time);

					double process_t = Exp.getExp(1/avg_serv_s0);	// schedule new process's death
					Event new_death = new Event(time + process_t, "death", "0");
					Simulator.Schedule.add(new_death);
				}

				break;

			// case "monitor":
			// 	System.out.println("!!!!!!!!");
			// 	s0.tot_queue_len += s0.ReqQueue.size();

			// 	s0.monitors++; 

			// 	double nextMon = Exp.getExp(avg_arr_rate);
			// 	Event new_mon = new Event(time + nextMon, "monitor", "n/a");
			// 	Simulator.Schedule.add(new_mon);
		}
	}

	public void s1_function (State sys, double time, double max_t, double avg_arr_rate, double avg_serv_s1,
							 double[] s3_times, double[] s3_probs)
	{
		Server0 s0 = sys.S0; Server1 s1 = sys.S1; Server2 s2 = sys.S2; Server3 s3 = sys.S3;

		switch(this.type)
		{
			// server 1 will never have a birth

			case "death":
				Request done = s1.ReqQueue.removeFirst();

				done.finish_t = time;
				done.response_t = done.finish_t - done.arr_t;

				s1.tot_response_t += done.response_t;
				s1.completed++;

				if (done.pro == 1)
				{
					s1.pro1 = false;
					System.out.println(done.id + " DONE S1,1: " + time);
					s1.pro1_util_t += done.finish_t - done.start_t;
				}
				else
				{
					s1.pro2 = false;
					System.out.println(done.id + " DONE S1,2: " + time);
					s1.pro2_util_t += done.finish_t - done.start_t;
				}

				// sending done to next server (s3)
				s3.ReqQueue.add(done);
				System.out.println(done.id + " FROM S1 TO S3: " + time);
				done.arr_t = time;

				// scheduling done's death in s3 right away if we can
				if (s3.ReqQueue.size() == 1)
				{
					done.start_t = time;
					double process_t = Custom.getCustom(s3_times, s3_probs);
					Event new_death = new Event(time + process_t, "death", "3");
					Simulator.Schedule.add(new_death);

					System.out.println(done.id + " START S3: " + time);
				}

				// scheduling the next death for s1
				if (s1.ReqQueue.size() > 0)
				if (s1.ReqQueue.peek().pro == 0 || s1.ReqQueue.size() > 1)
				{
					// we need to remove the first request that isn't being processed by a processor
					Request new_req = s1.ReqQueue.peek();

					if (new_req.pro != 0)	// next req in queue is currently being processed
					{
						// new_req = s1.ReqQueue.get(s1.ReqQueue.size() - 2);
						Request temp = s1.ReqQueue.removeFirst();
						new_req = s1.ReqQueue.peek();	// grab next req
						s1.ReqQueue.addFirst(temp);

						new_req.start_t = time;

						if (done.pro == 1)
						{
							new_req.pro = 1; s1.pro1 = true; // !! changed at 8:58pm
							System.out.println(new_req.id + " START S1,1: " + time);
						}
						else
						{
							new_req.pro = 2; s1.pro2 = true;
							System.out.println(new_req.id + " START S1,2: " + time);
						}
					}
					else	// next req in queue is not being processed; choice between pros is 50/50
					{
						new_req.start_t = time;

						double whichPro = Custom.getCustom(new double[] {1.0,2.0}, new double[] {0.5,0.5});
						if (whichPro == 1)
						{
							new_req.pro = 1; s1.pro1 = true;
							System.out.println(new_req.id + " START S1,1: " + time);
						}
						else
						{
							new_req.pro = 2; s1.pro2 = true;
							System.out.println(new_req.id + " START S1,2: " + time);
						}
					}

					// schedule new_req's death
					double process_t = Exp.getExp(1/avg_serv_s1);
					Event new_death = new Event(time + process_t, "death", "1");
					Simulator.Schedule.add(new_death);
				}

				break;

			// case "monitor":
			// 	s1.tot_queue_len += s1.ReqQueue.size();

			// 	s1.monitors++; 

			// 	double nextMon = Exp.getExp(avg_arr_rate);
			// 	Event new_mon = new Event(time + nextMon, "monitor", "n/a");
			// 	Simulator.Schedule.add(new_mon);
		}
	}

	public void s2_function (State sys, double time, double max_t, double avg_arr_rate, double avg_serv_s2, int K2, 
							 double[] s3_times, double[] s3_probs)
	{
		// server 2 will not have births
		Server0 s0; Server1 s1 = sys.S1; Server2 s2 = sys.S2; Server3 s3 = sys.S3;

		switch(this.type)
		{
			case "death":
				Request done = s2.ReqQueue.removeFirst();
				done.finish_t = time;
				done.response_t = done.finish_t - done.arr_t;

				System.out.println(done.id + " DONE S2: " + done.finish_t);

				s2.tot_response_t += done.response_t;
				s2.completed++;

				s2.util_t += done.finish_t - done.start_t;

				// sending done to s3
				s3.ReqQueue.add(done);
				System.out.println(done.id + " FROM S2 TO S3: " + time);
				done.arr_t = time;

				// scheduling done's death in s3 right away if we can
				if (s3.ReqQueue.size() == 1)
				{
					done.start_t = time;
					double process_t = Custom.getCustom(s3_times, s3_probs);
					Event new_death = new Event(time + process_t, "death", "3");
					Simulator.Schedule.add(new_death);

					System.out.println(done.id + " START S3: " + time);
				}
				
				// scheduling the next death in s2
				if (s2.ReqQueue.size() > 0)	
				{
					Request new_req = s2.ReqQueue.peek();
					new_req.start_t = time;

					System.out.println(new_req.id + " START S2: " + time);

					double process_t = Exp.getExp(1/avg_serv_s2);
					Event new_death = new Event(time + process_t, "death", "2");
					Simulator.Schedule.add(new_death);
				}

				break;

			// case "monitor":
			// 	s2.tot_queue_len += s2.ReqQueue.size();

			// 	s2.monitors++;

			// 	double nextMon = Exp.getExp(avg_arr_rate);
			// 	Event new_mon = new Event(time + nextMon, "monitor", "n/a");
			// 	Simulator.Schedule.add(new_mon);
		}
	}

	public void s3_function(State sys, double time, double max_t, double avg_arr_rate, double avg_serv_s1, 
							double avg_serv_s2, double[] s3_times, double[] s3_probs, double p3out, double p31, 
							double p32, int K2)
	{
		Server0 s0 = sys.S0; Server1 s1 = sys.S1; Server2 s2 = sys.S2; Server3 s3 = sys.S3;

		switch(this.type)
		{
			// server 3 will never have births

			case "death":
				Request done = s3.ReqQueue.removeFirst();
				done.finish_t = time;
				done.response_t = done.finish_t - done.arr_t;

				System.out.println(done.id + " DONE S3: " + time);

				s3.tot_response_t += done.response_t;
				s3.completed++;

				s3.util_t += done.finish_t - done.start_t;

				// sending done to either out, s1, or s2
				double nextDest = Custom.getCustom(new double[] {1.0,2.0,3.0}, new double[] {p31, p32, p3out});
				
				// done goes to s1
				if (nextDest == 1.0)
				{
					// sending done to s1
					s1.ReqQueue.add(done);
					System.out.println(done.id + " FROM S3 TO S1: " + time);
					done.arr_t = time;

					// scheduling done's death in s1 right away if we can (1)
					if (s1.ReqQueue.size() == 1) // done is the only request in s1's queue
					{
						done.start_t = time;
					
						// deciding which processor done goes to
						double whichPro = Custom.getCustom(new double[] {1.0, 2.0}, new double[] {0.5,0.5});
						if (whichPro == 1.0)
						{
							s1.pro1 = true; done.pro = 1;
							double process_t = Exp.getExp(1/avg_serv_s1);
							Event new_death = new Event(time + process_t, "death", "1");
							Simulator.Schedule.add(new_death);

							System.out.println(done.id + " START S1,1: " + time);
						}
						else
						{
							s1.pro2 = true; done.pro = 2;
							double process_t = Exp.getExp(1/avg_serv_s1);
							Event new_death = new Event(time + process_t, "death", "1");
							Simulator.Schedule.add(new_death);

							System.out.println(done.id + " START S1,2: " + time);
						}
					}

					// scheduling done's death in s1 right away if we can (2)
					else if (s1.ReqQueue.size() == 2)	// includes done; done is the second req, but not yet assigned to a processor
					{									// this means only ONE of the processors is busy and done can still be 
														// processed right away
						if (s1.pro1 == false)
						{
							done.pro = 1;
							double process_t = Exp.getExp(1/avg_serv_s1);
							Event new_death = new Event(time + process_t, "death", "1");
							Simulator.Schedule.add(new_death);
							s1.pro1 = true;

							done.start_t = time;

							System.out.println(done.id + " START S1,1: " + time);
						}
						else
						{
							done.pro = 2;
							double process_t = Exp.getExp(1/avg_serv_s1);
							Event new_death = new Event(time + process_t, "death", "1");
							Simulator.Schedule.add(new_death);
							s1.pro2 = true;

							done.start_t = time;

							System.out.println(done.id + " START S1,2: " + time);
						}

					}
				}
				// done goes to s2
				else if (nextDest == 2) 
				{
					// sending done to s2
					if (s2.ReqQueue.size() >= K2)
					{
						s2.dropped++;
						System.out.println(done.id + " DROP S2: " + time);
					}
					else
					{
						s2.ReqQueue.add(done);
						System.out.println(done.id + " FROM S3 TO S2: " + time);
						done.arr_t = time;
					}

					// scheduling done's death in s2 right away if we can
					if (s2.ReqQueue.size() == 1)		
					{
						done.start_t = time;
	
						double process_t = Exp.getExp(1/avg_serv_s2);	
						Event new_death = new Event(time + process_t, "death", "2");
						Simulator.Schedule.add(new_death);

						System.out.println(done.id + " START S2: " + time);
					}
				}

				// if nextDest == 3, then nothing to do with done. done exits system. schedule next death for s3
				sys.fully_completed++;
				done.last_finish_t = time;

				sys.tot_response_t += done.last_finish_t - done.first_arr_t;

				System.out.println(done.id + " FROM S3 TO OUT: " + time);

				if (s3.ReqQueue.size() > 0)
				{
					Request new_req = s3.ReqQueue.peek();
					new_req.start_t = time;

					System.out.println(new_req.id + " START S3: " + time);

					double process_t = Custom.getCustom(s3_times, s3_probs);
					Event new_death = new Event(time + process_t, "death", "3");
					Simulator.Schedule.add(new_death);
				}

				break;

			// case "monitor":
			// 	s3.tot_queue_len += s3.ReqQueue.size();
			// 	s3.monitors++;

			// 	double nextMon = Exp.getExp(avg_arr_rate);
			// 	Event new_mon = new Event(time + nextMon, "monitor", "n/a");
			// 	Simulator.Schedule.add(new_mon);
		}
	}
}
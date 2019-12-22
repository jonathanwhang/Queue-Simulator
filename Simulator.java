import java.util.PriorityQueue;

class Simulator {
	static PriorityQueue<Event> Schedule = new PriorityQueue<Event>();
	static State sys;
	
	public static double avg_arr_rate = 0;
	public static double avg_serv_s0 = 0, avg_serv_s1 = 0, avg_serv_s2 = 0;
	public static double s3_times[] = new double[3], s3_probs[] = new double[3];
	public static int K2 = 0;
	public static double p01 = 0, p02 = 0;
	public static double p3out = 0, p31 = 0, p32 = 0;
	
	public static void simulate(double max_t) // time here denotes maxtime
	{
		State sys = new State();
		Server0 s0 = sys.S0; Server1 s1 = sys.S1; Server2 s2 = sys.S2; Server3 s3 = sys.S3;
		
		double intarr_t = Exp.getExp(avg_arr_rate);
		Event new_birth = new Event(intarr_t, "birth", "0");
		Event new_mon = new Event(intarr_t, "monitor", "n/a");
		Schedule.add(new_birth);
		Schedule.add(new_mon);
		
		double cur_t = 0;
		while(cur_t < max_t)
		{
			Event e = Schedule.remove();
			cur_t = e.time;
			e.function(sys, cur_t, max_t, avg_arr_rate, 
					   avg_serv_s0, avg_serv_s1, avg_serv_s2, 
					   s3_times, s3_probs, K2, p01, p02, p3out, p31, p32);
		}

		System.out.println();

		double s0_util = s0.util_t / max_t;
		double s0_qlen = s0.tot_queue_len / (double)sys.monitors;
		double s0_tresp = s0.tot_response_t / (double)s0.completed;

			System.out.println("S0 UTIL: " + s0_util);
			System.out.println("S0 QLEN: " + s0_qlen);
			System.out.println("S0 TRESP: " + s0_tresp);
			System.out.println();

		double s11_util = s1.pro1_util_t / max_t;
		double s12_util = s1.pro2_util_t / max_t;
		double s1_qlen = s1.tot_queue_len / (double)sys.monitors;
		double s1_tresp = s1.tot_response_t / (double)s1.completed;

			System.out.println("S1,1 UTIL: " + s11_util);
			System.out.println("S1,2 UTIL: " + s12_util);
			System.out.println("S1 QLEN: " + s1_qlen);
			System.out.println("S1 TRESP: " + s1_tresp);
			System.out.println();

		double s2_util = s2.util_t / max_t;
		double s2_qlen = s2.tot_queue_len / (double)sys.monitors;
		double s2_tresp = s2.tot_response_t / (double)s2.completed;
		int s2_dropped = s2.dropped;

			System.out.println("S2 UTIL: " + s2_util);
			System.out.println("S2 QLEN: " + s2_qlen);
			System.out.println("S2 TRESP: " + s2_tresp);
			System.out.println("S2 DROPPED: " + s2_dropped);
			System.out.println();

		double s3_util = s3.util_t / max_t;
		double s3_qlen = s3.tot_queue_len / (double)sys.monitors;
		double s3_tresp = s3.tot_response_t / (double)s3.completed;

			System.out.println("S3 UTIL: " + s3_util);
			System.out.println("S3 QLEN: " + s3_qlen);
			System.out.println("S3 TRESP: " + s3_tresp);
			System.out.println();

		double qtot = (s0.tot_queue_len + s1.tot_queue_len + s2.tot_queue_len + s3.tot_queue_len) / (double)sys.monitors;
		double tresp = sys.tot_response_t / (double)(sys.fully_completed);

			System.out.println("QTOT: " + qtot);
			System.out.println("TRESP: " + tresp);
		
		// double util0 = sys.s0_util_t / max_t;
		// double util1 = sys.s1_util_t / max_t;

		// System.out.println("UTIL 0: " + util0);
		// System.out.println("UTIL 1: " + util1);

		// double qLen0 = sys.tot_s0_queue_len / sys.monitors;
		// double qLen1 = sys.tot_s1_queue_len / sys.monitors;

		// System.out.println("QLEN 0: " + qLen0);
		// System.out.println("QLEN 1: " + qLen1);

		// double tot_resp = sys.tot_response_t / sys.completed;

		// System.out.println("TRESP: " + tot_resp);

		// int tot_redir = sys.redirected;
	
		// System.out.println("REDIRECTED: " + tot_redir);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		double max_t = Double.parseDouble(args[0]); // 1
		avg_arr_rate = Double.parseDouble(args[1]); // 2
		avg_serv_s0 = Double.parseDouble(args[2]);  // 3
		avg_serv_s1 = Double.parseDouble(args[3]);	// 4
		avg_serv_s2 = Double.parseDouble(args[4]);	// 5
		s3_times[0] = Double.parseDouble(args[5]);	// 6
		s3_probs[0] = Double.parseDouble(args[6]);	// 7
		s3_times[1] = Double.parseDouble(args[7]);	// 8
		s3_probs[1] = Double.parseDouble(args[8]);	// 9
		s3_times[2] = Double.parseDouble(args[9]);	// 10
		s3_probs[2] = Double.parseDouble(args[10]);	// 11
		K2 = Integer.parseInt(args[11]);			// 12
		p01 = Double.parseDouble(args[12]);			// 13
		p02 = Double.parseDouble(args[13]);			// 14
		p3out = Double.parseDouble(args[14]);		// 15
		p31 = Double.parseDouble(args[15]);			// 16
		p32 = Double.parseDouble(args[16]);			// 17
        
		simulate(max_t);
	}

}

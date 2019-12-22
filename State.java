import java.util.LinkedList;

public class State {
	public Server0 S0 = new Server0();
	public Server1 S1 = new Server1();
	public Server2 S2 = new Server2();
	public Server3 S3 = new Server3();

	public double tot_count = 0;
	public double tot_response_t = 0;
	public int fully_completed = 0;
	public int monitors = 0;
}
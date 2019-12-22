import java.util.LinkedList;

public class Server {
    public LinkedList<Request> ReqQueue = new LinkedList<Request>();
    
	public double tot_response_t = 0;
	public int tot_queue_len = 0;

	public int completed = 0;
	public int monitors = 0;
}
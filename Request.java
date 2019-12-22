public class Request {
	public String id;
	public double arr_t;
	public double start_t;
	public double finish_t;
	public double response_t;
	public double pro;
	
	public double first_arr_t;
	public double last_finish_t;
	
	public Request(String i, double arr, double start, double finish, double resp, double proc)
	{
		id = i;
		arr_t = arr;
		start_t = start;
		finish_t = finish;
		response_t = resp;

		pro = proc;
	}
}
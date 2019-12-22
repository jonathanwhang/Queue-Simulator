import java.lang.Math;

public class Custom {
	
	public static double getCustom(double[] outcomes, double[] probabilities)
	{
		double cumProbs[] = new double[probabilities.length];
		cumProbs[0] = probabilities[0];
		
		for (int i=1; i<probabilities.length; i++)
		{
			cumProbs[i] = cumProbs[i-1] + probabilities[i];
		}
		
		double randProb = Math.random();
		
		double ret = 0;
		for (int i=0; i<outcomes.length; i++)
		{
			if (randProb < cumProbs[i])
			{
				ret = outcomes[i];
				break;
			}
		}
		
		return ret;
    }
}
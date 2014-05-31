package at.ac.uibk.dps.biohadoop.nsgaii.algorithm;

//Look at http://en.wikipedia.org/wiki/Test_functions_for_optimization for optimizations
public class Functions {

//	ZDT4
//	public static double f1(double[] x) {
//		return x[0];
//	}
//
//	public static double f2(double[] x) {
//		return g(x) * h(f1(x), g(x));
//	}
//
//	private static double g(double[] x) {
//		double result = 0;
//
//		for (int i = 1; i < x.length; i++) {
//			result += Math.pow(x[i], 2) - 10 * Math.cos(4 * Math.PI * x[i]);
//		}
//
//		return 91 + result;
//	}
//
//	private static double h(double f1_x, double g_x) {
//		return 1 - Math.sqrt(f1_x / g_x);
//	}
//	
////	ZDT3
//	public static double f1(double[] x) {
//		return x[0];
//	}
//
//	public static double f2(double[] x) {
//		return g(x) * h(f1(x), g(x));
//	}
//	
//	private static double g(double[] x) {
//		double result = 0;
//		
//		for (int i = 1; i < x.length; i++) {
//			result += x[i];
//		}
//		
//		return 1 + 9/29 * result;
//	}
//	
//	private static double h(double f1_x, double g_x) {
//		return 1 - Math.sqrt(f1_x / g_x) - (f1_x / g_x) * Math.sin(10 * Math.PI * f1_x);
//	}	

//	ZDT2
	public static double f1(double[] x) {
		return x[0];
	}

	public static double f2(double[] x) {
		return g(x) * h(f1(x), g(x));
	}
	
	private static double g(double[] x) {
		double result = 0;
		
		for (int i = 1; i < x.length; i++) {
			result += x[i];
		}
		
		return 1 + 9/29 * result;
	}
	
	private static double h(double f1_x, double g_x) {
		return 1 - Math.pow(f1_x / g_x, 2);
	}

//	ZDT1
//	public static double f1(double[] x) {
//		return x[0];
//	}
//
//	public static double f2(double[] x) {
//		return g(x) * h(f1(x), g(x));
//	}
//	
//	private static double g(double[] x) {
//		double result = 0;
//		
//		for (int i = 1; i < x.length; i++) {
//			result += x[i];
//		}
//		
//		return 1 + 9/29 * result;
//	}
//	
//	private static double h(double f1_x, double g_x) {
//		return 1 - Math.sqrt(f1_x / g_x);
//	}

//	From PS ws2013-2014
//	public static double f1(double[] x) {
//		double result = x[0];
//		int n = x.length;
//		
//		double sum = 0;
//		double count = 0; // length of J
//		for (int j = 3; j < n; j += 2) {
//			double pow = 0.5 * (1.0 + (3 * (j - 2)) / (n + 2));
//			double term = Math.pow(x[j] - Math.pow(x[0], pow), 2);
//			sum += term;
//			count++;
//		}
//		
//		return result + (2 / count) * sum;
//	}
//
//	public static double f2(double[] x) {
//		double result = 1 - Math.sqrt(x[0]);
//		int n = x.length;
//		
//		double sum = 0;
//		double count = 0; // length of J
//		for (int j = 2; j < n; j += 2) {
//			double pow = 0.5 * (1.0 + (3 * (j - 2)) / (n + 2));
//			double term = Math.pow(x[j] - Math.pow(x[0], pow), 2);
//			sum += term;
//			count++;
//		}
//		
//		return result + (2 / count) * sum;
//	}
}

package Simulator;

/**
 * Elevator kinematics equations from Peters, R. (1995). Ideal Lift Kinematic. Elevator Technology 8. IAEE Publication
 * @author Jorge Huertas (huertas.ja@uniandes.edu.co)
 */
public class Kinematics {

	public static double ad0(double j, double t) {
		double y;
		y = Math.pow(t, 3.0);
		return j * y / 6.0;
	}

	public static double ad1(double a, double j, double t) {
		double y = Math.pow(a, 3.0);
		double b_y = Math.pow(j, 2.0);
		double c_y = Math.pow(a, 2.0);
		double d_y = Math.pow(t, 2.0);
		return (y / (6.0 * b_y) - c_y * t / (2.0 * j)) + a * d_y / 2.0;
	}

	public static double ad2(double a, double j, double vr, double t) {
		double y = Math.pow(a, 3.0);
		double b_y = Math.pow(j, 2.0);
		double c_y = Math.pow(a, 2.0);
		double d_y = Math.pow(t, 3.0);
		double e_y = Math.pow(t, 2.0);
		double f_y = Math.pow(t, 2.0);
		double g_y = Math.pow(vr, 2.0);
		double h_y = Math.pow(a, 2.0);
		double i_y = Math.pow(vr, 3.0);
		double j_y = Math.pow(a, 3.0);
		return (((((y / (6.0 * b_y) - t * c_y / (2.0 * j)) - j * d_y / 6.0) + a * e_y / 2.0) + j * vr * f_y / (2.0 * a)) - j * g_y * t / (2.0 * h_y)) + i_y * j / (6.0 * j_y);
	}

	public static double ad3(double a, double j, double v, double t) {
		double y;
		y = Math.pow(v, 2.0);
		return (-1.0 * a * v / (2.0 * j) - y / (2.0 * a)) + v * t;
	}

	public static double ad4(double a, double v, double j, double d, double t) {
		double y = Math.pow(v, 2.0);
		double b_y = Math.pow(t, 2.0);
		double c_y = Math.pow(d, 2.0);
		double d_y = Math.pow(v, 2.0);
		double e_y = Math.pow(t, 3.0);
		double f_y = Math.pow(d, 3.0);
		double g_y = Math.pow(v, 3.0);
		return (((((-1.0 * a * v / (2.0 * j) - y / (2.0 * a)) + v * t) + b_y * j * d / (2.0 * v)) - t * j * c_y / (d_y * 2.0)) - e_y * j / 6.0) + f_y * j / (g_y * 6.0);
	}

	public static double ad5(double a, double j, double v, double d, double t) {
		double y = Math.pow(v, 2.0);
		double b_y = Math.pow(a, 3.0);
		double c_y = Math.pow(d, 2.0);
		double d_y = Math.pow(v, 2.0);
		double e_y = Math.pow(a, 2.0);
		double f_y = Math.pow(j, 2.0);
		double g_y = Math.pow(a, 2.0);
		double h_y = Math.pow(t, 2.0);
		return (((((((-1.0 * a * v / (2.0 * j) - y / (2.0 * a)) - c_y * a / (d_y * 2.0)) - d * e_y / (2.0 * j * v)) - b_y / (f_y * 6.0)) + v * t) + a * d * t / v) + t * g_y / (2.0 * j)) - h_y * a / 2.0;
	}

	public static double ad6(double a, double j, double v, double d, double t) {
		double y = Math.pow(a, 3.0);
		double b_y = Math.pow(v, 2.0);
		double c_y = Math.pow(a, 2.0);
		double d_y = Math.pow(d, 2.0);
		double e_y = Math.pow(t, 3.0);
		double f_y = Math.pow(d, 3.0);
		double g_y = Math.pow(v, 3.0);
		double h_y = Math.pow(t, 2.0);
		double i_y = Math.pow(d, 2.0);
		double j_y = Math.pow(v, 2.0);
		double k_y = Math.pow(a, 2.0);
		double l_y = Math.pow(t, 2.0);
		double m_y = Math.pow(j, 2.0);
		double n_y = Math.pow(d, 2.0);
		double o_y = Math.pow(v, 2.0);
		double p_y = Math.pow(a, 2.0);
		double q_y = Math.pow(v, 2.0);
		double r_y = Math.pow(a, 2.0);
		double s_y = Math.pow(t, 2.0);
		double t_y = Math.pow(v, 3.0);
		double u_y = Math.pow(a, 3.0);
		return (((((((((((((((((-1.0 * b_y / (2.0 * a) + v * t) - j * d * v / (2.0 * c_y)) - d_y * j / (2.0 * v * a)) + e_y * j / 6.0) - a * v / (2.0 * j)) - f_y * j / (g_y * 6.0)) - h_y * j * d / (2.0 * v)) + j * i_y * t / (2.0 * j_y)) + a * t * d / v) + t * k_y / (2.0 * j)) - l_y * a / 2.0) - y / (m_y * 6.0)) - n_y * a / (o_y * 2.0)) - d * p_y / (2.0 * j * v)) + t * d * j / a) + t * q_y * j / (2.0 * r_y)) - s_y * v * j / (2.0 * a)) - j * t_y / (u_y * 6.0);
	}

	public static double av0(double j, double t) {
		double y = Math.pow(t, 2.0);
		return j * y / 2.0;
	}

	public static double av1(double a, double j, double t) {
		double y = Math.pow(a, 2.0);
		return -1.0 * y / (2.0 * j) + a * t;
	}

	public static double av2(double a, double j, double vr, double t) {
		double y = Math.pow(a, 2.0);
		double b_y = Math.pow(t, 2.0);
		double c_y = Math.pow(vr, 2.0);
		double d_y = Math.pow(a, 2.0);
		return (((-1.0 * y / (2.0 * j) + a * t) - j * b_y / 2.0) + t * vr * j / a) - c_y * j / (2.0 * d_y);
	}

	public static double av4(double j, double vr, double d, double t) {
		double y = Math.pow(t, 2.0);
		double b_y = Math.pow(d, 2.0);
		double c_y = Math.pow(vr, 2.0);
		return ((vr - j * y / 2.0) + d * j * t / vr) - b_y * j / (c_y * 2.0);
	}

	public static double av5(double a, double j, double v, double d, double t) {
		double y = Math.pow(a, 2.0);
		return ((v + a * d / v) + y / (2.0 * j)) - a * t;
	}

	public static double av6(double a, double j, double v, double d, double t) {
		double y = Math.pow(a, 2.0);
		double b_y = Math.pow(t, 2.0);
		double c_y = Math.pow(d, 2.0);
		double d_y = Math.pow(v, 2.0);
		double e_y = Math.pow(v, 2.0);
		double f_y = Math.pow(a, 2.0);
		return ((((((((v + a * d / v) + y / (j * 2.0)) - a * t) - j * d * t / v) - j * v * t / a) + j * b_y / 2.0) + c_y * j / (d_y * 2.0)) + j * d / a) + e_y * j / (f_y * 2.0);
	}

	public static double bd0(double j, double t) {
		double y = Math.pow(t, 3.0);
		return j * y / 6.0;
	}

	public static double bd1(double a, double j, double t) {
		double y = Math.pow(a, 3.0);
		double b_y = Math.pow(j, 2.0);
		double c_y = Math.pow(a, 2.0);
		double d_y = Math.pow(t, 2.0);
		return (y / (6.0 * b_y) - c_y * t / (2.0 * j)) + a * d_y / 2.0;
	}

	public static double bd2(double a, double j, double d, double t) {
		double y = Math.pow(a, 3.0);
		double b_y = Math.pow(j, 2.0);
		double c_y = Math.pow(a, 1.5);
		double d_y = Math.pow(a, 3.0);
		double e_y = Math.pow(j, 2.0);
		double f_y = Math.pow(a, 2.0);
		double g_y = Math.pow(t, 2.0);
		double h_y = Math.pow(t, 2.0);
		double i_y = Math.pow(a, 3.0);
		double j_y = Math.pow(t, 3.0);
		double k_y = Math.pow(a, 3.0);
		double l_y = Math.pow(a, 1.5);
		double m_y = Math.pow(j, 2.0);
		double n_y = Math.pow(j, 2.0);
		double o_y = Math.pow(a, 3.0);
		double p_y = Math.pow(j, 2.0);
		double q_y = Math.pow(j, 2.0);
		return ((((((((y / (12.0 * b_y) + c_y * Math.sqrt(d_y + 4.0 * d * m_y) / (12.0 * e_y)) - d / 4.0) - 3.0 * t * f_y / (4.0 * j)) + g_y * a / 4.0) + h_y * Math.sqrt (i_y + 4.0 * d * n_y) / (4.0 * Math.sqrt(a))) + Math.sqrt(o_y + 4.0 * d * p_y) * Math.sqrt(a) * t / (4.0 * j)) - j_y * j / 6.0) - t * j * d / (a * 2.0)) + d * Math.sqrt(k_y + 4.0 * d * q_y) / (12.0 * l_y);
	}

	public static double bd3(double a, double j, double d, double t) {
		double y = Math.pow(a, 3.0);
		double b_y = Math.pow(t, 2.0);
		double c_y = Math.pow(a, 2.0);
		double d_y = Math.pow(t, 2.0);
		double e_y = Math.pow(a, 3.0);
		double f_y = Math.pow(t, 3.0);
		double g_y = Math.pow(j, 2.0);
		double h_y = Math.pow(a, 3.0);
		double i_y = Math.pow(a, 1.5);
		double j_y = Math.pow(j, 2.0);
		double k_y = Math.pow(a, 3.0);
		double l_y = Math.pow(a, 1.5);
		double m_y = Math.pow(a, 3.0);
		double n_y = Math.pow(j, 2.0);
		double o_y = Math.pow(j, 2.0);
		double p_y = Math.pow(j, 2.0);
		double q_y = Math.pow(j, 2.0);
		return ((((((((-d / 4.0 + a * b_y / 4.0) - c_y * 3.0 * t / (4.0 * j)) + Math.sqrt (m_y + 4.0 * d * n_y) * Math.sqrt(a) * t / (4.0 * j)) - j * t * d / (2.0 * a)) + d_y * Math.sqrt(e_y + 4.0 * d * o_y) / (4.0 * Math.sqrt(a))) - j * f_y / 6.0) + y / (g_y * 12.0)) + Math.sqrt(h_y + 4.0 * d * p_y) * i_y / (j_y * 12.0)) + d * Math.sqrt(k_y + 4.0 * d * q_y) / (12.0 * l_y);
	}

	public static double bd4(double a, double j, double d, double t) {
		double y = Math.pow(t, 2.0);
		double b_y = Math.pow(a, 2.0);
		double c_y = Math.pow(a, 1.5);
		double d_y = Math.pow(a, 3.0);
		double e_y = Math.pow(j, 2.0);
		double f_y = Math.pow(a, 3.0);
		double g_y = Math.pow(j, 2.0);
		double h_y = Math.pow(a, 3.0);
		double i_y = Math.pow(j, 2.0);
		double j_y = Math.pow(j, 2.0);
		return ((((-d - a * y / 2.0) + Math.sqrt(h_y + 4.0 * d * i_y) * t * Math.sqrt(a) / j) + b_y * t / (2.0 * j)) - c_y * Math.sqrt(d_y + 4.0 * d * j_y) / (2.0 * e_y)) - 2.0 * f_y / (3.0 * g_y);
	}

	public static double bd5(double a, double j, double d, double t) {
		double y = Math.pow(a, 3.0);
		double b_y = Math.pow(j, 2.0);
		y += 4.0 * d * b_y;
		y = Math.pow(y, 1.5);
		b_y = Math.pow(j, 2.0);
		double c_y = Math.pow(a, 1.5);
		double d_y = Math.pow(a, 3.0);
		double e_y = Math.pow(j, 2.0);
		double f_y = Math.pow(a, 2.0);
		double g_y = Math.pow(t, 2.0);
		double h_y = Math.pow(t, 3.0);
		double i_y = Math.pow(j, 2.0);
		double j_y = Math.pow(a, 1.5);
		double k_y = Math.pow(t, 2.0);
		double l_y = Math.pow(a, 3.0);
		double m_y = Math.pow(a, 3.0);
		double n_y = Math.pow(a, 1.5);
		double o_y = Math.pow(a, 3.0);
		double p_y = Math.pow(a, 3.0);
		double q_y = Math.pow(j, 2.0);
		double r_y = Math.pow(j, 2.0);
		double s_y = Math.pow(j, 2.0);
		double t_y = Math.pow(j, 2.0);
		return (((((((((-d - 2.0 * d_y / (3.0 * e_y)) + f_y * t / j) - a * g_y / 2.0) + j * h_y / 6.0) + Math.sqrt(p_y + 4.0 * d * q_y) * Math.sqrt(a) * t / j) + 2.0 * d * t * j / a) + y / (i_y * 3.0 * j_y)) - k_y * Math.sqrt(l_y + 4.0 * d * r_y) / (Math.sqrt(a) * 2.0)) - Math.sqrt(m_y + 4.0 * d * s_y) * n_y / b_y) - 2.0 * d * Math.sqrt(o_y + 4.0 * d * t_y) / c_y;
	}

	public static double bv0(double j, double t) {
		double y = Math.pow(t, 2.0);
		return j * y / 2.0;
	}

	public static double bv1(double a, double j, double t) {
		double y = Math.pow(a, 2.0);
		return -1.0 * y / (2.0 * j) + a * t;
	}

	public static double bv2(double a, double j, double d, double t) {
		double y = Math.pow(a, 2.0);
		double b_y = Math.pow(t, 2.0);
		double c_y = Math.pow(a, 3.0);
		double d_y = Math.pow(a, 3.0);
		double e_y = Math.pow(j, 2.0);
		double f_y = Math.pow(j, 2.0);
		return ((((-3.0 * y / (4.0 * j) - j * b_y / 2.0) + a * t / 2.0) + t * Math.sqrt(c_y + 4.0 * d * e_y) / (2.0 * Math.sqrt(a))) + Math.sqrt(d_y + 4.0 * d * f_y) * Math.sqrt(a) / (4.0 * j)) - j * d / (2.0 * a);
	}

	public static double bv3(double a, double j, double d, double t) {
		double y;
		double b_y;
		double c_y;
		double d_y;
		double e_y;
		double f_y;
		y = Math.pow(a, 2.0);
		b_y = Math.pow(a, 3.0);
		c_y = Math.pow(t, 2.0);
		d_y = Math.pow(a, 3.0);
		e_y = Math.pow(j, 2.0);
		f_y = Math.pow(j, 2.0);
		return ((((-3.0 * y / (4.0 * j) + Math.sqrt(b_y + 4.0 * d * e_y) * Math.sqrt(a) / (4.0 * j)) - d * j / (2.0 * a)) - j * c_y / 2.0) + t * a / 2.0) + Math.sqrt (d_y + 4.0 * d * f_y) * t / (Math.sqrt(a) * 2.0);
	}

	public static double bv4(double a, double j, double d, double t) {
		double y = Math.pow(a, 2.0);
		double b_y = Math.pow(a, 3.0);
		double c_y = Math.pow(j, 2.0);
		return (y / (2.0 * j) + Math.sqrt(b_y + 4.0 * d * c_y) * Math.sqrt(a) / j) - t * a;
	}

	public static double bv5(double a, double j, double d, double t)
	{
		double y = Math.pow(a, 2.0);
		double b_y = Math.pow(t, 2.0);
		double c_y = Math.pow(a, 3.0);
		double d_y = Math.pow(a, 3.0);
		double e_y = Math.pow(j, 2.0);
		double f_y = Math.pow(j, 2.0);
		return ((((y / j + j * b_y / 2.0) - t * a) - t * Math.sqrt(c_y + 4.0 * d * e_y) / Math.sqrt(a)) + Math.sqrt(d_y + 4.0 * d * f_y) * Math.sqrt(a) / j) + 2.0 * d * j / a;
	}

	public static double cd0(double j, double t) {
		double y = Math.pow(t, 3.0);
		return j * y / 6.0;
	}

	public static double cd1(double j, double d, double t) {
		double y = Math.pow(t, 2.0);
		double b_y = Math.pow(d, 0.3333);
		double c_y = Math.pow(d, 0.6667);
		double d_y = Math.pow(t, 3.0);
		double e_y = Math.pow(j, 0.6667);
		double f_y = Math.pow(j, 0.3333);
		return ((d / 6.0 + 0.5 * e_y * 1.5874377291440287 * b_y * y) - 0.5 * f_y * 1.2598919398737178 * c_y * t) - j * d_y / 6.0;
	}

	public static double cd2(double j, double d, double t) {
		double y = Math.pow(d, 0.6667);
		double b_y = Math.pow(d, 0.3333);
		double c_y = Math.pow(j, 0.6667);
		double d_y = Math.pow(t, 3.0);
		double e_y = Math.pow(j, 0.3333);
		double f_y = Math.pow(t, 2.0);
		return ((d / 6.0 - 0.5 * e_y * t * 1.2598919398737178 * y) + f_y / 2.0 * c_y * 1.5874377291440287 * b_y) - j * d_y / 6.0;
	}

	public static double cd3(double j, double d, double t) {
		double y = Math.pow(d, 0.3333);
		double b_y = Math.pow(d, 0.6667);
		double c_y = Math.pow(j, 0.6667);
		double d_y = Math.pow(t, 2.0);
		double e_y = Math.pow(t, 3.0);
		double f_y = Math.pow(j, 0.3333);
		return ((-13.0 * d / 3.0 - c_y * d_y * 1.5874377291440287 * y) + 4.0 * f_y * t * 1.2598919398737178 * b_y) + j * e_y / 6.0;
	}

	public static double cv0(double j, double t) {
		double y = Math.pow(t, 2.0);
		return j * y / 2.0;
	}

	public static double cv1(double j, double d, double t) {
		double y = Math.pow(d, 0.6667);
		double b_y = Math.pow(j, 0.3333);
		double c_y = Math.pow(t, 2.0);
		double d_y = Math.pow(j, 0.6667);
		double e_y = Math.pow(d, 0.3333);
		return (-0.5 * b_y * 1.2598919398737178 * y - j * c_y / 2.0) + d_y * 1.5874377291440287 * e_y * t;
	}

	public static double cv2(double j, double d, double t) {
		double y = Math.pow(d, 0.6667);
		double b_y = Math.pow(d, 0.3333);
		double c_y = Math.pow(j, 0.3333);
		double d_y = Math.pow(t, 2.0);
		double e_y = Math.pow(j, 0.6667);
		return (-0.5 * c_y * 1.2598919398737178 * y - j * d_y / 2.0) + e_y * t * 1.5874377291440287 * b_y;
	}

	public static double cv3(double j, double d, double t) {
		double y = Math.pow(d, 0.6667);
		double b_y = Math.pow(d, 0.3333);
		double c_y = Math.pow(j, 0.3333);
		double d_y = Math.pow(t, 2.0);
		double e_y = Math.pow(j, 0.6667);
		return (4.0 * c_y * 1.2598919398737178 * y - 2.0 * e_y * t * 1.5874377291440287 * b_y) + j * d_y / 2.0;
	}

}

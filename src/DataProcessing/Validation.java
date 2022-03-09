package DataProcessing;


public class Validation implements Comparable<Validation>{
	int mes;
	int dia;
	int badge;
	String tipo;
	String rol;
	long segundo;
	int periodo;

	public Validation(int mes, int dia, int badge, String tipo, String rol, long segundo, int periodo) {
		super();
		this.mes = mes;
		this.dia = dia;
		this.badge = badge;
		this.tipo = tipo;
		this.rol = rol;
		this.segundo = segundo;
		this.periodo = periodo;
	}

	@Override
	public int compareTo(Validation o) {
		if (this.mes<o.mes) {
			return -1;
		}
		else if (this.mes > o.mes) {
			return 1;
		}
		else {
			if (this.dia < o.dia) {
				return -1;
			}
			else if (this.dia > o.dia) {
				return 1;
			}
			else {
				if (this.badge < o.badge) {
					return -1;
				}
				else if (this.badge > o.badge) {
					return 1;
				}
				else {
					if (this.segundo < o.segundo) {
						return -1;
					} 
					else if (this.segundo > o.segundo) {
						return 1;
					}
					else {
						if (this.tipo != o.tipo) {
							if (this.tipo == "IN") {
								return -1;
							}
							else {
								return 1;
							}
						}
						else {
							return 0;
						}
					}
				}
			}
		}
	}
}

package chatfrida.com.chatfrida;

public class Lugar {

    public String Direccion;
    public String Comida;
    public String Nombre;
    public int HorarioFin;
    public int HorarioInicio;
    public double Latitud;
    public double Longitud;
    public double Puntaje;
    public double Distancia;
    public Lugar(String direccion,String comida, String nombre, int horarioFin, int horarioInicio, double latitud, double longitud, double puntaje, double distancia) {
        this.Direccion = direccion;
        this.Nombre = nombre;
        this.Comida = comida;
        this.HorarioFin = horarioFin;
        this.HorarioInicio = horarioInicio;
        this.Latitud = latitud;
        this.Longitud = longitud;
        this.Puntaje = puntaje;
        this.Distancia = distancia;
    }
    public  Lugar(){}

}
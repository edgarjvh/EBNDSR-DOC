package vistas;

@SuppressWarnings("ALL")
public class SpinnerItems {
    private int registrado;
    private int idRepresentante;
    private String apellidosRep;
    private String nombresRep;
    private int idAlumno;
    private String apellidosAl;
    private String nombresAl;
    private String imagen;

    public SpinnerItems(int registrado, int idRepresentante, String apellidosRep, String nombresRep, int idAlumno, String apellidosAl, String nombresAl, String imagen){
        this.registrado = registrado;
        this.idRepresentante = idRepresentante;
        this.apellidosRep = apellidosRep;
        this.nombresRep = nombresRep;
        this.idAlumno = idAlumno;
        this.apellidosAl = apellidosAl;
        this.nombresAl = nombresAl;
        this.imagen = imagen;
    }

    public int getRegistrado() {
        return registrado;
    }

    public void setRegistrado(int registrado) {
        this.registrado = registrado;
    }

    public int getIdRepresentante() {
        return idRepresentante;
    }

    public void setIdRepresentante(int idRepresentante) {
        this.idRepresentante = idRepresentante;
    }

    public String getApellidosRep() {
        return apellidosRep;
    }

    public void setApellidosRep(String apellidosRep) {
        this.apellidosRep = apellidosRep;
    }

    public String getNombresRep() {
        return nombresRep;
    }

    public void setNombresRep(String nombresRep) {
        this.nombresRep = nombresRep;
    }

    public int getIdAlumno() {
        return idAlumno;
    }

    public void setIdAlumno(int idAlumno) {
        this.idAlumno = idAlumno;
    }

    public String getApellidosAl() {
        return apellidosAl;
    }

    public void setApellidosAl(String apellidosAl) {
        this.apellidosAl = apellidosAl;
    }

    public String getNombresAl() {
        return nombresAl;
    }

    public void setNombresAl(String nombresAl) {
        this.nombresAl = nombresAl;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }
}

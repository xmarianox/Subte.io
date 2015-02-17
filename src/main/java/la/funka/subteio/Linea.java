package la.funka.subteio;

public class Linea {
    private String name;
    private String status;
    private Double frequency;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getFrequency(){return frequency;}

    public void setFrequency(Double frequency){this.frequency = frequency;}

}

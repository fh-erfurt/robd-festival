package de.bord.festival;

public class Band {
    private int id;
    private String name;
    private String phoneNumber;
    private double priceProEvent;
    public Band(int id,String name, String phoneNumber, double priceProEvent){
        this.id=id;
        this.name=name;
        this.phoneNumber=phoneNumber;
        this.priceProEvent=priceProEvent;
    }
}

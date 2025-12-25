package org.example.project.model;

public class Publisher {
    private int id;
    private String name,city,country,contact;

    public Publisher(int id,String name,String city,String country,String contact){
        this.id=id; this.name=name; this.city=city; this.country=country; this.contact=contact;
    }

    public int getId(){ return id; }
    public String getName(){ return name; }
    public String getCity(){ return city; }
    public String getCountry(){ return country; }
    public String getContact(){ return contact; }
}
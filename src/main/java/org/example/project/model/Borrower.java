package org.example.project.model;

public class Borrower {
    private int id;
    private String firstName, lastName, contact;
    private int typeId;

    public Borrower(int id, String firstName, String lastName, int typeId, String contact) {
        this.id=id; this.firstName=firstName; this.lastName=lastName;
        this.typeId=typeId; this.contact=contact;
    }

    public int getId() { return id; }
    public String getFirstName(){ return firstName; }
    public String getLastName(){ return lastName; }
    public int getTypeId(){ return typeId; }
    public String getContact(){ return contact; }
}

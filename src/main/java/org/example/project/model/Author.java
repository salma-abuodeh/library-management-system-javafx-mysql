package org.example.project.model;

public class Author {
    private int authorId;
    private String firstName;
    private String lastName;
    private String country;
    private String bio;

    public Author(int authorId, String firstName, String lastName, String country, String bio) {
        this.authorId = authorId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.country = country;
        this.bio = bio;
    }

    public int getAuthorId() { return authorId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getCountry() { return country; }
    public String getBio() { return bio; }

    public void setAuthorId(int authorId) { this.authorId = authorId; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setCountry(String country) { this.country = country; }
    public void setBio(String bio) { this.bio = bio; }
}

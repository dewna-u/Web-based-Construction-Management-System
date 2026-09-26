package com.buildflow.cms.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SupplierForm {

    private Long id;

    @NotBlank(message = "Supplier name is required")
    @Size(max = 200, message = "Name must be at most 200 characters")
    private String name;

    @Size(max = 150, message = "Contact person must be at most 150 characters")
    private String contactPerson;

    @Size(max = 30, message = "Phone must be at most 30 characters")
    private String phone;

    @Email(message = "Email must be valid")
    @Size(max = 150, message = "Email must be at most 150 characters")
    private String email;

    @Size(max = 500, message = "Address must be at most 500 characters")
    private String address;

    private boolean active = true;

    private Long linkedUserId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Long getLinkedUserId() {
        return linkedUserId;
    }

    public void setLinkedUserId(Long linkedUserId) {
        this.linkedUserId = linkedUserId;
    }
}

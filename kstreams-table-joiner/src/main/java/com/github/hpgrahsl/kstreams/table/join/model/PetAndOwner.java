package com.github.hpgrahsl.kstreams.table.join.model;

public class PetAndOwner {

    public Pet pet;
    public Owner owner;

    public PetAndOwner() {
    }

    public PetAndOwner(Pet pet, Owner owner) {
        this.pet = pet;
        this.owner = owner;
    }

    public static PetAndOwner create(Pet pet, Owner owner) {
        return new PetAndOwner(pet, owner);
    }

    public Pet pet() {
        return pet;
    }

    public Owner owner() {
        return owner;
    }

}

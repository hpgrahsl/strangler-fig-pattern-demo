package com.github.hpgrahsl.kstreams.table.join.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OwnerWithPets {

    private static final Logger LOGGER = LoggerFactory.getLogger(OwnerWithPets.class);

    public Owner owner;
    public List<Pet> pets = new ArrayList<>();

    public OwnerWithPets addPet(PetAndOwner petAndOwner) {
        LOGGER.info("Adding: " + petAndOwner);

        owner = petAndOwner.owner;
        pets.add(petAndOwner.pet);

        return this;
    }

    public OwnerWithPets removePet(PetAndOwner petAndOwner) {
        LOGGER.info("Removing: " + petAndOwner);

        Iterator<Pet> it = pets.iterator();
        while (it.hasNext()) {
            Pet p = it.next();
            if (p.id == petAndOwner.pet.id) {
                it.remove();
                break;
            }
        }

        return this;
    }

    @Override
    public String toString() {
        return "OwnerWithPets{" +
            "owner=" + owner +
            ", pets=" + pets +
            '}';
    }

}

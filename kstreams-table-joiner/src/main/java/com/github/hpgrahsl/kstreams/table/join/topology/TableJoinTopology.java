package com.github.hpgrahsl.kstreams.table.join.topology;

import com.github.hpgrahsl.kstreams.table.join.model.Pet;
import com.github.hpgrahsl.kstreams.table.join.model.Owner;
import com.github.hpgrahsl.kstreams.table.join.model.OwnerWithPets;
import com.github.hpgrahsl.kstreams.table.join.model.PetAndOwner;
import java.util.Collections;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.debezium.serde.DebeziumSerdes;
import io.quarkus.kafka.client.serialization.JsonbSerde;

@ApplicationScoped
public class TableJoinTopology {

    @ConfigProperty(name = "owners.topic")
    String ownersTopic;

    @ConfigProperty(name = "pets.topic")
    String petsTopic;

    @ConfigProperty(name = "owners-with-pets.topic")
    String ownersWithPetsTopic;

    @Produces
    public Topology createStreamsTopology() {
        StreamsBuilder builder = new StreamsBuilder();

        Serde<Integer> ownerKeySerde = DebeziumSerdes.payloadJson(Integer.class);
        ownerKeySerde.configure(Collections.emptyMap(), true);

        Serde<Owner> ownerSerde = DebeziumSerdes.payloadJson(Owner.class);
        ownerSerde.configure(Collections.singletonMap("from.field", "after"), false);

        JsonbSerde<PetAndOwner> petAndOwnerSerde = new JsonbSerde<>(PetAndOwner.class);
        JsonbSerde<OwnerWithPets> ownerWithPetsSerde = new JsonbSerde<>(OwnerWithPets.class);

        Serde<Integer> petKeySerde = DebeziumSerdes.payloadJson(Integer.class);
        petKeySerde.configure(Collections.emptyMap(), true);

        Serde<Pet> petSerde = DebeziumSerdes.payloadJson(Pet.class);
        petSerde.configure(Collections.singletonMap("from.field", "after"), false);

        KTable<Integer, Pet> pets = builder.table(
            petsTopic,
            Consumed.with(petKeySerde, petSerde)
        );

        KTable<Integer, Owner> owners = builder.table(
            ownersTopic,
            Consumed.with(ownerKeySerde, ownerSerde)
        );

        KTable<Integer, OwnerWithPets> ownerWithPets = pets.join(
                owners,
                pet -> pet.owner_id,
                PetAndOwner::new,
                Materialized.with(Serdes.Integer(), petAndOwnerSerde)
            )
            .groupBy(
                (petId, petAndOwner) -> KeyValue.pair(petAndOwner.owner.id, petAndOwner),
                Grouped.with(Serdes.Integer(), petAndOwnerSerde)
            )
            .aggregate(
                OwnerWithPets::new,
                (ownerId, petAndOwner, agg) -> agg.addPet(petAndOwner),
                (ownerId, petAndOwner, agg) -> agg.removePet(petAndOwner),
                Materialized.with(Serdes.Integer(), ownerWithPetsSerde)
            );

        ownerWithPets.toStream()
            .to(
                ownersWithPetsTopic,
                Produced.with(Serdes.Integer(), ownerWithPetsSerde)
            );

        return builder.build();
    }
}

# Remaining TC4 scan identities without a current equivalent

The migration report now contains **6** unresolved identities (down from 22).
They remain inactive because mapping them to a merely similar current object
would assign original knowledge and aspects to the wrong gameplay identity.

| Legacy identity | Original aspects | Why it remains unresolved |
|---|---|---|
| `itemNugget:21` (`clusterCinnabar`) | Ordo 1, Metallum 4, Terra 1, Permutatio 4, Venenum 2 | No `native_cinnabar_cluster` is registered in the current port. |
| `itemTripleMeatTreat` | recipe-derived base, plus Sano 1 and minus Fames 1 | The Triple Meat Treat item is not registered. Its single modifier is not the complete scan. |
| `blockAiry:2` (`leavesFiller1`) | Lux 1 | The invisible Silverwood canopy light-filler block is not registered as a distinct modern block. |
| `blockAiry:3` (`leavesFiller2`) | Lux 1 | The second invisible Silverwood canopy light-filler block is not registered as a distinct modern block. |
| `itemFocusPech` | Praecantatio 5, Venenum 5, Perditio 5, Alienis 5, Telum 5 | The private Pech projectile focus is different from the craftable Focus of Trade and is not registered. |
| `blockEldritchPortal` | Vacuos 8, Alienis 8, Iter 8 | No Eldritch Portal block is registered; `temporary_hole` is a different mechanic. |

The earlier count remained high because the first migration only accepted
one-to-one names that had already been written into `TC_STACKS`. TC4 stored
many different objects as metadata of a single item/block and used one generic
entity ID for every golem. The current port splits those into independent
registry IDs, so their equivalence had to be verified from the original
metadata constants and current registrations before activating the scans.

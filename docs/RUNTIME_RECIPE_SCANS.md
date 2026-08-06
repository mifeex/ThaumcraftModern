# Runtime recipe-derived scans

After datapack recipes are loaded, the dedicated/server authority builds a
temporary scan layer for recipe outputs that have no explicit datapack scan.
The layer is included in the existing knowledge/registry synchronization sent
to clients.

## Precedence

1. Direct or tag scan JSON.
2. Runtime recipe-derived definition.
3. Optional heuristic compatibility fallback.

Reloading datapacks deletes the previous runtime layer before rebuilding it,
so removed recipes cannot leave stale scan definitions.

## TC4 calculation

For every aspect in the selected ingredients:

`floor(sum * 0.75 / output count)`

Recipes implementing `AspectCostProvider` additionally contribute:

`floor(sqrt(cost) / output count)`

When an ingredient accepts multiple interchangeable items, the known
alternative with the lowest total aspect value is selected so a large tag does
not count as hundreds of recipes. When several recipes make the same output,
their aspect vectors are combined with a weighted mean. Recipe weight is the
total aspect value of its ingredients plus its magical cost, capped at 4096:

`floor(sum(recipe aspect * recipe weight) / sum(recipe weight))`

Consequently several simple recipes still contribute, while a genuinely
compound production chain has proportionally greater influence.

## Safety limits

- Explicit definitions can never be overwritten.
- Self-references and unresolved cycles do not generate a definition.
- At most 64 fixed-point passes are performed.
- Recipes with more than 64 ingredients are rejected.
- Ingredients with more than 256 alternatives are rejected.
- At most 100,000 sorted recipes are considered per reload.
- Ingredients returning crafting containers are rejected conservatively.
- Empty or unknown ingredients reject the recipe instead of inventing aspects.
- Zero contributions are discarded and every aspect is capped at 64.
- Only the primary `Recipe#getResultItem` is considered; secondary/byproduct
  APIs are not interpreted as independently craftable outputs.

Generic mod recipes work when they expose stable ingredients and a primary
result through the vanilla `Recipe` interface. A Create recipe type that hides
its processing inputs or outputs behind Create-only APIs requires a small
adapter; it will otherwise be skipped safely.

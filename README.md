# iceberg-remove-missing-file-references

Removes references to data and delete files that no longer exist in storage from a branch of an Iceberg table (REST catalog, S3).

## Usage

Dry run
```sh
docker run --rm -v "$PWD/catalog.properties:/catalog.properties:ro" \
  ghcr.io/kinolaev/iceberg-remove-missing-file-references \
  /catalog.properties db.table main
```

Commit
```sh
docker run --rm -v "$PWD/catalog.properties:/catalog.properties:ro" \
  ghcr.io/kinolaev/iceberg-remove-missing-file-references \
  /catalog.properties db.table main true
```

package com.github.kinolaev.iceberg;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.ManifestContent;
import org.apache.iceberg.ManifestFile;
import org.apache.iceberg.ManifestFiles;
import org.apache.iceberg.RowDelta;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.Table;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.FileIO;
import org.apache.iceberg.rest.RESTCatalog;

public class RemoveMissingFileReferences {
  public static void main(String[] args) throws IOException {
    Map<String, String> catalogProps = new HashMap<>();
    try (FileInputStream in = new FileInputStream(args[0])) {
      Properties props = new Properties();
      props.load(in);
      for (String name : props.stringPropertyNames()) {
        catalogProps.put(name, props.getProperty(name));
      }
    }
    TableIdentifier tableId = TableIdentifier.parse(args[1]);
    String branch = args[2];
    boolean commit = args.length > 3 && Boolean.parseBoolean(args[3]);
    try (RESTCatalog catalog = new RESTCatalog()) {
      catalog.initialize("iceberg", catalogProps);
      Table table = catalog.loadTable(tableId);
      Snapshot snapshot = table.snapshot(branch);
      RowDelta rowDelta = table.newRowDelta().validateFromSnapshot(snapshot.snapshotId());
      int missing = 0;
      FileIO io = table.io();
      for (ManifestFile manifest : snapshot.allManifests(io)) {
        if (manifest.content() == ManifestContent.DATA) {
          try (CloseableIterable<DataFile> files =
              ManifestFiles.read(manifest, io, table.specs())) {
            for (DataFile file : files) {
              if (!io.newInputFile(file.location()).exists()) {
                System.out.println(file.location());
                rowDelta.removeRows(file);
                missing++;
              }
            }
          }
        } else if (manifest.content() == ManifestContent.DELETES) {
          try (CloseableIterable<DeleteFile> files =
              ManifestFiles.readDeleteManifest(manifest, io, table.specs())) {
            for (DeleteFile file : files) {
              if (!io.newInputFile(file.location()).exists()) {
                System.out.println(file.location());
                rowDelta.removeDeletes(file);
                missing++;
              }
            }
          }
        }
      }
      if (missing > 0 && commit) {
        rowDelta.toBranch(branch).commit();
      }
    }
  }
}

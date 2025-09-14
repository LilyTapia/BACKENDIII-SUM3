package cl.bancoxyz.bff.legacyapi.infra;

import com.opencsv.CSVReaderHeaderAware;
import com.opencsv.exceptions.CsvValidationException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class LegacyCsvClient {
  @Value("${legacy.data.baseUrl:https://raw.githubusercontent.com/KariVillagran/bank_legacy_data/main/data}")
  private String baseUrl;

  private List<Map<String, String>> read(String file) throws CsvValidationException {
    // Primero intenta vía HTTP (baseUrl). Si falla (sin red o 404), cae a classpath: data/<file>
    try {
      URL url = new URL(baseUrl + "/" + file);
      try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
           CSVReaderHeaderAware csv = new CSVReaderHeaderAware(br)) {
        List<Map<String, String>> rows = new ArrayList<>();
        Map<String, String> row;
        while ((row = csv.readMap()) != null) rows.add(row);
        return rows;
      }
    } catch (IOException remoteError) {
      // Fallback a recursos locales
      try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("data/" + file)) {
        if (is == null) throw new UncheckedIOException("Recurso no encontrado en classpath: data/" + file, new IOException("not found"));
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
             CSVReaderHeaderAware csv = new CSVReaderHeaderAware(br)) {
          List<Map<String, String>> rows = new ArrayList<>();
          Map<String, String> row;
          while ((row = csv.readMap()) != null) rows.add(row);
          return rows;
        }
      } catch (IOException localError) {
        throw new UncheckedIOException("Error leyendo CSV local: " + file, localError);
      }
    }
  }

  public List<Map<String, String>> transacciones() throws CsvValidationException {
    return read("transacciones.csv");
  }

  public List<Map<String, String>> intereses() throws CsvValidationException {
    return read("intereses.csv");
  }

  public List<Map<String, String>> cuentasAnuales() throws CsvValidationException {
    return read("cuentas_anuales.csv");
  }
}

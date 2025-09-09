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
    try {
      URL url = new URL(baseUrl + "/" + file);
      try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
          CSVReaderHeaderAware csv = new CSVReaderHeaderAware(br)) {
        List<Map<String, String>> rows = new ArrayList<>();
        Map<String, String> row;
        while ((row = csv.readMap()) != null)
          rows.add(row);
        return rows;
      }
    } catch (IOException e) {
      throw new UncheckedIOException("Error leyendo CSV: " + file, e);
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
package by.bsuir.lab1.model.parse;

import by.bsuir.lab1.model.domain.Band;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public class CsvBandParser {

  private CsvMapper mapper;

  public CsvBandParser() {
    mapper = new CsvMapper();
    mapper.enable(CsvParser.Feature.WRAP_AS_ARRAY);
  }

  public List<Band> parseAsList(InputStream stream) throws IOException {
    MappingIterator<String[]> it = mapper.readerFor(String[].class).readValues(stream);
    List<Band> bands = new LinkedList<>();
    while (it.hasNext()) {
      String[] row = it.next();
      try {
        bands.add(new Band(Double.parseDouble(row[25]), Double.parseDouble(row[28])));
      } catch (NumberFormatException e) {
        // skip band if one of the values is unknown
      }
    }
    return bands;
  }

}

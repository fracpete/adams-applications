/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * ReportToArff.java
 * Copyright (C) 2016 University of Waikato, Hamilton, New Zealand
 */

package adams.honours;

import adams.core.base.BaseRegExp;
import adams.core.io.DirectoryLister;
import adams.core.io.PlaceholderDirectory;
import adams.core.io.PlaceholderFile;
import adams.data.io.input.AbstractReportReader;
import adams.data.io.output.ArffSpreadSheetWriter;
import adams.data.report.AbstractField;
import adams.data.report.Report;
import adams.data.spreadsheet.Cell;
import adams.data.spreadsheet.DefaultSpreadSheet;
import adams.data.spreadsheet.Row;
import adams.data.spreadsheet.SpreadSheet;
import adams.env.Environment;
import adams.flow.transformer.ReportFileReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Takes the reports generated by the adams flow and turns them into an arff file
 *
 * @author sjb90
 * @version $Revision$
 */
public class ReportToArff {

  public static void main(String[] args) throws Exception {
    Environment.setEnvironmentClass(Environment.class);

    ArffSpreadSheetWriter writer = new ArffSpreadSheetWriter();
    List<Report> reports = getReports(args[0]);
    SpreadSheet ss = toSpreadSheet(reports);
    writer.write(ss, args[1]);
  }

  private static SpreadSheet toSpreadSheet(List<Report> reports) {
    // Create a spreadsheet
    SpreadSheet result = new DefaultSpreadSheet();
    // Get the header row
    Row headerRow = result.getHeaderRow();
    // Loop through all reports and make sure all possible fields are added to the spreadsheet
    reports.stream().forEach(report1 -> {
      report1.getFields().
        stream().
        forEach(abstractField -> headerRow.addCell(abstractField.toString()).setContentAsString(abstractField.toString()));
    });
    headerRow.addCell("dist").setContentAsString("Distance");

    for(Report report : reports) {
      if(report.getDoubleValue("Object.count") > 2)
	continue;
      Row row = result.addRow();
      List<AbstractField> fields = report.getFields();
      for (AbstractField f : fields) {
	Cell cell;
	switch (f.getDataType()) {
	  case NUMERIC:
	    cell = row.addCell(f.toString());
	    if(cell == null) {
	      System.out.println(f.toString());
	      System.out.println("cell is null");
	      System.out.println(report.getStringValue("Timestamp"));
	      continue;
	    }
	    cell.setContent(report.getDoubleValue(f));
	    break;
	  case BOOLEAN:
	    cell = row.addCell(f.toString());
	    if(cell == null) {
	      System.out.println(f.toString());
	      System.out.println("cell is null");
	      System.out.println(report.getStringValue("Timestamp"));
	      continue;
	    }
	    cell.setContent(report.getBooleanValue(f));
	    break;
	  default:
	    cell = row.addCell(f.toString());
	    if(cell == null) {
	      System.out.println(f.toString());
	      System.out.println("cell is null");
	      System.out.println(report.getStringValue("Timestamp"));
	      continue;
	    }
	    cell.setContent(report.getStringValue(f));
	}
      }
      // Calculate distance. For this we need to make use of the number of objects and their coordinates
      row.addCell("dist").setContent(getDistance(report));
    }
    return result;
  }

  private static double getDistance(Report report) {
    double result = 0.0;
    if(report.getDoubleValue("Object.count") < 2) {
      result = 0;
    }
    else {
      // Find middle of the rectangles
      double o1X = report.getDoubleValue("Object.1.x");
      double o1Y = report.getDoubleValue("Object.1.y");
      double o1H = report.getDoubleValue("Object.1.height");
      double o1W = report.getDoubleValue("Object.1.width");

      double o2X = report.getDoubleValue("Object.2.x");
      double o2Y = report.getDoubleValue("Object.2.y");
      double o2H = report.getDoubleValue("Object.2.height");
      double o2W = report.getDoubleValue("Object.2.width");

      double o1MX = o1X + (o1W/2);
      double o1MY = o1Y + (o1H/2);

      double o2MX = o2X + (o2W/2);
      double o2MY = o2Y + (o2H/2);
      result = Math.sqrt(Math.pow(o2MX - o1MX, 2) + Math.pow(o2MY - o1MY,2));
    }

    return result;
  }

  private static List<Report> getReports(String directory) throws IOException {
    List<Report> result = new ArrayList<>();
    AbstractReportReader<Report> reader = new ReportFileReader().getReader();

    Files.walk(Paths.get(directory)).forEach(filePath -> {
      if (Files.isRegularFile(filePath)) {
	PlaceholderFile f = new PlaceholderFile(filePath.toString());
	if(f.getExtension().equals("report")) {
	  reader.setInput(f);
	  result.add(reader.read().get(0));
	}
      }
    });
    return result;
  }
}
